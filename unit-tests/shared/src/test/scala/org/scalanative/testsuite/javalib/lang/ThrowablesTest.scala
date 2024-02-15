package org.scalanative.testsuite.javalib.lang

// Portions of this Suite were ported, with thanks & gratitude,
// from Scala.js testsuite/javalib/lang/ThrowablesTestOnJDK7.scala
//
// The rest is an original contribution to Scala Native.

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import scala.scalanative.junit.utils.AssumesHelper._
import org.scalanative.testsuite.utils.Platform

class ThrowablesTest {

  // Consolidate boilerplate; aids consistency.

  private def getThrowableMessage(throwable: Throwable): String = {
    if (throwable == null) "<null>" else throwable.getMessage
  }

  private def checkCause(
      throwable: Throwable,
      expectedCause: Throwable
  ): Unit = {
    assumeNotASAN()
    val resultCause = throwable.getCause

    val causeMessage = getThrowableMessage(throwable)

    val expectedMessage = getThrowableMessage(expectedCause)

    assertTrue(
      s"cause: '${causeMessage}' != expected: '${expectedMessage}'",
      resultCause == expectedCause
    )
  }

  private def checkMessage(
      throwable: Throwable,
      expectedMessage: String
  ): Unit = {
    assumeNotASAN()
    val resultMessage = throwable.getMessage

    assertTrue(
      s"message: '${resultMessage}' != expected: '${expectedMessage}'",
      resultMessage == expectedMessage
    )
  }

  private def checkStackTraceString(
      trace: String,
      usesAnonymousThrowable: Boolean = false
  ): Unit = {
    assumeSupportsStackTraces()
    val startText =
      if (usesAnonymousThrowable)
        "org.scalanative.testsuite.javalib.lang.ThrowablesTest$$anon$1"
      else "java.lang.Throwable"
    assertTrue(
      s"Expected trace to start with '${startText}' and it did not. - `$trace`",
      trace.startsWith(startText)
    )

    val containsText =
      "\tat org.scalanative.testsuite.javalib.lang.ThrowablesTest"
    assertTrue(
      s"Expected trace to contain '${containsText}' and it did not.",
      trace.contains(containsText)
    )
  }

  private def checkStackTrace(
      throwable: Throwable,
      usesAnonymousThrowable: Boolean = false
  ): Unit = {
    assumeSupportsStackTraces()
    val sw = new java.io.StringWriter
    val pw = new java.io.PrintWriter(sw)

    throwable.printStackTrace(pw)

    checkStackTraceString(sw.toString, usesAnonymousThrowable)
  }

  private def checkSuppressed(
      throwable: Throwable,
      expectedLength: Int
  ): Unit = {
    assumeNotASAN()
    val getSuppressedLength = throwable.getSuppressed.length
    assertTrue(
      s"getSuppressed.length: ${getSuppressedLength} != " +
        s"expected: ${expectedLength}",
      getSuppressedLength == expectedLength
    )
  }

  private def checkConstructed(
      throwable: Throwable,
      expectedMessage: String,
      expectedCause: Throwable,
      expectedSuppressedLength: Int,
      usesAnonymousThrowable: Boolean = false
  ): Unit = {
    checkMessage(throwable, expectedMessage)
    checkCause(throwable, expectedCause)
    checkSuppressed(throwable, 0)
    checkStackTrace(throwable, usesAnonymousThrowable)
  }

  // Zero & two argument constructor tests will exercise cases where
  // the rightmost two arguments are true.

  @Test def throwableMessageCauseFalseFalse(): Unit = {
    val expectedMessage = "Athchomar chomakea"
    val expectedCause = new Throwable("Khal Drogo")

    val throwable =
      new Throwable(expectedMessage, expectedCause, false, false) {}

    checkConstructed(
      throwable,
      expectedMessage,
      expectedCause,
      0,
      usesAnonymousThrowable = true
    )
  }

  @Test def throwableMessage(): Unit = {
    val expectedMessage = "Hello World"
    val expectedCause = null

    val throwable = new Throwable(expectedMessage)

    checkConstructed(throwable, expectedMessage, expectedCause, 0)
  }

  @Test def throwableCause(): Unit = {
    val expectedMessageStem = "Primum Mobile"
    val expectedMessage = s"java.lang.Throwable: ${expectedMessageStem}"

    val expectedCause = new Throwable(expectedMessageStem)

    val throwable = new Throwable(expectedCause)

    checkConstructed(throwable, expectedMessage, expectedCause, 0)
  }

  @Test def throwable(): Unit = {
    val expectedMessage = null
    val expectedCause = null

    val throwable = new Throwable()

    checkConstructed(throwable, expectedMessage, expectedCause, 0)
  }

  // Thirteen public methods are documented for class Throwable.
  //
  // Five (5) methods: fillinStackTrace(), getCause(), getMessage(),
  // getSuppressed(), printStackTrace(PrintWriter), have been exercised by
  // constructor tests above and do not need separate tests.
  //
  // Six methods are exercised below. getStackTrace() does not have a
  // test of its own. It gets exercised by setStackTrace().
  //
  // The following two (2) do not have explicit tests:
  //
  // * Scala Native supports only the US Locale, so getLocalizedMessage()
  //   is just a call to getMessage(), which is exercised.
  //
  // * printStackTrace() is a call to printStackTrace(System.err) where
  //   the latter is exercised.
  //
  // This accounts for all thirteen methods.

  @Test def addSuppressedExceptionInvalidArguments(): Unit = {
    assertThrows(
      classOf[java.lang.NullPointerException], {
        val throwable = new Throwable()
        throwable.addSuppressed(null)
      }
    )

    assertThrows(
      classOf[java.lang.IllegalArgumentException], {
        val throwable = new Throwable("Expect IllegalArgumentException")
        throwable.addSuppressed(throwable)
      }
    )
  }

  @Test def addSuppressedExceptionEnabledEqualsTrue(): Unit = {
    val throwable = new Throwable()

    val sl = throwable.getSuppressed().length
    assertTrue(s"length: ${sl} != expected: 0", sl == 0)

    val suppressed1 = new IllegalArgumentException("suppressed_1")
    val suppressed2 = new UnsupportedOperationException("suppressed_2")

    // There is no ordering guarantee in suppressed exceptions, so we compare
    // sets.

    throwable.addSuppressed(suppressed1)
    assertTrue(
      s"first suppressed set did not match expected",
      throwable.getSuppressed().toSet == Set(suppressed1)
    )

    throwable.addSuppressed(suppressed2)
    assertTrue(
      s"second suppressed set did not match expected",
      throwable.getSuppressed().toSet == Set(suppressed1, suppressed2)
    )
  }

  @Test def addSuppressedExceptionEnabledEqualsFalse(): Unit = {
    val throwable = new Throwable(null, null, false, true) {}

    val sl0 = throwable.getSuppressed().length
    assertTrue(s"starting  suppressed length: ${sl0} != expected: 0", sl0 == 0)

    val suppressed1 = new IllegalArgumentException("suppressed_1")
    val suppressed2 = new UnsupportedOperationException("suppressed_2")

    throwable.addSuppressed(suppressed1)
    val sl1 = throwable.getSuppressed().length
    assertTrue(s"first suppressed length: ${sl1} != expected: 0", sl1 == 0)

    throwable.addSuppressed(suppressed2)
    val sl2 = throwable.getSuppressed().length
    assertTrue(s"second suppressed length: ${sl2} != expected: 0", sl2 == 0)
  }

  @Test def initCauseCauseCasesWhichThrowAnException(): Unit = {
    assertThrows(
      classOf[java.lang.IllegalArgumentException], {
        val throwable = new Throwable()
        throwable.initCause(throwable)
      }
    )

    assertThrows(
      classOf[java.lang.IllegalStateException], {
        val throwable = new Throwable(new Throwable("Lyta-Zod"))
        throwable.initCause(new Throwable("Jayna-Zod"))
      }
    )

    locally {
      val throwable = new Throwable()
      throwable.initCause(new Throwable("Kem"))

      assertThrows(
        classOf[java.lang.IllegalStateException],
        throwable.initCause(new Throwable("Cor-Vex"))
      )

      assertThrows(
        classOf[java.lang.IllegalStateException],
        throwable.initCause(new Throwable("Jor-El"))
      )
    }
  }

  @Test def initCauseCause(): Unit = {
    val throwable = new Throwable()
    // Constructor test above has already verified that initial cause is null.

    val causeMsg = "Nyssa-Vex"
    val cause = new Throwable(causeMsg)

    throwable.initCause(cause)

    val result = throwable.getCause

    val resultMsg = if (result == null) "null" else result.getMessage

    assertTrue(
      s"unexpected cause: '${resultMsg}' != expected: '${causeMsg}'",
      result == cause
    )
  }

  @Test def printStackTracePrintStream(): Unit = {
    assumeNotASAN()
    val throwable = new Throwable("Dev-Em")
    val baos = new java.io.ByteArrayOutputStream
    val ps = new java.io.PrintStream(baos)
    val encoding = "UTF-8"

    throwable.printStackTrace(ps)

    checkStackTraceString(baos.toString(encoding))
  }

  @Test def setStackTraceStackTraceInvalidArguments(): Unit = {
    assertThrows(
      classOf[java.lang.NullPointerException], {
        val throwable = new Throwable()
        throwable.setStackTrace(null)
      }
    )

    assertThrows(
      classOf[java.lang.NullPointerException], {
        val throwable = new Throwable()
        val newStackTrace = Array(
          new StackTraceElement("Zero", "noMethod", "noFile", 0),
          new StackTraceElement("One", "noMethod", "noFile", 1),
          null.asInstanceOf[StackTraceElement],
          new StackTraceElement("Three", "noMethod", "noFile", 3)
        )

        throwable.setStackTrace(newStackTrace)
      }
    )
  }

  @Test def setStackTraceStackTraceWritableTrue(): Unit = {
    val throwable = new Throwable(null, null, true, true) {}

    val newStackTrace = Array(
      new StackTraceElement("Zero", "noMethod", "noFile", 0),
      new StackTraceElement("One", "noMethod", "noFile", 1),
      new StackTraceElement("Two", "noMethod", "noFile", 2),
      new StackTraceElement("Three", "noMethod", "noFile", 3)
    )

    val beforeStackTrace = throwable.getStackTrace()

    throwable.setStackTrace(newStackTrace)

    val afterStackTrace = throwable.getStackTrace()

    assertFalse(
      s"elements after setStackTrace() did not change",
      afterStackTrace.sameElements(beforeStackTrace)
    )

    assertTrue(
      s"elements after setsetStackTrace() are not as expected",
      afterStackTrace.sameElements(newStackTrace)
    )
  }

  @Test def setStackTraceStackTraceWritableFalse(): Unit = {
    val throwable = new Throwable(null, null, true, false) {}

    val newStackTrace = Array(
      new StackTraceElement("Zero", "noMethod", "noFile", 0),
      new StackTraceElement("One", "noMethod", "noFile", 1),
      new StackTraceElement("Two", "noMethod", "noFile", 2),
      new StackTraceElement("Three", "noMethod", "noFile", 3)
    )

    val beforeStackTrace = throwable.getStackTrace()

    throwable.setStackTrace(newStackTrace)

    val afterStackTrace = throwable.getStackTrace()

    assertTrue(
      s"stackTrace elements of non-writable stack differ",
      afterStackTrace.sameElements(beforeStackTrace)
    )
  }

  @Test def setStackTraceStackTraceWriteToReturnedStack(): Unit = {
    assumeNotASAN()
    val throwable = new Throwable()
    val trace1 = throwable.getStackTrace()

    val savedElement0 = trace1(0)
    trace1(0) = null

    val trace2 = throwable.getStackTrace()

    assertTrue(
      s"writing into returned trace should not affect next getStackTrace",
      trace2(0) == savedElement0
    )

    assertTrue(
      s"second getStackTrace() should not change first result",
      trace1(0) == null
    )
  }

  @Test def testToString(): Unit = {
    val expectedClassName = "java.lang.Throwable"

    locally {
      val throwable = new Throwable()

      val expected = expectedClassName
      val result = throwable.toString
      assertTrue(
        s"result: ${result} != expected: ${expected}",
        result == expected
      )
    }

    locally {
      val message = "Seg-El"
      val throwable = new Throwable(message)

      val expected = s"${expectedClassName}: ${message}"
      val result = throwable.toString
      assertTrue(
        s"result: ${result} != expected: ${expected}",
        result == expected
      )
    }
  }

  @Test def commonConstructors(): Unit = {
    // In the folling tests we only check that all required constructors are defined
    val throwable = new Throwable() {}
    val exception = new Exception()
    val msg = "msg"
    Seq(
      // Errors
      new Error(),
      new Error(msg),
      new Error(throwable),
      new Error(msg, throwable),
      new Error(msg, throwable, false, false) {},
      new VirtualMachineError() {},
      new VirtualMachineError(msg) {},
      new VirtualMachineError(throwable) {},
      new VirtualMachineError(msg, throwable) {},
      new InternalError(),
      new InternalError(msg),
      new InternalError(throwable),
      new InternalError(msg, throwable),
      new LinkageError(),
      new LinkageError(msg),
      new LinkageError(msg, throwable),
      // Exceptions
      new Exception(),
      new Exception(msg),
      new Exception(throwable),
      new Exception(msg, throwable),
      new Exception(msg, throwable, false, false) {},
      new RuntimeException(),
      new RuntimeException(msg),
      new RuntimeException(throwable),
      new RuntimeException(msg, throwable),
      new RuntimeException(msg, throwable, false, false) {}
    ).foreach(assertNotNull(_))
  }
}
