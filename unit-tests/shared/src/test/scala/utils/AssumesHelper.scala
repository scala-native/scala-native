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
    // On Windows linking with LTO Full does not provide debug symbols, even
    // if flag -g is used. Becouse of that limitation StackTraces do not work.
    // If env variable exists and is set to true don't run tests in this file
    Assume.assumeFalse(
      "StackTrace tests not available in the current build",
      sys.env.get("SCALANATIVE_CI_NO_DEBUG_SYMBOLS").exists(_.toBoolean)
    )

    // libunwind does not work with AddressSanitizer
    assumeNotASAN()
  }
}
