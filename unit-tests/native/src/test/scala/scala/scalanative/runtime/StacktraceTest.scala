package scala.scalanative.runtime

import org.junit.Test
import org.junit.Assert
import org.junit.Assert._
import scala.scalanative.meta.LinktimeInfo._
import scala.language.implicitConversions
import scala.scalanative.unsigned._
import scala.scalanative.unsafe._

class StackTraceTest {

  @Test
  def foobar(): Unit = {
    val exc = new Exception
    exc.printStackTrace()
  }

}
