#!/bin/sh

if [ ! -f mesh-skos.ttl ]; then
    curl -O -L https://github.com/NatLibFi/Finto-data/raw/master/vocabularies/mesh/mesh-skos.ttl
fi

GRAPH_ID=`curl -X POST \
                -H "Content-Type: application/json" \
                -u admin:admin \
                -d @mesh-graph.json \
                http://localhost:8080/api/graphs?returnIdOnly=true`

# basic SKOS, no additional vocabulary specific types or attributes are defined
curl -X POST \
     -H "Content-Type: application/json" \
     -u admin:admin \
     -d @mesh-types.json \
     http://localhost:8080/api/graphs/$GRAPH_ID/types?batch=true

curl \
    -X POST \
    -H "Content-Type: text/turtle" \
    -u admin:admin \
    -d @mesh-skos.ttl \
    http://localhost:8080/api/graphs/$GRAPH_ID/nodes?batch=true
