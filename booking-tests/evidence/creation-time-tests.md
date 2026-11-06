# Creation-time tests (#6)

Initial plan: `4e540099d7646c6bc5f41da0dfa3cd81970ed5df`.
Tested source: `0107c413944b2f590475f03818d82b8773c5140f`.

The seven methods in `CreationTimeTest.java` implement its seven existing rows in
`templates/test-map.csv`. Expected results still come from R1 and R5. Inspection
of `BookingService.create` did not require a change to the planned cases.

Each test gets a fresh `TestFixtures` instance and an unrelated booking. The
rejection helper checks INVALID_TIME, a null ID, the full repository snapshot
and zero notifications. The success helper checks CREATED, a fresh non-null ID,
all saved fields, exactly one additional booking and zero notifications.

## Runs

Run from `booking-tests` using Java 17 and the pinned Maven wrapper:

| Checkpoint | Command | Result | Saved evidence |
| --- | --- | --- | --- |
| Initial plan, before new tests | `.\mvnw.cmd clean verify` | 3 passed; BookingService lines 22/31, branches 7/20 | `runs/starter-coverage/` |
| Creation tests and starter tests | `.\mvnw.cmd clean test` | 10 passed; no failures, errors or skips | `runs/creation-time-tests/` |

Both source trees were clean before their runs. Starter coverage was copied out
of `target` before the second clean build. The saved directories contain console
output and Surefire reports; the starter directory also contains the complete
JaCoCo report. `templates/run-log.csv` records both source commits.

On this machine, the default Java is 25. The runs used the existing Temurin
17.0.21+9 installation at `%LOCALAPPDATA%\seng300-tools\jdk-17.0.21+9`, with its
`bin` directory first on PATH. The wrapper needed the existing short Maven path
`C:\Users\WILLIA~1\M2E4AB~1` as the session's `MAVEN_USER_HOME`, as in the setup
evidence. The first wrapper attempt failed before tests started; its output is
retained in `runs/starter-coverage-wrapper-failure.txt`.

Raw run evidence is ignored by Git and must be included in the final group ZIP.
This starter archive supplies the baseline needed for #11; final coverage and
the two gap investigations still belong to #12 and #13.

## Boundary-test facts for #25

`startAtNowIsInvalid` uses start 0 and end 60. R1 requires a start after now, so
the expected result is INVALID_TIME, a null ID, unchanged storage and no request.
The snapshot assertion would catch a rejected request that still saves a booking.
The mutant discussion must wait for the group's actual three-mutant findings in
#19. These in-memory tests do not establish real database transaction behaviour.
