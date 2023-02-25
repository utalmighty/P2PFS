FROM openjdk:17
WORKDIR /P2PFS

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvn package
COPY target/*.jar P2PFS.jar
ENTRYPOINT [ "java", "-jar", "P2PFS.jar" ]
