.PHONY: up down build test seed logs clean

up:
	docker compose up -d

down:
	docker compose down -v

build:
	mvn clean package -DskipTests

test:
	mvn clean test

seed:
	curl -X POST http://localhost:8081/api/v1/admin/seed -H "X-Tenant-ID: tenant-corp-alpha"

logs:
	docker compose logs -f agentguard-backend

clean:
	mvn clean
