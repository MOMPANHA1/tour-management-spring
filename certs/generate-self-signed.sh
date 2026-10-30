#!/usr/bin/env bash
# Generates a self-signed TLS cert for Keycloak, plus a Java truststore that
# trusts it, so the Spring app (also a JVM) can call Keycloak over HTTPS
# without disabling certificate validation.
#
# Run this once before `docker compose up`, and again whenever KC_HOSTNAME
# changes (the cert's SAN list must include whatever host/IP browsers and the
# backend use to reach Keycloak).
#
# Usage: ./generate-self-signed.sh [host1] [host2] ...
#   e.g. ./generate-self-signed.sh auth.example.com 203.0.113.10

set -euo pipefail
cd "$(dirname "$0")"

EXTRA_HOSTS=("$@")
TRUSTSTORE_PASSWORD="${TRUSTSTORE_PASSWORD:-changeit}"

mkdir -p keycloak

SAN="subjectAltName=DNS:keycloak,DNS:localhost,IP:127.0.0.1"
for h in "${EXTRA_HOSTS[@]}"; do
  if [[ "$h" =~ ^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    SAN="${SAN},IP:${h}"
  else
    SAN="${SAN},DNS:${h}"
  fi
done

echo "Generating self-signed cert with SAN: ${SAN}"

openssl req -x509 -newkey rsa:2048 -sha256 -days 825 -nodes \
  -keyout keycloak/tls.key \
  -out keycloak/tls.crt \
  -subj "/CN=keycloak" \
  -addext "${SAN}"

rm -f truststore.jks
keytool -importcert -noprompt \
  -alias keycloak-self-signed \
  -file keycloak/tls.crt \
  -keystore truststore.jks \
  -storepass "${TRUSTSTORE_PASSWORD}"

echo
echo "Done:"
echo "  certs/keycloak/tls.crt   -> mounted into the keycloak container"
echo "  certs/keycloak/tls.key   -> mounted into the keycloak container (keep private!)"
echo "  certs/truststore.jks     -> mounted into the app container (password: ${TRUSTSTORE_PASSWORD})"
