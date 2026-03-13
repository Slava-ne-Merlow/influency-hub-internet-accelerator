from functools import lru_cache

from pydantic import BaseSettings, Field, ValidationError


class Settings(BaseSettings):
    """Application configuration."""

    xray_api_server: str = Field("127.0.0.1:10085", env="XRAY_API_SERVER")
    xray_inbound_tag: str = Field("vless-reality", env="XRAY_INBOUND_TAG")
    xray_inbound_port: int = Field(443, env="XRAY_INBOUND_PORT")
    xray_public_host: str = Field("", env="XRAY_PUBLIC_HOST")
    xray_public_port: int = Field(0, env="XRAY_PUBLIC_PORT")
    xray_reality_public_key: str = Field("", env="XRAY_REALITY_PUBLIC_KEY")
    xray_reality_short_id: str = Field("", env="XRAY_REALITY_SHORT_ID")
    xray_reality_sni: str = Field("", env="XRAY_REALITY_SNI")
    xray_flow: str = Field("xtls-rprx-vision", env="XRAY_FLOW")
    xray_fingerprint: str = Field("chrome", env="XRAY_FINGERPRINT")
    command_timeout_seconds: float = Field(5.0, env="COMMAND_TIMEOUT_SECONDS")
    vpn_admin_api_key: str = Field(..., env="VPN_ADMIN_API_KEY")

    class Config:
        env_file = ".env"
        case_sensitive = False


@lru_cache()
def get_settings() -> Settings:
    try:
        return Settings()
    except ValidationError as exc:
        missing_fields = {error["loc"][0] for error in exc.errors() if error.get("type") == "value_error.missing"}
        if "vpn_admin_api_key" in missing_fields:
            raise RuntimeError("VPN_ADMIN_API_KEY environment variable must be set") from exc
        raise
