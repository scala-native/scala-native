package scala.scalanative

import java.nio.file.Files

import org.scalatest.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

import scala.scalanative.api.CompilationFailedException
import scala.scalanative.linker.StaticForwardersSuite.compileAndLoad
import scala.scalanative.nir.*

class NIRCompilerTest3 extends AnyFlatSpec with Matchers with Inspectors {
  inline def nativeCompilation(source: String): Unit = {
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
  it should "report error for extern in top-level val definition" in {
    intercept[CompilationFailedException] {
      NIRCompiler(_.compile("""
        |import scala.scalanative.unsafe.extern
        |
        |val foo: Int = extern
        |""".stripMargin))
    }.getMessage should include("extern` cannot be used in val definition")
  }

  it should "compile top-level extern var definition" in nativeCompilation(
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

  val ErrorBothExternAndExported =
    "Member cannot be defined both exported and extern"

  it should "report error for top-level exported extern" in {
    intercept[CompilationFailedException] {
      NIRCompiler(_.compile("""
        |import scala.scalanative.unsafe.{extern, exported}
        |
        |@exported
        |def foo: Int = extern
        |""".stripMargin))
    }.getMessage should startWith(ErrorBothExternAndExported)
  }

  it should "report error for top-level exported accessor extern" in {
    intercept[CompilationFailedException] {
      NIRCompiler(_.compile("""
         |import scala.scalanative.unsafe.*
         |
         |@exportAccessors
         |var foo: Int = extern
         |""".stripMargin))
    }.getMessage should startWith(ErrorBothExternAndExported)
  }

  it should "all to define top level exports" in {
    compileAndLoad("source.scala" -> """
      |import scala.scalanative.unsafe.*
      |
      |@exported
      |def foo: Int = 42
      |
      |@exportAccessors("my_get_bar")
      |val bar: Long = 42L
      |
      |@exportAccessors("my_get_baz", "my_set_baz")
      |var baz: Byte = 42
      |""".stripMargin) { defns =>
      val Owner = Global.Top("source$package$")
      val expected = Seq(
        Sig.Method("foo", Seq(Type.Int)),
        Sig.Extern("foo"),
        Sig.Field("bar", Sig.Scope.Private(Owner)),
        Sig.Method("bar", Seq(Type.Long)),
        Sig.Extern("my_get_bar"),
        Sig.Field("baz"),
        Sig.Method("baz", Seq(Type.Byte)),
        Sig.Method("baz_$eq", Seq(Type.Byte, Type.Unit)),
        Sig.Extern("my_get_baz"),
        Sig.Extern("my_set_baz")
      ).map(Owner.member(_))

      val loaded = defns.map(_.name)
      assert(expected.diff(loaded).isEmpty)
    }
  }

  it should "allow to inline function passed to CFuncPtr.fromScalaFunction" in nativeCompilation(
    """
        |import scala.scalanative.unsafe.*
        |
        |opaque type Visitor = CFuncPtr1[Int, Int]
        |object Visitor:
        |  inline def apply(inline f: Int => Int): Visitor = f
        |
        |@extern def useVisitor(x: Visitor): Unit = extern
        |
        |@main def test(n: Int): Unit = 
        |  def callback(x: Int) = x*x + 2
        |  val visitor: Visitor = (n: Int) => n * 10
        |  useVisitor(Visitor(callback))
        |  useVisitor(Visitor(_ * 10))
        |  useVisitor(visitor)
        | 
        |""".stripMargin
  )

  it should "report error when inlining extern function" in {
    intercept[CompilationFailedException] {
      NIRCompiler(_.compile("""
           |import scala.scalanative.unsafe.*
           |
           |@extern object Foo{
           |   inline def foo(): Int = extern
           |}
           |""".stripMargin))
    }.getMessage should include("Extern method cannot be inlined")
  }

  it should "report error when inlining extern function in extern trait" in {
    intercept[CompilationFailedException] {
      NIRCompiler(_.compile("""
           |import scala.scalanative.unsafe.*
           |
           |@extern trait Foo{
           |   inline def foo(): Int = extern
           |}
           |""".stripMargin))
    }.getMessage should include("Extern method cannot be inlined")
  }

  it should "report error when inlining extern function in top-level" in {
    intercept[CompilationFailedException] {
      NIRCompiler(_.compile("""
           |import scala.scalanative.unsafe.*
           |
           |@extern inline def foo(): Int = extern
           |""".stripMargin))
    }.getMessage should include("Extern method cannot be inlined")
  }

  it should "report error when inlining exported function" in {
    intercept[CompilationFailedException] {
      NIRCompiler(_.compile("""
           |import scala.scalanative.unsafe.*
           |
           |@exported inline def foo(): Int = 42
           |""".stripMargin))
    }.getMessage should include("Exported method cannot be inlined")
  }

  it should "report error when inlining exported field" in {
    intercept[CompilationFailedException] {
      NIRCompiler(_.compile("""
           |import scala.scalanative.unsafe.*
           |
           |@exportAccessors inline val foo: Int = 42
           |""".stripMargin))
    }.getMessage should include("Exported field cannot be inlined")
  }
}
