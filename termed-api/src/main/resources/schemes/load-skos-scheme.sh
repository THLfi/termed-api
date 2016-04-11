#!/bin/sh

curl \
    -X POST \
    -H "Content-Type: application/json" \
    -u admin:admin \
    -d @skos.json \
    http://localhost:8080/api/schemes
