FROM openjdk:17 as buildstage
FROM maven
CMD [ "mvn", "clean", "package" ]
COPY /target/*.jar /P2PFS.jar
ENTRYPOINT [ "java", "-jar", "/P2PFS.jar" ]