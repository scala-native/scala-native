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
..       parsed-literal does not allow scala highlighting, so there is a
..       slight visual change in the output. Can you even detect it?

.. parsed-literal::

    libraryDependencies += "org.scala-native" %%% "junit-runtime" % |release|
    addCompilerPlugin("org.scala-native" % "junit-plugin" % |release| cross CrossVersion.full)

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
