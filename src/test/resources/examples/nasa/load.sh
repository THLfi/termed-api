#!/bin/sh

curl \
    -X PUT \
    -H "Content-Type: application/json" \
    -u admin:admin \
    -d @example-graph.json \
    http://localhost:8080/api/graphs/37cd9e2f-223c-4b89-858f-f89e61a380c7

curl \
    -X POST \
    -H "Content-Type: application/json" \
    -u admin:admin \
    -d @example-types.json \
    http://localhost:8080/api/graphs/37cd9e2f-223c-4b89-858f-f89e61a380c7/types?batch=true

curl \
    -X POST \
    -H "Content-Type: application/rdf+xml" \
    -u admin:admin \
    -d @example-nodes.rdf \
    http://localhost:8080/api/graphs/37cd9e2f-223c-4b89-858f-f89e61a380c7/nodes?batch=true
