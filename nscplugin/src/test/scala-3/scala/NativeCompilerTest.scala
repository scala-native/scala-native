package org.scalanative

import org.junit.Test
import org.junit.Assert._

import scala.scalanative.api._
import scala.scalanative.util.Scope
import scala.scalanative.io.VirtualDirectory
import java.nio.file.Files

class NativeCompilerTest:

  def nativeCompilation(source: String): Unit = {
    try scalanative.NIRCompiler(_.compile(source))
    catch {
      case ex: CompilationFailedException =>
        fail(s"Failed to compile source: ${ex}")
    }
  }

  def compileAll(sources: (String, String)*): Unit = {
    Scope { implicit in =>
      val outDir = Files.createTempDirectory("native-test-out")
      val compiler = scalanative.NIRCompiler.getCompiler(outDir)
      val sourcesDir = scalanative.NIRCompiler.writeSources(sources.toMap)
      val dir = VirtualDirectory.real(outDir)

      try scalanative.NIRCompiler(_.compile(sourcesDir))
      catch {
        case ex: CompilationFailedException =>
          fail(s"Failed to compile source: $ex")
      }
    }
  }

  @Test def issue8612(): Unit = nativeCompilation(
    """
    |object Foo1:
    |  def assert1(x: Boolean) = if !x then ???
    |  inline def assert2(x: Boolean) = if !x then ???
    |  inline def assert3(inline x: Boolean) = if !x then ???
    |
    |  assert1(???)
    |  assert2(???)
    |  assert3(???)
    |
    |object Foo2:
    |  def assert1(x: Boolean) = if !x then ???
    |  transparent inline def assert2(x: Boolean) = if !x then ???
    |  transparent inline def assert3(inline x: Boolean) = if !x then ???
    |
    |  assert1(???)
    |  assert2(???)
    |  assert3(???)
    |""".stripMargin
  )

  @Test def issue505(): Unit = nativeCompilation("""
  |object Test {
  |  def main(args: Array[String]): Unit = {
  |    val a: Int = synchronized(1)
  |    val b: Long = synchronized(1L)
  |    val c: Boolean = synchronized(true)
  |    val d: Float = synchronized(1f)
  |    val e: Double = synchronized(1.0)
  |    val f: Byte = synchronized(1.toByte)
  |    val g: Char = synchronized('1')
  |    val h: Short = synchronized(1.toShort)
  |    val i: String = synchronized("Hello")
  |    val j: List[Int] = synchronized(List(1))
  |    synchronized(())
  |  }
  |}
  """.stripMargin)

  // Reproducer for https://github.com/typelevel/shapeless-3/pull/61#discussion_r779376350
  @Test def inlineMacroWithLazyVals(): Unit = {
    compileAll(
      "Test.scala" -> "@main def run(): Unit = Macros.foo()",
      "Macros.scala" -> """
        |import scala.quoted.*
        |object Macros:
        |  def foo_impl()(using q: Quotes): Expr[Unit] = '{
        |     ${val x = ReflectionUtils(quotes).Mirror(); '{()} }
        |     println()
        |   }
        |
        |  inline def foo(): Unit = ${foo_impl()}
        |end Macros
        |
        |class ReflectionUtils[Q <: Quotes](val q: Q) {
        |  given q.type = q // Internally defined as lazy val, leading to problems
        |  import q.reflect._
        |
        |  case class Mirror(arg: String)
        |  object Mirror{
        |    def apply(): Mirror = Mirror("foo")
        |  }
        |}""".stripMargin
    )
  }
