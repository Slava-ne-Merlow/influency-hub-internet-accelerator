import logging
import secrets
from typing import Optional

from fastapi import Depends, HTTPException, Request, Security, status
from fastapi.security import APIKeyHeader

from app.core.config import Settings, get_settings

logger = logging.getLogger(__name__)

service_key_header = APIKeyHeader(
    name="X-Service-Key",
    scheme_name="ServiceKeyAuth",
    description="Shared API key for service-to-service access.",
    auto_error=False,
)


def verify_service_key(
    request: Request,
    service_key: Optional[str] = Security(service_key_header),
    settings: Settings = Depends(get_settings),
) -> None:
    if service_key and secrets.compare_digest(service_key, settings.vpn_admin_api_key):
        return

    logger.warning(
        "Service key authentication failed for path=%s client=%s",
        request.url.path,
        request.client.host if request.client else "unknown",
    )
    raise HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Invalid or missing service key",
    )
