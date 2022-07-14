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
}
