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

## Changes
