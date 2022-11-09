package scala.scalanative.unsafe

import java.nio.file.Files

import org.scalatest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

import scala.scalanative.api.CompilationFailedException
import scala.scalanative.linker.ReachabilitySuite
import scala.scalanative.nir._
import scala.scalanative.NIRCompiler

class ExportedMembersTest extends AnyFlatSpec with Matchers {
  val NonModuleStaticError =
    "Exported members must be statically reachable, definition within class or trait is currently unsupported"
  val NonPublicMethod = "Exported members needs to be defined in public scope"
  val DuplicatedNames = "dupl"
  val IncorrectAccessorAnnotation =
    "Cannot export field, use `@exportAccessors()` annotation to generate external accessors"
  val IncorrectMethodAnnotation =
    "Incorrect annotation found, to export method use `@exported` annotation"

  "The compiler" should "report error when exporting class method" in {
    intercept[CompilationFailedException] {
      NIRCompiler(
        _.compile(
          """import scala.scalanative.unsafe._
          |class ExportInClass() {
          |  @exported
          |  def foo(l: Int): Int = 42
          |}""".stripMargin
        )
      )
    }.getMessage should include(NonModuleStaticError)
  }

  it should "report error when exporting non static module method" in {
    intercept[CompilationFailedException] {
      NIRCompiler(
        _.compile(
          """import scala.scalanative.unsafe._
          |class Wrapper() {
          | object inner {
          |   @exported
          |   def foo(l: Int): Int = 42
          | }
          |}""".stripMargin
        )
      )
    }.getMessage should include(NonModuleStaticError)
  }

  it should "report error when exporting private method" in {
    intercept[CompilationFailedException] {
      NIRCompiler(
        _.compile(
          """import scala.scalanative.unsafe._
          |object lib {
          | @exported private def foo(l: Int): Int = 42
          |}""".stripMargin
        )
      )
    }.getMessage should include(NonPublicMethod)
  }

  it should "report error when exporting private field" in {
    intercept[CompilationFailedException] {
      NIRCompiler(
        _.compile(
          """import scala.scalanative.unsafe._
         |object lib {
         | @exportAccessors 
         | private val foo: Int = 42
         | 
         | // Without this in Scala 3 foo would be defined as val in <init> method
         | def bar = this.foo
         |}""".stripMargin
        )
      )
    }.getMessage should include(NonPublicMethod)
  }

  it should "report error when exporting protected field" in {
    intercept[CompilationFailedException] {
      NIRCompiler(
        _.compile(
          """import scala.scalanative.unsafe._
         |object lib {
         | @exportAccessors protected val foo: Int = 42
         |}""".stripMargin
        )
      )
    }.getMessage should include(NonPublicMethod)
  }

  it should "report error when exporting private variable" in {
    intercept[CompilationFailedException] {
      NIRCompiler(
        _.compile(
          """import scala.scalanative.unsafe._
          |object lib {
          | @exportAccessors protected var foo: Int = 42
          |}""".stripMargin
        )
      )
    }.getMessage should include(NonPublicMethod)
  }

  it should "report error when exporting protected variable" in {
    intercept[CompilationFailedException] {
      NIRCompiler(
        _.compile(
          """import scala.scalanative.unsafe._
          |object lib {
          | @exportAccessors protected var foo: Int = 42
          |}""".stripMargin
        )
      )
    }.getMessage should include(NonPublicMethod)
  }

  it should "report error when exporting protected method" in {
    intercept[CompilationFailedException] {
      NIRCompiler(
        _.compile(
          """import scala.scalanative.unsafe._
         |object lib {
         | @exported protected def foo(l: Int): Int = 42
         |}""".stripMargin
        )
      )
    }.getMessage should include(NonPublicMethod)
  }

  it should "report error when exporting duplicated names" in {
    intercept[CompilationFailedException] {
      NIRCompiler(
        _.compile(
          """import scala.scalanative.unsafe._
         |object lib {
         | @exported def foo(l: Int): Int = 42
         | @exported("foo") def bar(r: Int): Int = r
         |}""".stripMargin
        )
      )
    }.getMessage should include(DuplicatedNames)
  }

  it should "report error when exporting accessor with incorrect annotation" in {
    intercept[CompilationFailedException] {
      NIRCompiler(
        _.compile(
          """import scala.scalanative.unsafe._
         |object lib {
         | @exported val foo: Int = 42
         |}""".stripMargin
        )
      )
    }.getMessage should include(IncorrectAccessorAnnotation)
  }

  it should "report error when exporting method with incorrect annotation" in {
    intercept[CompilationFailedException] {
      NIRCompiler(
        _.compile(
          """import scala.scalanative.unsafe._
          |object lib {
          | @exportAccessors def foo(): Int = 42
          |}""".stripMargin
        )
      )
    }.getMessage should include(IncorrectMethodAnnotation)
  }

}
