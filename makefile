run_server:
	@docker --context vm2_contabo compose -f docker-compose.loadbalancer2.yml up -d --force-recreate --build & pid2=$$!; \
	docker --context vm1_contabo compose -f docker-compose.loadbalancer1.yml up -d --force-recreate --build & pid1=$$!; \
	wait $$pid2; \
	wait $$pid1;

run_with_tunnel:
	@ssh -o ExitOnForwardFailure=yes -N -R 1000:127.0.0.1:1000 vm2_contabo & \
	TUNNEL_PID=$$!; \
	trap "kill $$TUNNEL_PID 2>/dev/null || true" EXIT INT TERM; \
	sleep 1; \
	docker compose -f docker-compose.distributed-source.yml up --force-recreate --build source

run_local:
	docker compose up --force-recreate --build