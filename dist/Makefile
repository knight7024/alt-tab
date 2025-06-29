DEV_COMPOSE = docker-compose -f conf/docker-compose.yaml -f conf/dev/docker-compose.dev.yaml -p alt-tab
PROD_COMPOSE = docker-compose -f conf/docker-compose.yaml -f conf/prod/docker-compose.prod.yaml -p alt-tab


# Phony
.PHONY: dev-up dev-watch dev-down dev-restart dev-logs dev-ps prod-up prod-down


# Development
dev-up:
	@echo "Starting development containers..."
	@$(DEV_COMPOSE) up --build -d

dev-watch:
	@echo "Starting development containers with logs..."
	@$(DEV_COMPOSE) up --build

dev-down:
	@echo "Stopping development containers..."
	@$(DEV_COMPOSE) down --remove-orphans

dev-restart:
	@make dev-down
	@make dev-up

dev-logs:
	@echo "Tailing development logs..."
	@$(DEV_COMPOSE) logs -f

dev-ps:
	@$(DEV_COMPOSE) ps


# Production
prod-up:
	@echo "Starting production containers..."
	@$(PROD_COMPOSE) up --build -d

prod-down:
	@echo "Stopping production containers..."
	@$(PROD_COMPOSE) down --remove-orphans
