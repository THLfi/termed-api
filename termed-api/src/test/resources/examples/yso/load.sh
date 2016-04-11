#!/bin/sh

if [ ! -f yso-skos.ttl ]; then
    curl -O -L https://github.com/NatLibFi/Finto-data/raw/master/vocabularies/yso/yso-skos.ttl
fi

# basic SKOS scheme, no additional vocabulary specific classes or attributes are defined
curl \
    -X PUT \
    -H "Content-Type: application/json" \
    -u admin:admin \
    -d @skos.json \
    http://localhost:8080/api/schemes/fea40a05-6a7d-4bc5-8db5-2fba38a7457f

curl \
    -X POST \
    -H "Content-Type: text/turtle" \
    -u admin:admin \
    -d @yso-skos.ttl \
    http://localhost:8080/api/schemes/fea40a05-6a7d-4bc5-8db5-2fba38a7457f/resources?batch=true
