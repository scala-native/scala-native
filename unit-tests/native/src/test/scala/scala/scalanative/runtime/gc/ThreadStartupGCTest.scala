package scala.scalanative.runtime.gc

import org.junit.Assert._
import org.junit.Assume.assumeTrue
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.unsafe._

class ThreadStartupGCTest {
  @Test def collectBeforeStackBoundsAreInitialized(): Unit = {
    assumeTrue("requires POSIX threads", !LinktimeInfo.isWindows)
    assumeTrue("requires multithreading", LinktimeInfo.isMultithreadingEnabled)
    assumeTrue(
      "requires Immix or Commix",
      LinktimeInfo.gc.isImmix || LinktimeInfo.gc.isCommix
    )
    assertEquals(
      0,
      ThreadStartupGCTest.collectBeforeStackBoundsAreInitialized()
    )
  }
}

@extern
private object ThreadStartupGCTest {
  @blocking
  @name("scalanative_test_collect_before_stack_bounds_are_initialized")
  def collectBeforeStackBoundsAreInitialized(): CInt = extern
}
