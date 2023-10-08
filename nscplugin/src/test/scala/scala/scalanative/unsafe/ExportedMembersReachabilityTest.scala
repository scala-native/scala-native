package scala.scalanative.unsafe

import java.nio.file.Files

import org.junit.Test
import org.junit.Assert._

import scala.scalanative.api.CompilationFailedException
import scala.scalanative.nir._
import scala.scalanative.linker.compileAndLoad

class ExportedMembersReachabilityTest {
  val Lib = Global.Top("lib$")

  @Test def generateModuleExportedMethods(): Unit = {
    compileAndLoad(
      "Test.scala" -> s"""
        |import scala.scalanative.unsafe._
        |object lib {
        |  @exported def foo(): Int = 42
        |  @exported("native_function") def bar(v: Int): Long = v + 42L
        |}
        |""".stripMargin
    ) { defns =>
      val expected = Seq(
        Sig.Method("foo", Seq(Type.Int)),
        Sig.Method("bar", Seq(Type.Int, Type.Long)),
        Sig.Extern("foo"),
        Sig.Extern("native_function")
      ).map(Lib.member(_))
      assertTrue(expected.diff(defns.map(_.name)).isEmpty)
    }
  }

  @Test def generateModuleExportedFieldAccessors(): Unit = {
    compileAndLoad(
      "Test.scala" -> s"""
        |import scala.scalanative.unsafe._
        |object lib {
        |  @exportAccessors 
        |  val foo: CString = c"Hello world"
        |  
        |  @exportAccessors("native_constant") 
        |  val bar: Long = 42L
        |}
        |""".stripMargin
    ) { defns =>
      val expected = Seq(
        Sig.Field("foo", Sig.Scope.Private(Lib)),
        Sig.Method("foo", Seq(Rt.BoxedPtr)),
        Sig.Extern("get_foo"),
        Sig.Field("bar", Sig.Scope.Private(Lib)),
        Sig.Method("bar", Seq(Type.Long)),
        Sig.Extern("native_constant")
      ).map(Lib.member(_))
      assertTrue(expected.diff(defns.map(_.name)).isEmpty)
    }
  }

  @Test def generateModuleExportedVariableAccessors(): Unit = {
    compileAndLoad(
      "Test.scala" -> s"""
         |import scala.scalanative.unsafe._
         |object lib {
         |  @exportAccessors 
         |  var foo: CString = c"Hello world"
         |  
         |  @exportAccessors("native_variable") 
         |  var bar: Long = 42L
         |
         |  @exportAccessors("native_get_baz", "native_set_baz")
         |  var baz: Byte = 42.toByte
         |}
         |""".stripMargin
    ) { defns =>
      val expected = Seq(
        // field 1
        Sig.Field("foo"),
        Sig.Method("foo", Seq(Rt.BoxedPtr)),
        Sig.Method("foo_$eq", Seq(Rt.BoxedPtr, Type.Unit)),
        Sig.Extern("get_foo"),
        Sig.Extern("set_foo"),
        // field  2
        Sig.Field("bar"),
        Sig.Method("bar", Seq(Type.Long)),
        Sig.Method("bar_$eq", Seq(Type.Long, Type.Unit)),
        Sig.Extern("native_variable"),
        Sig.Extern("set_bar"),
        // field 3
        Sig.Field("baz"),
        Sig.Method("baz", Seq(Type.Byte)),
        Sig.Method("baz_$eq", Seq(Type.Byte, Type.Unit)),
        Sig.Extern("native_get_baz"),
        Sig.Extern("native_set_baz")
      ).map(Lib.member(_))
      assertTrue(expected.diff(defns.map(_.name)).isEmpty)
    }
  }
}
