# Test Plan

## Input Groups and Boundaries

## Cancellation decision table

| Caller | Stored state | Time remaining | Expected result | Saved data | New notification requests |
| --- | --- | --- | --- | --- | --- |
| Owner | ACTIVE | 119 (<120) | TOO_LATE | Unchanged | 0 |
| Owner | ACTIVE | 120 (=120) | CANCELLED | Target state becomes CANCELLED | 1 |
| Owner | ACTIVE | 121 (>120) | CANCELLED | Target state becomes CANCELLED | 1 |
| Owner | CANCELLED | 119 (<120) | INACTIVE | Unchanged | 0 |
| Owner | CANCELLED | 120 (=120) | INACTIVE | Unchanged | 0 |
| Owner | CANCELLED | 121 (>120) | INACTIVE | Unchanged | 0 |
| Owner | COMPLETED | 119 (<120) | INACTIVE | Unchanged | 0 |
| Owner | COMPLETED | 120 (=120) | INACTIVE | Unchanged | 0 |
| Owner | COMPLETED | 121 (>120) | INACTIVE | Unchanged | 0 |
| Non-owner | ACTIVE | 119 (<120) | DENIED | Unchanged | 0 |
| Non-owner | ACTIVE | 120 (=120) | DENIED | Unchanged | 0 |
| Non-owner | ACTIVE | 121 (>120) | DENIED | Unchanged | 0 |
| Non-owner | CANCELLED | 119 (<120) | DENIED | Unchanged | 0 |
| Non-owner | CANCELLED | 120 (=120) | DENIED | Unchanged | 0 |
| Non-owner | CANCELLED | 121 (>120) | DENIED | Unchanged | 0 |
| Non-owner | COMPLETED | 119 (<120) | DENIED | Unchanged | 0 |
| Non-owner | COMPLETED | 120 (=120) | DENIED | Unchanged | 0 |
| Non-owner | COMPLETED | 121 (>120) | DENIED | Unchanged | 0 |

## Two Action Sequences

Seed these bookings directly before each sequence:

| ID | Room | Owner | Start | End | State |
| --- | --- | --- | --- | --- | --- |
| b1 | R1 | alice | 120 | 180 | ACTIVE |
| b2 | R2 | carol | 180 | 240 | ACTIVE |

### Successful cancellation, then cancellation again

Proposed test: `CancellationSequenceTest.repeatedCancellationAddsNoNotification`.

| Step | Action | Expected response | Saved b1 | Total notification requests |
| --- | --- | --- | --- | --- |
| 1 | `cancel("b1", "alice")` | CANCELLED | State becomes CANCELLED | 1, containing the cancelled b1 |
| 2 | `cancel("b1", "alice")` | INACTIVE | Still CANCELLED; matches the snapshot after step 1 | Still 1; the request list matches step 1 |

After step 1, check that b1 keeps its ID, room, owner, start and end, and that b2
matches its starting value. Check the same fields after step 2 and compare the
whole repository with the snapshot after step 1. The notification payload must
match the saved cancelled b1.

Exactly 120 minutes is allowed by R4. After the first cancellation, R3 makes the
second call INACTIVE. R5 requires the second call to leave both stored data and
the existing notification list unchanged. A response-only test could miss a
duplicate notification on the second call.

### Non-owner attempt, then owner cancellation

Proposed test: `CancellationSequenceTest.deniedCancellationDoesNotBlockOwner`.

| Step | Action | Expected response | Saved b1 | Total notification requests |
| --- | --- | --- | --- | --- |
| 1 | `cancel("b1", "bob")` | DENIED | ACTIVE; all fields match the starting snapshot | 0 |
| 2 | `cancel("b1", "alice")` | CANCELLED | State becomes CANCELLED | 1, containing the cancelled b1 |

After step 1, compare the whole repository with its starting snapshot and check
that the notifier is empty. After step 2, only b1's state changes. Its other fields
and all of b2 stay unchanged, and the notification payload matches the saved
cancelled b1.

R3 rejects bob because he does not own b1. That rejection must leave b1 available
for alice's cancellation at the same 120-minute boundary. Checking only DENIED
could miss an accidental state change that blocks alice's later request.

## Changes
