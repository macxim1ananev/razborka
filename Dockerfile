FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /workspace
COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY user/pom.xml user/pom.xml
COPY auth/pom.xml auth/pom.xml
COPY listing/pom.xml listing/pom.xml
COPY search/pom.xml search/pom.xml
COPY app/pom.xml app/pom.xml
COPY common/src common/src
COPY user/src user/src
COPY auth/src auth/src
COPY listing/src listing/src
COPY search/src search/src
COPY app/src app/src
RUN mvn -pl app -am -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/app/target/app-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=docker"]
