## Revision history


### Version 2.1.0-RC2 - November 5th 2015

 * Incorporates a whole new filtering system. See #94, #101, #102


### Version 2.1.0-RC1 - October 27th 2015

 * Resolved transaction deadlocks in HSQLDB. See #89, #93, #96, #98
 * Fixed permissions for case record creation and writing. See #85, #97
 * Newlines in cells are now supported. See #92
 * No change to a value should not be audited. See #91
 * Upgraded referential integrity to prevent bad imports. See #90
 * Resolved appearance issues when sorting. See #88


### Version 2.0.1-RC7 - October 5th 2015

 * Added support for HTML writing into the data resource. See #73
 * Fixed date button CSS. See #75
 * Added timeout  to automatically logout after 15 minutes. See #80
 * Case states are now displayed. See #82
 * Improved client-side authentication failure message. See #84


### Version 2.0.1-RC6 - September 29th 2015

 * Fixed write permissions access from the front end. See #85, #64


### Version 2.0.1-RC5 - September 28th 2015

 * Connected users now includes the current user. See #79
 * Added error handling for unsupported browsers. See #77
 * Added multiple selection copy/paste. See #70
 * Support for field-specific permissions. See #65
 * Improved support for coloured labels. See #66


### Version 2.0.1-RC4 - September 22nd 2015

 * Fixed a number of websocket issues. See #76
 * Changed Spring config to use a JNDI environment to set property sources
 * Updated handsontable to 0.18.0
 * Significant improvements in testing
 * Added an order field to the cases table. See #69


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
