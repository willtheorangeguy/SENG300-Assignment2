# Test Plan

Expected results come from rules R1-R5 in the assignment handout and `API.md`.
This is the initial plan, to be committed before inspecting method bodies.

## Input Groups and Boundaries

Times are whole-minute offsets from the supplied fixed clock. For example, 0 is
now, -1 is one minute ago and 120 is two hours from now.

| Input | Groups and examples | Expected result |
| --- | --- | --- |
| Start time (R1) | Before now (-1), exactly now (0), after now (1); end at 60 | -1 and 0 return INVALID_TIME; 1 is valid if there is no conflict |
| End time (R1) | Before start (59), equal to start (60), after start (61); start at 60 | 59 and 60 return INVALID_TIME; 61 is valid if there is no conflict |
| Validation order (R1/R2) | Start at 0 and end at 90, with an ACTIVE booking from 60 to 120 in the same room | INVALID_TIME takes precedence over CONFLICT |
| Interval position (R2) | Against an existing 60-120 booking: partial overlap (30-90 and 90-150), inside (75-105), surrounding (30-150), equal (60-120) | CONFLICT for ACTIVE bookings in the same room |
| Interval boundaries (R2) | Touching (1-60 and 120-180), separated (1-30 and 150-180) | CREATED when no other booking conflicts; touching is allowed in both directions |
| Room and stored state (R2) | Same or different room; ACTIVE, CANCELLED or COMPLETED booking at the requested times | Only an ACTIVE booking in the same room can conflict |
| Caller and stored state (R3) | Owner or non-owner; ACTIVE, CANCELLED or COMPLETED | Non-owner always gets DENIED; owner of an inactive booking gets INACTIVE |
| Cancellation time (R4) | Owner of an ACTIVE booking with 119, 120 or 121 minutes remaining | TOO_LATE at 119; CANCELLED at 120 and 121 |

The values on either side of each boundary check where the result changes.
Exactly 120 minutes and touching intervals check whether equality is handled
correctly. Use valid future creation times for R2 so R1 does not hide a conflict.

For R5, check the response, complete saved data and notification requests. Rejected
requests leave the repository unchanged and add no notification. A successful
create saves one ACTIVE booking with a fresh non-null ID and sends no notification.
A successful cancellation changes only the target's state and requests exactly
one notification containing that cancelled booking. Other bookings stay unchanged.

Use valid IDs and fresh in-memory fixtures. Seed inactive states directly.
Nulls, missing IDs, fractional-minute times, concurrent requests, login and real
email delivery are outside this plan. Core tests assume dependencies succeed;
the failed-save experiment belongs to Part 6.

### Test map conventions

[`test-map.csv`](test-map.csv) gives the proposed test names and concrete cases for
these groups, the decision table from #3 and both sequences from #5. Its cases were
drafted in #4; test authors can update the proposed names during implementation.

Each row starts with a fresh fixture and an empty notifier. A booking is written
as `(id; room; owner; start; end; state)`, with times measured as above. Every row
also seeds `u = (b2; R2; carol; 180; 240; ACTIVE)` and checks that it stays unchanged.
An "empty" setup means no booking besides u. Compare repository contents by ID,
without depending on list order. New IDs must differ from all seeded IDs.

Bracketed values identify inputs to a proposed parameterized test. Sequence rows
keep the same fixture and clock for both calls, with cumulative notification
counts. All other rows describe one call.

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

Initial-plan commit: `4e540099d7646c6bc5f41da0dfa3cd81970ed5df`.

For #6, inspected `BookingService.create` after that commit and saved starter-only
coverage before adding tests. The seven `CreationTimeTest` rows in `test-map.csv`
still match R1 and R5, so no expected results or input groups changed. The methods
keep the planned names. Shared assertion helpers check every stored field and the
notification list; rejection checks compare snapshots without relying on order.
