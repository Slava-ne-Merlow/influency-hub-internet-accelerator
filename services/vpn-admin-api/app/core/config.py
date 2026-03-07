from functools import lru_cache
from pydantic import BaseSettings, Field


class Settings(BaseSettings):
    """Application configuration."""

    xray_api_server: str = Field("127.0.0.1:10085", env="XRAY_API_SERVER")
    xray_inbound_tag: str = Field("vless-reality", env="XRAY_INBOUND_TAG")
    xray_inbound_port: int = Field(443, env="XRAY_INBOUND_PORT")
    command_timeout_seconds: float = Field(5.0, env="COMMAND_TIMEOUT_SECONDS")

    class Config:
        env_file = ".env"
        case_sensitive = False


@lru_cache()
def get_settings() -> Settings:
    return Settings()
