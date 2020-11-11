package scala

import org.junit.Test
import org.junit.Assert._
import scala.scalanative.buildinfo.ScalaNativeBuildInfo

class HashCodeTest {
  case class MyData(string: String, num: Int)

  def scala212orOlder: Boolean =
    ScalaNativeBuildInfo.scalaVersion
      .split('.')
      .take(3)
      .map(_.toInt) match {
      case Array(2, n, _) if n <= 12 => true
      case _                         => false
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
