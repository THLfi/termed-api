# Termed

Termed is a web-based vocabulary and metadata editor.

## Development

### Running the back-end

To start the REST API, run in the *termed-api* directory:
```
mvn spring-boot:run
```
API should respond at port `8080`.

### Running the front-end

If `npm` and `grunt` are already installed in the system, run in the *termed-ui* directory:
```
npm install
grunt dev
```
Application should respond at `http://localhost:8000`.

One can also use `npm` and `grunt` installed by maven front-end plugin by running in the *client*
directory:
```
mvn install
./node_modules/grunt-cli/bin/grunt dev
```

### Using profile-specific properties

To use different configurations based on Spring profile, such as *dev*, add a new property
file:
```
/termed-api/src/main/resources/application-dev.properties
```
with config like:
```
spring.datasource.url=jdbc:postgresql:termed
spring.datasource.username=termed
spring.datasource.password=
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.properties.hibernate.search.default.directory_provider=filesystem
spring.jpa.properties.hibernate.search.default.indexBase=index
```

and run:
```
mvn spring-boot:run -Dspring.profiles.active=dev
```

## Production

### Creating a war file

After running `mvn install`, a war file can be found at `/termed-war/target/termed.war`.

External properties can be configured using standard mechanisms provided by spring boot:
http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html
