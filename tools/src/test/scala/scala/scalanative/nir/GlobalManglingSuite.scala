package scala.scalanative
package nir

import org.scalatest._
import org.scalatest.funsuite.AnyFunSuite
import Sig.Scope.Private

class GlobalManglingSuite extends AnyFunSuite {
  Seq(
    Global.Top("foo"),
    Global.Top("foo.bar.Baz"),
    Global.Top("1"),
    Global.Top("-1bar"),
    Global.Member(Global.Top("1"), Sig.Field("2")),
    Global.Member(Global.Top("-1bar"), Sig.Field("-2foo")),
    Global.Member(Global.Top("foo"), Sig.Field("field")),
    Global.Member(Global.Top("foo"),
                  Sig.Field("field", Private(Global.Top("foo")))),
    Global.Member(Global.Top("foo"), Sig.Ctor(Seq.empty)),
    Global.Member(Global.Top("foo"), Sig.Ctor(Seq(Type.Int))),
    Global.Member(Global.Top("foo"), Sig.Method("bar", Seq(Type.Unit))),
    Global.Member(
      Global.Top("foo"),
      Sig.Method("bar", Seq(Type.Unit), Private(Global.Top("foo")))),
    Global.Member(
      Global.Top("foo"),
      Sig.Method("bar", Seq(Type.Int, Type.Unit), Private(Global.Top("foo")))),
    Global.Member(Global.Top("foo"),
                  Sig.Method("bar", Seq(Type.Int, Type.Unit))),
    Global.Member(Global.Top("foo"), Sig.Proxy("bar", Seq(Type.Int))),
    Global.Member(Global.Top("foo"), Sig.Proxy("bar", Seq(Type.Int, Type.Int))),
    Global.Member(Global.Top("foo"), Sig.Extern("malloc")),
    Global.Member(Global.Top("foo"), Sig.Generated("type"))
  ).foreach { g =>
    test(s"mangle/unmangle global `${g.toString}`") {
      val mangled = g.mangle
      assert(mangled.nonEmpty, "empty mangle")
      val unmangled = Unmangle.unmangleGlobal(mangled)
      assert(unmangled == g, "different unmangle")
      val remangled = unmangled.mangle
      assert(mangled == remangled, "different remangle")
    }
  }
}
