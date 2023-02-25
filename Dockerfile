FROM openjdk:17 as buildstage
WORKDIR /P2PFS

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw package
COPY target/*.jar P2PFS.jar

FROM openjdk:17
COPY --from=buildstage /P2PFS/P2PFS.jar .
ENTRYPOINT [ "java", "-jar", "P2PFS.jar" ]
