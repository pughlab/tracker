---
layout: default
---

# Tools required

Working on the development of the tracker requires the following tools:

* A Java 8 JDK
* Apache Maven 3.1+

Java 8 is required because the tracker uses Nashorn to enable scripting. Most
recent operating systems support Java 8.

The packaging system also uses Node.js and a variety of tools to bundle and
process the front-end code, but Maven is used to download and install an
appropriate version, so you shouldn't generally need to worry about it.

## Useful commands

To run all the tests, package, and prepare the Maven reports:

    mvn test package site

To build and run a local tracker server (choose a port you like):

    mvn -Djetty.port=9999 jetty:run


## Debian packaging

**Work in progress** To build a Debian package:

    mvn deploy -P debian-package


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
