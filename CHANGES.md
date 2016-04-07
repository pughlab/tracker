## Revision history


### Version 2.2.5 - 7th April 2016

 * Fix critical issue in schema changes. See #182


### Version 2.2.4 - 6th April 2016

 * Add link rendering to cells. See #180
 * Fixed an issue where changing attribute types could break editing. See #179
 * Improved error reporting from the front and the back end. See #178
 * Made role names unique per study rather than globally. See #177
 * Reduced form validation issues when creating new attributes. See #174


### Version 2.2.3 - 17th February 2016

 * Allow form dates to be empty. See #172
 * Forms can switch to a read only display on save. See #171


### Version 2.2.2 - 17th February 2016

 * Handle formatted dates in forms. See #168


### Version 2.2.1 - 16th February 2016

 * Fixed a nasty bug where deleting could remove all cases. See #165
 * Added page views. See #164
 * Fixed NPEs in the state changing plugin. See #163
 * Make sure empty cases contain the identifier. See #162
 

### Version 2.2.0 - 3rd February 2016

 * Fixed state label rules on string values. See #140
 * Fixed the load script to handle numbers. See #154
 * Fixed an issue where logout didn't update connected users. See #161, #158
 * Fix an issue with case deletion in MySQL. See #159
 * Don't let people type in the top filter row. See #160
 

### Version 2.1.6 - 21st January 2016

 * Fixed attribute creation from the UI. See #152
 * Fixed a few small bugs in study creation from the UI
 

### Version 2.1.5 - 14th January 2016

 * Allow a date format to be set for a study. See #141, #150
 * Allow dates to be entered textually. See #149
 * Fix internal issues when creating new cases. See #148
 * Fixes for invalid value display. See #144, #146, #147.
 * Fix inaccessible dropdowns at the bottom of the grid. See #145
 * Added a new formula system for calculating values. See #143
 * Fixed issues with physical/logical rows breaking data. See #139, #140
 * Fixed search handling. See #138.


### Version 2.1.4 - December 1st 2015

 * Make the study order display more sensible bug. See #136
 * Make sure initially empty studies allow new cases. See #135
 * Add a study creation script. See #134
 * Corrected issues blocking management of study labels and states. See #132
 * Allow access to study notes for all studies. See #131


### Version 2.1.3 - December 1st 2015

 * Modestly incompatible change: table names are uppercase. See #128
 * Adding missing Debian dependency for package install. See #127


### Version 2.1.2 - November 30th 2015

 * Fixed issues with Excel downloads when missing state labels. See #124
 * Restructured and fixed the admin editing logic. See #115, #123
 * Allow filters to match values with spaces in. See #122
 * Corrected Debian package database configuration. See #118
 * Added a Markdown-based per-study description. See #116


### Version 2.1.1 - November 19th 2015

 * Fixed an issue with logging configuration. See #117


### Version 2.1.0 - November 17th 2015

 * Added a Debian package. See #113
 * Allow rows to be deleted. See #107
 * Fixed an issue saving views. See #115
 * Fixed an issue with excessive redaction for audits. See #114
 * Fixed other small bugs. See #106
 * Updated to a newer Restlets. See #105


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
