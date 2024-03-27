#include <boost/beast/core.hpp>
#include <boost/beast/http.hpp>
#include <boost/beast/version.hpp>
#include <boost/asio/dispatch.hpp>
#include <boost/asio/strand.hpp>
#include <boost/config.hpp>
#include <algorithm>
#include <cstdlib>
#include <functional>
#include <iostream>
#include <memory>
#include <string>
#include <thread>
#include <vector>

namespace beast = boost::beast;
namespace http = beast::http;
namespace net = boost::asio;
using tcp = boost::asio::ip::tcp;

template <class Body, class Allocator>
http::message_generator handle_request(http::request<Body, 
  http::basic_fields<Allocator>>&& req) {
    auto const bad_request = [&req](beast::string_view why) {
      http::response<http::string_body> res{http::status::bad_request, req.version()};
      res.set(http::field::server, BOOST_BEAST_VERSION_STRING);
      res.set(http::field::content_type, "text/html");
      res.keep_alive(req.keep_alive());
      res.body() = std::string(why);
      res.prepare_payload();
      return res;
    };

    auto const not_found = [&req](beast::string_view target) {
      http::response<http::string_body> res{http::status::not_found, req.version()};
      res.set(http::field::server, BOOST_BEAST_VERSION_STRING);
      res.set(http::field::content_type, "text/html");
      res.keep_alive(req.keep_alive());
      res.body() = "The resource '" + std::string(target) + "' was not found.";
      res.prepare_payload();
      return res;
    };

    auto const server_error = [&req](beast::string_view what) {
      http::response<http::string_body> res{http::status::internal_server_error, req.version()};
      res.set(http::field::server, BOOST_BEAST_VERSION_STRING);
      res.set(http::field::content_type, "text/html");
      res.keep_alive(req.keep_alive());
      res.body() = "An error occurred: '" + std::string(what) + "'";
      res.prepare_payload();
      return res;
    };

    if(req.method() != http::verb::get &&
      req.method() != http::verb::head) {
        return bad_request("Unknown HTTP-method");
      }

    if (req.target() == "/api/example") {
      http::response<http::string_body> res{http::status::ok, req.version()};
      res.set(http::field::server, BOOST_BEAST_VERSION_STRING);
      res.set(http::field::content_type, "application/json");
      res.keep_alive(req.keep_alive());
      res.body() = "{\"message\": \"Hello, World!\"}";
      res.prepare_payload();
      return res;
    }

    return not_found(req.target());
}

//------------------------------------------------------------------------------

void fail(beast::error_code ec, char const* what) {
  std::cerr << what << ": " << ec.message() << "\n";
}

class session : public std::enable_shared_from_this<session> {
  beast::tcp_stream stream_;
  beast::flat_buffer buffer_;
  http::request<http::string_body> req_;

  public:
    session(tcp::socket&& socket) : stream_(std::move(socket)) {
    }

    void run() {
      net::dispatch(stream_.get_executor(), beast::bind_front_handler(
        &session::do_read, shared_from_this()));
    }

    void do_read() {
      req_ = {};
      stream_.expires_after(std::chrono::seconds(30));

      http::async_read(stream_, buffer_, req_, beast::bind_front_handler(
        &session::on_read, shared_from_this()));
    }

    void on_read(beast::error_code ec, std::size_t bytes_transferred) {
      boost::ignore_unused(bytes_transferred);

      if(ec == http::error::end_of_stream) {
        return do_close();
      }
      if(ec) {
        return fail(ec, "read");
      }

      send_response(handle_request(std::move(req_)));
    }

    void send_response(http::message_generator&& msg) {
      bool keep_alive = msg.keep_alive();

      beast::async_write(stream_, std::move(msg), beast::bind_front_handler(
        &session::on_write, shared_from_this(), keep_alive));
    }

    void on_write(bool keep_alive, beast::error_code ec, 
      std::size_t bytes_transferred) {
        boost::ignore_unused(bytes_transferred);

        if(ec) {
          return fail(ec, "write");
        }
        if(!keep_alive) {
          return do_close();
        }

        do_read();
    }

    void do_close() {
      beast::error_code ec;
      stream_.socket().shutdown(tcp::socket::shutdown_send, ec);
    }
};

//------------------------------------------------------------------------------

class listener : public std::enable_shared_from_this<listener> {
  net::io_context& ioc_;
  tcp::acceptor acceptor_;

  public:
    listener(net::io_context& ioc, tcp::endpoint endpoint)
      : ioc_(ioc), acceptor_(net::make_strand(ioc)) {
        beast::error_code ec;

        acceptor_.open(endpoint.protocol(), ec);
        if(ec) {
          fail(ec, "open");
          return;
        }

        acceptor_.set_option(net::socket_base::reuse_address(true), ec);
        if(ec) {
          fail(ec, "set_option");
          return;
        }

        acceptor_.bind(endpoint, ec);
        if(ec) {
          fail(ec, "bind");
          return;
        }

        acceptor_.listen(net::socket_base::max_listen_connections, ec);
        if(ec) {
          fail(ec, "listen");
          return;
        }
    }

    void run() {
      do_accept();
    }

  private:
    void do_accept() {
      acceptor_.async_accept(net::make_strand(ioc_), beast::bind_front_handler(
        &listener::on_accept, shared_from_this()));
    }

    void on_accept(beast::error_code ec, tcp::socket socket) {
      if(ec) {
        fail(ec, "accept");
        return;
      }
      else {
        std::make_shared<session>(std::move(socket))->run();
      }

      do_accept();
    }
};

int main(int argc, char* argv[]) {
  if(argc != 4) {
    std::cerr 
      << "Usage: auth <address> <port> <threads>\n"
      << "Example:\n"
      << "auth 0.0.0.0 8080 1\n";
    return EXIT_FAILURE;
  }

  auto const address = net::ip::make_address(argv[1]);
  auto const port = static_cast<unsigned short>(std::atoi(argv[2]));
  auto const threads = std::max<int>(1, std::atoi(argv[3]));

  net::io_context ioc{threads};
  std::make_shared<listener>(ioc, tcp::endpoint{address, port})->run();

  std::vector<std::thread> v;
  v.reserve(threads - 1);
  for(auto i = threads - 1; i > 0; --i) {
    v.emplace_back(
      [&ioc] {
      ioc.run();
    });
  }

  ioc.run();

  return EXIT_SUCCESS;
}