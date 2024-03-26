SHELL := /bin/bash

.PHONY: all
all: rebuild start

.PHONY: build
build:
	docker-compose build

.PHONY: rebuild
rebuild:
	docker-compose build --force-rm

.PHONY: start
start:
	docker-compose up -d

.PHONY: stop
stop:
	docker-compose stop

.PHONY: restart
restart: stop start

.PHONY: logs
logs:
	docker-compose logs -f --tail 100

.PHONY: clear
clear:
	@(docker-compose kill  && \
	docker-compose rm -f) 

.PHONY: local
local: conan-rebuild local-rebuild

.PHONY: local-rebuild
local-rebuild:
	@(cd backend/build && \
	source conanbuild.sh && \
	cmake -DCMAKE_BUILD_TYPE=Release .. && \
	cmake --build . && \
	source deactivate_conanbuild.sh && \
	./auth 0.0.0.0 8001 . 1)

.PHONY: conan-rebuild
conan-rebuild:
	@(rm -rf backend/build/ && \
	mkdir backend/build && \
	cd backend/build && \
	conan install .. --profile=cxxprofile --output-folder=. --build=missing)

.PHONY: db-cqlsh
db-cqlsh:
	@(docker exec -it scylla-node1 cqlsh)

.PHONY: db-init
db-init:
	@(docker exec scylla-node1 cqlsh -f /scylla-init.txt)