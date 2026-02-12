<h1 align="center">
  <br/>
  <a href="https://testlum.com"><img src="https://testlum.com/img/logo.1acbeb84.svg" alt="Testlum" width="100"></a>
</h1>
<h2 align="center">Run tests fast and easy</h2>

[![Testlum Engine Build](https://github.com/TestlumFramework/Testlum/actions/workflows/build.yml/badge.svg)](https://github.com/TestlumFramework/Testlum/actions/workflows/build.yml)
[![License](http://img.shields.io/:license-Apache%202-red.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

# Table of Contents
- [Key Features](#key-features)
- [Contributing](#contributing)
- [Running](#running)
- [Documentation](#documentation)
- [Social Media](#social-media)
- [License](#license)

# Testlum
Codeless end-to-end testing framework which makes your testing easier and faster

# Key Features

- ✅ **Web UI Testing**  
  Test any web application with support for modern browsers, advanced actions, and element validation.

- 📱 **Mobile Testing**  
  Run tests on mobile browsers or native apps using real devices or emulators.

- 💾 **Database Testing**  
  Execute and validate SQL/NoSQL queries across various databases like PostgreSQL, MySQL, MongoDB, etc.

- 🌐 **HTTP API Testing**  
  Easily send requests, validate responses, handle authentication (API Key, JWT, OAuth), and simulate complex flows.

- 🔌 **WebSocket Support**  
  Open WebSocket connections, send/receive messages, and verify real-time communication.

- 📨 **Message Brokers**  
  Publish and consume messages to/from brokers like Kafka, RabbitMQ, and AWS SQS.

- 🔄 **CI/CD Integration**  
  Seamlessly plug into pipelines with rich CLI support and flexible reporting.

# Contributing
Please read [CONTRIBUTING.md](CONTRIBUTING.md) before submitting your pull requests

# Running
Prerequisites

Make sure you have the following installed:
- Java 17 or higher
- Maven
- or Docker

### Run locally
After cloning repository to your local machine: 
- navigate to root project folder
- build executable jar file ```mvn clean package -DskipTests```
- and run it by passing test resources location ```java -jar engine/target/testlum-1.0.2.jar -c="global-config-file-name.xml" -p"=absolute path to folder with test resources"```
- examples of test resources can be found in our [Wiki](https://github.com/TestlumFramework/Testlum/releases)

```java
public static void main(String[] args) {
    TESTLUMStarter.main(new String[]{"-c=global-config.xml", "-p=/path-to-resources"});
}
```

### Run in docker
After cloning repository to your local machine:
- navigate to root project folder
- build image from [Dockerfile](Dockerfile) 

```shell
  docker build --no-cache --build-arg APP_VERSION=1.0.2 -t testlum:1.0.2 .
  ./run-docker testlum:1.0.2 global-config.xml ~/Users/user/test-resources 
```

Or You can use our official docker image from [Docker Hub](https://hub.docker.com/r/testlum/testlum)

```shell
  docker pull testlum/testlum
```

# Documentation

Full user documentation for this project is currently available on:
👉 [View Documentation](https://testlum.developerhub.io/version-2/overview)

We are in the process of migrating the documentation to the [GitHub Wiki](../../wiki).  
Once the migration is complete, the current documentation link will be deprecated.


# Social Media

## 🌐 Explore More at [Testlum.com](https://testlum.com)

Want to learn more about our vision, tools, and everything we’re building around modern testing automation?

👇 Check out our official website

<a href="https://testlum.com"><img src="https://testlum.com/img/logo.1acbeb84.svg" alt="Testlum" width="50"></a>

### 🔍 What you’ll find there:
- 📘 **Documentation & Tutorials** – Dive deep into how our testing framework works and how to get started in minutes.
- 🛠️ **Product Features** – Discover what makes our tool unique and how it can simplify your QA workflow.
- 📢 **Blog Posts & Updates** – Stay up to date with the latest improvements, insights, and best practices in automation.
- 📦 **Integrations & Examples** – See how to connect with popular tools and services.
- 🤝 **Get Involved** – Find ways to contribute, provide feedback, or request custom features.

Whether you're a developer, tester, or team lead — **Testlum** is your go-to place to build reliable, scalable, and efficient testing setups.

### 🧭 Start your journey now:
🔗 [https://testlum.com](https://testlum.com)

## Discord 

  ### 💬 Join Our Community on Discord!
  Looking to connect with other community using our testing framework? Got questions, feedback, or just want to see what others are building?

  We’ve got you covered!  
  Come hang out with us in our official **Discord community** – a place built for users like you. Whether you're a beginner or a seasoned automation wizard, there's a spot for you here.

  ### 🚀 What You’ll Find Inside:
- 👩‍💻 **Support & Help:** Get fast answers to your questions from the community and the core devs.
- 🔧 **Tips & Tricks:** Discover new ways to test smarter and faster.
- 🧪 **Feature Discussions:** Help shape the future of the framework.
- 💡 **Showcase Your Projects:** Share what you’re building and get inspired by others.
- 🎉 **Community Events & Updates:** Be the first to know what’s coming next.

### ✅ It’s totally free.
### 💙 Super welcoming and beginner-friendly.

<a target="_blank" href="https://discord.gg/JxfcZPqBNY"><img src="https://dcbadge.limes.pink/api/server/JxfcZPqBNY" alt=""></a>

We can’t wait to meet you there!

## YouTube
## 🎥 Learn With Us on YouTube

Prefer learning by watching?  
We’ve got you covered with hands-on video content on our official **YouTube channel**!

👇 **Subscribe here**

<a href="https://www.youtube.com/channel/UC5F7ZWCL-hzYz6Sr2zv2MQA">
<img src="https://upload.wikimedia.org/wikipedia/commons/b/b8/YouTube_Logo_2017.svg" width="120" alt="YouTube"/>
</a>

### 📺 What you’ll find:
- ✅ **Step-by-step tutorials** on using our testing framework
- 🔍 **Real-world testing scenarios** and walkthroughs
- 💡 **Tips, tricks & shortcuts** to boost your test automation skills
- 🛠️ **Feature highlights** and how-tos
- 🎙️ **Behind-the-scenes insights** into what we're building

Whether you're just starting out or you're a test automation pro, there’s something valuable waiting for you.

Hit that **subscribe** button and never miss an update!

# License
Testlum's source code is made available under the [Apache 2.0 license](LICENSE).


