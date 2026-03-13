from typing import List, Optional

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
    flow: Optional[str] = Field(
        default=None,
        description="Optional VLESS flow override. If omitted, service uses XRAY_FLOW.",
        example="xtls-rprx-vision",
    )


class ActionStatus(BaseModel):
    status: str = Field(
        default="ok",
        description="Operation result.",
        example="ok",
    )


class CreatedUserInfo(BaseModel):
    email: str = Field(
        ...,
        min_length=1,
        description="Unique Xray user identifier.",
        example="api-test-user",
    )
    uuid: str = Field(
        ...,
        regex=r"^[0-9a-fA-F\-]{36}$",
        description="VLESS client UUID.",
        example="22222222-2222-2222-2222-222222222222",
    )


class UserConnectionInfo(BaseModel):
    uri: str = Field(
        ...,
        description="Ready-to-use VLESS REALITY URI.",
        example=(
            "vless://22222222-2222-2222-2222-222222222222@vpn.example.com:443"
            "?security=reality&encryption=none&flow=xtls-rprx-vision&type=tcp"
            "&sni=www.google.com&fp=chrome&pbk=PUBLIC_KEY&sid=abcd1234#api-test-user"
        ),
    )
    host: str = Field(
        ...,
        min_length=1,
        description="Public Xray host exposed to clients.",
        example="vpn.example.com",
    )
    port: int = Field(
        ...,
        description="Public Xray port exposed to clients.",
        example=443,
    )
    sni: str = Field(
        ...,
        min_length=1,
        description="REALITY SNI value.",
        example="www.google.com",
    )
    public_key: str = Field(
        ...,
        min_length=1,
        description="REALITY public key.",
        example="PUBLIC_KEY",
    )
    short_id: str = Field(
        ...,
        min_length=1,
        description="REALITY short id.",
        example="abcd1234",
    )


class CreateUserResponse(BaseModel):
    status: str = Field(
        default="ok",
        description="Operation result.",
        example="ok",
    )
    user: CreatedUserInfo
    connection: UserConnectionInfo


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
