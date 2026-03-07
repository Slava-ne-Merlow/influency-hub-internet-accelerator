from typing import List
from pydantic import BaseModel, Field


class HealthResponse(BaseModel):
    status: str = "ok"


class User(BaseModel):
    email: str = Field(..., min_length=1)


class UsersResponse(BaseModel):
    items: List[User]


class CreateUserRequest(BaseModel):
    email: str = Field(..., min_length=1)
    uuid: str = Field(..., regex=r"^[0-9a-fA-F\-]{36}$")
    level: int = 0
    flow: str = Field("xtls-rprx-vision")


class ActionStatus(BaseModel):
    status: str = "ok"


class UserStats(BaseModel):
    email: str = Field(..., min_length=1)
    uplink: int
    downlink: int


class UsersStatsResponse(BaseModel):
    items: List[UserStats]


class OnlineUser(BaseModel):
    email: str = Field(..., min_length=1)
    online: bool = True


class OnlineUsersResponse(BaseModel):
    items: List[OnlineUser]
