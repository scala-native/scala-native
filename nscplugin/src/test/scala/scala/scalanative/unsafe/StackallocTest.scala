package scala.scalanative.unsafe

import java.nio.file.Files

import org.junit._
import org.junit.Assert._
import org.junit.Assume._

import scala.scalanative.api.CompilationFailedException
import scala.scalanative.linker.StaticForwardersSuite.compileAndLoad
import scala.scalanative.buildinfo.ScalaNativeBuildInfo._
import scala.scalanative.NIRCompiler

class StackallocTest {
  def assumeIsScala3() = assumeTrue(
    "Not possible to express in Scala 2",
    scalaVersion.startsWith("3.")
  )

  val StackallocConcreteType = "Stackalloc requires concrete type"
  @Test def noType(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
          |object Test {
          |  val x = stackalloc()
          |}""".stripMargin
          )
        )
    )
    assertTrue(err.getMessage().contains(StackallocConcreteType))
  }

  @Test def inferredType(): Unit = NIRCompiler(
    _.compile(
      """import scala.scalanative.unsafe._
          |object Test {
          |  val x: Ptr[Int] = stackalloc()
          |  val y: Ptr[Ptr[_]] = stackalloc(10)
          |}""".stripMargin
    )
  )

  @Ignore("Unable to distinguish inlined generic param from non-inlined")
  @Test def genericParamType(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
            |object Test {
            |  def create[T]() = stackalloc[T]()
            |  val x = create[Int]()
            |  val y = create[String]()
            |}""".stripMargin
          )
        )
    )
    assertTrue(err.getMessage().contains(StackallocConcreteType))
  }

  @Test def inlineGenericParamType(): Unit = {
    assumeIsScala3()
    NIRCompiler(
      _.compile(
        """import scala.scalanative.unsafe._
          |object Test {
          |  inline def create[T]() = stackalloc[T]()
          |  val x = create[Int]()
          |  val y = create[String]()
          |}""".stripMargin
      )
    )
  }

  @Test def any(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
          |object Test {
          |  val x = stackalloc[Any](10)
          |}""".stripMargin
          )
        )
    )
    assertTrue(err.getMessage().contains(StackallocConcreteType))
  }

  @Test def nothing(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
          |object Test {
          |  val x = stackalloc[Nothing](10)
          |}""".stripMargin
          )
        )
    )
    assertTrue(err.getMessage().contains(StackallocConcreteType))
  }

  @Test def anyAlias(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
          |object Test {
          |  type A = Any
          |  type B = A
          |  val x = stackalloc[B]()
          |}""".stripMargin
          )
        )
    )
    assertTrue(err.getMessage().contains(StackallocConcreteType))
  }

  @Test def abstractType(): Unit = {
    assumeIsScala3()
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
          |object Test {
          |  type A
          |  val x = stackalloc[A]()
          |}""".stripMargin
          )
        )
    )
    assertTrue(err.getMessage().contains(StackallocConcreteType))
  }

}
