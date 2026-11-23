package org.scalanative.testsuite.runtime

import org.junit.Assert._
import org.junit.Test

class TryFinallyTest {
  import TryFinallyTest._

  private var finallyRuns = 0

  private def expectThrown(msg: String)(body: => Any): Unit = {
    finallyRuns = 0
    try {
      body
      fail("expected an exception")
    } catch {
      case e: IllegalStateException => assertEquals(msg, e.getMessage)
    }
    assertEquals("finally runs", 1, finallyRuns)
  }

  // Issue #5367
  @Test def finallyRunsWhenCallInCatchThrows(): Unit = expectThrown("second") {
    try throw new IllegalStateException("first")
    catch { case _: IllegalStateException => fails("second") }
    finally finallyRuns += 1
  }

  @Test def finallyRunsWhenCallInBoundCatchThrows(): Unit =
    expectThrown("second") {
      val res: Int =
        try failsInt("first")
        catch {
          case e: IllegalStateException =>
            e.getMessage.length + failsInt("second")
        } finally finallyRuns += 1
      res
    }

  @Test def finallyRunsWhenCastInCatchThrows(): Unit = {
    finallyRuns = 0
    val value: Any = "string"
    try {
      val res: Integer =
        try throw new IllegalStateException("first")
        catch { case _: IllegalStateException => value.asInstanceOf[Integer] }
        finally finallyRuns += 1
      fail(s"expected an exception, got $res")
    } catch {
      case _: ClassCastException => ()
    }
    assertEquals("finally runs", 1, finallyRuns)
  }

  @Test def finallyRunsWhenMatchInCatchThrows(): Unit = {
    finallyRuns = 0
    val value: Any = 42
    try {
      val res: String =
        try throw new IllegalStateException("first")
        catch {
          case _: IllegalStateException =>
            value match { case s: String => s }
        } finally finallyRuns += 1
      fail(s"expected an exception, got $res")
    } catch {
      case _: MatchError => ()
    }
    assertEquals("finally runs", 1, finallyRuns)
  }

  @Test def finallyRunsWhenNestedTryInCatchThrows(): Unit =
    expectThrown("third") {
      try throw new IllegalStateException("first")
      catch {
        case _: IllegalStateException =>
          try fails("second")
          catch { case _: IllegalStateException => fails("third") }
      } finally finallyRuns += 1
    }

  @Test def finallyRunsWhenNestedTryFinallyInCatchThrows(): Unit = {
    var innerRuns = 0
    expectThrown("second") {
      try throw new IllegalStateException("first")
      catch {
        case _: IllegalStateException =>
          try fails("second")
          finally innerRuns += 1
      } finally finallyRuns += 1
    }
    assertEquals("inner finally runs", 1, innerRuns)
  }

  @Test def finallyRunsWhenNoCatchMatches(): Unit = expectThrown("first") {
    try fails("first")
    catch { case _: IllegalArgumentException => () }
    finally finallyRuns += 1
  }

  @Test def finallyRunsWhenCatchRethrows(): Unit = expectThrown("first") {
    try fails("first")
    catch { case e: IllegalStateException => throw e }
    finally finallyRuns += 1
  }

  @Test def finallyRunsWhenCatchReturns(): Unit = {
    finallyRuns = 0
    assertEquals(5, returnFromCatch(() => finallyRuns += 1))
    assertEquals("finally runs", 1, finallyRuns)
  }

  @Test def finallyRunsWhenCatchReturnsNormally(): Unit = {
    finallyRuns = 0
    val res =
      try failsInt("first")
      catch { case _: IllegalStateException => 7 }
      finally finallyRuns += 1
    assertEquals(7, res)
    assertEquals("finally runs", 1, finallyRuns)
  }

  @Test def finallyExceptionReplacesCatchException(): Unit =
    expectThrown("finally") {
      try fails("first")
      catch { case _: IllegalStateException => fails("second") }
      finally {
        finallyRuns += 1
        fails("finally")
      }
    }
}

object TryFinallyTest {
  @noinline def fails(msg: String): Unit =
    throw new IllegalStateException(msg)

  @noinline def failsInt(msg: String): Int =
    throw new IllegalStateException(msg)

  @noinline def returnFromCatch(onFinally: () => Unit): Int = {
    try fails("first")
    catch { case _: IllegalStateException => return 5 }
    finally onFinally()
    -1
  }
}
