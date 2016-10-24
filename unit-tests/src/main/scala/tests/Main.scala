package tests

import java.lang.System.exit

object Main {
  val suites = Seq[Suite](
      tests.SuiteSuite,
      java.lang.IntegerSuite,
      java.lang.LongSuite,
      java.lang.FloatSuite,
      java.lang.DoubleSuite,
      java.util.RandomSuite,
      scala.scalanative.issues._260,
      scala.scalanative.issues._314,
      scala.scalanative.issues._337,
      scala.scalanative.issues._350,
      scala.scalanative.native.CStringSuite,
      scala.scalanative.native.CInteropSuite,
      scala.scalanative.native.InstanceOfSuite,
      scala.ArrayIntCopySuite,
      scala.ArrayDoubleCopySuite,
      scala.ArrayObjectCopySuite,
      scala.EqualitySuite
  )

  def main(args: Array[String]): Unit = {
    if (!suites.forall(_.run)) exit(1) else exit(0)
  }
}
