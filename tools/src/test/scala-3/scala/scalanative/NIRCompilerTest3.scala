package scala.scalanative

import java.nio.file.Files

import org.scalatest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

import scala.scalanative.api.CompilationFailedException

class NIRCompilerTest3 extends AnyFlatSpec with Matchers with Inspectors {
  def nativeCompilation(source: String): Unit = {
    try scalanative.NIRCompiler(_.compile(source))
    catch {
      case ex: CompilationFailedException =>
        fail(s"Failed to compile source: ${ex.getMessage}", ex)
    }
  }

  "The compiler" should "allow to define top level extern methods" in nativeCompilation(
    """
      |import scala.scalanative.unsafe.extern
      |
      |def foo(): Int = extern
      |""".stripMargin
  )

  it should "report error for top-level extern method without result type" in {
    intercept[CompilationFailedException] {
      NIRCompiler(_.compile("""
        |import scala.scalanative.unsafe.extern
        |
        |def foo() = extern
        |""".stripMargin))
    }.getMessage should include("extern method foo needs result type")
  }

  it should "allow to define top level extern variable" in nativeCompilation(
    """
      |import scala.scalanative.unsafe.extern
      |
      |var foo: Int = extern
      |""".stripMargin
  )

  it should "report error for top-level extern variable without result type" in {
    intercept[CompilationFailedException] {
      NIRCompiler(_.compile("""
        |import scala.scalanative.unsafe.extern
        |
        |var foo = extern
        |""".stripMargin))
    }.getMessage should include("extern field foo needs result type")
  }

}
