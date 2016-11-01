#!/bin/sh

GRAPH_ID=`curl -X POST \
                -H "Content-Type: application/json" \
                -u admin:admin \
                -d @animals-graph.json \
                http://localhost:8080/api/graphs?returnIdOnly=true`

curl -X POST \
     -H "Content-Type: application/json" \
     -u admin:admin \
     -d @animals-types.json \
     http://localhost:8080/api/graphs/$GRAPH_ID/types?batch=true

curl -X POST \
     -H "Content-Type: application/json" \
     -u admin:admin \
     -d @animals-nodes.json \
     http://localhost:8080/api/graphs/$GRAPH_ID/types/Concept/nodes?batch=true
