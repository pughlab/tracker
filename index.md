---
layout: home
---

# What is the tracker?

A fairly generic tracking system, designed to be connected to some kind of persistent
storage and some other kind of authentication system, allow a kind of spreadsheet view
with the data persisted, and different users seeing different data.

# Documentation

 * [User Guide](pages/user-guide.html)
 * [Administration Guide](pages/admin-guide.html)
 * [Deployment Guide](pages/deployment-guide.html)

# Intended use

As a replacement for Excel for tracking project data, especially involving time, in an
environment that requires data to be kept confidential. So it's a bit like Google Docs,
but with a local install to deliver security, and a slightly more database like
structure.

# Features

 * Flexible, grid-based views
 * Fine-grained role-based permissions on views
 * Synchronous updates from other users
 * Full auditing and logging of changes


# Installation and Launch

There are two ways to install and run tracker: using Docker or by installing it
yourself.

# Docker

Download and install the latest version of [Docker](http://docker.com) and
build the container.

    sudo docker build -t tracker .

Then you can launch it using the following:

    sudo docker run -p 9999:9999 -i -t tracker

# Manual installation

Tracker requires Java 8 and Maven 3.1+. Once the dependencies are in place,
you can build tracker using

    mvn install

The WAR can be deployed on any web server. You can also test it using Jetty.

    mvn -Djetty.port=9999 jetty:run

Regardless of whether you use Docker or Maven/Jetty, the tracker will be running
at http://localhost:9999. Use admin:admin to access the test data.
