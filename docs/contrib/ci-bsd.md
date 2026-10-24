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
