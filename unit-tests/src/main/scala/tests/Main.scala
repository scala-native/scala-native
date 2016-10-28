package tests

import java.lang.System.exit

object Main {
  def main(args: Array[String]): Unit = {
    // tests.Discover object is code-generated in the sbt build
    if (!tests.Discover.suites.forall(_.run)) exit(1) else exit(0)
  }
}
