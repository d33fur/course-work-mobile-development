SHELL := /bin/bash

.PHONY: server-all
server-all: server-rebuild server-start

.PHONY: server-build
server-build:
	cd server && \
	docker compose build

.PHONY: server-rebuild
server-rebuild:
	cd server && \
	docker compose build --force-rm

.PHONY: server-start
server-start:
	cd server && \
	docker compose up -d

.PHONY: server-stop
server-stop:
	cd server && \
	docker compose stop

.PHONY: server-restart
server-restart: server-stop server-start

.PHONY: server-logs
server-logs:
	cd server && \
	docker compose logs -f --tail 100

.PHONY: server-clear
server-clear:
	@(cd server && \
	docker compose kill  && \
	docker compose rm -f) 

.PHONY: server-local-all
server-local-all: server-local-conan-reinstall server-local-rebuild

.PHONY: server-local-rebuild
server-local-rebuild:
	@(cd server/build && \
	source conanbuild.sh && \
	cmake -DCMAKE_BUILD_TYPE=Release .. && \
	cmake --build . && \
	source deactivate_conanbuild.sh && \
	./auth 0.0.0.0 8001 . 1)

.PHONY: server-local-conan-reinstall
server-local-conan-reinstall:
	@(rm -rf server/build/ && \
	mkdir server/build && \
	cd server/build && \
	conan install .. --profile=cxxprofile --output-folder=. --build=missing)

# .PHONY: db-cqlsh
# db-cqlsh:
# 	@(docker exec -it scylla-node1 cqlsh)

# .PHONY: db-init
# db-init:
# 	@(docker exec scylla-node1 cqlsh -f /scylla-init.txt)