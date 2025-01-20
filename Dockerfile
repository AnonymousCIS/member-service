FROM openjdk:17-jdk
ARG JAR_FILE=build/libs/memberservice-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENV SPRING_PROFILES_ACTIVE=default,jwt
ENV DB_HOST=localhost:1521
ENV DDL_AUTO=update

ENTRYPOINT ["java", "-jar", "-Dconfig.server=${CONFIG_SERVER}", "-Ddb.host=${DB_HOST}", "-Ddb.password=${DB_PASSWORD}", "-Ddb.username=${DB_USERNAME}", "-Dddl.auto=${DDL_AUTO}", "-DjwtSecret=${JWTSECRET}", "-DJwtValidTime=${JWTVALIDTIME}", "-Deureka.server=${EUREKA_SERVER}", "-Dhostname=${HOSTNAME}","app.jar"]

EXPOSE 3011