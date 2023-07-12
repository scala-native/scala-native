package scala.scalanative

import java.nio.file.Files

import org.junit.Test
import org.junit.Assert._

import scala.scalanative.api.CompilationFailedException
import scala.scalanative.linker.StaticForwardersSuite.compileAndLoad
import scala.scalanative.nir.*

class NIRCompilerTest3 {
  inline def nativeCompilation(source: String): Unit = {
    try scalanative.NIRCompiler(_.compile(source))
    catch {
      case ex: CompilationFailedException =>
        fail(s"Failed to compile source: $ex")
    }
  }

  @Test def topLevelExternMethods(): Unit = nativeCompilation(
    """
      |import scala.scalanative.unsafe.extern
      |
      |def foo(): Int = extern
      |""".stripMargin
  )

  @Test def topLevelExternMethodNoResultType(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile("""
        |import scala.scalanative.unsafe.extern
        |
        |def foo() = extern
        |""".stripMargin))
    )
    assertTrue(err.getMessage().contains("extern method foo needs result type"))
  }

  @Test def externInNonExternTopLevelDefn(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile("""
        |import scala.scalanative.unsafe.extern
        |
        |val foo: Int = extern
        |""".stripMargin))
    )
    assertTrue(
      err.getMessage().contains("extern` cannot be used in val definition")
    )
  }

  @Test def topLevelExternVar(): Unit = nativeCompilation(
    """
      |import scala.scalanative.unsafe.extern
      |
      |var foo: Int = extern
      |""".stripMargin
  )

  @Test def topLevelExternVarNoResultType(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile("""
        |import scala.scalanative.unsafe.extern
        |
        |var foo = extern
        |""".stripMargin))
    )
    assertTrue(err.getMessage().contains("extern field foo needs result type"))
  }

  val ErrorBothExternAndExported =
    "Member cannot be defined both exported and extern"

  @Test def topLevelExportedExtern(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile("""
        |import scala.scalanative.unsafe.{extern, exported}
        |
        |@exported
        |def foo: Int = extern
        |""".stripMargin))
    )
    assertTrue(err.getMessage().startsWith(ErrorBothExternAndExported))
  }

  @Test def topLevelExportedAccessorExtern(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile("""
         |import scala.scalanative.unsafe.*
         |
         |@exportAccessors
         |var foo: Int = extern
         |""".stripMargin))
    )
    assertTrue(err.getMessage().startsWith(ErrorBothExternAndExported))
  }

  @Test def topLevelExports(): Unit = {
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
      assertTrue(expected.diff(loaded).isEmpty)
    }
  }

  @Test def inlineCFuncPtrFromScalaFunction(): Unit = nativeCompilation(
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

  @Test def inlineExternFunction(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile("""
           |import scala.scalanative.unsafe.*
           |
           |@extern object Foo{
           |   inline def foo(): Int = extern
           |}
           |""".stripMargin))
    )
    assertTrue(err.getMessage().contains("Extern method cannot be inlined"))
  }

  @Test def inlineExternFunctionInTrait(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile("""
           |import scala.scalanative.unsafe.*
           |
           |@extern trait Foo{
           |   inline def foo(): Int = extern
           |}
           |""".stripMargin))
    )
    assertTrue(err.getMessage().contains("Extern method cannot be inlined"))
  }

  @Test def inlineTopLevelExternFunction(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile("""
           |import scala.scalanative.unsafe.*
           |
           |@extern inline def foo(): Int = extern
           |""".stripMargin))
    )
    assertTrue(err.getMessage().contains("Extern method cannot be inlined"))
  }

  @Test def inlineExportedFunction(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile("""
           |import scala.scalanative.unsafe.*
           |
           |@exported inline def foo(): Int = 42
           |""".stripMargin))
    )
    assertTrue(err.getMessage().contains("Exported method cannot be inlined"))
  }

  @Test def inlineExportedField(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile("""
           |import scala.scalanative.unsafe.*
           |
           |@exportAccessors inline val foo: Int = 42
           |""".stripMargin))
    )
    assertTrue(err.getMessage().contains("Exported field cannot be inlined"))
  }
}
