---
layout: default
---

# Administrator's Guide

## Table of contents

1. [Overview](#overview)
2. [Installation](#installation)
2. [Managing studies](#studies)
3. [Security](#security)
    1. Authentication and authorization
    2. Permissions
4. [Scripting](#scripting)


## <a name="overview"></a> Overview

## <a name="installation"></a> Installation

## <a name="studies"></a> Managing studies

The tracker allows you to set up any number of studies, each of which can be
managed independently. Each study has its own administrators, users, and data,
and users of one study cannot access data from any other study.

### What's in a study?

A study contains:

* A set of cases
* A set of attributes
* A set of views
* A set of roles

### Cases

Cases are the records in a study. If you are tracking fifty participants,
there will be fifty cases. By themselves, cases aren't very interesting.

You can think of cases as corresponding to the rows in an Excel file, or
records in an Access database.

### Attributes

Attributes define the structure of each case in a study. For example, if cases
have a participant identifier, you'll need an attribute for that. Once you have
a participant identifier, each case can have a value for it.

You can think of attributes like the columns in an Excel file, or fields in an
an Access database.

Every attribute has a type, that says what kind of values it can contain.
The tracker allows you to use one of the following types of attribute:

* Strings
* Options (like strings, but restricted to a dropdown list of options)
* Booleans
* Dates
* Numbers

All attributes can also have a special value `N/A`, for "not available", to
explicitly mark when a value is missing, as distinct from an initially empty
or blank value.

### Views

Views make a visual display for a set of cases and attributes. They are the
most significant visual difference from Excel, as different users can work
with different views, while sharing the same underlying data.

For example, if you needed to keep a set of patient initials confidential to
some users, you'd make two different views: one which includes it, and one
which doesn't. You can then use the security system to restrict users to
the view that contains the information they need.

### Roles

Finally, roles give permissions to a group of users. We won't go into
that in too much detail now, as we'll cover it later in the [Security](#security)
section.

## <a name="security"></a> Security

## <a name="scripting"></a> Scripting
