# Garden Access Policy

This document defines the access policy for viewing other users' gardens.

## Rules
1. A user can always view their own gardens.
2. For another user's gardens:
   - The viewer must be following the target user.
   - If either user has blocked the other, access is denied.

## Endpoints

### 1) Garden Detail
`GET /api/v1/gardens/{gardenId}`

- If the garden belongs to the viewer, access is allowed.
- Otherwise, access requires an active follow relationship (viewer → owner).
- Blocked users are denied.

### 2) Garden List by User
`GET /api/v1/users/{userId}/gardens`

- Returns only unlocked gardens.
- If `userId` is the viewer, all unlocked gardens are returned.
- Otherwise, access requires an active follow relationship (viewer → target).
- Blocked users are denied.

## Notes
- The follow check is one-way (viewer must follow owner).
- Block status is mutual: any block between users denies access.
