# Initial test plan

Write this before inspecting method bodies.

## Input groups and boundaries

## Cancellation decision table

Use a fresh fixture for each row. The caller is either the booking's owner or a
different user. Times are whole minutes before the booking starts, measured from
the supplied fixed clock. Expected results follow rules R3-R5 in the handout.

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

"Unchanged" means the entire repository matches its starting snapshot, including
the target's fields and an unrelated seeded booking. On success, only the target's
state changes; its ID, room, owner, start and end stay the same. Check that the one
recorded notification request contains that cancelled booking. This checks the
request to the notifier, not email delivery.

R3 gives ownership priority: a non-owner gets DENIED even if the booking is inactive
or starts in 119 minutes. For an owner, an inactive state takes priority over the
time check. R4 allows exactly 120 minutes, so testing only 119 and 121 would miss
an error at the cutoff.

The repeated non-owner and inactive rows show that changing the time must not
change those results. They can use parameterized tests. All 18 combinations can
be seeded directly, including a COMPLETED booking with a future start; the starter
allows seeded states and does not require a completion operation. Missing IDs,
null values and fractional-minute times are outside the input domain in API.md.

## Two action sequences

## Changes after code inspection
