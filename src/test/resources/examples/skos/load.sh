#!/bin/sh

GRAPH_ID=`curl -X POST \
                -H "Content-Type: application/json" \
                -u admin:admin \
                -d @example-skos-graph.json \
                http://localhost:8080/api/graphs?returnIdOnly=true`

curl -X POST \
     -H "Content-Type: application/json" \
     -u admin:admin \
     -d @example-skos-types.json \
     http://localhost:8080/api/graphs/$GRAPH_ID/types?batch=true
