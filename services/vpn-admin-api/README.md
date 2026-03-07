# Influency Hub Internet Accelerator VPN API

FastAPI wrapper around local Xray gRPC API for managing VLESS users and stats.

## Run locally

```bash
uvicorn app.main:app --host 0.0.0.0 --port 8080
```

## Build via Docker

```bash
docker build -t vpn-admin-api .
docker run --net=host --rm -p 8080:8080 vpn-admin-api
```

Xray API must be reachable at `127.0.0.1:10085` from the container (host network recommended).
