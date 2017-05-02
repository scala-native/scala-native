package tests

import java.lang.System.exit

import scala.scalanative.SyscallsSuite

object Main {
  def main(args: Array[String]): Unit = {
    // tests.Discover object is code-generated in the sbt build
    val suites = tests.Discover.suites
      .filter(_.getClass.getName.contains(args.lift(0).getOrElse("")))

    var success = true

    suites.foreach { suite =>
      val suiteSuccess = suite.run()
      success = success && suiteSuccess
    }

    if (!success) {
      println("Some tests failed. See above for details.")
    }

    exit(if (success) 0 else 1)
  }
}
