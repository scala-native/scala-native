# BSD CI: FreeBSD & NetBSD — Handoff Document

> **Branch**: `ci/freebsd-netbsd`  
> **Based on**: `ci/termux-runtime` (will be rebased onto `main` before merge)  
> **Created**: 2026-10-24  
> **Workflow file**: [`.github/workflows/run-tests-bsd.yml`](../../.github/workflows/run-tests-bsd.yml)

---

## Motivation

Scala Native has BSD-specific code paths in `posixlib`, `javalib`, `nativelib`, and the test
suite, but no CI job exercised them. Open issues filed against FreeBSD and NetBSD behaviour
were unvalidatable without a CI target.

This document tracks the status of each concern so that subsequent contributors can pick up
where the last one left off.

---

## Architecture

Both targets use **`vmactions`** (QEMU VMs on `ubuntu-latest`):

| VM Action | OS | LLVM | JDK | GC libraries |
|-----------|-----|------|-----|--------------|
| `vmactions/freebsd-vm@v1` | FreeBSD 14.2 x86_64 | 18 (pkg `llvm18`) | Temurin 21 (coursier) | Boehm GC (`boehm-gc` pkg) |
| `vmactions/netbsd-vm@v1` | NetBSD 10.1 x86_64 | latest stable (pkgsrc `clang`) | OpenJDK 21 (pkgsrc) | Boehm GC (`boehm-gc` pkgsrc) |

### What is tested (single configuration, Scala 3 only)

| Step | sbt command | Purpose |
|------|-------------|---------|
| GC smoke test | `test-gc 3` | Compile + run sandbox under none/immix/commix/boehm GC |
| Full runtime suite | `test-runtime 3` | `testsJVM`, `tests`, `junitTestOutputsNative`, `scalaPartestJunitTests` |

`testsExt` is intentionally excluded from the first iteration to keep job time under 90 min.

### Virtual threads / Continuations

`delimcc.h` already includes `__FreeBSD__` and `__NetBSD__` in the x86_64 jmpbuf guard:

```c
#elif defined(__x86_64__) &&                                                   \
    (defined(__linux__) || defined(__APPLE__) || defined(__FreeBSD__) ||       \
     defined(__OpenBSD__) || defined(__NetBSD__))
```

`LinktimeInfo.isContinuationsSupported` also covers both BSDs. Virtual threads should therefore
work on x86_64. The CI job will surface any runtime failures. Track them in the table below.

---

## Known Issues / Blockers

> Update this table as CI runs produce results. Link to the relevant GitHub issue for each row.

| OS | Component | Symptom | Status | Issue |
|----|-----------|---------|--------|-------|
| FreeBSD | *(first run pending)* | | ⬜ Unknown | |
| NetBSD | *(first run pending)* | | ⬜ Unknown | |

### Potential areas of concern (from existing guarded code)

These are locations in the source already guarded behind `isFreeBSD`/`isNetBSD` — they
either have known gaps or needed BSD-specific workarounds. Verify each after the first CI run:

| File | Guard | Notes |
|------|-------|-------|
| `posixlib/sys/times.scala` | `!isFreeBSD && !isNetBSD` | `tms_*` fields have different struct layout on BSD |
| `posixlib/time.scala` | `!isFreeBSD` | `tm_gmtoff`/`tm_zone` layout differs on FreeBSD |
| `posixlib/netdb.scala` | `isFreeBSD \|\| isNetBSD \|\| ...` | `addrinfo` layout variant |
| `posixlib/sys/socket.scala` | `isMac \|\| isFreeBSD \|\| isOpenBSD \|\| isNetBSD` | `sa_len` field |
| `unit-tests/posixlib/GlobTest` | `!isNetBSD` | Some glob flags absent on NetBSD |
| `unit-tests/posixlib/LocaleTest` | `!isFreeBSD && !isNetBSD` | Locale behaviour differs |
| `unit-tests/posixlib/sys/TimesTest` | `(isFreeBSD \|\| isNetBSD) && !is32BitPlatform` | BSD-specific tms path |
| `javalib/NetworkInterface` | BSD-specific ioctls | May need porting |

---

## Work Progress

| Task | Status | PR / Commit |
|------|--------|-------------|
| Create `ci/freebsd-netbsd` branch | ✅ Done | branch `ci/freebsd-netbsd` |
| Write `.github/workflows/run-tests-bsd.yml` | ✅ Done | see workflow file |
| Write `docs/contrib/ci-bsd.md` (this file) | ✅ Done | |
| First manual `workflow_dispatch` run | ⬜ Pending | |
| Triage FreeBSD failures | ⬜ Pending | |
| Triage NetBSD failures | ⬜ Pending | |
| Fix blocking issues or add `assume` / skip guards | ⬜ Pending | |
| Rebase onto `main` and open PR | ⬜ Pending | |

---

## How to Trigger Manually

From the GitHub Actions UI, select **"Run tests BSD (FreeBSD & NetBSD)"** → **Run workflow**
on branch `ci/freebsd-netbsd`.

Or via the GitHub CLI:

```bash
gh workflow run run-tests-bsd.yml --ref ci/freebsd-netbsd
```

---

## Extending This CI Job

### Adding testsExt

Once the basic `test-runtime 3` is green, add extended tests by appending to the `run:` block:

```bash
sbt -J-Xmx5G "testsExt3/test"
```

### Adding Scala 2.13

The job intentionally tests Scala 3 only. To add 2.13 (for partest coverage like multiarch):
change the sbt commands to `test-gc 2.13` / `test-runtime 2.13` and run both in sequence
(or split into a matrix).

### Adding aarch64

`vmactions/freebsd-vm` can be pointed at an aarch64 VM image by changing `runs-on` to
`ubuntu-latest` with an arm runner and adjusting the `release`. NetBSD aarch64 support in
vmactions is experimental as of 2026-10. Check vmactions releases for current status.

---

## Package Name Reference

### FreeBSD (`pkg`)

| What | Package name |
|------|-------------|
| LLVM 18 | `llvm18` |
| Boehm GC | `boehm-gc` |
| zlib dev | `zlib` |
| OpenJDK 21 | `openjdk21` (alternative to coursier) |

### NetBSD (`pkgin` / pkgsrc)

| What | Package name |
|------|-------------|
| Clang/LLVM | `clang` (tracks pkgsrc current, ≥ LLVM 17 on NetBSD 10) |
| Boehm GC | `boehm-gc` |
| zlib | `zlib` |
| OpenJDK 21 | `openjdk21` |
| SBT | Not in pkgsrc — install from upstream tgz |
