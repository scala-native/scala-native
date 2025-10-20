package java.lang.process

import org.junit.Test
import org.junit.Assert.*
import org.junit.Assume.*
import org.junit.Ignore

import scala.scalanative.JavaConverters.*

class WindowsUtilsTest {

  @Test
  def testWindowsArgvToCommandLine(): Unit = {
    val pairs = Seq(
      "foo" -> "foo",
      "" -> """""""",
      "foo bar" -> """"foo bar"""",
      """foo \"bar\"""" -> """"foo \\\"bar\\\""""",
      """a"b""" -> """"a\"b"""",
      """a\"b""" -> """"a\\\"b"""",
      """foo\""" -> """foo\""",
      """"quoted"""" -> """"\"quoted\""""",
      """trailing\backslash\""" -> """trailing\backslash\"""
    )

    val argv = pairs.map(_._1)
    val escaped = pairs.map(_._2)
    val fullCommandLine = escaped.mkString(" ")
    assertEquals(
      escaped,
      argv.map(arg => WindowsUtils.argvToCommand(Iterator(arg).asJava))
    )
    assertEquals(
      fullCommandLine,
      WindowsUtils.argvToCommand(argv.iterator.asJava)
    )
  }

}
