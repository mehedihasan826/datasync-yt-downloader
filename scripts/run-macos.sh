#!/bin/bash
set -e

echo "Starting DataSync YT Downloader..."

if [ -f ".env" ]; then
  while IFS='=' read -r key value || [ -n "$key" ]; do
    # skip empty lines and comments
    [[ -z "$key" || "$key" =~ ^# ]] && continue

    # trim possible Windows CRLF
    value="${value%$'\r'}"

    # export safely even when value contains spaces
    export "$key=$value"
  done < .env
fi

./mvnw spring-boot:run
