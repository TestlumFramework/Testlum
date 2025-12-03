# Multi-Stage build
ARG MAVEN_TAG=3.8.6-jdk-11
ARG JDK_TAG=11.0.28_6-jdk
  # Build container
FROM maven:${MAVEN_TAG} AS maven-build

WORKDIR /testlum/
COPY . .
RUN mvn clean install -P professional -DskipTests

# Target container
FROM  eclipse-temurin:${JDK_TAG}

WORKDIR /testlum/

ARG JAR_FILE=engine/target/testlum-1.0.2.jar

COPY --from=maven-build /testlum/${JAR_FILE} testlum.jar

#ENTRYPOINT ["java", "-jar", "testlum.jar"]
ENTRYPOINT ["java", "-DTESTING_IN_PIPELINE=true", "-jar", "testlum.jar"]