from typing import Annotated

from fastapi import APIRouter, Depends, Path, status

from app.api.dependencies import verify_service_key
from app.core.config import get_settings, Settings
from app.schemas import (
    ActionStatus,
    CreateUserRequest,
    ErrorResponse,
    HealthResponse,
    OnlineUsersResponse,
    UsersResponse,
    UsersStatsResponse,
    UserStats,
)
from app.services.xray_service import XrayService

public_router = APIRouter()
protected_router = APIRouter(dependencies=[Depends(verify_service_key)])

XRAY_TAG = "xray-admin"
MONITORING_TAG = "monitoring"
COMMON_ERROR_RESPONSES = {
    401: {
        "model": ErrorResponse,
        "description": "Missing or invalid X-Service-Key header.",
    },
    500: {
        "model": ErrorResponse,
        "description": "Xray command failed, timed out, or returned invalid output.",
    }
}


def get_xray_service(settings: Settings = Depends(get_settings)) -> XrayService:
    return XrayService(settings)


@public_router.get(
    "/health",
    response_model=HealthResponse,
    status_code=status.HTTP_200_OK,
    tags=[MONITORING_TAG],
    summary="Health check",
    description="Returns service health status for deployment and monitoring checks.",
)
def health() -> HealthResponse:
    return HealthResponse()


@protected_router.get(
    "/users",
    response_model=UsersResponse,
    tags=[XRAY_TAG],
    summary="List inbound users",
    description="Returns all users currently configured on inbound tag `vless-reality`.",
    responses=COMMON_ERROR_RESPONSES,
)
def list_users(service: XrayService = Depends(get_xray_service)) -> UsersResponse:
    users = service.get_users()
    return UsersResponse(items=users)


@protected_router.post(
    "/users",
    response_model=ActionStatus,
    status_code=status.HTTP_201_CREATED,
    tags=[XRAY_TAG],
    summary="Create inbound user",
    description="Adds a new VLESS user to inbound tag `vless-reality` via local Xray runtime API.",
    responses=COMMON_ERROR_RESPONSES,
)
def create_user(
    payload: CreateUserRequest, service: XrayService = Depends(get_xray_service)
) -> ActionStatus:
    service.add_user(payload)
    return ActionStatus()


@protected_router.delete(
    "/users/{email}",
    response_model=ActionStatus,
    tags=[XRAY_TAG],
    summary="Delete inbound user",
    description="Removes a user from inbound tag `vless-reality` by email.",
    responses={
        **COMMON_ERROR_RESPONSES,
        422: {"description": "Invalid email path parameter."},
    },
)
def delete_user(
    email: Annotated[
        str,
        Path(
            ...,
            description="Unique Xray user identifier to remove.",
            example="test-user",
            min_length=1,
        ),
    ],
    service: XrayService = Depends(get_xray_service),
) -> ActionStatus:
    service.remove_user(email)
    return ActionStatus()


@protected_router.get(
    "/stats/users",
    response_model=UsersStatsResponse,
    tags=[XRAY_TAG],
    summary="List users traffic stats",
    description="Returns aggregated uplink and downlink traffic counters for all users.",
    responses=COMMON_ERROR_RESPONSES,
)
def stats_users(service: XrayService = Depends(get_xray_service)) -> UsersStatsResponse:
    stats = service.get_users_stats()
    return UsersStatsResponse(items=stats)


@protected_router.get(
    "/stats/users/{email}",
    response_model=UserStats,
    tags=[XRAY_TAG],
    summary="Get user traffic stats",
    description="Returns uplink and downlink counters for a specific user.",
    responses={
        **COMMON_ERROR_RESPONSES,
        422: {"description": "Invalid email path parameter."},
    },
)
def stats_user(
    email: Annotated[
        str,
        Path(
            ...,
            description="Unique Xray user identifier to query.",
            example="test-user",
            min_length=1,
        ),
    ],
    service: XrayService = Depends(get_xray_service),
) -> UserStats:
    return service.get_user_stats(email)


@protected_router.get(
    "/online",
    response_model=OnlineUsersResponse,
    tags=[XRAY_TAG],
    summary="List online users",
    description="Returns all users currently reported by Xray as online.",
    responses=COMMON_ERROR_RESPONSES,
)
def online_users(service: XrayService = Depends(get_xray_service)) -> OnlineUsersResponse:
    users = service.get_online_users()
    return OnlineUsersResponse(items=users)
