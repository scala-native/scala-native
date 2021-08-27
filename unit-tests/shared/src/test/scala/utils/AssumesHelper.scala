package scala.scalanative.junit.utils

import org.junit.Assume
import org.scalanative.testsuite.utils.Platform

object AssumesHelper {
  def assumeNotJVMCompliant(): Unit =
    Assume.assumeFalse("Not compliant with JDK", Platform.executingInJVM)
}
