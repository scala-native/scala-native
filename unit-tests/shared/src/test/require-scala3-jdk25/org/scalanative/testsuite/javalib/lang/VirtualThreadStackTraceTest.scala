package org.scalanative.testsuite.javalib.lang

import java.util.concurrent.atomic.AtomicReference

import org.junit.Assert._
import org.junit._

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadStackTraceTest {
  @BeforeClass def checkRuntime(): Unit = {
    AssumesHelper.assumeSupportsVirtualThreads()
    AssumesHelper.assumeSupportsStackTraces()
  }
}

class VirtualThreadStackTraceTest {
  private val Timeout = 5000L

  @Test def exceptionStackTraceOmitsCarrierFrames(): Unit = {
    val traceRef = new AtomicReference[Array[StackTraceElement]]()

    runOnVirtualThread(Timeout) {
      traceRef.set(captureExceptionStackTrace())
    }

    val trace = traceRef.get()
    assertNotNull(trace)
    assertTrue("stack trace should not be empty", trace.nonEmpty)
    // User frame: exact method name or class (Scala Native may mangle method names)
    val hasUserFrame = trace.exists { elem =>
      val method = elem.getMethodName()
      val clazz = elem.getClassName()
      (method != null && (method == "captureExceptionStackTrace" || method
        .contains("captureExceptionStackTrace"))) ||
        (clazz != null && clazz.contains("VirtualThreadStackTraceTest"))
    }
    assertTrue(
      "stack trace should contain the user helper frame",
      hasUserFrame
    )
    assertFalse(
      "exception stack trace should not expose ForkJoin carrier frames",
      trace.exists(_.getClassName() == "java.util.concurrent.ForkJoinPool")
    )
  }

  private def captureExceptionStackTrace(): Array[StackTraceElement] =
    new Exception("vt stack").getStackTrace()
}
