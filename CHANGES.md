## Revision history


### Version 2.0.1-RC3 - September 8th 2015

 * Restored create study functionality. See #63
 * Password prompts can now be configured externally. See #61
 * Removed broken JMockit, which hurt continuous integration. See #60


### Version 2.0.1-RC2 - September 2nd 2015

 * Added a view permission to studies. See #59
 * Improved import scripts to handle XLSX files properly
 

### Version 2.0.1-RC1 - August 31st 2015

 * Internal change: attributes are now linked by id rather than name
 * Added a new role management interface and per-study roles
 * Support for numeric fields
 * Note: DB schema has changed


### Version 2.0.0-RC5 - August 17th 2015

 * Added support for OpenID Connect authentication
 * Revised login system to adapt to forms or OIDC redirection
 * Fixed several issues involving role management


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
