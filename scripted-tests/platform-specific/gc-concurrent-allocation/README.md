# Concurrent allocation recovery (#5120)

Run from the repository root:

```sh
sbt "test-scripted 3 run/gc-concurrent-allocation"
```

The fixture runs against both Immix and Commix, using ordinary Scala allocation
and management beans (`MemoryMXBean` and `GarbageCollectorMXBean`). It requires
no native fixture or runtime test hooks. It covers:

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

Linking happens in a separate `prepareAllocationBinary` step. Each
`runAllocationRegression` or `runAllocationChecks` step executes the prepared
binary with a shared 50-second budget across all its scenarios. Forced process
cleanup has a further five-second limit, leaving headroom below one minute.
Each scenario and the complete task report their elapsed time.

Worker errors fail the test. These are behavioral regressions; they do not
directly inspect allocator lists or force a particular collector handoff
interleaving.

## Allocation overhead benchmark

`benchmark/AllocationBenchmark.scala` is a standalone benchmark outside the
scripted source path. To build it, use:

```scala
Compile / sources := Seq(baseDirectory.value / "benchmark/AllocationBenchmark.scala")
Compile / mainClass := Some("AllocationBenchmark")
```

Use identical app sources, SDK versions, and compiler flags with the original
and candidate GC. Run alternating baseline/candidate pairs sequentially, without
overlapping builds or stress processes. A single worker can use a fixed heap:

```sh
GC_INITIAL_HEAP_SIZE=256M GC_MAXIMUM_HEAP_SIZE=256M \
  ./AllocationBenchmark objects 1 8
```

For 16 workers, give both variants the same growth headroom, for example an
initial 256 MiB and maximum 8 GiB. The original GC can report a spurious OOM
under a fixed concurrent heap; those failures are not performance results.
Report capacity alongside throughput because growth changes collection frequency.

Repeat with scenarios `small` (64-byte array payload), `medium`
(544 bytes), `large` (8,192 bytes), and `blocks` (98,304 bytes). Every scenario
retains a bounded set of objects and reports a checksum, allocation count,
elapsed time, collection count, accumulated GC time, and logical heap capacity.
The management bean's GC time is collection duration, not isolated pause time.
Measure peak RSS separately. Require successful runs; worker failures invalidate
performance results.

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
`SCALANATIVE_GC_LOG_LEVEL=info GC_LOG_FILE=...` records heap growth events.
Commix debug logging also traces allocation blocks and can substantially affect
measurements. Keep diagnostic logging and concurrent stress processes out of
separately timed throughput comparisons.

The issue fix retains the original growth heuristic. Page reclamation, occupancy
tuning, globally shared allocation pools, and generational GC remain separate
follow-up work. The collection epoch identifies completed collections, not
object age.
