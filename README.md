<h1 align="center">
  <br/>
  <a href="https://testlum.com"><img src="https://testlum.com/img/logo.1acbeb84.svg" alt="Testlum" width="100"></a>
</h1>
<h2 align="center">The best codeless end-to-end testing framework</h2>

[![Testlum Engine Build](https://github.com/TestlumFramework/Testlum/actions/workflows/build.yml/badge.svg)](https://github.com/TestlumFramework/Testlum/actions/workflows/build.yml)
[![License](http://img.shields.io/:license-Apache%202-red.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

<h2 align="center"></h2>


# Key Features

- 🌐 **Web UI Testing**  
  Test any web application with support for modern browsers, advanced actions, and element validation.

- 📱 **Mobile Testing**  
  Run tests on mobile browsers or native apps using real devices or emulators.

- 💾 **Database Testing**  
  Execute and validate SQL/NoSQL queries across various databases like PostgreSQL, MySQL, MongoDB, etc.

- 🔌 **HTTP API Testing**  
  Easily send requests, validate responses, handle authentication (API Key, JWT, OAuth), and simulate complex flows.

- **Tests are defined in XML scenarios rather than code, making it accessible to non-developers**

- **20+ Integrations**

# Table of Contents

- [Test Sample](#TestSample)
- [Running](#running)
- [Documentation](#documentation)
- [Social Media](#Social)
- [Contributing](#contributing)
- [License](#license)

# TestSample

```xml
<scenario>
    <overview>
        <name>End-to-end test. Using Web, Postgres, Http commands</name>
        <description>Open web browser and press 'Sign In' button, check users in the database, get profile via http call</description>
    </overview>

    <settings>
        <tags>web</tags>
    </settings>

    <web comment="Start web browser">
        <click comment="Click on 'Sign In' button" locator="login.signIn"/>
    </web>
    
    <postgres comment="Make sure that users were added to 'users' table" file="expected_2.json">
        <query>SELECT * FROM users</query>
    </postgres>

    <http comment="Get user profile from API">
        <get endpoint="/user/profile/1">
            <response code="200" file="expected_3.json"/>
        </get>
    </http>
</scenario>
```

# Running
Prerequisites

Make sure you have the following installed:
- Java [17,21] 
- Maven

### Run locally
After cloning repository to your local machine: 
- navigate to root project folder
- build executable jar file ```mvn clean install -DskipTests```
- and run it by passing test resources location ```java -jar engine/target/testlum-1.0.2.jar -c="global-config-file-name.xml" -p"=absolute path to folder with test resources"```
- examples of test resources can be found in our [Wiki](https://github.com/TestlumFramework/Testlum/wiki)

### If you want to build docker image from the latest code
- navigate to root project folder
- build image from [Dockerfile](Dockerfile)
```shell
  docker build --no-cache --build-arg APP_VERSION=1.0.2 -t testlum:1.0.2 .
  ./run-docker testlum:1.0.2 global-config.xml ~/Users/user/test-resources 
```

Or You can use our official docker image from [Packages](https://github.com/TestlumFramework/Testlum/pkgs/container/testlum)

### Pull it 
```shell
  docker pull ghcr.io/testlumframework/testlum:latest
```

# Documentation

Full user documentation for this project is available on:
👉 [View Documentation](https://github.com/TestlumFramework/Testlum/wiki)


# Social

- 🌐 Check for more features on website 👉 [Open](https://testlum.com)
- 💬 Ask more questions on discord 👉 [Open](https://discord.gg/JxfcZPqBNY)
- 🎥 Get more interesting insights on YouTube 👉 [Open](https://www.youtube.com/channel/UC5F7ZWCL-hzYz6Sr2zv2MQA)


Subscribe and never miss an update

# Contributing
Please read [CONTRIBUTING.md](CONTRIBUTING.md) before submitting your pull requests

# License
Testlum source code is made available under the [Apache 2.0 license](LICENSE).


