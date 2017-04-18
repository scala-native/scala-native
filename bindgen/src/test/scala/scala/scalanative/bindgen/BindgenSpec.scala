package scala.scalanative

//import scala.language.implicitConversions
import org.scalatest._
import scala.sys.process._

class BindgenSpec extends FlatSpec with Matchers {

  def run(args: String*) = {
    val bin =
      sys.props.get("native.bin").getOrElse(sys.error("native.bin is not set"))
    Process(Seq(bin) ++ args).lines.mkString("\n")
  }

  "Bindgen" should "generate stub for test.h" in {
    val source = run("src/test/resources/test.h",
                     "Test",
                     "scala.scalanative.bindgen.test")
    val expected =
      """package scala.scalanative.bindgen.test
        |
        |import scala.scalanative.native._
        |
        |@extern
        |object Test {
        |  object color {
        |    val RED = 1
        |    val GREEN = 2
        |    val BLUE = 100
        |  }
        |  type SomeInt = CInt
        |  def strchr(s: CString, c: CInt): CString = extern
        |}""".stripMargin

    assert(source == expected)
  }
}
