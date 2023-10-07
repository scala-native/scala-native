package scala.scalanative.linker

import org.junit.Test
import org.junit.Assert._

import scala.scalanative.nir._
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
      val ExternModule = Global.Top("Foo$")
      val ImplicitClass = Global.Top("Foo$Ext")
      val expected: Seq[Global] = Seq(
        ExternModule,
        // All ExternModule members shall be extern with exception of Ext implicit class constructor
        ExternModule.member(Sig.Extern("doConvert")),
        ExternModule.member(Sig.Extern("implicitConvert")),
        ExternModule.member(
          Sig.Method("Ext", Seq(Type.Int, Type.Ref(ImplicitClass)))
        ),
        ImplicitClass,
        ImplicitClass.member(Sig.Method("convert", Seq(Type.Long))),
        ImplicitClass.member(Sig.Method("add", Seq(Type.Int, Type.Int)))
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
      val ExternModule = Global.Top("Foo$")
      val ImplicitClass = Global.Top("Foo$Ext")
      val ImplicitModule = Global.Top("Foo$Ext$")
      val expected: Seq[Global] = Seq(
        ExternModule,
        // All ExternModule members shall be extern with exception of Ext implicit class constructor
        ExternModule.member(Sig.Extern("doConvert")),
        ExternModule.member(Sig.Extern("implicitConvert")),
        ExternModule.member(Sig.Method("Ext", Seq(Type.Int, Type.Int))),
        ImplicitClass,
        ImplicitClass.member(Sig.Method("convert", Seq(Type.Long))),
        ImplicitClass.member(Sig.Method("add", Seq(Type.Int, Type.Int))),
        ImplicitModule,
        ImplicitModule.member(
          Sig.Method("convert$extension", Seq(Type.Int, Type.Long))
        ),
        ImplicitModule.member(
          Sig.Method("add$extension", Seq.fill(3)(Type.Int))
        )
      )
      assertEquals(Set.empty, expected.diff(defns.map(_.name)).toSet)
    }
  }

}
