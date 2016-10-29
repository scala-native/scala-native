package tests

import java.lang.System.exit

object Main {
  def main(args: Array[String]): Unit = {
    // tests.Discover object is code-generated in the sbt build
    val suites = tests.Discover.suites
    var failed = false
    suites.foreach { suite =>
      val result = suite.run()
      failed = failed || result
    }
    exit(if (failed) 1 else 0)
  }
}
