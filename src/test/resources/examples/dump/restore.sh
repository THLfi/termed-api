#!/bin/sh

curl \
    -X POST \
    -H "Content-Type: application/json" \
    -u admin:admin \
    -d @termed-dump-latest.json \
    http://localhost:8080/api/restore?batch=true
