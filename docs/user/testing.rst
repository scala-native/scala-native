.. _testing:

Testing
=======

Scala Native comes with JUnit support out of the box.
This means that you can write JUnit tests, in the same way
you would do for a Java project.

To enable JUnit support, add the following lines to your `build.sbt` file:

.. Note: Using parsed-literal here instead of code-block:: scala
..       allows this file to reference the Single Point of Truth in
..       docs/config.py for the Scala Version. That is a big reduction
..       in the likelihood of version skew.
..       A user can "cut & paste" from the output but the SN Release Manager
..       need not change this source.
..
..       parsed-literal does not allow scala highlighting, so there is a
..       slight visual change in the output. Can you even detect it?

.. parsed-literal::

    enablePlugins(ScalaNativeJUnitPlugin)

If you want to get more detailed output from the JUnit runtime, also include the following line:

.. code-block:: scala

    testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-s", "-v")

Then, add your tests, for example in the `src/test/scala/` directory:

.. code-block:: scala

    import org.junit.Assert._
    import org.junit.Test

    class MyTest {
      @Test def superComplicatedTest(): Unit = {
        assertTrue("this assertion should pass", true)
      }
    }

Finally, run the tests in `sbt` by running `test` to run all tests.
You may also use `testOnly` to run a particular test, for example:

.. code-block:: shell

    testOnly MyTest
    testOnly MyTest.superComplicatedTest

Testing with debug metadata
---------------------------
Debug builds with enabled debug metadata allows to produce stack traces containing source positions,
however, to obtain them runtime needs to parse the produced debug metadata.
This operation is performed when generating stack traces for the first time and can take more than 1 second.
This behavior can influence tests expecting to finish within some fixed amount of time. 
To mitigate this issue set the environment variable `SCALANATIVE_TEST_PREFETCH_DEBUG_INFO=1` to ensure that debug info would be loaded
before starting test execution.

Debugging sygnals
-----------------
In case of problems with unexpected signals crashing the test (SIGSEGV, SIGBUS) 
you can set the environment variable `SCALANATIVE_TEST_DEBUG_SIGNALS=1` to enable debug signal handlers
in the test runner. When enabled test runner would set up signal handlers printing stack trace for most of the available signals for a given platform.


Continue to :ref:`profiling`.
