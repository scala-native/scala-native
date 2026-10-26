# BSD CI: FreeBSD and NetBSD

Branch: `ci/freebsd-netbsd`, based on `ci/termux-runtime`.
Rebased on `6514027f6` on 2026-10-24.

The workflow is [run-tests-bsd.yml](../../.github/workflows/run-tests-bsd.yml).
Both jobs run x86_64 VMs on Ubuntu through vmactions, with 8 GiB RAM
and a 120 minute job timeout.

| Target | Compiler | JDK |
|--------|----------|-----|
| FreeBSD 14.4 | `llvm18` | native `openjdk21`, `/usr/local/openjdk21` |
| NetBSD 10.1 | pkgsrc `clang` | pkgsrc `openjdk21`, `/usr/pkg/java/openjdk21` |

FreeBSD installs `boehm-gc-threaded` and links `libgc-threaded`; its plain
`boehm-gc` package is built without threads and omits `gc_pthread_redirects.h`.
NetBSD installs `boehm-gc`. FreeBSD uses base zlib; NetBSD installs pkgsrc
zlib and supplies `/usr/pkg/include` and `/usr/pkg/lib` through
`SCALANATIVE_INCLUDE_DIRS` and `SCALANATIVE_LIB_DIRS`.

## Bootstrap and coverage

[scripts/ci/run-bsd-tests.sh](../../scripts/ci/run-bsd-tests.sh) downloads
the portable sbt launcher JAR for the version in `project/build.properties`.
Linux coursier executables and Linux JDK distributions cannot be used as
native FreeBSD tools. The build currently requires sbt 2.0.9.

Each job runs `test-gc 3`, which exercises sandbox, testInterface and
junitTestOutputsNative with none, immix, commix and Boehm collectors.
A fresh sbt process then runs `testsJVM3/test`, `tests3/test`,
`junitTestOutputsNative3/test` and `scalaPartestJunitTests3/test`.
The explicit list excludes testsExt; the `test-runtime 3` alias includes it.

Both BSDs appear in the x86_64 continuation guards in `delimcc.h` and
`LinktimeInfo.isContinuationsSupported`. This is source-level support;
virtual threads still require validation on the actual VMs.

## Progress

| Task | Status |
|------|--------|
| Initial workflow and handoff | Done, original commit `d0e90fcfd` |
| Rebase onto latest Termux work | Done, base `6514027f6` |
| Correct native JDK and portable sbt bootstrap | Done |
| Workflow lint and shell syntax validation | Passed |
| First VM run | Failed in package setup; no compilation reached |
| Package setup fixes | FreeBSD 14.4 matches repository ABI; NetBSD uses base pkg_add |
| FreeBSD struct tm layout | Fixed C assertions and Scala field accessors |
| NetBSD cached pcre2 and certificate setup | Fixed dependency update; reuse existing trust store |
| FreeBSD Boehm bootstrap | Install threaded package and select libgc-threaded |
| Runtime failure triage | Pending |

The initial run failed because FreeBSD 14.2 rejected packages targeting 14.4
and NetBSD did not have pkgin installed. No BSD runtime results are available yet. Do not add skip guards until a
failure has been reproduced and its cause established.

## Running CI

PRs run both jobs. The temporary branch push trigger was removed once
PR #5123 opened to avoid duplicate VM jobs. Scheduled
runs at 03:00 UTC are restricted to the canonical repository.

Once the workflow exists on the default branch, manual runs are available:

```sh
gh workflow run run-tests-bsd.yml --repo scala-native/scala-native --ref ci/freebsd-netbsd
```

After the first green run, consider adding testsExt and Scala 2.13 coverage.
Confirm supported VM images and package availability before adding architectures.

## Local debugging and confirmed failures

The [Docker/QEMU development rig](../../scripts/ci/bsd-dev/README.md) uses
persistent copies of the CI images with SSH, source sync and focused C probes.

The FreeBSD Boehm test-interface failure in run `40114710628` is a diagnostic
signal-handler conflict: Boehm uses SIGUSR1/SIGUSR2 for stop-the-world, and
SignalConfig replaced them with fatal handlers. Preserve those handlers for
Boehm, along with SIGXCPU/SIGXFSZ used on other platforms. This also applies
to single-threaded Scala builds that can have native worker threads.
The container signal probe reproduces the crash when handlers are overwritten
and succeeds when they are preserved. The existing test-interface suite also
passes locally with Boehm and SCALANATIVE_TEST_DEBUG_SIGNALS=1 (14 tests).

The NetBSD job in that run did not reach compilation: package setup stalled
on an unresponsive fallback mirror. CI now uses the verified CDN 2026Q3
repository and SCP, avoiding the pre-prepare rsync package lookup. A local
NetBSD 10.1 guest passes all four loopback socket ABI probe cases; that CI run
provides no evidence of a NetBSD socket failure.

Run `40119146393` confirms that FreeBSD passes the complete four-GC smoke
matrix after the signal fix. Its subsequent JVM suite reports seven failures:
VM hostname resolution, Git checkout ownership, interface index/name assumptions,
and a FreeBSD link-local IPv6 loopback expectation. CI now configures the hostname
and Git safe directory; the network tests check name/index consistency and
accept FreeBSD's link-local address form.

NetBSD now gets through package setup and reaches native compilation. The
`_SC_AVPHYS_PAGES` guard in MemoryInfo.c applied only to the macOS alternative
because of operator precedence. Grouping all Unix alternatives before that guard
fixes compilation. The corrected file compiles and runs in the local NetBSD
guest, reporting physical memory and page size and returning zero for unavailable
free-page information.

Run `40132106769` gets NetBSD through C compilation, then fails native linking
on `log`, `log10` and `ceil`: LLVM-generated calls require libm, which the
NetBSD platform link list omitted. Add `m` alongside `pthread`. A local NetBSD
math probe reproduces the undefined symbols without `-lm` and runs with it.

FreeBSD's JVM suite is down to one failure: `NetworkInterfaceTestOnJDK9`
requires `%lo0` on `::1`, but OpenJDK 21 can omit that scope. Compare the
address separately and, when a scope is present, check that it names the
loopback interface or its numeric index. The local NetBSD OpenJDK 21 probe
also returns unscoped `::1` and `fe80:2::1`; the embedded link-local interface
index applies to JVM as well as native execution on NetBSD.

Validation for these fixes: toolsJVM3 compiles, the focused JVM network test
passes on the host, and the same compiled JUnit test passes inside the NetBSD
10.1 guest using OpenJDK 21 (`-Xint` for QEMU TCG execution).

Run `40134496807` reaches native unit-test compilation on FreeBSD, where
kevent.c lacked stdint.h and referenced NOTE_EXITSTATUS, absent on FreeBSD.
Include the integer types and return zero for that optional flag; NOTE_EXIT
still subscribes to process exit events.

NetBSD reaches the JUnit output smoke checks. Its intentionally disabled
unwinder returns empty traces, exposing a reporter bug that indexes frame zero
when there are no printable frames. Bound the last printable index and add
native regression tests for empty traces and filtered frames with an empty cause.

Compiler stderr previously always used the error log level. Compiler process
logging now recognizes warning and error diagnostics and keeps subsequent
source context at the matching level; generic subprocess logging is unchanged.
Preserve BSD-provided unistd constants before applying fallbacks, and avoid a
compile-time warning for the expected unsupported POSIX_SPAWN_SETSID fallback.
Use C header-selected entry points for clock_gettime, nanosleep and socket so
NetBSD links the current ABI instead of compatibility symbols.

Validation: the five changed C sources compile with -Werror in both local BSD
guests; a NetBSD time/socket ABI probe links the header-selected shims. Three
compiler-logging tests, two native reporter regressions, and all forty existing
native JUnit output checks pass locally. Full BSD runtime CI remains pending.

Run `40137835795` passes NetBSD's none/immix smoke checks, then exits with
EINVAL when Commix creates its first named semaphore. NetBSD, like FreeBSD,
requires a leading slash. A local NetBSD sem_open probe fails without the slash
and passes creation, unlink, post, wait and close with it.

FreeBSD completes 912 native suites, but neither stack-overflow suite finishes;
output stops for roughly 34 minutes before a SIGKILL (status 137). A focused C
probe using the runtime's actual stack guards reproduces an infinite SIGSEGV
loop: the approximate stack bounds place a guard in the uncommitted stack
range. FreeBSD then faults on the page above that guard; unprotecting the guard
does not resolve the fault. Query BSD pthread stack bounds and start with the
active stack page, allowing tryGrowStack to commit intervening pages before
installing the guard. The probe now recovers from repeated main/worker overflows
on both FreeBSD and NetBSD. CI runs both Scala overflow suites first with
per-test progress before the full native suite.

The same FreeBSD run reports two ordinary assertion failures. Monetary locale
data varies in currency symbols and spacing across FreeBSD locale databases;
accept the observed exact formats. Empty-file block allocation varies by
filesystem; check nonnegative block counts, positive I/O block sizes, and
stat/fstat consistency rather than assuming an empty file occupies one block.

Validation: the unchanged FreeBSD stack discovery makes the committed C
regression probe time out (SIGALRM, status 142); with the fix, the probe builds
with -Werror and passes on both BSD guests. The modified Scala unit tests compile
successfully, and shell syntax/format checks pass. Full runtime CI is still
required to confirm completion of the Scala overflow suites and remaining tests.

Run `40184739712` passes the complete FreeBSD job. NetBSD passes the none,
Immix and Commix smoke checks, then cannot start the Boehm sandbox because
the runtime loader cannot find libgc.so.1. SCALANATIVE_LIB_DIRS supplies the
link-time search path, but the pkgsrc library directory must also be available
at runtime. Export LD_LIBRARY_PATH with /usr/pkg/lib in the NetBSD job and
local rig instructions, preserving any existing entries. A local NetBSD Boehm
probe reproduces the missing-library error without this path and successfully
allocates and collects with it. NetBSD's full runtime suite is still pending.

Run `40211862870` passes FreeBSD again. NetBSD passes all four GC smoke
configurations with multithreading explicitly enabled in the sandbox. Normal
main-return shutdown succeeds without IllegalMonitorStateException, verifying
the scenario reported in #3839.

NetBSD then reports four JVM assertion failures: three network-interface tests
require a scope suffix omitted by OpenJDK 21, and the ping redirection test
expects a numeric destination while NetBSD ping reverse-resolves localhost.
Share the existing optional-scope validation across the network tests, checking
any supplied scope against the interface name/index. Request numeric ping output
with -n on Unix, preserving Windows' separate echo-count meaning of -n.

Validation: all thirty tests in InterfaceAddressTest, NetworkInterfaceTest,
NetworkInterfaceTestOnJDK9 and ProcessTestOnJDK9 pass on the host JVM and in the
local NetBSD 10.1 guest using OpenJDK 21 (-Xint under QEMU TCG). The NetBSD native
runtime suite remains pending.

Run `40215073467` passes FreeBSD and the complete NetBSD JVM suite. NetBSD
native linking fails on posix_spawn_file_actions_addchdir_np: NetBSD 10 provides
the unsuffixed posix_spawn_file_actions_addchdir instead, even though its POSIX
version macro predates Issue 8. Detect that API using the NetBSD version and
select the unsuffixed call. Older NetBSD targets must not emit a reference to
the unavailable _np symbol; retain the fork fallback for custom directories.

Validation: the previous launcher reproduces the undefined symbol in the local
NetBSD guest. The corrected shim compiles with -Werror and the spawn/chdir probe
confirms the child's working directory and unchanged parent directory. A
simulated pre-10 NetBSD feature guard links and reports the fork fallback.
FreeBSD links with its existing capability result, and the host spawn/chdir
probe passes. Full NetBSD native runtime CI remains pending.

Run `40233847278` reaches the end of the NetBSD native suite with three failures.
For regular files, NetBSD uses NODEV (-1) for st_rdev; assert that sentinel
instead of zero, retaining the stat/fstat consistency check. LanginfoTest sees
ASCII after setting a UTF-8 locale because the legacy setlocale symbol uses
different category numbering. Forward setlocale through the C header-selected
entry point; a local NetBSD probe reproduces CODESET=646 with the compatibility
symbol and obtains UTF-8, the expected date format and thousands separator
through the new shim.

NetBSD's posix_spawn resolves relative executable paths before applying its
chdir file action. The extended spawn probe reproduces ENOENT for ./pwd in a
child-only directory and succeeds with the absolute executable path. Resolve
relative command candidates against the requested child directory on NetBSD,
preserving argv[0] and leaving the parent working directory unchanged. The
existing ProcessTest.dirOverride covers this runtime path.

Validation: the runtime and native test sources compile. All 28 focused native
checks in LanginfoTest, StatTest and ProcessTest pass on the host (one existing
ignored test). NetBSD probes confirm the modern locale ABI, the NODEV value
through the actual C stat shim, and relative-spawn failure versus absolute-path
success. The locale C shim compiles with -Werror on both BSD guests and the
host; the Windows branch passes a host syntax check. Full NetBSD CI is pending.
