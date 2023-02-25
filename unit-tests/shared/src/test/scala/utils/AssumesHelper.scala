package scala.scalanative.junit.utils

import org.junit.Assume
import org.scalanative.testsuite.utils.Platform

object AssumesHelper {
  def assumeNotJVMCompliant(): Unit =
    Assume.assumeFalse("Not compliant with JDK", Platform.executingInJVM)

  def assumeNot32Bit(): Unit = if (!Platform.executingInJVM) {
    Assume.assumeFalse(
      "Not compliant on 32-bit platforms",
      Platform.is32BitPlatform
    )
  }

  def assumeNotASAN(): Unit = if (!Platform.executingInJVM) {
    Assume.assumeFalse(
      "Not compliant with Address Sanitizer",
      Platform.asanEnabled
    )
  }

  def assumeMultithreadingIsEnabled(): Unit =
    Assume.assumeTrue(
      "Requires multithreaded runtime",
      Platform.isMultithreadingEnabled
    )

  def assumeSupportsStackTraces() = {
    // libunwind does not work with AddressSanitizer
    assumeNotASAN()
  }

  def assumeNotExecutedInForkJoinPool() = {
    Assume.assumeFalse(
      "SN executes all tests using ForkJoinPool based executor in multithreading mode",
      Platform.executingInScalaNative && Platform.isMultithreadingEnabled
    )
  }
}
