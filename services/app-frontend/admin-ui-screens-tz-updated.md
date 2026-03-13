# Admin UI --- MVP Technical Specification

Source of API: `openapi.yml`\
UI is implemented as a **Telegram Mini App**.

Goal of MVP:

-   View key statistics
-   Process access requests
-   Process chat requests
-   Manage manual user access

------------------------------------------------------------------------

# 1. General approach

The MVP contains **three main screens**:

1.  Statistics
2.  Requests
3.  Users

This covers the main moderation workflow.

Separate entities (VPN, groups, chats) are **not implemented as
standalone screens** in the MVP except as aggregated statistics.

------------------------------------------------------------------------

# 2. Authorization

A separate login screen is **not required**, because the application is
opened from Telegram as a Mini App.

Authorization flow:

1.  Mini App receives `initData`
2.  Frontend calls

```{=html}
<!-- -->
```
    POST /api/auth/telegram

3.  Backend returns

```{=html}
<!-- -->
```
    {
      accessToken,
      user
    }

4.  Token is stored
5.  Frontend may additionally call

```{=html}
<!-- -->
```
    GET /api/users/me

to obtain the latest profile data.

------------------------------------------------------------------------

## Application states

Instead of a login screen the app must support:

### Initialization

Loading state while auth is performed.

### Authorization error

Shown if `/auth/telegram` fails.

### No access

Shown if the user role is `USER`.

------------------------------------------------------------------------

# 3. Roles

System roles:

    OWNER
    ADMIN
    USER

### OWNER

-   sees all screens
-   moderates chat requests
-   manages user access

### ADMIN

-   sees statistics
-   sees access requests
-   does not see chat requests

### USER

-   sees **No access** screen

------------------------------------------------------------------------

# 4. Navigation

Use **bottom navigation**.

Sections:

    Statistics
    Requests
    Users

------------------------------------------------------------------------

# 5. Statistics screen

## Purpose

Quick overview of the system.

No tables or complex charts --- only key numbers.

------------------------------------------------------------------------

## Metrics

### Pending access requests

Source

    GET /api/requests/pending

------------------------------------------------------------------------

### Pending chat requests

Source

    GET /api/chat-requests

Visible only to OWNER.

------------------------------------------------------------------------

### Total users

Source

    GET /api/users

------------------------------------------------------------------------

### Users with active manual access

Condition

    manualAccessType != NONE

------------------------------------------------------------------------

### Total VPN users

Source

    GET /api/vpn/summary

Field

    totalUsers

------------------------------------------------------------------------

### Active VPN users

Source

    GET /api/vpn/summary

Field

    activeUsers

------------------------------------------------------------------------

## UX

-   large numbers
-   minimal text
-   card click can navigate to related screen

------------------------------------------------------------------------

# 6. Requests screen

Handles **pending requests only**.

Two tabs:

    Access
    Chats

------------------------------------------------------------------------

# 6.1 Access tab

Displays user access requests.

Sources:

    GET /api/requests/pending
    GET /api/users

Data mapping:

    AccessRequestDto.userId -> UserDto.id

------------------------------------------------------------------------

## Request card fields

-   avatar
-   first name
-   last name
-   username
-   Telegram ID
-   request date (`createdAt`)
-   current access type (`manualAccessType`)

------------------------------------------------------------------------

## Actions

### Approve for 3 months

    POST /api/requests/{id}/approve
    decisionType: APPROVE_3_MONTH

### Approve forever

    POST /api/requests/{id}/approve
    decisionType: APPROVE_FOREVER

### Reject

    POST /api/requests/{id}/reject

------------------------------------------------------------------------

# 6.2 Chat requests tab

Visible only to OWNER.

Source

    GET /api/chat-requests

------------------------------------------------------------------------

## Chat request card

Fields:

-   chat title (`title`)
-   username
-   chat type (`type`)
-   status
-   joined date (`joinedAt`)
-   initiator (`initiatedBy`, optional)

------------------------------------------------------------------------

## Actions

### Approve

    POST /api/chat-requests/{id}/approve

### Reject

    POST /api/chat-requests/{id}/reject

------------------------------------------------------------------------

# 7. Users screen

Source:

    GET /api/users

------------------------------------------------------------------------

## User card

Fields:

-   avatar
-   first name
-   last name
-   username
-   Telegram ID
-   role
-   manual access status
-   access expiration (`manualAccessUntil`)

------------------------------------------------------------------------

## Access status display

    manualAccessType == NONE
    → No access

    manualAccessType == THREE_MONTHS
    → Access until <manualAccessUntil>

    manualAccessType == FOREVER
    → Permanent access

------------------------------------------------------------------------

# 8. Access management

### Revoke access

    POST /api/users/{id}/manual-access/revoke

### Grant 3‑month access

    POST /api/users/{id}/manual-access/grant
    accessType: THREE_MONTHS

### Grant permanent access

    POST /api/users/{id}/manual-access/grant
    accessType: FOREVER

------------------------------------------------------------------------

# 9. User detail view

Opens when a user card is clicked.

Displays:

-   avatar
-   name
-   username
-   Telegram ID
-   role
-   access status
-   expiration date

------------------------------------------------------------------------

## Additional actions

### Open Telegram profile

If username exists:

    https://t.me/{username}

If username missing:

    tg://user?id={telegramId}

------------------------------------------------------------------------

# 10. User filters

Filtering is **client‑side**.

Available filters:

-   all users
-   users with access
-   users without access
-   by role
-   search by name
-   search by username
-   search by Telegram ID

------------------------------------------------------------------------

# 11. Avatars

Source:

    UserDto.avatarUrl

Used in:

-   requests list
-   users list
-   user detail view

------------------------------------------------------------------------

# 12. Out of MVP scope

Not implemented in MVP:

-   profile editing
-   request history
-   VPN user screen
-   bulk operations
-   server‑side filtering
-   complex tables

------------------------------------------------------------------------

# Final MVP scope

Three screens:

    Statistics
    Requests
    Users

This is sufficient for the first working version of the admin UI.
