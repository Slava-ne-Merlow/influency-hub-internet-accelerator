import logging

from fastapi import Depends, FastAPI

from app.api.dependencies import verify_service_key
from app.api.routes import protected_router, public_router
from app.core.config import get_settings

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
)

# Fail fast if the shared service key is not configured.
get_settings()

app = FastAPI(
    title="VPN Admin API",
    version="0.1.0",
    summary="Admin API for managing VLESS users in local Xray runtime.",
    description=(
        "Production-friendly HTTP API used to manage Xray users and query traffic statistics. "
        "The service calls local `xray` and `grpcurl` binaries and does not expose Xray gRPC outside."
    ),
    contact={
        "name": "Influency Hub",
    },
    openapi_tags=[
        {
            "name": "monitoring",
            "description": "Operational endpoints used for health checks.",
        },
        {
            "name": "xray-admin",
            "description": "User management and traffic statistics endpoints backed by local Xray runtime API.",
        },
    ],
)
app.include_router(public_router)
app.include_router(protected_router)


@app.get("/", include_in_schema=False, dependencies=[Depends(verify_service_key)])
def root():
    return {"status": "ok"}
