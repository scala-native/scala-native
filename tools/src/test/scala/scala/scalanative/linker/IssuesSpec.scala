package scala.scalanative.linker

import scala.scalanative.checker.Check
import scala.scalanative.LinkerSpec

import org.scalatest.matchers.should._

class IssuesSpec extends LinkerSpec with Matchers {
  private val mainClass = "Test$"
  private val sourceFile = "Test.scala"

  private def testLinked(source: String)(fn: Result => Unit): Unit =
    link("Test", sources = Map("Test.scala" -> source)) {
      case (_, result) => fn(result)
    }

  private def checkNoLinkageErrors(source: String) =
    testLinked(source.stripMargin) { result =>
      val erros = Check(result)
      erros shouldBe empty
    }

  "Issue #2880" should "handle lambas correctly" in checkNoLinkageErrors {
    """
    |object Test {
    |  trait ContextCodec[In, Out] {
    |    def decode(in: In, shouldFailFast: Boolean): Out
    |  }
    |
    |  def lift[In, Out](f: In => Out): ContextCodec[In, Out] =
    |    (in, shouldFailFast) => f(in)
    |
    |  def main(args: Array[String]): Unit = {
    |    lift[Any, Any](_ => ???).decode("foo", false)
    |  }
    |}
    |"""
  }

}
