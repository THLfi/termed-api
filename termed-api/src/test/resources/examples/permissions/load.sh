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
    -u superuser:superuser \
    -d @animals-users.json \
    http://localhost:8080/api/users?batch=true

curl \
    -X POST \
    -H "Content-Type: application/json" \
    -u admin:admin \
    -d @animals-data.json \
    http://localhost:8080/api/resources?batch=true
