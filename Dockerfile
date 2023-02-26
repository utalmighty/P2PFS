FROM openjdk:17 as buildstage
WORKDIR /P2PFS
ENTRYPOINT [ "java", "-jar", "P2PFS.jar" ]
EXPOSE 9001/tcp