# Termed

Termed is a web-based vocabulary and metadata editor.

## Development

### Running the back-end

To start the REST API, run in the *termed-api* directory:
```
mvn spring-boot:run
```
API should respond at port `8080`.

### Using profile-specific properties

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

### Creating a war file

Run `mvn install` at project root. A war file can be found at `/target/termed.war`.

External properties can be configured using standard mechanisms provided by spring boot:
http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html
