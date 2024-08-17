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
    Assume.assumeFalse(
      "NetBSD doesn't work well with unwind, disable stacktrace tests",
      Platform.isNetBSD
    )

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

  def assumeNotExecutedInForkJoinPool() = {
    Assume.assumeFalse(
      "SN executes all tests using ForkJoinPool based executor in multithreading mode",
      Platform.executingInScalaNative && Platform.isMultithreadingEnabled
    )
  }

  def assumeNotCrossCompiling() = {
    Assume.assumeFalse(
      "Ignore when running in emulated mode",
      Seq("CROSSCOMPILING_EMULATOR", "CROSS_ROOT", "CROSS_TRIPLE").exists(
        sys.env.get(_).isDefined
      )
    )
  }

  def assumeNotRoot() = {
    Assume.assumeFalse(
      "Ignore when running as root user",
      sys.props.get("user.name").forall(_.equalsIgnoreCase("root"))
    )
  }
}
