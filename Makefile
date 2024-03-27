BLUE = \033[0;34m
ESC = \033[0m

codegen:
	@echo "$(BLUE)Generating code...$(ESC)"
	@cd src/fixtures/typescript && npm install && npm run dump --silent > ../schema.json

containers:
	@cd docker && ./bin/up

ssh:
	@cd docker && ./bin/console

tests: codegen
	@echo "$(BLUE)Running tests...$(ESC)"
	@lein run test

results:
	@lein run serve

bun-run:
	@cd src/fixtures/typescript && npm run server 

clean:
	rm -rf store/
