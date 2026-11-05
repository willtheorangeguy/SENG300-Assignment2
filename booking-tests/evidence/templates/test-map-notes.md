# Test map notes

The expected results in `test-map.csv` come from R1-R5 in the assignment handout
and `API.md`. The test names are proposed names for the five classes in issues
#6-#10; their authors can adjust the names when implementing them.

All times are minute offsets from the supplied fixed clock: 0 means now and -1
means one minute ago. Each row starts with a fresh repository and an empty
recording notifier. Each booking below is written as
`(id; room; owner; start; end; state)`. A new booking's ID must be non-null and
different from every seeded ID.

Every row also seeds `u = (b2; R2; carol; 180; 240; ACTIVE)`, an unrelated booking
that must remain unchanged. An "empty" setup means no booking besides u. Compare
the full repository by booking ID, without depending on list order. On success,
check all fields of the saved booking and any recorded notification payload.

Bracketed state/time values identify separate inputs to a proposed parameterized
test. Sequence rows use the same fixture and fixed clock for both calls. Their
notification counts are cumulative; all other counts are for one call.
