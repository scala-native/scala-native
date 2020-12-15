package scala.scalanative.reporter
import org.scalatest.{Assertion, Assertions}
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec
import scala.scalanative.NIRCompiler

trait NirErrorReporterSpec extends AnyFlatSpec with Matchers with Assertions {
  import scala.scalanative.api

  case class TestCompilerError(pos: Int, msg: String)
      extends api.CompilerError {
    override def getPosition: Integer = pos
    override def getErrorMsg: String  = msg
  }

  def verifyThat(pred: String, suffix: String = "")(
      body: Seq[String] => Unit) = {
    it should s"verify that $pred $suffix" in {
      body(Seq(pred))
    }
  }

  def reportsErrors(source: String)(expected: String*): Assertion = {
    val errors      = NIRCompiler(_.compileAndReport(source))
    val errMsgs     = errors.map(_.getErrorMsg).toSet
    val expectedSet = expected.toSet

    assert((errMsgs -- expectedSet).isEmpty, "underapproximation")
    assert((expectedSet -- errMsgs).isEmpty, "overapproximation")
  }

  def allows(source: String): Assertion =
    reportsErrors(source)()

}
