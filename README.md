# Termed API

Termed is a web-based vocabulary and metadata editor. 

Termed API provides the back-end (database and JSON REST API) of the editor.

## Running

Run the API with:
```
mvn spring-boot:run
```
API should respond at port `8080`.

## Using profile-specific properties

To use different configurations based on Spring profile, such as *dev*, add a new property
file:
```
/src/main/resources/application-dev.properties
```
with config like:
```
spring.datasource.url=jdbc:postgresql:termed
spring.datasource.username=termed
spring.datasource.password=
spring.datasource.driver-class-name=org.postgresql.Driver

fi.thl.termed.index=/var/lib/termed/index
```

and run:
```
mvn spring-boot:run -Dspring.profiles.active=dev
```
