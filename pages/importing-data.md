---
layout: default
---

## Importing Data

The tracker has a Perl-based import tool that can import an Excel file, given a
block of YAML based configuration. If you have a large amount of existing data,
this will be the easiest way to get started.

The import script is included in the repository at:

```
src/main/etc/load.pl
```

To run it, you'll need a command like:

```shell
perl load.pl --config mystudy.yml
```

The magic is in `mystudy.yml`, which will be a YAML file for the study. The easiest
way to write one of these is to start with an empty skeleton version, run it, see
all the columns that don't map, and then add them into the YAML, progressively,
until you get no errors. Then the import is a good one. For this reason, it is
best to run the imports on a local MySQL so as not to mangle the system in
production, and then copy the YAML file and the spreadsheet onto the server for
final use.

### A skeleton YAML file

Looks a bit like this:

```yaml
---
## The study name goes here
study: 'INSPIRE'
overwrite: true

## Database settings, they'll look like this virtally all the time, although
## you'll typically need to modify for production.
database: 'dbi:mysql:database=tracker'
username: 'tracker'
password: 'tracker'
commands:
  - "set session sql_mode = 'ansi,strict_all_tables'"

## The input files. There can be more than one (in theory) but that's for another
## day. Mainly, put the path here.
files:
  - path: 'My Great Spreadsheet.xlsx'
    file_key: 'input.data'

## For this file, (see the file_key under files), use the specified Excel sheet.
## The other stuff is for more advanced use.
'input.data':
  sheet: "Sheet1"
  merge_on: []
  fieldmap:

## An array of fields. Leave it blank to get started.
fields:

## A map of fields to types. Leave it blank to get started.
types:
```

Now, if you run the import command, you'll get a bunch of output errors and
warnings. It'll typically look a bit like this.

```
[11:58:22] tpugh10:/Users/stuartw/git/tracker-webapp/src/main/etc$ perl load.pl --config sample.yml
INFO load.pl 62> Reading config file: sample.yml
INFO load.pl 139> Opening spreadsheet: /Users/stuartw/Tracker Data/MYELSTONE.Project.Tracking.FinalTracker.xlsx
INFO load.pl 162> Reading data from sheet: Sheet1
INFO load.pl 167> Reading rows: 0 to 177, columns: 0 to 66
WARN load.pl 242> Can't map header InitialSpecimenID ignoring column
WARN load.pl 242> Can't map header MatchingBMID ignoring column
WARN load.pl 242> Can't map header MatchingctDNAID ignoring column
WARN load.pl 242> Can't map header MYELSTONEStudyID ignoring column
WARN load.pl 242> Can't map header Drawnumber ignoring column
WARN load.pl 242> Can't map header ClinicalIdentifier ignoring column
WARN load.pl 242> Can't map header PatientInitials ignoring column
WARN load.pl 242> Can't map header Specimentype ignoring column
WARN load.pl 242> Can't map header ClinicalComments ignoring column
WARN load.pl 242> Can't map header PatientMRN ignoring column
...
```

This will give you a good list of the spreadsheet column names. I then use Atom to copy and paste
this into a text file, and select the header names, building a list that looks like:

```
InitialSpecimenID
MatchingBMID
MatchingctDNAID
MYELSTONEStudyID
Drawnumber
ClinicalIdentifier
PatientInitials
Specimentype
ClinicalComments
PatientMRN
...
```

Now you can edit these into the YAML file. Now, you only need to worry about the
`fields` and `types` blocks, as follows:

```yaml
...

fields:
  - 'InitialSpecimenID'
  - 'MatchingBMID'
  - 'MatchingctDNAID'
  - 'MYELSTONEStudyID'
  - 'Drawnumber'
  - 'ClinicalIdentifier'
  - 'PatientInitials'
  - 'Specimentype'
  - 'ClinicalComments'
  - 'PatientMRN'
  ...

types:
  'InitialSpecimenID': 'String'
  'MatchingBMID': 'String'
  'MatchingctDNAID': 'String'
  'MYELSTONEStudyID': 'String'
  'Drawnumber': 'String'
  'ClinicalIdentifier': 'String'
  'PatientInitials': 'String'
  'Specimentype': 'String'
  'ClinicalComments': 'String'
  'PatientMRN': 'String'
  ...

```

Obviously, the fields won't usually be strings. So you can then code them to say
which is which. You can set a field type as being one of:

 * 'String'
 * 'Date'
 * 'Boolean'
 * 'Number'

And then re-run the import. If all is good, you should get no warnings about being
unable to map headers, but you might if you have miscoded a field or, more likely,
Excel has messed with your dates.

When all loads clean, copy your config file onto the server, along with your
Excel file. Then log onto the server, amend the database connection to be correct
for your server, and re-run the Perl command...

```shell
perl load.pl --config mystudy.yml
```

Then the study should be loaded onto the main tracker. You'll probably need to
add an administration role for it, and maybe some views, but from here on, everything
can be done from the web interface. 
