---
layout: default
---

### [Administrator's Guide](admin-guide.html)

# Permissions

The tracker has a fine-grained permissions system, so that an administrator can assign rights to individual users very precisely.

## Roles

The main way permissions are assigned is through roles. A role assigns a set of permissions to a set of users. Users can be in many different roles, and get all the permissions from all of their roles.

Almost all roles are attached to a study. The permissions within these roles are automatically scoped to that study, and have no effect anywhere else in the system. This means that a study administrator can create and modify roles for that study without having privacy effects on any other study.

## Administrators

There are two kinds of administrator in the tracker:

 * Study administrators look after an individual study, and have delegated permissions that allow them to do more or less anything within that study.
 * System administrators have permissions to create studies, and assignment permissions that extend beyond a single study.

System administrators are set through a special role is `ROLE_ADMIN`, which is a attached to a special "pseudo-study", called `ADMIN`. All roles in this "pseudo-study" are not scoped, and can therefore set permissions that extend across studies.

> **TASK**
>
> To add a new system administrator, navigate to the `ROLE_ADMIN` and add their username to the role. From then on, they'll be able to manage the system.

## Users

Users are defined very simply, by a *principal name*. This is usually either a login name, or if LDAP authentication is being used, it's the `userPrincipalName` field in the LDAP records.

Roles have a set of users and a set of permissions.

## Study permissions

The tracker provides the following permissions for use within a study.

### `view`

The `view` permission means the users in the role can see the study in their display when they log in to the tracker. No detailed views or data can be accessed, but the existence of the study is apparent. And that's all.

### <code>create</code>

The `create` permission lets users create new records. It's normally only relevant if the user has access to a view where they can create records (although in theory a service user might also use this permission to create new records without going through the interface).

### <code>delete</code>

The `delete` permission lets users delete records. It's normally only relevant if the user has access to a view where they can delete records (although in theory a service user might also use this permission to delete records without going through the interface).

### <code>read:<i>view</i></code>

The `read` permission allows read access to a named view, or the view can be a wildcard `*`, which grants read access to all views. The view will be shown under the study, and can be selected from the login page, but there aren't any editing options displayed, and the data can't be changed.

### <code>write:<i>view</i></code>

The `write` permission allows write access to a named view, or the view can be a wildcard `*`, which grants write access to all views. The view will be shown under the study, and can be selected from the login page. Also, a toggle button is displayed which allows data to be edited within the view.

### <code>attribute:<i>read/write</i>:<i>name</i></code>

This permission grants read or write access to a given attribute. For example, to grant all permissions, you can use `attribute:*:*`, to grant only data read access you can use `attribute:read:*`. However, you can use it to control fine-grained permissions, such as: `attribute:read:*` with `attribute:write:att1,att2,...`, which lets users in that role read all attributes, but only write the explicitly named ones. This provides a very powerful and focused control over an individual attribute field.

### <code>download:<i>view</i></code>

The `download` permission allows download access to a named view, or the view can be a wildcard `*`, which grants download access to all views. If the view is accessible (via `read`) an additional download button will be offered, which lets people download the data in an Excel spreadsheet format.

## System permissions

There are also a few system permissions. These only apply for roles attached to the special "pseudo-study", called `ADMIN`.

### `admin`

Users with the admin permission are allowed to create new studies.
