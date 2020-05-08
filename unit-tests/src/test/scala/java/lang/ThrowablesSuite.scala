package java.lang

// Portions of this Suite were ported, with thanks & gratitude,
// from Scala.js testsuite/javalib/lang/ThrowablesTestOnJDK7.scala
//
// The rest is an original contribution to Scala Native.

object ThrowablesSuite extends tests.Suite {

  // Consolidate boilerplate; aids consistency.

  private def getThrowableMessage(throwable: Throwable): String = {
    if (throwable == null) "<null>" else throwable.getMessage
  }

  private def checkCause(throwable: Throwable,
                         expectedCause: Throwable): Unit = {
    val resultCause = throwable.getCause

    val causeMessage = getThrowableMessage(throwable)

    val expectedMessage = getThrowableMessage(expectedCause)

    assert(resultCause == expectedCause,
           s"cause: '${causeMessage}' != expected: '${expectedMessage}'")
  }

  private def checkMessage(throwable: Throwable,
                           expectedMessage: String): Unit = {
    val resultMessage = throwable.getMessage

    assert(resultMessage == expectedMessage,
           s"message: '${resultMessage}' != expected: '${expectedMessage}'")
  }

  private def checkStackTraceString(trace: String): Unit = {
    val startText = "java.lang.Throwable"
    assert(trace.startsWith(startText),
           s"Expected trace to start with '${startText}' and it did not.")

    val containsText = "\tat <none>.main(Unknown Source)"
    assert(trace.contains(containsText),
           s"Expected trace to contain '${containsText}' and it did not.")
  }

  private def checkStackTrace(throwable: Throwable): Unit = {
    val sw = new java.io.StringWriter
    val pw = new java.io.PrintWriter(sw)

    throwable.printStackTrace(pw)

    checkStackTraceString(sw.toString)
  }

  private def checkSuppressed(throwable: Throwable, expectedLength: Int) {
    val getSuppressedLength = throwable.getSuppressed.length
    assert(getSuppressedLength == expectedLength,
           s"getSuppressed.length: ${getSuppressedLength} != " +
             s"expected: ${expectedLength}")
  }

  private def checkConstructed(throwable: Throwable,
                               expectedMessage: String,
                               expectedCause: Throwable,
                               expectedSuppressedLength: Int): Unit = {
    checkMessage(throwable, expectedMessage)
    checkCause(throwable, expectedCause)
    checkSuppressed(throwable, 0)
    checkStackTrace(throwable)
  }

  // Zero & two argument constructor tests will exercise cases where
  // the rightmost two arguments are true.

  test("Throwable(message, cause, false, false)") {
    val expectedMessage = "Athchomar chomakea"
    val expectedCause   = new Throwable("Khal Drogo")

    val throwable = new Throwable(expectedMessage, expectedCause, false, false)

    checkConstructed(throwable, expectedMessage, expectedCause, 0)
  }

  test("Throwable(message)") {
    val expectedMessage = "Hello World"
    val expectedCause   = null

    val throwable = new Throwable(expectedMessage)

    checkConstructed(throwable, expectedMessage, expectedCause, 0)
  }

  test("Throwable(cause)") {
    val expectedMessageStem = "Primum Mobile"
    val expectedMessage     = s"java.lang.Throwable: ${expectedMessageStem}"

    val expectedCause = new Throwable(expectedMessageStem)

    val throwable = new Throwable(expectedCause)

    checkConstructed(throwable, expectedMessage, expectedCause, 0)
  }

  test("Throwable()") {
    val expectedMessage = null
    val expectedCause   = null

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

  test("addSuppressed(exception) - invalid arguments") {
    assertThrows[java.lang.NullPointerException] {
      val throwable = new Throwable()
      throwable.addSuppressed(null)
    }

    assertThrows[java.lang.IllegalArgumentException] {
      val throwable = new Throwable("Expect IllegalArgumentException")
      throwable.addSuppressed(throwable)
    }
  }

  test("addSuppressed(exception) - enabled == true") {
    val throwable = new Throwable()

    val sl = throwable.getSuppressed().length
    assert(sl == 0, s"length: ${sl} != expected: 0")

    val suppressed1 = new IllegalArgumentException("suppressed_1")
    val suppressed2 = new UnsupportedOperationException("suppressed_2")

    // There is no ordering guarantee in suppressed exceptions, so we compare
    // sets.

    throwable.addSuppressed(suppressed1)
    assert(throwable.getSuppressed().toSet == Set(suppressed1),
           s"first suppressed set did not match expected")

    throwable.addSuppressed(suppressed2)
    assert(throwable.getSuppressed().toSet == Set(suppressed1, suppressed2),
           s"second suppressed set did not match expected")
  }

  test("addSuppressed(exception) - enabled == false") {
    val throwable = new Throwable(null, null, false, true)

    val sl0 = throwable.getSuppressed().length
    assert(sl0 == 0, s"starting  suppressed length: ${sl0} != expected: 0")

    val suppressed1 = new IllegalArgumentException("suppressed_1")
    val suppressed2 = new UnsupportedOperationException("suppressed_2")

    throwable.addSuppressed(suppressed1)
    val sl1 = throwable.getSuppressed().length
    assert(sl1 == 0, s"first suppressed length: ${sl1} != expected: 0")

    throwable.addSuppressed(suppressed2)
    val sl2 = throwable.getSuppressed().length
    assert(sl2 == 0, s"second suppressed length: ${sl2} != expected: 0")
  }

  test("initCause(cause) - cases which throw an Exception") {
    assertThrows[java.lang.IllegalArgumentException] {
      val throwable = new Throwable()
      throwable.initCause(throwable)
    }

    assertThrows[java.lang.IllegalStateException] {
      val throwable = new Throwable(new Throwable("Lyta-Zod"))
      throwable.initCause(new Throwable("Jayna-Zod"))
    }

    locally {
      val throwable = new Throwable()
      throwable.initCause(new Throwable("Kem"))

      assertThrows[java.lang.IllegalStateException] {
        throwable.initCause(new Throwable("Cor-Vex"))
      }

      assertThrows[java.lang.IllegalStateException] {
        throwable.initCause(new Throwable("Jor-El"))
      }
    }
  }

  test("initCause(cause)") {
    val throwable = new Throwable()
    // Constructor test above has already verified that initial cause is null.

    val causeMsg = "Nyssa-Vex"
    val cause    = new Throwable(causeMsg)

    throwable.initCause(cause)

    val result = throwable.getCause

    val resultMsg = if (result == null) "null" else result.getMessage

    assert(result == cause,
           s"unexpected cause: '${resultMsg}' != expected: '${causeMsg}'")
  }

  test("printStackTrace(PrintStream)") {
    val throwable = new Throwable("Dev-Em")
    val baos      = new java.io.ByteArrayOutputStream
    val ps        = new java.io.PrintStream(baos)
    val encoding  = "UTF-8"

    throwable.printStackTrace(ps)

    checkStackTraceString(baos.toString(encoding))
  }

  test("setStackTrace(stackTrace) - invalid arguments") {
    assertThrows[java.lang.NullPointerException] {
      val throwable = new Throwable()
      throwable.setStackTrace(null)
    }

    assertThrows[java.lang.NullPointerException] {
      val throwable = new Throwable()
      val newStackTrace = Array(
        new StackTraceElement("Zero", "noMethod", "noFile", 0),
        new StackTraceElement("One", "noMethod", "noFile", 1),
        null.asInstanceOf[StackTraceElement],
        new StackTraceElement("Three", "noMethod", "noFile", 3)
      )

      throwable.setStackTrace(newStackTrace)
    }
  }

  test("setStackTrace(stackTrace) - writable == true") {
    val throwable = new Throwable(null, null, true, true)

    val newStackTrace = Array(
      new StackTraceElement("Zero", "noMethod", "noFile", 0),
      new StackTraceElement("One", "noMethod", "noFile", 1),
      new StackTraceElement("Two", "noMethod", "noFile", 2),
      new StackTraceElement("Three", "noMethod", "noFile", 3)
    )

    val beforeStackTrace = throwable.getStackTrace()

    throwable.setStackTrace(newStackTrace)

    val afterStackTrace = throwable.getStackTrace()

    assert(!afterStackTrace.sameElements(beforeStackTrace),
           s"elements after setStackTrace() did not change")

    assert(afterStackTrace.sameElements(newStackTrace),
           s"elements after setsetStackTrace() are not as expected")
  }

  test("setStackTrace(stackTrace) - writable == false") {
    val throwable = new Throwable(null, null, true, false)

    val newStackTrace = Array(
      new StackTraceElement("Zero", "noMethod", "noFile", 0),
      new StackTraceElement("One", "noMethod", "noFile", 1),
      new StackTraceElement("Two", "noMethod", "noFile", 2),
      new StackTraceElement("Three", "noMethod", "noFile", 3)
    )

    val beforeStackTrace = throwable.getStackTrace()

    throwable.setStackTrace(newStackTrace)

    val afterStackTrace = throwable.getStackTrace()

    assert(afterStackTrace.sameElements(beforeStackTrace),
           s"stackTrace elements of non-writable stack differ")
  }

  test("setStackTrace(stackTrace) - write to returned stack") {
    val throwable = new Throwable()
    val trace1    = throwable.getStackTrace()

    val savedElement0 = trace1(0)
    trace1(0) = null

    val trace2 = throwable.getStackTrace()

    assert(trace2(0) == savedElement0,
           s"writing into returned trace should not affect next getStackTrace")

    assert(trace1(0) == null,
           s"second getStackTrace() should not change first result")
  }

  test("toString()") {
    val expectedClassName = "java.lang.Throwable"

    locally {
      val throwable = new Throwable()

      val expected = expectedClassName
      val result   = throwable.toString
      assert(result == expected, s"result: ${result} != expected: ${expected}")
    }

    locally {
      val message   = "Seg-El"
      val throwable = new Throwable(message)

      val expected = s"${expectedClassName}: ${message}"
      val result   = throwable.toString
      assert(result == expected, s"result: ${result} != expected: ${expected}")
    }
  }
}
