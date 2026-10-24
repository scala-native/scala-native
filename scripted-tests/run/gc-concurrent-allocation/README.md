# Concurrent allocation recovery (#5120)

Run from the repository root:

```sh
sbt "test-scripted 3 run/gc-concurrent-allocation"
```

The fixture runs against both Immix and Commix, using ordinary Scala allocation
and management beans (`MemoryMXBean` and `GarbageCollectorMXBean`). It requires no native fixture or runtime test hooks.
It covers:

- Heap stability with 16 allocating workers after warmup, while allocation
  continues. This scenario starts with 256 MiB, allows up to 2 GiB, and requires
  capacity to remain below 1 GiB. Growth greater than 256 KiB during the ten-second
  observation window fails the test. `GC_TIME_RATIO=1` isolates allocation
  recovery from the time-based growth heuristic.
- Retained-string allocations with 16 active workers, and with one active worker
  alongside 32 parked threads. Every active worker completes at least 1,024 chunks.
- Small, medium, and multi-block byte-array allocations interleaved with explicit
  collections. Retained arrays are checked for content corruption. This scenario
  also runs with multithreading disabled.

Each scenario has a process timeout, and worker errors fail the test. These are
behavioral regressions; they do not directly inspect allocator lists or force a
particular collector handoff interleaving.

## Long-running validation

`reproducer/HeapGrowthRepro.scala` preserves the workload from
[#5120](https://github.com/scala-native/scala-native/issues/5120), with a nonzero
exit on uncaught worker failures. Using matching locally published plugin/runtime
versions, set these sbt settings in this fixture directory:

```scala
Compile / sources := Seq(baseDirectory.value / "reproducer/HeapGrowthRepro.scala")
Compile / mainClass := Some("HeapGrowthRepro")
nativeConfig ~= (_.withGC(scala.scalanative.build.GC.immix))
```

Run `Compile / nativeLink`, then run the binary for each of 1, 2, 8, 16, and 32
workers. Repeat the build with `GC.commix`:

```sh
GC_INITIAL_HEAP_SIZE=4M GC_MAXIMUM_HEAP_SIZE=8G ./HeapGrowthRepro 16 600
```

Use the default growth settings for these long runs. The 8 GiB maximum must
remain well above observed capacity. Retain stdout and stderr, require continuing
allocation progress, and reject worker errors. A run that reaches its maximum
does not establish a plateau. Finite soaks establish observed stability rather
than a guarantee for every workload.

The reported `heap_mb` is logical GC capacity, not resident memory. Measure RSS
separately with `/usr/bin/time -l` on macOS or `/usr/bin/time -v` on Linux.
`SCALANATIVE_GC_LOG_LEVEL=debug GC_LOG_FILE=...` records collection and growth
events. Keep debug logging and concurrent stress processes out of separately
timed throughput comparisons.

The issue fix retains the original growth heuristic. Page reclamation, occupancy
tuning, globally shared allocation pools, and generational GC remain separate
follow-up work. The collection epoch identifies completed collections, not
object age.
