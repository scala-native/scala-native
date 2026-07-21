package scala.scalanative.linker
import org.junit.Assert._
import org.junit.Test

import scala.scalanative.LinkerSpec

// Tests if given code snippets correctlly links when multihreading is disabled
class SinglethreadCompatibilityTest extends LinkerSpec {
  private def testLinks(source: String): Unit =
    link(
      "Test",
      Map("Test.scala" -> source),
      setupConfig = _.withMultithreading(false)
    ) { (config, result) =>
      ()
    }

  @Test def processWaitFor(): Unit = testLinks(
    """
      |object Test {
      |  def main(args: Array[String]): Unit = {
      |    val exitValue = new java.lang.ProcessBuilder("ls", "-l").start().waitFor()
      |    println("exitValue: " + exitValue)
      |  }
      |}
      |""".stripMargin
  )
  @Test def processWaitForWithTimeout(): Unit = testLinks(
    """
      |object Test {
      |  def main(args: Array[String]): Unit = {
      |    val exitValue = new java.lang.ProcessBuilder("ls", "-l").start().waitFor(1, java.util.concurrent.TimeUnit.SECONDS)
      |    println("exitValue: " + exitValue)
      |  }
      |}
      |""".stripMargin
  )
}
