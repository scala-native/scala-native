# Testing

Scala Native comes with JUnit support out of the box. This means that
you can write JUnit tests, in the same way you would do for a Java
project.

To enable JUnit support, add the following lines to your `build.sbt` file:
```scala
  enablePlugins(ScalaNativeJUnitPlugin)
```

If you want to get more detailed output from the JUnit runtime, also
include the following line:

``` scala
testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-s", "-v")
```

Then, add your tests, for example in the `src/test/scala/`
directory:

``` scala
import org.junit.Assert._
import org.junit.Test

class MyTest {
  @Test def superComplicatedTest(): Unit = {
    assertTrue("this assertion should pass", true)
  }
}
```

Finally, run the tests in `sbt` by running
`test` to run all tests. You may also use
`testOnly` to run a particular test, for example:

``` shell
testOnly MyTest
testOnly MyTest.superComplicatedTest
```

## Source level debugging

Scala Native provides initial support for generating source level debug information, which can be used to map code executed in the debugger to the original sources or to represent local variables.
When executing on MacOS it also allows to obtain approximated source code lines in the exception stack traces.
Be aware that both Scala Native optimizer and
LLVM optimizers can remove some of the optimized out debug information.
For best experience run with disabled optimizations:

```scala
import scala.scalanative.build._

nativeConfig ~= { c =>
  c.withSourceLevelDebuggingConfig(_.enableAll) // enable generation of debug information
  .withOptimize(false)  // disable Scala Native optimizer
  .withMode(Mode.debug) // compile using LLVM without optimizations
}
```

When using LLDB based debugger you can use our [custom formatter](https://github.com/scala-native/scala-native/blob/main/ScalaNativeLLDBFormatter.py) which would provide more user-friendly information about Scala types, e.g. representation of Arrays and Strings.

### Testing with debug metadata
Debug builds with enabled debug metadata allows to produce stack traces containing source positions, however, to obtain them runtime needs to parse the produced debug metadata. This operation is performed when generating stack traces for the first time and can take more than 1 second. This behavior can influence tests expecting to finish within some fixed amount of time.
To mitigate this issue set the environment variable `SCALANATIVE_TEST_PREFETCH_DEBUG_INFO=1` to ensure that debug info would be loaded before starting test execution.

### Debugging with multithreading
To achive (almost) no-overhead for stopping threads during garbage collection, Scala Native uses specialized signal handlers which can trigger controlled segmentation fault during StopTheWorld event. These might lead to poor experience when iterating through the execution of the code in the debugger.
To mittigate this issue you can replace default yield points mechanism with a conservative, but slower mechanism checking for a global flag to be set using `SCALANATIVE_GC_TRAP_BASED_YIELDPOINTS=0` env variable when building.
Trap based yieldpoint mechanism is used by default in release modes, while the debug mode uses conventional approach.

Libraries that install their own SIGSEGV handler (for example libclang's crash
recovery) can intercept GC safepoints. If they re-raise the signal, its original
fault address is lost and Scala Native cannot recognize the safepoint. Select
conventional polling when building applications embedding such libraries:

```scala
nativeConfig ~= (_.withTrapBasedGCYieldPoints(false))
```

This retains multithreading support. `None` selects the mode default; the
build-time `SCALANATIVE_GC_TRAP_BASED_YIELDPOINTS` environment variable overrides
the configuration. Rebuild the binary after changing this setting.

Fatal runtime signal diagnostics are written directly to stderr, including the
signal code, original PC/SP, process identity, and (for GC handlers) mutator,
trap cell and GC stopping state. Software-generated signals report the sender
instead of interpreting the signal-info union as a fault address. The runtime
then restores the default action and re-raises the original signal, allowing a
core dump if enabled by the OS. Symbolize the recorded PC using the matching
binary and debug information; the core's terminating PC may be in `raise`.

For intermittent `SEGV_MAPERR` failures, retain the binary, debug information,
stderr record and core dump, and compare the same optimized workload with traps
enabled and disabled. Resolve the faulting instruction before attributing a
failure to GC or the test that most recently finished. An unmapped-address fault
is fatal, including while GC is stopping threads.

The standalone regression suite `bash scripts/tests/gc-signals.sh` exercises
signal classification, fatal reporting and concurrent trap initialization for
Immix and Commix. On Linux, set `GC_SIGNAL_TEST_LIBCLANG` to the path of
`libclang.so` to additionally reproduce interception of a protected trap cell
by libclang's crash recovery.

## Debugging signals

In case of problems with unexpected signals crashing the test (SIGSEGV, SIGBUS) you can set the environment variable `SCALANATIVE_TEST_DEBUG_SIGNALS=1` to enable debug signal handlers in the test runner.
When enabled test runner would set up signal handlers printing stack trace for most of the available signals
for a given platform.

Continue to [profiling](profiling.md).
