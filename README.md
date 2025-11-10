# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.6/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.5.6/gradle-plugin/packaging-oci-image.html)
* [GraalVM Native Image Support](https://docs.spring.io/spring-boot/3.5.6/reference/packaging/native-image/introducing-graalvm-native-images.html)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/3.5.6/reference/actuator/index.html)
* [Spring Data MongoDB](https://docs.spring.io/spring-boot/3.5.6/reference/data/nosql.html#data.nosql.mongodb)
* [Spring Data Neo4j](https://docs.spring.io/spring-boot/3.5.6/reference/data/nosql.html#data.nosql.neo4j)
* [Spring Data Redis (Access+Driver)](https://docs.spring.io/spring-boot/3.5.6/reference/data/nosql.html#data.nosql.redis)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/3.5.6/reference/using/devtools.html)
* [Docker Compose Support](https://docs.spring.io/spring-boot/3.5.6/reference/features/dev-services.html#features.dev-services.docker-compose)
* [Apache Freemarker](https://docs.spring.io/spring-boot/3.5.6/reference/web/servlet.html#web.servlet.spring-mvc.template-engines)
* [Graphite](https://docs.spring.io/spring-boot/3.5.6/reference/actuator/metrics.html#actuator.metrics.export.graphite)
* [OAuth2 Client](https://docs.spring.io/spring-boot/3.5.6/reference/web/spring-security.html#web.security.oauth2.client)
* [Spring REST Docs](https://docs.spring.io/spring-restdocs/docs/current/reference/htmlsingle/)
* [Spring Security](https://docs.spring.io/spring-boot/3.5.6/reference/web/spring-security.html)
* [Anthropic Claude](https://docs.spring.io/spring-ai/reference/api/chat/anthropic-chat.html)
* [MongoDB Atlas Vector Database](https://docs.spring.io/spring-ai/reference/api/vectordbs/mongodb.html)
* [Thymeleaf](https://docs.spring.io/spring-boot/3.5.6/reference/web/servlet.html#web.servlet.spring-mvc.template-engines)
* [Spring Web](https://docs.spring.io/spring-boot/3.5.6/reference/web/servlet.html)
* [WebSocket](https://docs.spring.io/spring-boot/3.5.6/reference/messaging/websockets.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
* [Accessing Data with MongoDB](https://spring.io/guides/gs/accessing-data-mongodb/)
* [Accessing Data with Neo4j](https://spring.io/guides/gs/accessing-data-neo4j/)
* [Messaging with Redis](https://spring.io/guides/gs/messaging-redis/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
* [Handling Form Submission](https://spring.io/guides/gs/handling-form-submission/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Using WebSocket to build an interactive web application](https://spring.io/guides/gs/messaging-stomp-websocket/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)
* [Configure AOT settings in Build Plugin](https://docs.spring.io/spring-boot/3.5.6/how-to/aot.html)

### Docker Compose support
This project contains a Docker Compose file named `compose.yaml`.
In this file, the following services have been defined:

* mongodb: [`mongo:latest`](https://hub.docker.com/_/mongo)
* mongodbatlas: [`mongodb/mongodb-atlas-local:latest`](https://hub.docker.com/r/mongodb/mongodb-atlas-local)
* neo4j: [`neo4j:latest`](https://hub.docker.com/_/neo4j)
* redis: [`redis:latest`](https://hub.docker.com/_/redis)

Please review the tags of the used images and set them to the same as you're running in production.

## GraalVM Native Support

This project has been configured to let you generate either a lightweight container or a native executable.
It is also possible to run your tests in a native image.

### Lightweight Container with Cloud Native Buildpacks
If you're already familiar with Spring Boot container images support, this is the easiest way to get started.
Docker should be installed and configured on your machine prior to creating the image.

To create the image, run the following goal:

```
$ ./gradlew bootBuildImage
```

Then, you can run the app like any other container:

```
$ docker run --rm -p 8080:8080 bookswap:0.0.1-SNAPSHOT
```

### Executable with Native Build Tools
Use this option if you want to explore more options such as running your tests in a native image.
The GraalVM `native-image` compiler should be installed and configured on your machine.

NOTE: GraalVM 22.3+ is required.

To create the executable, run the following goal:

```
$ ./gradlew nativeCompile
```

Then, you can run the app as follows:
```
$ build/native/nativeCompile/bookswap
```

You can also run your existing tests suite in a native image.
This is an efficient way to validate the compatibility of your application.

To run your existing tests in a native image, run the following goal:

```
$ ./gradlew nativeTest
```

### Gradle Toolchain support

There are some limitations regarding Native Build Tools and Gradle toolchains.
Native Build Tools disable toolchain support by default.
Effectively, native image compilation is done with the JDK used to execute Gradle.
You can read more about [toolchain support in the Native Build Tools here](https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html#configuration-toolchains).

