import json
import sys
from pathlib import Path

from fastapi.openapi.utils import get_openapi

try:
    import yaml
except ModuleNotFoundError:
    yaml = None


ROOT_DIR = Path(__file__).resolve().parents[1]
if str(ROOT_DIR) not in sys.path:
    sys.path.insert(0, str(ROOT_DIR))

from app.main import app  # noqa: E402


def main() -> None:
    openapi_schema = get_openapi(
        title=app.title,
        version=app.version,
        summary=app.summary,
        description=app.description,
        routes=app.routes,
        tags=app.openapi_tags,
    )

    output_dir = ROOT_DIR / "openapi"
    output_dir.mkdir(parents=True, exist_ok=True)

    json_path = output_dir / "openapi.json"
    yaml_path = output_dir / "openapi.yaml"

    json_path.write_text(
        json.dumps(openapi_schema, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8",
    )
    if yaml is not None:
        yaml_path.write_text(
            yaml.safe_dump(openapi_schema, sort_keys=False, allow_unicode=True),
            encoding="utf-8",
        )


if __name__ == "__main__":
    main()
