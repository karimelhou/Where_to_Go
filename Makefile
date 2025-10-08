.PHONY: up down logs psql test

up:
docker-compose --env-file .env up -d --build

down:
docker-compose down -v

logs:
docker-compose logs -f app

psql:
docker exec -it $$(docker-compose ps -q postgres) psql -U $${DB_USER:-deals} -d $${DB_NAME:-deals}

test:
mvn test
