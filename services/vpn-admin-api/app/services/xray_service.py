import json
import logging
import subprocess
import tempfile
from pathlib import Path
from typing import Any, Dict, List, Union

from fastapi import HTTPException, status

from app.core.config import Settings
from app.schemas import CreateUserRequest, OnlineUser, User, UserStats

logger = logging.getLogger(__name__)


class XrayService:
    def __init__(self, settings: Settings) -> None:
        self.settings = settings

    def _run_command(self, args: List[str], expect_json: bool = True) -> Union[Dict[str, Any], str]:
        logger.debug("Running command: %s", " ".join(args))
        try:
            result = subprocess.run(
                args,
                capture_output=True,
                text=True,
                timeout=self.settings.command_timeout_seconds,
                check=False,
            )
        except subprocess.TimeoutExpired as exc:
            logger.error("Command timed out: %s", exc)
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Xray command timed out",
            ) from exc

        stdout = result.stdout.strip() if result.stdout else ""
        stderr = result.stderr.strip() if result.stderr else ""

        if result.returncode != 0:
            logger.error("Command failed (%s): stdout=%s stderr=%s", result.returncode, stdout, stderr)
            message = stderr or stdout or "Xray command failed"
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=message,
            )

        if not stdout:
            logger.error("Empty response from Xray command")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Empty response from Xray command",
            )

        if not expect_json:
            return stdout

        try:
            return json.loads(stdout)
        except json.JSONDecodeError as exc:
            logger.error("Failed to parse JSON response: %s; raw=%s", exc, stdout)
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Unable to parse Xray response",
            ) from exc

    @staticmethod
    def _safe_int(value: Any) -> int:
        try:
            return int(value)
        except (TypeError, ValueError):
            return 0

    def get_users(self) -> List[User]:
        payload = {"tag": self.settings.xray_inbound_tag}
        cmd = [
            "grpcurl",
            "-plaintext",
            "-d",
            json.dumps(payload),
            self.settings.xray_api_server,
            "xray.app.proxyman.command.HandlerService.GetInboundUsers",
        ]
        data = self._run_command(cmd)
        users = data.get("users") or data.get("clients") or []
        if not isinstance(users, list):
            logger.error("Unexpected users payload: %s", users)
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Invalid users payload from Xray",
            )

        result: List[User] = []
        for user in users:
            email = user.get("email") if isinstance(user, dict) else None
            if not email:
                logger.warning("Skipping user without email: %s", user)
                continue
            result.append(User(email=email))
        return result

    def add_user(self, request: CreateUserRequest) -> None:
        payload = {
            "inbounds": [
                {
                    "tag": self.settings.xray_inbound_tag,
                    "port": self.settings.xray_inbound_port,
                    "protocol": "vless",
                    "settings": {
                        "clients": [
                            {
                                "id": request.uuid,
                                "email": request.email,
                                "level": request.level,
                                "flow": request.flow,
                            }
                        ],
                        "decryption": "none",
                    },
                }
            ]
        }

        tmp_path = Path()
        try:
            with tempfile.NamedTemporaryFile("w", delete=False, suffix=".json") as tmp_file:
                json.dump(payload, tmp_file)
                tmp_file.flush()
                tmp_path = Path(tmp_file.name)

            cmd = [
                "xray",
                "api",
                "adu",
                f"--server={self.settings.xray_api_server}",
                str(tmp_path),
            ]
            self._run_command(cmd, expect_json=False)
        finally:
            if tmp_path and tmp_path.exists():
                tmp_path.unlink(missing_ok=True)

    def remove_user(self, email: str) -> None:
        cmd = [
            "xray",
            "api",
            "rmu",
            f"--server={self.settings.xray_api_server}",
            f"-tag={self.settings.xray_inbound_tag}",
            email,
        ]
        self._run_command(cmd, expect_json=False)

    def get_users_stats(self) -> List[UserStats]:
        payload = {"pattern": "user>>>", "reset": False}
        cmd = [
            "grpcurl",
            "-plaintext",
            "-d",
            json.dumps(payload),
            self.settings.xray_api_server,
            "xray.app.stats.command.StatsService.QueryStats",
        ]
        data = self._run_command(cmd)
        stats = data.get("stat") or data.get("stats") or []
        if not isinstance(stats, list):
            logger.error("Unexpected stats payload: %s", stats)
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Invalid stats payload from Xray",
            )

        aggregated: Dict[str, Dict[str, int]] = {}
        for entry in stats:
            if not isinstance(entry, dict):
                continue
            name = entry.get("name", "")
            value = self._safe_int(entry.get("value", 0))
            if not name.startswith("user>>>"):
                continue
            parts = name.split(">>>")
            if len(parts) < 4:
                continue
            email = parts[1]
            direction = parts[3]
            if direction not in {"uplink", "downlink"}:
                continue
            aggregated.setdefault(email, {"uplink": 0, "downlink": 0})
            aggregated[email][direction] = value

        return [UserStats(email=email, uplink=values["uplink"], downlink=values["downlink"]) for email, values in aggregated.items()]

    def get_user_stats(self, email: str) -> UserStats:
        uplink = self._get_stat_value(email, "uplink")
        downlink = self._get_stat_value(email, "downlink")
        return UserStats(email=email, uplink=uplink, downlink=downlink)

    def _get_stat_value(self, email: str, direction: str) -> int:
        name = f"user>>>{email}>>>traffic>>>{direction}"
        payload = {"name": name, "reset": False}
        cmd = [
            "grpcurl",
            "-plaintext",
            "-d",
            json.dumps(payload),
            self.settings.xray_api_server,
            "xray.app.stats.command.StatsService.GetStats",
        ]
        data = self._run_command(cmd)
        stat = data.get("stat") or {}
        if not isinstance(stat, dict):
            logger.error("Unexpected stat payload: %s", stat)
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Invalid stat payload from Xray",
            )
        value = self._safe_int(stat.get("value", 0))
        return value

    def get_online_users(self) -> List[OnlineUser]:
        cmd = [
            "grpcurl",
            "-plaintext",
            "-d",
            "{}",
            self.settings.xray_api_server,
            "xray.app.stats.command.StatsService.GetAllOnlineUsers",
        ]
        data = self._run_command(cmd)
        users = data.get("users") or data.get("user") or []
        if not isinstance(users, list):
            logger.error("Unexpected online users payload: %s", users)
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Invalid online users payload from Xray",
            )

        result: List[OnlineUser] = []
        for user in users:
            if not isinstance(user, str) or not user:
                continue
            if user.startswith("user>>>") and user.endswith(">>>online"):
                parts = user.split(">>>")
                if len(parts) >= 3:
                    result.append(OnlineUser(email=parts[1], online=True))
            else:
                result.append(OnlineUser(email=user, online=True))

        return result
