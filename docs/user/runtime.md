# Runtime / Garbage Collector Settings

Scala Native comes with some ability to change the runtime characteristics.

## Garbage Collector (GC) Settings

The garbage collector is selected with `nativeConfig.withGC` and tuned with
a `GarbageCollectorConfig`, the one place that defines every setting and its
default:

``` scala
import scala.concurrent.duration._
import scala.scalanative.build._

nativeConfig ~= {
  _.withGC(GC.commix)
    .withGarbageCollectorConfig(
      _.withInitialHeapSize(128.mb)
        .withMaxHeapSize(1.gb)
        .withThreads(4)
        .withSyncTimeout(30.seconds)
        .withLogLevel(GarbageCollectorConfig.LogLevel.Info)
    )
}
```

The sizes are `MemorySize` values. In sbt builds the `bytes`, `kb`, `mb` and
`gb` extensions of integers are imported with the plugin, other build tools
can `import scala.scalanative.build.MemorySize.syntax._` or use
`MemorySize.mb(128)` and `MemorySize.parse("128m")`. Units are binary, 1 kb
is 1024 bytes.

### How a value is chosen

Every setting has an environment variable. The value used by the program
is the first one defined in:

1. the environment variable when the **program starts**,
2. `GarbageCollectorConfig` in the build,
3. the same environment variable when the program is **built**,
4. a legacy `-D` compile option (see below),
5. the default.

The value found when building (2 to 5) is compiled into the program, so the
build defines the defaults and the environment of the program can still
override them. An environment variable with a value that cannot be used is
ignored with a warning, both when building and when the program starts.

### Validation

The build fails when a setting defined in `GarbageCollectorConfig`:

-   is not supported by the selected garbage collector,
-   has an invalid value, e.g. a ratio outside of 0 to 1 or less than 1
    thread,
-   contradicts another setting, e.g. `initialHeapSize` greater than
    `maxHeapSize`, or `statsFile` without `statsEnabled`.

Values taken from the environment of the build or from legacy compile
options never fail the build: these not supported by the selected garbage
collector are skipped, invalid ones are ignored with a warning.

### Settings

Sizes in environment variables are given in bytes, kilobytes (`k` or `K`),
megabytes (`m` or `M`) or gigabytes (`g` or `G`), e.g. `1048576`, `1024k`,
`1M` or `1G`.

#### `initialHeapSize`

Initial size of the heap.

Environment variable: `GC_INITIAL_HEAP_SIZE`

Supported by: Immix, Commix, Boehm, None without multithreading

Default: chosen by the garbage collector

#### `maxHeapSize`

Maximal size of the heap. Immix and Commix need at least 1 MB.

Environment variable: `GC_MAXIMUM_HEAP_SIZE`

Supported by: Immix, Commix, Boehm, None without multithreading

Default: the size of the physical memory

#### `threadHeapBlockSize`

Size of the memory blocks each thread allocates from. A thread maps a new
block when the current one is full, so the size has to be greater than the
largest object or array the program allocates: a larger allocation never
fits and ends with an `OutOfMemoryError`. Larger blocks reserve more
virtual memory per thread. Sizes above the physical memory are reduced to
it when the program starts.

Environment variable: `GC_THREAD_HEAP_BLOCK_SIZE`

Supported by: None with multithreading

Default: 64 MB

#### `threads`

Number of garbage collector threads. The value built into the program is
capped at the number of processors - 1 when the program starts, the one
from the environment variable is used exactly. The number in use is logged
at the `info` log level.

Environment variable: `GC_NPROCS`

Supported by: Commix

Default: number of processors - 1, at most 8

#### `timeRatio`

The heap grows when a collection, from its request until it ends, took this
fraction of the time since the previous collection ended. Explicit
`System.gc()` calls do not count.

Environment variable: `GC_TIME_RATIO`

Supported by: Immix, Commix

Default: 0.05

#### `freeRatio`

Fraction of the heap that should be free after a collection.

Environment variable: `GC_FREE_RATIO`

Supported by: Commix

Default: 0.5

#### `maxFreeRatio`

Growth for `timeRatio` only happens while less than this fraction of the
heap is free after a collection. Immix applies it to heaps above 128 MB.

Environment variable: `GC_MAX_FREE_RATIO`

Supported by: Immix, Commix

Default: 0.8

#### `syncTimeout`

When multithreading is enabled, the GC needs to stop all threads before
collecting garbage. If a thread is stuck, e.g. due to undefined behaviour,
or executing foreign blocking code without the `@blocking` annotation, the
GC would wait indefinitely. After this timeout the GC aborts the process
with diagnostic information. `Duration.Zero`, or 0 in the environment
variable, disables the timeout.

Environment variable: `SCALANATIVE_GC_SYNC_TIMEOUT_MS` (milliseconds)

Supported by: Immix, Commix

Default: 60 seconds

#### `syncWarningInterval`

Interval of the warnings printed while waiting for threads to reach a
safepoint, with information about the threads which are stuck.

Environment variable: `SCALANATIVE_GC_SYNC_WARNING_INTERVAL_MS` (milliseconds)

Supported by: Immix, Commix

Default: 10 seconds

#### `logLevel`

Verbosity of the GC log messages:

-   `LogLevel.Debug`, `debug` - all messages (internal operations, allocation tracking)
-   `LogLevel.Info`, `info` - info, warnings and errors (parsed options, stats)
-   `LogLevel.Warn`, `warn` - warnings and errors
-   `LogLevel.Error`, `error` - only errors
-   `LogLevel.Off`, `none` - no GC logging

Log messages include timestamps in `[HH:MM:SS.mmm]` format with
millisecond precision. With Boehm the level applies only to the messages
of the Scala Native runtime, not to these of the collector itself.

Environment variable: `SCALANATIVE_GC_LOG_LEVEL`

Supported by: Immix, Commix, Boehm, None

Default: `warn`

#### `logFile`

File the GC log is appended to instead of the standard error stream. A
relative path is resolved against the working directory of the running
program. An empty environment variable selects the standard error stream
also when a file was set in the build. Recommended with the `debug` log
level. Boehm reads the environment variable of the same name on its own,
it cannot be set in the build.

Environment variable: `GC_LOG_FILE`

Supported by: Immix, Commix, None

Default: the standard error stream

#### `statsFile`

File the statistics of each collection are written to. A relative path is
resolved against the working directory of the running program. An empty
environment variable disables the statistics set in the build.

Environment variable: `GC_STATS_FILE`

Supported by: Immix, Commix, with `statsEnabled`

Default: no statistics

#### `statsEnabled`

Compiles the support for `statsFile` into the garbage collector. Without it
`GC_STATS_FILE` is ignored, also by Immix which used to honour it always.

Environment variable: none, it cannot be changed when the program starts

Supported by: Immix, Commix

Default: false

#### `trapBasedYieldPoints`

How threads are stopped for a collection. With traps a thread reads a
memory page which the GC protects to stop it, which is faster than checking
a flag but is reported by debuggers as a memory access fault, see
[testing](./testing.md).

Environment variable: `SCALANATIVE_GC_TRAP_BASED_YIELDPOINTS` (`1` or `0`), only when building

Supported by: Immix, Commix, with multithreading

Default: true in release modes, false in the debug mode

### Boehm GC

Scala Native supports the [Boehm-Demers-Weiser Garbage Collector](https://www.hboehm.info/gc/),
the names of the environment variables shared by the garbage collectors
follow it where possible. Boehm reads its environment variables on its own,
all of them are listed in
[environment.md](https://github.com/ivmai/bdwgc/blob/master/docs/environment.md).
Only `initialHeapSize`, `maxHeapSize` and `logLevel` can be set in the
build.

### Legacy compile options

Builds that defined `-DGC_INITIAL_HEAP_SIZE=5m`, `-DGC_MAXIMUM_HEAP_SIZE=5m`,
`-DGC_THREAD_HEAP_BLOCK_SIZE=32m`, `-DGC_NPROCS=4` or `-DGC_ENABLE_STATS`
with `nativeConfig.withCompileOptions` keep working: the define is used
unless the setting is defined in `GarbageCollectorConfig` or in the
environment of the build. Prefer `GarbageCollectorConfig`, which is
validated. GC assertions are still enabled with `-DGC_ASSERTIONS`.

## Examples

If you are developing in the Scala Native *sandbox*, the following are
examples showing some error conditions using Immix, the default GC. They are reported when the program starts, the same combination set in `GarbageCollectorConfig` fails the build instead. Adjust the path to your executable as needed (Scala 2.13 shown).

*Example 1:*
``` shell
export GC_INITIAL_HEAP_SIZE=64k
export GC_MAXIMUM_HEAP_SIZE=512k
./sandbox/.2.13/target/scala-2.13/sandbox
```
*Note: Error shown.*
```
GC_MAXIMUM_HEAP_SIZE too small to initialize heap.
Minimum required: 1m
```

*Example 2:*
```shell
export GC_INITIAL_HEAP_SIZE=2m
export GC_MAXIMUM_HEAP_SIZE=1m;
./sandbox/.2.13/target/scala-2.13/sandbox
```
*Note: Error shown.*
```
GC_MAXIMUM_HEAP_SIZE should be at least GC_INITIAL_HEAP_SIZE
```

Continue to [release types](./release-types.md).
