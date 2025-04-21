# Multi-Stage build
ARG MAVEN_TAG=3.8.6-jdk-11
ARG JDK_TAG=11.0-jdk
  # Build container
FROM maven:${MAVEN_TAG} AS maven-build

WORKDIR /testlum/
COPY . .
RUN mvn clean install -P professional -DskipTests

# Target container
FROM  openjdk:${JDK_TAG}

# Install essential tools and dependencies
RUN apt-get update -y \
    && apt-get -qqy dist-upgrade \
    && apt-get -qqy install wget unzip curl gnupg ca-certificates software-properties-common gettext-base xdg-utils firefox-esr xvfb \
    && apt-get -f install

#Version numbers
ARG CHROME_DRIVER_VERSION=123.0.6312.58

# Google Chrome
RUN wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb \
    && apt-get update \
    && apt-get install -y ./google-chrome-stable_current_amd64.deb || apt --fix-broken install -y \
    && rm ./google-chrome-stable_current_amd64.deb

# ChromeDriver
RUN wget --no-verbose -O /tmp/chromedriver_linux64.zip https://storage.googleapis.com/chrome-for-testing-public/$CHROME_DRIVER_VERSION/linux64/chromedriver-linux64.zip \
	&& unzip /tmp/chromedriver_linux64.zip -d /tmp \
	&& mv /tmp/chromedriver-linux64/chromedriver /usr/bin/chromedriver \
	&& chmod 755 /usr/bin/chromedriver

# Xvfb
RUN apt-get update -qqy \
	&& apt-get -qqy install xvfb \
	&& rm -rf /var/lib/apt/lists/* /var/cache/apt/*

RUN mkdir -p ~/.cache/selenium/ && touch ~/.cache/selenium/resolution.properties && chmod 766 ~/.cache/selenium/resolution.properties

WORKDIR /testlum/

ARG JAR_FILE=engine/target/testlum-1.0.0.jar

COPY --from=maven-build /testlum/${JAR_FILE} testlum.jar

#ENTRYPOINT ["java", "-jar", "testlum.jar"]
ENTRYPOINT ["java", "-DTESTING_IN_PIPELINE=true", "-jar", "testlum.jar"]