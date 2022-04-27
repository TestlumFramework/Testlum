FROM maven:3.8.5-openjdk-8 as maven-build

WORKDIR /e2e/
COPY . .
RUN mvn clean install -DskipTests

FROM openjdk:8

RUN apt-get update -y \
    && apt-get -qqy dist-upgrade \
    && apt-get -qqy install software-properties-common gettext-base unzip \
    wget \
    curl \
    firefox-esr \
	&& rm -rf /var/lib/apt/lists/* /var/cache/apt/*

#Version numbers
ARG CHROME_DRIVER_VERSION=100.0.4896.60
ARG FIREFOX_DRIVER_VERSION=0.31.0
ARG FIREFOX_VERSION=99.0.1

# Google Chrome
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
	&& echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list \
	&& apt-get update -qqy \
	&& apt-get -qqy install google-chrome-stable \
	&& rm /etc/apt/sources.list.d/google-chrome.list \
	&& rm -rf /var/lib/apt/lists/* /var/cache/apt/* \
	&& sed -i 's/"$HERE\/chrome"/"$HERE\/chrome" --no-sandbox --disable-dev-shm-usage/g' /opt/google/chrome/google-chrome


# ChromeDriver
RUN wget --no-verbose -O /tmp/chromedriver_linux64.zip https://chromedriver.storage.googleapis.com/$CHROME_DRIVER_VERSION/chromedriver_linux64.zip \
	&& rm -rf /opt/chromedriver \
	&& unzip /tmp/chromedriver_linux64.zip -d /opt \
	&& rm /tmp/chromedriver_linux64.zip \
	&& mv /opt/chromedriver /opt/chromedriver-$CHROME_DRIVER_VERSION \
	&& chmod 755 /opt/chromedriver-$CHROME_DRIVER_VERSION \
	&& ln -fs /opt/chromedriver-$CHROME_DRIVER_VERSION /usr/bin/chromedriver

# FireFox
RUN set -x \
   && apt install -y \
       libx11-xcb1 \
       libdbus-glib-1-2 \
   && curl -sSLO https://download-installer.cdn.mozilla.net/pub/firefox/releases/${FIREFOX_VERSION}/linux-x86_64/en-US/firefox-${FIREFOX_VERSION}.tar.bz2 \
   && tar -jxf firefox-* \
   && mv firefox /opt/ \
   && chmod 755 /opt/firefox \
   && chmod 755 /opt/firefox/firefox

# FirefoxDriver
RUN set -x \
   && curl -sSLO https://github.com/mozilla/geckodriver/releases/download/v${FIREFOX_DRIVER_VERSION}/geckodriver-v${FIREFOX_DRIVER_VERSION}-linux64.tar.gz \
   && tar zxf geckodriver-*.tar.gz \
   && mv geckodriver /usr/bin/

# Xvfb
RUN apt-get update -qqy \
	&& apt-get -qqy install xvfb \
	&& rm -rf /var/lib/apt/lists/* /var/cache/apt/*

ARG JAR_FILE=target/e2e-testing-tool.jar

WORKDIR /e2e/

COPY --from=maven-build /e2e/${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]