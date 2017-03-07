package tests

import java.lang.System.exit

object Main {
  def main(args: Array[String]): Unit = {
    // tests.Discover object is code-generated in the sbt build
<<<<<<< a1c0ad70245c746a849fdc4505c3d23629293e6a
    val suites = tests.Discover.suites
      .filter(_.getClass.getName.contains(args.lift(0).getOrElse("")))

=======
    val suites  = Seq(scala.scalanative.SyscallsSuite)//tests.Discover.suites
>>>>>>> added test for pipe, fork ,write, read and dup
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
