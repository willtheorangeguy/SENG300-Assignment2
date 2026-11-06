# Failed-save experiment (#22)

Tested source: `ceb6606954c688bbddfcf57da4b3a3fc730fb71c`.
Initial plan: `4e540099d7646c6bc5f41da0dfa3cd81970ed5df`.

`FailureRepositoryTest.java` contains both tests and a small `FailingRepository`
that implements `BookingRepository`. It delegates storage to the supplied
in-memory repository. Once `failOnSave` is called, `save` throws the supplied
exception before delegating, so it cannot change the stored booking.

## Cases and results

Both tests seed an ACTIVE booking owned by alice at 120-180 minutes and an
unrelated booking. They use the supplied fixed time. Failure stays disabled
during seeding.

| Test | Action and assertions |
| --- | --- |
| `failedSaveLeavesCancellationUnchanged` | Enable failure, then cancel as alice. Assert the same RepositoryException reaches the caller, the full stored snapshot is unchanged and the notifier is empty. |
| `successfulSaveCancelsAndNotifies` | Leave failure disabled and cancel as alice. Assert CANCELLED, every saved field, the unchanged unrelated booking, two stored bookings and exactly one request containing the cancelled booking. |

Run from `booking-tests`: `.\mvnw.cmd clean test`.
Result: 12 passed (3 starter, 7 creation, 2 experiment), with no failures, errors
or skips. The working tree was clean before the run. Console output and Surefire
reports are saved under `runs/failed-save-tests/`; `templates/run-log.csv` records
the command and source commit. This used the same Java 17 installation and session
Maven path described in `creation-time-tests.md`.

Raw evidence is ignored by Git and must be included in the final group ZIP.

## Notes for the learning record (#23)

The first implementation used a repository wrapper with failure disabled by
default, then enabled it after setup. It included a normal-save control from the
start. Both experiment tests passed on the first run, so no later code changes
were needed. Run notes were added afterwards.

Sources used were the Part 6 Option B instructions, `API.md`, the save contract in
`BookingRepository.java`, and the [JUnit 5.11.4 exception assertion documentation](https://docs.junit.org/5.11.4/user-guide/#writing-tests-exceptions-expected).
The JUnit documentation explains that `assertThrows` returns the thrown exception;
the test uses that object to check that the original exception reaches the caller.

An always-successful repository cannot exercise the failed-save path. Here the
failure is injected only when an otherwise valid cancellation reaches `save`.
This checks service behaviour under the stated repository contract; the wrapper
cannot prove that a real database preserves data when a write fails.

Merge this experiment before the first PIT run in #14. The control and failure
tests belong to the core suite used for both PIT comparisons and final coverage.
Ravjotk can use these facts for #23's learning record; this note does not claim
their review or personal experience.
