#!/bin/sh

curl \
    -X POST \
    -H "Content-Type: application/json" \
    -u admin:admin \
    -d @animals-scheme.json \
    http://localhost:8080/api/schemes

curl \
    -X POST \
    -H "Content-Type: application/json" \
    -u admin:admin \
    -d @animals-data.json \
    http://localhost:8080/api/resources?batch=true
