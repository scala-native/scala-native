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

Then, add your tests, for example in the [src/test/scala/]{.title-ref}
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

Finally, run the tests in [sbt]{.title-ref} by running
[test]{.title-ref} to run all tests. You may also use
[testOnly]{.title-ref} to run a particular test, for example:

``` shell
testOnly MyTest
testOnly MyTest.superComplicatedTest
```

## Source level debugging

Scala Native provides initial support for generating source level debug informations, which can be used to map code executed in the debugger to the original sources or to represent local variables. 
When executing on MacOS it also allows to obtain approximated source code lines in the exception stack traces. 
Be aware that both Scala Native optimizer and
LLVM optimizers can remove some of the optimized out debug informations.
For best experience run with disabled optimizations:

```scala
   nativeConfig ~= {
    .withSourceLevelDebuggingConfig(_.enableAll) // enable generation of debug informations
    .withOptimize(false)  // disable Scala Native optimizer
    .withMode(scalanative.build.Mode.debug) // compile using LLVM without optimizations
    } 
  ```
When using LLDB based debugger you can use our [custom formatter](../../ScalaNativeLLDBFormatter.py) which would provide more user friendly information about Scala types, e.g. representation of Arrays and Strings.

### Testing with debug metadata
Debug builds with enabled debug metadata allows to produce stack traces containing source positions, however, to obtain them runtime needs to parse the produced debug metadata. This operation is performed when generating stack traces for the first time and can take more than 1 second. This behavior can influence tests expecting to finish within some fixed amount of time. 
To mitigate this issue set the environment variable `SCALANATIVE_TEST_PREFETCH_DEBUG_INFO=1` to ensure that debug info would be loaded before starting test execution.

### Debugging with multithreading
To achive (almost) no-overhead for stopping threads during garbage collection, Scala Native uses specialized signal handlers which can trigger controlled segmentation fault during StopTheWorld event. These might lead to poor experience when iterating through the execution of the code in the debugger. 
To mittigate this issue you can replace default yield points mechanism with a conservative, but slower mechanism checking for a global flag to be set using `SCALANATIVE_GC_TRAP_BASED_YIELDPOINTS=0` env variable when building. 
Trap based yieldpoint mechanism is used by default in release modes, while the debug mode uses conventional approach.

## Debugging signals

In case of problems with unexpected signals crashing the test (SIGSEGV, SIGBUS) you can set the environment variable `SCALANATIVE_TEST_DEBUG_SIGNALS=1` to enable debug signal handlers in the test runner. 
When enabled test runner would set up signal handlers printing stack trace for most of the available signals
for a given platform.

Continue to [profiling](./profiling.rst).
