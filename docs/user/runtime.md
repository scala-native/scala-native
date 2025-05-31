# Runtime / Garbage Collector Settings

Scala Native comes with some ability to change the runtime characteristics.

## Garbage Collector (GC) Settings

Scala Native supports the [Boehm-Demers-Weiser Garbage Collector](https://www.hboehm.info/gc/). The environment variables defined in Boehm are shared as much as possible by all the Garbage Collectors supported in Scala Native so they are consistent and easier to use. Refer to the Boehm section below for the list. The variables supported are listed below for each GC.

## All Garbage Collectors

The following **environment** variables will be used for all GCs. They can go from 1 MB to the system memory maximum or up to about 512 GB or less than 4 GB for 32 bit systems. The size can be specified in bytes, kilobytes(k or K), megabytes(m or M), or gigabytes(g or G).

*Examples: 1048576, 1024k, 1M, or 1G etc.*

- GC_INITIAL_HEAP_SIZE changes the minimum heap size.
- GC_MAXIMUM_HEAP_SIZE changes the maximum heap size.

The GC_INITIAL_HEAP_SIZE and GC_MAXIMUM_HEAP_SIZE are ignored by None GC when multi-threading is enabled and GC_THREAD_HEAP_BLOCK_SIZE is used instead. See the None GC section below for details.

## Compile Time Memory Settings

The GC_INITIAL_HEAP_SIZE, GC_MAXIMUM_HEAP_SIZE and for None GC the GC_THREAD_HEAP_BLOCK_SIZE can be set at **compile time** by using **defines** in the build. Refer to the None GC section below for details about GC_THREAD_HEAP_BLOCK_SIZE.

The following examples shows how to set compile time memory values which is useful for the developer to provide a good user experience. A key feature is that the values can be overridden with environment variables by the user if supported on the platform. We also support the define DEBUG_PRINT to see the values passed to the GC as shown below in the first example build setup.

*Note: Currently, Boehm only supports values in bytes for **defines** as shown in the third example.*

``` scala
import scala.scalanative.build._

// Compile time GC defines (default Immix GC)
nativeConfig ~= { c =>
  c.withCompileOptions(
    _ ++ Seq(
      "-DGC_INITIAL_HEAP_SIZE=5m",
      "-DGC_MAXIMUM_HEAP_SIZE=5m",
      "-DDEBUG_PRINT"
    )
  )
}

// None GC with Multi-threading
nativeConfig ~= { c =>
  c.withCompileOptions(
    _ ++ Seq(
      "-DGC_THREAD_HEAP_BLOCK_SIZE=32m"
    )
  ).withGC(GC.none)
    .withMultithreading(true)
}

// Boehm GC (use number of bytes) - 5m shown
nativeConfig ~= { c =>
  c.withCompileOptions(
    _ ++ Seq(
      "-DGC_INITIAL_HEAP_SIZE=5242880",
      "-DGC_MAXIMUM_HEAP_SIZE=5242880"
    )
  ).withGC(GC.boehm)
}
```

More GC settings may be added in the future using the Boehm
setting names where applicable.

## Boehm GC

The Boehm GC uses the two variables shown above. The following is
available for Boehm and Commix.

-   GC_NPROCS

The following document shows all the variables available for Boehm:
[environment.md](https://github.com/ivmai/bdwgc/blob/master/docs/environment.md).

## None GC

The None GC uses the two variables shown above **or** the following one when multi-threading is selected.

-   GC_THREAD_HEAP_BLOCK_SIZE (defaults to 64 MB)

 When using this pseudo-GC implementation, GC_THREAD_HEAP_BLOCK_SIZE env variable can be set to control the granuality of allocated heap memory blocks for each of the threads. This env variable is ignored in single-threaded execution.

## Immix GC

The Immix GC uses the two variables shown above as well as the following
variable.

-   GC_STATS_FILE (set to the file name)

## Commix GC

In addition to the variables described above for Immix, Commix has the
following variable shared with Boehm.

-   GC_NPROCS (default is processor count - 1 up to 8 maximum)

Commix also adds a few more variables which do not match the Boehm
settings yet.

-   GC_TIME_RATIO (default is .05)
-   GC_FREE_RATIO (default is .5)

Note: GC_STATS_FILE shared with Immix is only honored if the compiler
defines -DGC_ENABLE_STATS for Commix.

## Examples

If you are developing in the Scala Native *sandbox*, the following are
examples showing some error conditions using Immix, the default GC. These errors will also occur if using **defines** as described above or mixing **defines** with environment variables. Adjust the path to your executable as needed (Scala 2.13 shown).

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

Continue to [lib](../lib/communitylib.md)
