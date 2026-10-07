FROM --platform=$BUILDPLATFORM maven:3-openjdk-17-slim AS maven-build

WORKDIR /testlum/
COPY . .
RUN mvn clean install -DskipTests

FROM eclipse-temurin:17-jre-jammy

WORKDIR /testlum/

COPY --from=maven-build /testlum/engine/target/testlum-*.jar /tmp/
RUN mv /tmp/testlum-*.jar testlum-final.jar

ENTRYPOINT ["java", "-jar", "testlum-final.jar"]
