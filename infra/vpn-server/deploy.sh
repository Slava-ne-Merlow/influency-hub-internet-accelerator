#!/usr/bin/env bash
set -e

mkdir -p /opt/influency/vpn-admin-api

rsync -a --delete services/vpn-admin-api/ /opt/influency/vpn-admin-api/
rsync -a infra/vpn-server/docker-compose.yml /opt/influency/docker-compose.yml

cd /opt/influency
docker compose up -d --build