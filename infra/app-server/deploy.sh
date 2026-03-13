#!/usr/bin/env bash
set -e

mkdir -p /opt/influency/app-backend

rsync -a --delete services/app-backend/ /opt/influency/app-backend/
rsync -a infra/app-server/docker-compose.yml /opt/influency/docker-compose.yml

cd /opt/influency
docker compose --env-file /opt/influency/.env up -d --build