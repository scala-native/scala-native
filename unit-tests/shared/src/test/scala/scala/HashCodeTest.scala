package scala

import org.junit.Test
import org.junit.Assert._
import scala.scalanative.buildinfo.ScalaNativeBuildInfo.scalaVersion

class HashCodeTest {
  case class MyData(string: String, num: Int)

  def scala212orOlder: Boolean = {
    scalaVersion.startsWith("2.11.") ||
    scalaVersion.startsWith("2.12.")
  }

  @Test def hashCodeOfStringMatchesScalaJVM(): Unit = {
    assertTrue("hello".hashCode == 99162322)
  }

  @Test def hashCodeOfCaseClassMatchesScalaJVM(): Unit = {
    val expectedHashCode =
      if (scala212orOlder) -1824015247
      else -715875225
    assertTrue(MyData("hello", 12345).hashCode == expectedHashCode)
  }

}
