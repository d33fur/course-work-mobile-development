from conan import ConanFile
from conan.tools.cmake import cmake_layout

class Audio2WaveformConan(ConanFile):
  settings = "os", "compiler", "build_type", "arch"
  generators = "CMakeDeps", "CMakeToolchain"

  def requirements(self):
    self.requires("boost/1.84.0")
    self.requires("libpqxx/7.9.0")
    self.requires("nlohmann_json/3.11.3")

  def layout(self):
    cmake_layout(self)