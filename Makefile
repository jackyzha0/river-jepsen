BLUE = \033[0;34m
ESC = \033[0m

containers-down:
	@cd docker && docker compose --compatibility -p jepsen -f docker-compose.yml ${COMPOSE} down

containers: containers-down
	@cd docker && ./bin/up --daemon -n 1

ssh:
	@cd docker && ./bin/console

tests: containers
	@echo "$(BLUE)Running tests...$(ESC)"
	@docker exec -it jepsen-control bash -c "source /root/.bashrc && lein run test --node n1"

results: containers
	@docker exec -it jepsen-control bash -c "source /root/.bashrc && lein run serve"

clean:
	rm -rf store/
