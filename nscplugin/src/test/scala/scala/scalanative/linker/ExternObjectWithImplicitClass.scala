package scala.scalanative
package linker

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.util.Scope

class ExternObjectWithImplicitClass {

  @Test def createsValidDefns(): Unit = {
    compileAndLoad(
      "Test.scala" ->
        """import scala.scalanative.unsafe.extern
          |@extern object Foo {
          |  implicit class Ext(v: Int) {
          |    def convert(): Long = Foo.implicitConvert(v) + Foo.doConvert(v)
          |    def add(x: Int) = v + x
          |  }
          |  implicit def implicitConvert(v: Int): Long = extern
          |  def doConvert(v: Int): Long = extern
          |}
          |""".stripMargin
    ) { defns =>
      val ExternModule = nir.Global.Top("Foo$")
      val ImplicitClass = nir.Global.Top("Foo$Ext")
      val expected: Seq[nir.Global] = Seq(
        ExternModule,
        // All ExternModule members shall be extern with exception of Ext implicit class constructor
        ExternModule.member(nir.Sig.Extern("doConvert")),
        ExternModule.member(nir.Sig.Extern("implicitConvert")),
        ExternModule.member(
          nir.Sig.Method("Ext", Seq(nir.Type.Int, nir.Type.Ref(ImplicitClass)))
        ),
        ImplicitClass,
        ImplicitClass.member(nir.Sig.Method("convert", Seq(nir.Type.Long))),
        ImplicitClass.member(
          nir.Sig.Method("add", Seq(nir.Type.Int, nir.Type.Int))
        )
      )
      assertEquals(Set.empty, expected.diff(defns.map(_.name)).toSet)
    }
  }

  @Test def createsValidDefnsForAnyVal(): Unit = {
    compileAndLoad(
      "Test.scala" ->
        """import scala.scalanative.unsafe.extern
          |@extern object Foo {
          |  implicit class Ext(val v: Int) extends AnyVal {
          |    def convert(): Long = Foo.implicitConvert(v) + Foo.doConvert(v)
          |    def add(x: Int) = v + x
          |  }
          |  implicit def implicitConvert(v: Int): Long = extern
          |  def doConvert(v: Int): Long = extern
          |}
          |""".stripMargin
    ) { defns =>
      val ExternModule = nir.Global.Top("Foo$")
      val ImplicitClass = nir.Global.Top("Foo$Ext")
      val ImplicitModule = nir.Global.Top("Foo$Ext$")
      val expected: Seq[nir.Global] = Seq(
        ExternModule,
        // All ExternModule members shall be extern with exception of Ext implicit class constructor
        ExternModule.member(nir.Sig.Extern("doConvert")),
        ExternModule.member(nir.Sig.Extern("implicitConvert")),
        ExternModule.member(
          nir.Sig.Method("Ext", Seq(nir.Type.Int, nir.Type.Int))
        ),
        ImplicitClass,
        ImplicitClass.member(nir.Sig.Method("convert", Seq(nir.Type.Long))),
        ImplicitClass.member(
          nir.Sig.Method("add", Seq(nir.Type.Int, nir.Type.Int))
        ),
        ImplicitModule,
        ImplicitModule.member(
          nir.Sig.Method("convert$extension", Seq(nir.Type.Int, nir.Type.Long))
        ),
        ImplicitModule.member(
          nir.Sig.Method("add$extension", Seq.fill(3)(nir.Type.Int))
        )
      )
      assertEquals(Set.empty, expected.diff(defns.map(_.name)).toSet)
    }
  }

}
