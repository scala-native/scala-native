package tests

import java.lang.System.exit

object Main {
  val suites = Seq[Suite](
    tests.SuiteSuite,
    java.lang.IntegerSuite,
    java.lang.FloatSuite,
    java.lang.DoubleSuite,
    java.util.RandomSuite,
    scala.scalanative.native.CStringSuite
  )

  def main(args: Array[String]): Unit = {
    if (!suites.forall(_.run)) exit(1) else exit(0)
  }
}
