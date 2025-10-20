package scala.scalanative
package unsafe

import java.nio.file.Files

import org.junit.Test
import org.junit.Assert.*

import scala.scalanative.api.CompilationFailedException
import scala.scalanative.linker.compileAndLoad

class ExportedMembersReachabilityTest {

  val Lib = nir.Global.Top("lib$")

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
        nir.Sig.Method("foo", Seq(nir.Type.Int)),
        nir.Sig.Method("bar", Seq(nir.Type.Int, nir.Type.Long)),
        nir.Sig.Extern("foo"),
        nir.Sig.Extern("native_function")
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
        nir.Sig.Field("foo", nir.Sig.Scope.Private(Lib)),
        nir.Sig.Method("foo", Seq(nir.Rt.BoxedPtr)),
        nir.Sig.Extern("get_foo"),
        nir.Sig.Field("bar", nir.Sig.Scope.Private(Lib)),
        nir.Sig.Method("bar", Seq(nir.Type.Long)),
        nir.Sig.Extern("native_constant")
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
        nir.Sig.Field("foo"),
        nir.Sig.Method("foo", Seq(nir.Rt.BoxedPtr)),
        nir.Sig.Method("foo_$eq", Seq(nir.Rt.BoxedPtr, nir.Type.Unit)),
        nir.Sig.Extern("get_foo"),
        nir.Sig.Extern("set_foo"),
        // field  2
        nir.Sig.Field("bar"),
        nir.Sig.Method("bar", Seq(nir.Type.Long)),
        nir.Sig.Method("bar_$eq", Seq(nir.Type.Long, nir.Type.Unit)),
        nir.Sig.Extern("native_variable"),
        nir.Sig.Extern("set_bar"),
        // field 3
        nir.Sig.Field("baz"),
        nir.Sig.Method("baz", Seq(nir.Type.Byte)),
        nir.Sig.Method("baz_$eq", Seq(nir.Type.Byte, nir.Type.Unit)),
        nir.Sig.Extern("native_get_baz"),
        nir.Sig.Extern("native_set_baz")
      ).map(Lib.member(_))
      assertTrue(expected.diff(defns.map(_.name)).isEmpty)
    }
  }
}
