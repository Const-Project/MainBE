# Random Feed Session API

This document describes the random feed session API that provides non-duplicated random items without client-side `excludeIds`.

## Goals
- Avoid duplicate items during random infinite scroll.
- Reduce request payload size.
- Keep performance stable for large lists.

## Endpoints

### 1) Start Session
`POST /api/v1/feed/random/session`

Request body:
```
{
  "size": 20
}
```

Response body:
```
{
  "sessionToken": "uuid",
  "items": [ ... ],
  "hasMore": true,
  "remaining": 120
}
```

### 2) Next Page
`POST /api/v1/feed/random/next`

Request body:
```
{
  "sessionToken": "uuid",
  "size": 20
}
```

Response body:
```
{
  "sessionToken": "uuid",
  "items": [ ... ],
  "hasMore": false,
  "remaining": 0
}
```

## Notes
- Session is stored in-memory by default and expires after 30 minutes.
- To enable Redis storage, set:
  - `random.feed.session.store=redis`
  - Configure `spring.data.redis.*` as usual.
- When `hasMore=false`, the session is removed.
- Random pool size = `size * 5`.
- Blocked users are excluded at session creation.

## Migration
- Keep existing `/api/v1/feed/random` (exclude list) during transition.
- Migrate clients to the session API gradually.
