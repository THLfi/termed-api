#!/bin/sh

if [ ! -f mesh-skos.ttl ]; then
    curl -O -L https://github.com/NatLibFi/Finto-data/raw/master/vocabularies/mesh/mesh-skos.ttl
fi

# basic SKOS scheme, no additional vocabulary specific classes or attributes are defined
curl \
    -X PUT \
    -H "Content-Type: application/json" \
    -u admin:admin \
    -d @skos.json \
    http://localhost:8080/api/schemes/11b68b64-4b2c-4201-919e-b6089f580cee

curl \
    -X POST \
    -H "Content-Type: text/turtle" \
    -u admin:admin \
    -d @mesh-skos.ttl \
    http://localhost:8080/api/schemes/11b68b64-4b2c-4201-919e-b6089f580cee/resources?batch=true
