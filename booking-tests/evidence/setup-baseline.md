# Starter setup baseline

Issue: #1. Owner: @willtheorangeguy.

## Recorded run

The unchanged starter at commit `deca3b9322e5bbe299a9683e4434dc16528b74a0`
was tested on 2026-11-04, starting at 17:55:25 UTC. The tracked working tree
was clean before the run; the branch was `chore/1-starter-baseline`.

| Check | Observed result |
| --- | --- |
| Platform | Windows 11, amd64 |
| `java -version` | Eclipse Temurin OpenJDK 17.0.21+9, 64-bit |
| `javac -version` | javac 17.0.21 |
| `.\mvnw.cmd --version` | Apache Maven 3.9.9, running on Java 17.0.21 |
| Maven wrapper | 3.3.2, supplied script and properties unchanged |
| `.\mvnw.cmd clean test` | BUILD SUCCESS, exit code 0 |
| Starter suite | BookingServiceStarterTest: 3 tests, 0 failures, 0 errors, 0 skipped |

The supplied JUnit 5.11.4, JaCoCo 0.8.12 and PIT 1.17.4 settings remain
unchanged. No implementation or test source was edited or inspected for this
setup task. Read the handout, `START_HERE.md`, `README.md` and `API.md` for the
planning inputs. This run establishes the test baseline; starter coverage is
the separate task #11 after the initial-plan commit in #2.

## Reproduce in PowerShell

From `booking-tests`, select an installed full JDK 17 in the current shell.
This machine's default Java is 25; a portable JDK 17 was installed outside the
repository at the following location. Teammates should substitute their own
JDK 17 location.

```powershell
$env:JAVA_HOME = "$env:LOCALAPPDATA/seng300-tools/jdk-17.0.21+9"
$env:PATH = "$env:JAVA_HOME/bin;$env:PATH"

# The supplied Windows wrapper cannot launch Maven when its cache path
# contains spaces. Use the existing cache's Windows short path for this shell.
$taskMavenCache = Join-Path $env:USERPROFILE '.m2'
New-Item -ItemType Directory -Force -Path $taskMavenCache | Out-Null
$env:MAVEN_USER_HOME = (New-Object -ComObject Scripting.FileSystemObject).GetFolder($taskMavenCache).ShortPath

java -version
javac -version
.\mvnw.cmd --version
.\mvnw.cmd clean test
```

The short-path workaround was verified on this machine. If Windows short names
are unavailable, set `MAVEN_USER_HOME` to a writable directory without spaces.
These environment changes apply only to that shell; system Java settings and
the supplied wrapper are unchanged. The initial wrapper-launch failure is
preserved separately and did not execute tests.

JDK source: the Eclipse Adoptium API, selecting a Windows x64 HotSpot JDK 17.
The downloaded `OpenJDK17U-jdk_x64_windows_hotspot_17.0.21_9.zip` was checked
against the package's published SHA-256:
`98518ecf554465057ee5509462631deeee374bc368c12a411d225cd29381f84b`.

## Preserved evidence

Paths below are relative to `booking-tests`. The console and Surefire reports
were copied before any subsequent clean build:

- `evidence/runs/starter-tests/environment.txt`: UTC timestamp, baseline commit,
  clean-tree check, Java/compiler/Maven output and test exit code.
- `evidence/runs/starter-tests/console.txt`: complete successful Maven output.
- `evidence/runs/starter-tests/surefire-reports/`: original XML and text reports.
- `evidence/runs/starter-tests/wrapper-path-failure.txt`: initial launch failure.

These raw files are local and ignored by Git under the supplied rules. Include
the complete `evidence/runs/starter-tests` folder separately in the final
submission archive. This tracked summary and `templates/run-log.csv` record
the result for review; teammates can regenerate raw results with the commands
above. Coverage and mutation counters are blank because neither was measured.

## Branch workflow and next handoff

1. Merge this setup PR into `main`, which remains the reference. #3-#5 can then
   supply the planning inputs to #2. Commit that initial plan before anyone
   reads method bodies; retain its commit ID for the report.
2. Complete #11 on the starter-only test suite and copy its JaCoCo reports before
   introducing additional tests. Implement tests on feature branches from the
   reference, then integrate passing tests without editing production code or
   the pinned metric settings. Include Option B before the first PIT run.
3. After the rule-based tests are committed, create an `investigate-bug` branch
   or separate worktree from the reference. Follow `faulty-version/README.md`,
   commit the supplied faulty patch before repairing it, and retain the same
   distinguishing test for reference/faulty/repaired comparisons. Keep the
   production repair on that branch and export its patch for submission.
4. If the required boundary mutant is absent from PIT, create a separate
   `manual-mutation/120-minute-boundary` branch or worktree from the committed
   reference. Make only that mutation, save its result, then return to the
   unchanged reference. Report manual results outside PIT totals.
5. Run reference coverage and PIT from the reference checkout, never from the
   repair/manual-mutation checkout. Archive each report before `clean`; use the
   same final test-suite commit for final PIT and coverage. Bring final test-only
   changes into the repair branch for its complete regression run.

The contribution template is initialized with handles only. The group number
and student IDs remain blank for the course submission; unfinished work is not
claimed. Setup execution and this documentation were prepared with Codex
assistance; no behavioral tests were added in this task.
