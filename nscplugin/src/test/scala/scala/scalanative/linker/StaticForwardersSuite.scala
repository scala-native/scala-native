package scala.scalanative.linker

import org.junit.Test
import org.junit.Assert._

import scala.scalanative.nir._
import scala.scalanative.util.Scope

class StaticForwardersSuite {

  @Test def generateStaticForwarders(): Unit = {
    compileAndLoad(
      "Test.scala" ->
        """ 
          |class Foo() {
          |  def foo(): String = {
          |    Foo.bar() + Foo.fooBar
          |  }
          |}
          |object Foo {
          |  def main(args: Array[String]): Unit  = {
          |    val x = new Foo().foo()
          |  }
          |  def bar(): String = "bar"
          |  def fooBar: String = "foo" + bar()
          |}
          """.stripMargin
    ) { defns =>
      val Class = Global.Top("Foo")
      val Module = Global.Top("Foo$")
      val expected = Seq(
        Class.member(Sig.Ctor(Nil)),
        Class.member(Sig.Method("foo", Seq(Rt.String))),
        Class.member(Sig.Method("bar", Seq(Rt.String), Sig.Scope.PublicStatic)),
        Class.member(
          Sig.Method("fooBar", Seq(Rt.String), Sig.Scope.PublicStatic)
        ),
        Class.member(Rt.ScalaMainSig),
        Module.member(Sig.Ctor(Nil)),
        Module.member(Sig.Method("bar", Seq(Rt.String))),
        Module.member(Sig.Method("fooBar", Seq(Rt.String))),
        Module.member(
          Sig.Method("main", Rt.ScalaMainSig.types, Sig.Scope.Public)
        )
      )
      assertTrue(expected.diff(defns.map(_.name)).isEmpty)
    }
  }

  @Test def generateStaticAccessor(): Unit = {
    compileAndLoad(
      "Test.scala" ->
        """ 
          |class Foo() {
          |  val foo = "foo"
          |}
          |object Foo {
          |  val bar = "bar"
          |}
          """.stripMargin
    ) { defns =>
      val Class = Global.Top("Foo")
      val Module = Global.Top("Foo$")
      val expected = Seq(
        Class.member(Sig.Field("foo", Sig.Scope.Private(Class))),
        Class.member(Sig.Method("foo", Seq(Rt.String))),
        Class.member(Sig.Method("bar", Seq(Rt.String), Sig.Scope.PublicStatic)),
        Module.member(Sig.Field("bar", Sig.Scope.Private(Module))),
        Module.member(Sig.Method("bar", Seq(Rt.String)))
      )
      assertTrue(expected.diff(defns.map(_.name)).isEmpty)
    }
  }
}
