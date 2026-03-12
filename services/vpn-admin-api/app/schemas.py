from typing import List

from pydantic import BaseModel, Field


class HealthResponse(BaseModel):
    status: str = Field(
        default="ok",
        description="Service health status.",
        example="ok",
    )


class User(BaseModel):
    email: str = Field(
        ...,
        min_length=1,
        description="Unique Xray user identifier. Used as logical user key in admin API.",
        example="test-user",
    )


class UsersResponse(BaseModel):
    """List of users configured on the target inbound."""

    items: List[User]


class CreateUserRequest(BaseModel):
    email: str = Field(
        ...,
        min_length=1,
        description="Unique user identifier in Xray. Usually mapped to your application user key.",
        example="test-user-2",
    )
    uuid: str = Field(
        ...,
        regex=r"^[0-9a-fA-F\-]{36}$",
        description="VLESS client UUID.",
        example="22222222-2222-2222-2222-222222222222",
    )
    level: int = Field(
        default=0,
        description="Xray client level.",
        example=0,
    )
    flow: str = Field(
        default="xtls-rprx-vision",
        description="VLESS flow for the client.",
        example="xtls-rprx-vision",
    )


class ActionStatus(BaseModel):
    status: str = Field(
        default="ok",
        description="Operation result.",
        example="ok",
    )

class ErrorResponse(BaseModel):
    detail: str = Field(
        ...,
        description="Human-readable error message.",
        example="Xray command failed",
    )


class UserStats(BaseModel):
    email: str = Field(
        ...,
        min_length=1,
        description="Unique Xray user identifier.",
        example="test-user",
    )
    uplink: int = Field(
        ...,
        description="Total upstream traffic in bytes.",
        example=123,
    )
    downlink: int = Field(
        ...,
        description="Total downstream traffic in bytes.",
        example=456,
    )


class UsersStatsResponse(BaseModel):
    """Aggregated traffic stats for all users."""

    items: List[UserStats]


class OnlineUser(BaseModel):
    email: str = Field(
        ...,
        min_length=1,
        description="Unique Xray user identifier.",
        example="test-user",
    )
    online: bool = Field(
        default=True,
        description="Whether the user is currently online.",
        example=True,
    )


class OnlineUsersResponse(BaseModel):
    """List of online users."""

    items: List[OnlineUser]
