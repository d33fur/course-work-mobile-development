SHELL := /bin/bash

.PHONY: server-all
server-all: server-rebuild server-start

.PHONY: server-build
server-build:
	@(docker compose build)

.PHONY: server-rebuild
server-rebuild:
	@(docker compose build --force-rm)

.PHONY: server-start
server-start:
	@(docker compose up -d)

.PHONY: server-stop
server-stop:
	@(docker compose stop)

.PHONY: server-restart
server-restart: server-stop server-start

.PHONY: server-logs
server-logs:
	@(docker compose logs -f --tail 100)

.PHONY: server-clear
server-clear:
	@(docker compose kill  && \
		docker compose rm -f) 

.PHONY: server-delete-postgresql-data
server-delete-postgresql-data:
	@(rm -rf database/data && \
	mkdir database/data)

.PHONY: postgresql-cli
postgresql-cli:
	@(docker exec -it postgresql-python psql -U admin123 -d words)