from fastapi import APIRouter, Depends, status

from app.core.config import get_settings, Settings
from app.schemas import (
    ActionStatus,
    CreateUserRequest,
    HealthResponse,
    OnlineUsersResponse,
    UsersResponse,
    UsersStatsResponse,
    UserStats,
)
from app.services.xray_service import XrayService

router = APIRouter()


def get_xray_service(settings: Settings = Depends(get_settings)) -> XrayService:
    return XrayService(settings)


@router.get("/health", response_model=HealthResponse, status_code=status.HTTP_200_OK)
def health() -> HealthResponse:
    return HealthResponse()


@router.get("/users", response_model=UsersResponse)
def list_users(service: XrayService = Depends(get_xray_service)) -> UsersResponse:
    users = service.get_users()
    return UsersResponse(items=users)


@router.post("/users", response_model=ActionStatus, status_code=status.HTTP_201_CREATED)
def create_user(
    payload: CreateUserRequest, service: XrayService = Depends(get_xray_service)
) -> ActionStatus:
    service.add_user(payload)
    return ActionStatus()


@router.delete("/users/{email}", response_model=ActionStatus)
def delete_user(email: str, service: XrayService = Depends(get_xray_service)) -> ActionStatus:
    service.remove_user(email)
    return ActionStatus()


@router.get("/stats/users", response_model=UsersStatsResponse)
def stats_users(service: XrayService = Depends(get_xray_service)) -> UsersStatsResponse:
    stats = service.get_users_stats()
    return UsersStatsResponse(items=stats)


@router.get("/stats/users/{email}", response_model=UserStats)
def stats_user(email: str, service: XrayService = Depends(get_xray_service)) -> UserStats:
    return service.get_user_stats(email)


@router.get("/online", response_model=OnlineUsersResponse)
def online_users(service: XrayService = Depends(get_xray_service)) -> OnlineUsersResponse:
    users = service.get_online_users()
    return OnlineUsersResponse(items=users)
