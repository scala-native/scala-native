package scala.scalanative.linker

import scala.scalanative.checker.Check
import scala.scalanative.LinkerSpec

import org.scalatest.matchers.should._

class IssuesSpec extends LinkerSpec with Matchers {
  private val mainClass = "Test"
  private val sourceFile = "Test.scala"

  private def testLinked(source: String, mainClass: String = mainClass)(
      fn: Result => Unit
  ): Unit =
    link(mainClass, sources = Map("Test.scala" -> source)) {
      case (_, result) => fn(result)
    }

  private def checkNoLinkageErrors(
      source: String,
      mainClass: String = mainClass
  ) =
    testLinked(source.stripMargin, mainClass) { result =>
      val erros = Check(result)
      erros shouldBe empty
    }

  "Issue #2790" should "link main classes using encoded characters" in {
    // All encoded character and an example of unciode encode character ';'
    val packageName = "foo.`b~a-r`.`b;a;z`"
    val mainClass = raw"Test-native~=<>!#%^&|*/+-:'?@;sc"
    val fqcn = s"$packageName.$mainClass".replace("`", "")
    checkNoLinkageErrors(
      mainClass = fqcn,
      source = s"""package $packageName
      |object `$mainClass`{ 
      |  def main(args: Array[String]) = () 
      |}
      |""".stripMargin
    )
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
