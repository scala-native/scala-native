# GC safepoint trap and deferred trampoline (Immix / Commix)

This note describes how **trap-based yield points** stop mutator threads for stop-the-world (STW) collection on **POSIX** when `SCALANATIVE_GC_USE_YIELDPOINT_TRAPS` is enabled (Immix or Commix, multithreaded builds). It is aimed at contributors debugging safepoints, signals, or deadlocks.

The design is described in terms of a well-known **polling-page safepoint** idea familiar from JVM implementations (e.g. the **OpenJDK HotSpot** VM). The trap-based yieldpoints are based on https://dl.acm.org/doi/10.1145/2887746.2754187 

## What the mutator does at a yield point

The compiler lowers `scalanative_GC_yield` (when traps are on) to a **volatile load** through the thread-local pointer `scalanative_GC_yieldpoint_trap`. That pointer refers to a small mapping: readable while idle, **`PROT_NONE` while the GC wants a safepoint**, so the load faults.

See `tools/.../AbstractCodeGen.scala` (GC yield lowering) and `gc/shared/YieldPointTrap.c` (`mprotect` arm/disarm).

## Why not call `Synchronizer_yield()` inside the signal handler?

POSIX limits which library calls are **async-signal-safe** inside a signal handler. `Synchronizer_yield()` uses atomics, `sigwait`, and other runtime that is not in that set. Calling it directly from `sigaction` works on many platforms in practice but is fragile.

In that family of VMs, the usual approach is: in the handler, **only** classify the fault, then **resume in normal thread context** at a small stub (or generated entry) that performs the real safepoint work—rather than running the full safepoint logic inside the async-signal handler.

Scala Native follows the same *idea* on POSIX (with its own `ucontext` + assembly trampoline):

1. **`SafepointTrapHandler`** (in `gc/immix/Synchronizer.c` and `gc/commix/Synchronizer.c`) decides whether `SIGSEGV` / `SIGBUS` (macOS) is our trap fault (page match, etc.).
2. **`scalanative_gc_safepoint_prepare_redirect(uap, mutator)`** (`gc/shared/SafepointPollTrampoline.c`) stores the faulting **instruction pointer** in `MutatorThread.safepointResumePc` and sets the **`ucontext` PC** to **`scalanative_gc_safepoint_poll_trampoline`**.
3. When the kernel returns from the signal, the thread runs the **assembly trampoline**, which saves registers, calls **`Synchronizer_yield()`** via `scalanative_gc_safepoint_poll_run_yield`, restores registers, and **branches to `safepointResumePc`**. By then the trap is disarmed, so the retried volatile load succeeds.

Windows uses the **vectored exception** path and still runs `Synchronizer_yield()` from the filter (no `ucontext`); the trampoline header stubs `prepare_redirect` to a no-op there.

## Source layout

| Piece | Role |
| --- | --- |
| `gc/shared/SafepointPollTrampoline.h` | Public C API between synchronizer and trampoline |
| `gc/shared/SafepointPollTrampoline.c` | `ucontext` PC get/set, `prepare_redirect`, small C helpers for the asm |
| `gc/shared/SafepointPollTrampoline-x86_64.S` | Trampoline: XMM0–15 + GPRs, then yield, resume |
| `gc/shared/SafepointPollTrampoline-aarch64.S` | Trampoline: Q0–Q7 + X0–X30, then yield, resume |
| `gc/immix/Synchronizer.c`, `gc/commix/Synchronizer.c` | Install trap handler; call `prepare_redirect` or fall back to in-handler yield |

## Platform support

- **Redirect + trampoline:** Linux and Apple **x86_64** and **aarch64** (when the preprocessor in `SafepointPollTrampoline.c` defines `SN_UC_GET_PC` / `SN_UC_SET_PC`).
- **Fallback:** If `prepare_redirect` returns `false` (unsupported OS/arch or missing `ucontext` layout), the handler calls **`Synchronizer_yield()`** directly, as before.
- **Darwin:** `ucontext.h` requires **`_XOPEN_SOURCE`** before include; `SafepointPollTrampoline.c` defines `_XOPEN_SOURCE` to `700` in the trap build.

## Limitations and future work

- **x86_64:** The trampoline saves **XMM0–15** (128-bit lanes). It does **not** save **YMM/ZMM** upper lanes, **MXCSR**, or x87 state. Poll sites that rely on wider AVX across the fault are theoretically at risk; extending the frame would follow the same pattern.
- **aarch64:** **Q0–Q7** are saved (typical caller-saved SIMD). **Callee-saved** `d8`–`d15` / `q8`–`q15` follow the AAPCS64 callee rules for the C code we call. Full FP control registers are not saved.
- **Other Unixes** (e.g. *BSD) may need extra `ucontext` field mappings to enable redirect; until then they use the in-handler fallback.

## Related user-facing behavior

STW timeouts and warnings (`SCALANATIVE_GC_SYNC_TIMEOUT_MS`, etc.) are documented in [Runtime](../user/runtime.md) under Immix/Commix. Trap faults are one way threads reach a safepoint; threads blocked in native code without `@blocking` may still never fault and can trigger those diagnostics.
