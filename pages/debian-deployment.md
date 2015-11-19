---
layout: default
---

## To install on Debian Wheezy/Jessie

### 1. Java

#### Option A -- for Debian Wheezy and others

First, we need Java 8. Since OpenJDK isn't supported on Wheezy, we need to install the Oracle version.

    $ sudo sh -c 'echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu precise main" >> /etc/apt/sources.list'
    $ sudo sh -c 'echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu precise main" >> /etc/apt/sources.list'
    $ sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys EEA14886
    $ sudo apt-get update
    $ sudo apt-get install oracle-java8-installer

#### Option B -- for Debian Jessie and newer

Install OpenJDK 8:

    $ sudo apt-get install openjdk-8-jre

### 2. MySQL

Install a MySQL server as follows

    $ sudo apt-get install mysql-server

### 3. Create a new database

    $ mysql -uroot -p
    mysql> CREATE USER 'tracker'@'localhost' IDENTIFIED BY 'some_pass';
    mysql> CREATE DATABASE tracker;
    mysql> GRANT ALL PRIVILEGES ON tracker.* TO 'tracker'@'localhost';
    mysql> quit

### 4. [Optional] secure the system

By default, data is stored in a few different locations:

* /var/lib/mysql -- the MySQL persisted data
* /var/log/tracker-webapp -- the tracker log files
* /var/log/nginx -- the nginx HTTP log files
* /usr/share/tracker-webapp -- the main tracker directory for code
* /etc/tracker-webapp -- the tracker configuration directory

Only the first three of these really need securing, as they are the main
places where confidential data may be stored.

The tracker will not remove or modify these locations if they already exist, so you can secure the system in advance of installing with ecryptfs.

For more on installing and setting up ecryptfs, see: [https://www.howtoforge.com/how-to-encrypt-directories-partitions-with-ecryptfs-on-debian-squeeze](https://www.howtoforge.com/how-to-encrypt-directories-partitions-with-ecryptfs-on-debian-squeeze).

First, install ecryptfs and set up encryption for everything under `/srv`

    $ sudo apt-get install ecryptfs-utils
    $ sudo mount -t ecryptfs /srv /srv

Next, add links to the relevant places, and move files as needed.

    $ sudo mkdir -p /srv/tracker-webapp/usr/share
    $ sudo ln -sf /srv/tracker-webapp/usr/share -T /usr/share/tracker-webapp
    $ sudo mkdir -p /srv/tracker-webapp/var/log
    $ sudo ln -sf /srv/tracker-webapp/var/log -T /var/log/tracker-webapp
    $ sudo mkdir -p /srv/tracker-webapp/etc
    $ sudo ln -sf /srv/tracker-webapp/etc -T /etc/tracker-webapp

You can do similar things, for, e.g., the MySQL data, to put them in places where they'll be safe too. If you're going to do this, obviously, please close down the MySQL server first.

For example, I migrated MySQL as follows:

    $ sudo /etc/init.d/mysql stop
    $ sudo cp -pfr /var/lib/mysql /tmp/mysql
    $ sudo mkdir -p /srv/mysql/var/lib
    $ sudo chown -R mysql:mysql /srv/mysql
    $ sudo rm -rf /var/lib/mysql
    $ sudo ln -sf /srv/mysql/var/lib -T /var/lib/mysql
    $ sudo bash -c "cp -pfr /tmp/mysql/* /var/lib/mysql/"
    $ sudo /etc/init.d/mysql start

### 5. Install the tracker webapp

First you'll need to add the repository to the sources list.

    $ sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 0B70E3C0
    $ echo "deb http://dl.bintray.com/morungos/deb jessie main" | sudo tee -a /etc/apt/sources.list
    $ sudo apt-get update
    $ sudo apt-get install tracker-webapp

### 6. Install and configure nginx

Why? We strongly recommend using `nginx` (or similar) as a reverse proxy. It
means that the tracker application can run as a non-privileged user elsewhere,
and it makes it easier to ensure any security updates (e.g., to SSL) can be
applied from a better resourced team.

We also recommend enabling and using SSL, if you are able to install a
certificate appropriately, as without that, data is being sent to and from the
tracker without encryption.

    $ sudo apt-get install nginx

Create a new site configuration file:

    $ sudo nano /etc/nginx/sites-available/tracker-webapp

Containing the following text (to enable the tracker on port 80 as a regular web server):

    server {
      listen 80;
      listen [::]:80 ipv6only=on;

      server_name \_;

      access_log   /var/log/tracker-webapp/tracker-webapp.access.log  combined;

      sendfile off;

      location / {
        proxy_pass              http://127.0.0.1:3000;
        proxy_set_header        X-Real-IP $remote_addr;
        proxy_set_header        X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header        X-Forwarded-Proto http;
        proxy_set_header        Host $http_host;
      }
    }

Just a brief point on this: it's different to some older nginx configurations which had separate listen statements for IPV4 and IPV6. That no longer works. See: [https://chrisjean.com/fix-nginx-emerg-bind-to-80-failed-98-address-already-in-use/](https://chrisjean.com/fix-nginx-emerg-bind-to-80-failed-98-address-already-in-use/)

Now, change the site links:

    $ sudo rm /etc/nginx/sites-enabled/default
    $ sudo ln -sf /etc/nginx/sites-available/tracker-webapp -T /etc/nginx/sites-enabled/tracker-webapp

And finally, reload `nginx`:

    $ sudo invoke-rc.d nginx reload

### 7. Updates

You don't need to do much to update the application:

    $ sudo apt-get update
    $ sudo apt-get install tracker-webapp

No other configuration changes should usually be needed during this process. `nginx` can stay running.
