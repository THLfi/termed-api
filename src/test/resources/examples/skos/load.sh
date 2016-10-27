#!/bin/sh

SCHEME_ID=`curl -X POST \
                -H "Content-Type: application/json" \
                -u admin:admin \
                -d @example-skos-scheme.json \
                http://localhost:8080/api/schemes?returnIdOnly=true`

curl -X POST \
     -H "Content-Type: application/json" \
     -u admin:admin \
     -d @example-skos-classes.json \
     http://localhost:8080/api/schemes/$SCHEME_ID/classes?batch=true
