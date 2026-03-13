# Influency Hub Internet Accelerator VPN API

FastAPI wrapper around local Xray gRPC API for managing VLESS users and stats.

## Run locally

```bash
export VPN_ADMIN_API_KEY=super-secret
export XRAY_PUBLIC_HOST=vpn.example.com
export XRAY_PUBLIC_PORT=443
export XRAY_REALITY_PUBLIC_KEY=PUBLIC_KEY
export XRAY_REALITY_SHORT_ID=abcd1234
export XRAY_REALITY_SNI=www.google.com
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

`POST /users` returns:

```json
{
  "status": "ok",
  "user": {
    "email": "api-test-user",
    "uuid": "22222222-2222-2222-2222-222222222222"
  },
  "connection": {
    "uri": "vless://22222222-2222-2222-2222-222222222222@vpn.example.com:443?security=reality&encryption=none&flow=xtls-rprx-vision&type=tcp&sni=www.google.com&fp=chrome&pbk=PUBLIC_KEY&sid=abcd1234#api-test-user",
    "host": "vpn.example.com",
    "port": 443,
    "sni": "www.google.com",
    "public_key": "PUBLIC_KEY",
    "short_id": "abcd1234"
  }
}
```

Required env values for VLESS URI generation:

- `XRAY_PUBLIC_HOST`
- `XRAY_PUBLIC_PORT`
- `XRAY_REALITY_PUBLIC_KEY`
- `XRAY_REALITY_SHORT_ID`
- `XRAY_REALITY_SNI`

Optional env values:

- `XRAY_FLOW` default `xtls-rprx-vision`
- `XRAY_FINGERPRINT` default `chrome`

## Build via Docker

```bash
docker build -t vpn-admin-api .
docker run --net=host --rm \
  -e VPN_ADMIN_API_KEY=super-secret \
  -e XRAY_PUBLIC_HOST=vpn.example.com \
  -e XRAY_PUBLIC_PORT=443 \
  -e XRAY_REALITY_PUBLIC_KEY=PUBLIC_KEY \
  -e XRAY_REALITY_SHORT_ID=abcd1234 \
  -e XRAY_REALITY_SNI=www.google.com \
  -p 8080:8080 vpn-admin-api
```

Xray API must be reachable at `127.0.0.1:10085` from the container (host network recommended).

## Generate OpenAPI spec

```bash
python3 scripts/generate_openapi.py
```

Generated files:

- `openapi/openapi.json`
- `openapi/openapi.yaml` if `PyYAML` is installed
