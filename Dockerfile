FROM maven AS build

COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

#
# Package stage
#
FROM openjdk:17

ARG ACTIVE_PROFILE
ARG DB_USERNAME
ARG DB_PASSWORD

ENV ACTIVE_PROFILE=$ACTIVE_PROFILE \
    DB_USERNAME=$DB_USERNAME \
    DB_PASSWORD=$DB_PASSWORD

COPY --from=build /home/app/target/*.jar /usr/local/lib/P2PFS.jar
ENTRYPOINT ["java","-jar","/usr/local/lib/P2PFS.jar"]
EXPOSE 8080/tcp