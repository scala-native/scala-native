# Immix concurrent allocation recovery (#5120)

Run the real scripted fixture from the repository root:

```sh
sbt "test-scripted 3 run/gc-immix-concurrent-allocation"
```

The fixture checks:

- Collector ownership is held before a failed allocation can grow the heap.
  A hook pauses the collector before stopping the world until two other
  mutators contend for collection ownership.
- Obsolete requests retry without another collection, including the epoch
  check performed after acquiring the collector lock.
- Recovery reuses small/medium runs and large chunks assigned to idle
  allocators. Native fixtures also check list links to block zero, contiguous
  non-power-of-two extents, free-block accounting, and whole-block reserves.
- Small, medium, and large reservations survive other allocators taking all
  remaining shared blocks. The collector-owned fixture simulates this by
  draining and then restoring the free-block pool before checking the retry.
- Retained-string allocation runs with 16 active threads and with 32 idle
  threads. Worker failures are propagated to the test process.
- The invariant checks also run with multithreading disabled.

`SCALANATIVE_GC_TESTING` enables the hooks and the native fixtures in
`gc/immix/AllocationTest.c`; they emit no production code otherwise. The
fixtures temporarily substitute local allocator lists while the world is
stopped and restore the runtime's lists before returning.

## Long-running validation

Use the exact allocation workload from
[#5120](https://github.com/scala-native/scala-native/issues/5120), compiled with
the same Scala version, release-fast mode, and multithreading enabled for both
the original and candidate runtime. Run each thread count separately:

```sh
GC_INITIAL_HEAP_SIZE=4M GC_MAXIMUM_HEAP_SIZE=8G \
  SCALANATIVE_GC_RELEASE_DELAY_MS=0 ./HeapGrowthRepro 16 600
```

Repeat with 1, 2, 8, and 32 workers. The 8 GiB maximum must remain well above
the observed capacity. Retain stdout and stderr, and reject worker exceptions
even when the original reproducer exits successfully. Compare capacity after
warmup and require continuing allocation progress without a sustained heap
growth trend. A run that reaches its maximum does not establish a plateau.

The reported `heap_mb` is logical GC capacity, not resident memory. Measure RSS
separately, for example with `/usr/bin/time -l` on macOS or `/usr/bin/time -v`
on Linux. `SCALANATIVE_GC_LOG_LEVEL=debug GC_LOG_FILE=...` records collection
and growth events for diagnosis. Keep debug logging and concurrent stress
processes out of the separately timed throughput comparisons.

The issue fix retains the original growth heuristic. Page reclamation,
occupancy tuning, globally shared allocation pools, and generational GC are
separate follow-up work. The collection epoch used here is not object age.
