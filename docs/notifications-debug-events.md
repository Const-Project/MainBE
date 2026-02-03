# Notification Debug Events

This document lists standardized debug event keys emitted by `NotificationService`.

## Configuration

- `notification.debug.enabled`: `true` to enable debug logs
- `notification.debug.sample-rate`: `0.0` to `1.0` sampling rate

## Event Keys

- `SSE_SUBSCRIBED`
  Emitted when a user subscribes to SSE.
  Fields: `userId`, `emitterId`, `hasLastEventId`
- `NOTIFICATION_SKIPPED`
  Emitted when a notification is skipped.
  Fields: `userId`, `type`, `reason` (`notificationDisabled` or `marketingConsentDisabled`)
- `NOTIFICATION_CREATED`
  Emitted after a notification is stored and emitters are resolved.
  Fields: `id`, `userId`, `type`, `emitters`
- `SSE_SENT`
  Emitted after a single SSE event is sent.
  Fields: `emitterId`, `eventId`
- `READ_DENIED`
  Emitted when a user attempts to read another user’s notification.
  Fields: `notificationId`, `userId`, `receiverId`
- `READ_OK`
  Emitted after a notification is marked read.
  Fields: `notificationId`, `userId`
- `PUSH_SKIPPED`
  Emitted when push send is skipped.
  Fields: `userId`, `type` (optional), `reason` (`notificationDisabled` or `missingToken`)
- `PUSH_SENT`
  Emitted after a push is sent successfully.
  Fields: `userId`, `type`, `token` (masked)
- `PUSH_FAILED`
  Emitted after a push send fails.
  Fields: `userId`, `type`, `token` (masked)
- `SCHEDULED_SKIPPED`
  Emitted when a scheduled notification is skipped.
  Fields: `type`, `userId`, `reason` (`notificationDisabled`)
