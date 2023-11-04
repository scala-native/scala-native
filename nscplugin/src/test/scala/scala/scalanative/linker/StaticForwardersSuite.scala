package scala.scalanative
package linker

import org.junit.Test
import org.junit.Assert._

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
      val Class = nir.Global.Top("Foo")
      val Module = nir.Global.Top("Foo$")
      val expected = Seq(
        Class.member(nir.Sig.Ctor(Nil)),
        Class.member(nir.Sig.Method("foo", Seq(nir.Rt.String))),
        Class.member(
          nir.Sig.Method("bar", Seq(nir.Rt.String), nir.Sig.Scope.PublicStatic)
        ),
        Class.member(
          nir.Sig
            .Method("fooBar", Seq(nir.Rt.String), nir.Sig.Scope.PublicStatic)
        ),
        Class.member(nir.Rt.ScalaMainSig),
        Module.member(nir.Sig.Ctor(Nil)),
        Module.member(nir.Sig.Method("bar", Seq(nir.Rt.String))),
        Module.member(nir.Sig.Method("fooBar", Seq(nir.Rt.String))),
        Module.member(
          nir.Sig
            .Method("main", nir.Rt.ScalaMainSig.types, nir.Sig.Scope.Public)
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
      val Class = nir.Global.Top("Foo")
      val Module = nir.Global.Top("Foo$")
      val expected = Seq(
        Class.member(nir.Sig.Field("foo", nir.Sig.Scope.Private(Class))),
        Class.member(nir.Sig.Method("foo", Seq(nir.Rt.String))),
        Class.member(
          nir.Sig.Method("bar", Seq(nir.Rt.String), nir.Sig.Scope.PublicStatic)
        ),
        Module.member(nir.Sig.Field("bar", nir.Sig.Scope.Private(Module))),
        Module.member(nir.Sig.Method("bar", Seq(nir.Rt.String)))
      )
      assertTrue(expected.diff(defns.map(_.name)).isEmpty)
    }
  }
}
