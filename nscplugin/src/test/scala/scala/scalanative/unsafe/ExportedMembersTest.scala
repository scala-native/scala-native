package scala.scalanative.unsafe

import java.nio.file.Files

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.NIRCompiler
import scala.scalanative.api.CompilationFailedException

class ExportedMembersTest {

  val NonModuleStaticError =
    "Exported members must be statically reachable, definition within class or trait is currently unsupported"
  val NonPublicMethod = "Exported members needs to be defined in public scope"
  val DuplicatedNames = "dupl"
  val IncorrectAccessorAnnotation =
    "Cannot export field, use `@exportAccessors()` annotation to generate external accessors"
  val IncorrectMethodAnnotation =
    "Incorrect annotation found, to export method use `@exported` annotation"

  @Test def exportingClassMethod(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
          |class ExportInClass() {
          |  @exported
          |  def foo(l: Int): Int = 42
          |}""".stripMargin
          )
        )
    )
    assertTrue(err.getMessage().contains(NonModuleStaticError))
  }

  @Test def exportingNonStaticModuleMethod(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
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
    )
    assertTrue(err.getMessage().contains(NonModuleStaticError))
  }

  @Test def exportingPrivateMethod(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
          |object lib {
          | @exported private def foo(l: Int): Int = 42
          |}""".stripMargin
          )
        )
    )
    assertTrue(err.getMessage().contains(NonPublicMethod))
  }

  @Test def exportingPrivateField(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
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
    )
    assertTrue(err.getMessage().contains(NonPublicMethod))
  }

  @Test def exportingProtectedField(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
         |object lib {
         | @exportAccessors protected val foo: Int = 42
         |}""".stripMargin
          )
        )
    )
    assertTrue(err.getMessage().contains(NonPublicMethod))
  }

  @Test def exportingPrivateVariable(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
          |object lib {
          | @exportAccessors protected var foo: Int = 42
          |}""".stripMargin
          )
        )
    )
    assertTrue(err.getMessage().contains(NonPublicMethod))
  }

  @Test def exportingProtectedVariable(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
          |object lib {
          | @exportAccessors protected var foo: Int = 42
          |}""".stripMargin
          )
        )
    )
    assertTrue(err.getMessage().contains(NonPublicMethod))
  }

  @Test def exportingProtectedMethod(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
         |object lib {
         | @exported protected def foo(l: Int): Int = 42
         |}""".stripMargin
          )
        )
    )
    assertTrue(err.getMessage().contains(NonPublicMethod))
  }

  @Test def exportingDuplicatedNamed(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
         |object lib {
         | @exported def foo(l: Int): Int = 42
         | @exported("foo") def bar(r: Int): Int = r
         |}""".stripMargin
          )
        )
    )
    assertTrue(err.getMessage().contains(DuplicatedNames))
  }

  @Test def exportingAccessorWithWrongAnnotation(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
         |object lib {
         | @exported val foo: Int = 42
         |}""".stripMargin
          )
        )
    )
    assertTrue(err.getMessage().contains(IncorrectAccessorAnnotation))
  }

  @Test def exportingMethodWithWrongAnnotation(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
          |object lib {
          | @exportAccessors def foo(): Int = 42
          |}""".stripMargin
          )
        )
    )
    assertTrue(err.getMessage().contains(IncorrectMethodAnnotation))
  }

}
