# Influency Hub Internet Accelerator VPN API

FastAPI wrapper around local Xray gRPC API for managing VLESS users and stats.

## Run locally

```bash
export VPN_ADMIN_API_KEY=super-secret
uvicorn app.main:app --host 0.0.0.0 --port 8080
```

If `VPN_ADMIN_API_KEY` is not set, the service fails on startup with a clear error.

## Authentication

All endpoints except `GET /health` require:

```http
X-Service-Key: <VPN_ADMIN_API_KEY>
```

Example:

```bash
curl -H "X-Service-Key: super-secret" http://127.0.0.1:8080/users
```

## Build via Docker

```bash
docker build -t vpn-admin-api .
docker run --net=host --rm -e VPN_ADMIN_API_KEY=super-secret -p 8080:8080 vpn-admin-api
```

Xray API must be reachable at `127.0.0.1:10085` from the container (host network recommended).

## Generate OpenAPI spec

```bash
python3 scripts/generate_openapi.py
```

Generated files:

- `openapi/openapi.json`
- `openapi/openapi.yaml` if `PyYAML` is installed
