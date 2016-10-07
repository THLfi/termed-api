#!/bin/sh

current_date=$(date +"%Y-%m-%d_%H-%M-%S")

curl \
    -X GET \
    -H "Accept: application/json" \
    -u admin:admin \
    -o termed-dump-${current_date}.json \
    http://localhost:8080/api/dump

ln -s -f termed-dump-${current_date}.json termed-dump-latest.json
