// Results: "-v" at least prints out test name when started.

import scala.scalanative.build._ // in order to tailor nativeConfig

ThisBuild / testOptions +=
  Tests.Argument(TestFrameworks.JUnit, "-a", "-s", "-v")
