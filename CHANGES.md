## Revision history


### Version 2.0.0-RC4 - August 10th 2015

 * Enabled LDAP authentication against multiple Active Directory servers
 * Fixed enter key in the login page
 * Improved permissions checking references to user names


### Version 2.0.0-RC3 - July 22nd 2015

 * Refactored the authentication configuration to prepare for LDAP
 * Added testing of authentication systems


### Version 2.0.0-RC2 - July 8th 2015

 * Replaced pop-up login with simple login page
 * Fixed a bad generic settingsKey - 839b840


### Version 2.0.0-RC1 - July 2nd 2015

 * Initial Java version of the tracker
 * Modified session authentication to send an error status with a message on failure to authenticate - 4cd8d8d
 * Various bug fixes and tests for the permissions system
 * Add usernames to log files - b9a7c77 
 

Previous versions were written using Node.js / Express stack, and were not released publicly.
