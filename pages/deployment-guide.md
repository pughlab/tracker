---
layout: default
---

# Deployment Guide

## Debian

For fuller details, including a script for a complete Debian deployment, see [the Debian deployment page](debian-deployment.html).

There is a Debian PPA for Jessie, although there's no architecture-dependent code so this also works fine for Wheezy. Add the following line to your `/etc/apt/sources.list` file:

    deb http://dl.bintray.com/morungos/deb jessie main

Then you can use the following commands to install:

    $ sudo apt-get update
    $ apt-get install tracker-webapp

Debian Jessie is preferred because `openjdk-8-jre` is a recommended dependency, and this is not (yet) available in any Ubuntu LTS server releases. However, on Wheezy you can also get by using Oracle Java 8 through the `oracle-java8-installer` package.

For a Debian deployment, the tracker uses the following locations:

* `/usr/share/tracker-webapp` -- the main tracker code directory
* `/var/log/tracker-webapp` -- contains the tracker log files
* `/etc/init.d/tracker-webapp` -- the wrapper script that manages the tracker process
* `/etc/tracker-webapp` -- tracker application configuration files

All can be symlinked to other locations if needed, either before or after
the tracker is installed.

In addition, the tracker sets up a pid file at:

* `/var/run/tracker-webapp`

This can't be symlinked, as Debian standards specify this is likely a
temporary file system and will be erased on boot. However, the only thing stored
here is a pid file, which is not a risk for information access.


## Storage

The tracker uses a regular relational database, typically MySQL with the
InnoDB storage engine for transaction management. By default under Debian
a MySQL database is configured, but if needed, the database configuration
can be changed later to any other database.


## Docker

Thanks to [Morgan Taschuk](https://github.com/morgantaschuk), there is some
work on compatibility with Docker.

Download and install the latest version of [Docker](http://docker.com) and
build the container.

    sudo docker build -t tracker .

Then you can launch it using the following:

    sudo docker run -p 9999:9999 -i -t tracker

This starts the tracker running at http://localhost:9999. Use the authorization
credentials username `admin` and password `admin` to access the test data.


## Configuration

There are several configuration files in `/etc/tracker`

* `tracker.properties` -- most configuration happens here
* `tracker.js` -- allows JavaScript hooks to be added for triggering events
* `tracker.xml` -- the Jetty web application configuration, which you can usually ignore

The settings can be configured in `/etc/tracker/tracker.properties`,
which will typically look something like this:

    ## LDAP settings
    ldap_domain=ads.uhnresearch.ca
    ldap_host=127.0.0.1
    ldap_port=389
    ldap_timeout=60
    ldap_search_template=OU=user,OU=accounts,DC=ads,DC=uhnresearch,DC=ca
    ldap_filter_template=(userPrincipalName={0})

    ## Database settings
    db_user=xxx
    db_password=xxx
    db_host=localhost
    db_port=3306
    db_name=tracker
