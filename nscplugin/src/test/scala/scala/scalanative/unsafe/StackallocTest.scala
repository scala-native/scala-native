package scala.scalanative.unsafe

import java.nio.file.Files

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import scala.scalanative.api.CompilationFailedException
import scala.scalanative.linker.StaticForwardersSuite.compileAndLoad
import scala.scalanative.buildinfo.ScalaNativeBuildInfo._
import scala.scalanative.NIRCompiler

class StackallocTest {
  val StackallocConcreateType = "Stackalloc requires concreate type"
  @Test def stackallocNoType(): Unit = {
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
    assertTrue(err.getMessage().contains(StackallocConcreateType))
  }

  @Test def stackallocInferedType(): Unit = NIRCompiler(
    _.compile(
      """import scala.scalanative.unsafe._
          |object Test {
          |  val x: Ptr[Int] = stackalloc()
          |  val y: Ptr[Ptr[_]] = stackalloc(10)
          |}""".stripMargin
    )
  )

  @Test def stackallocAny(): Unit = {
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
    assertTrue(err.getMessage().contains(StackallocConcreateType))
  }

  @Test def stackallocNothing(): Unit = {
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
    assertTrue(err.getMessage().contains(StackallocConcreateType))
  }

  @Test def stackallocAnyAlias(): Unit = {
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
    assertTrue(err.getMessage().contains(StackallocConcreateType))
  }

  @Test def stackallocAbstractType(): Unit = {
    assumeTrue(
      "Not possible to express in Scala 2",
      scalaVersion.startsWith("3.")
    )
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
    assertTrue(err.getMessage().contains(StackallocConcreateType))
  }

}
