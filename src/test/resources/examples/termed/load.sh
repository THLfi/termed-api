#!/bin/sh

SCHEME_ID=`curl -X POST \
                -H "Content-Type: application/json" \
                -u admin:admin \
                -d @animals-scheme.json \
                http://localhost:8080/api/schemes?returnIdOnly=true`

curl -X POST \
     -H "Content-Type: application/json" \
     -u admin:admin \
     -d @animals-classes.json \
     http://localhost:8080/api/schemes/$SCHEME_ID/classes?batch=true

curl -X POST \
     -H "Content-Type: application/json" \
     -u admin:admin \
     -d @animals-resources.json \
     http://localhost:8080/api/schemes/$SCHEME_ID/classes/Concept/resources?batch=true
