---
layout: default
---

# Installation and Launch

There are two ways to install and run tracker: using Docker or by installing it
yourself.

## Docker

Download and install the latest version of [Docker](http://docker.com) and
build the container.

    sudo docker build -t tracker .

Then you can launch it using the following:

    sudo docker run -p 9999:9999 -i -t tracker

## Manual installation

The tracker requires Java 8 and Maven 3.1+. Once the dependencies are in place,
you can build tracker using

    mvn install

The WAR can be deployed on any web server. You can also test it using Jetty.

    mvn -Djetty.port=9999 jetty:run

Regardless of whether you use Docker or Maven/Jetty, the tracker will be running
at http://localhost:9999. Use admin:admin to access the test data.
