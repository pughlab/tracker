---
layout: default
---

# Deployment Guide


## Debian

For a Debian deployment, the tracker uses the following locations:

* `/usr/share/tracker` -- the main tracker code directory
* `/var/log/tracker` -- contains the tracker log files
* `/etc/init.d/tracker` -- the wrapper script that manages the tracker process
* `/etc/tracker` -- tracker application configuration files

All can be symlinked to other locations if needed, either before or after
the tracker is installed.


## Storage

The tracker uses a regular relational database, typically MySQL with the
InnoDB storage engine for transaction management. By default under Debian
a MySQL database is configured, but if needed, the database configuration
can be changed later to any other database.


## Configuration

There are several configuration files in `/etc/tracker`

* `tracker.properties` -- most configuration happens here
* `tracker.js` -- allows JavaScript hooks to be added for triggering events
* `tracker.xml` -- the Jetty web application configuration, which you can usually ignore

The settings can be configured in `/etc/tracker/tracker.properties`,
which will typically look something like this:

```
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
```
