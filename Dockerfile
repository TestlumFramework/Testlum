ARG APP_VERSION="0.0.0"

FROM maven:3-openjdk-17-slim AS maven-build

ARG APP_VERSION

WORKDIR /testlum/
COPY . .
RUN mvn clean install -DskipTests

FROM eclipse-temurin:17-jdk-jammy

ARG APP_VERSION
RUN echo "Container version: ${APP_VERSION}"

WORKDIR /testlum/

COPY --from=maven-build /testlum/engine/target/testlum-${APP_VERSION}.jar testlum-final.jar

ENTRYPOINT ["java", "-jar", "testlum-final.jar"]
