#!/bin/sh

curl \
    -X PUT \
    -H "Content-Type: application/json" \
    -u admin:admin \
    -d @skos.json \
    http://localhost:8080/api/schemes/37cd9e2f-223c-4b89-858f-f89e61a380c7

curl \
    -X POST \
    -H "Content-Type: application/rdf+xml" \
    -u admin:admin \
    -d @access.skos \
    http://localhost:8080/api/schemes/37cd9e2f-223c-4b89-858f-f89e61a380c7/resources?batch=true
