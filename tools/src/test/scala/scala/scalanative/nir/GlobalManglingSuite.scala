package scala.scalanative
package nir

import org.scalatest._

class GlobalManglingSuite extends FunSuite {
  Seq(
    Global.Top("foo"),
    Global.Top("foo.bar.Baz"),
    Global.Member(Global.Top("foo"), Sig.Field("field")),
    Global.Member(Global.Top("foo"), Sig.Ctor(Seq.empty)),
    Global.Member(Global.Top("foo"), Sig.Ctor(Seq(Type.Int))),
    Global.Member(Global.Top("foo"), Sig.Method("bar", Seq(Type.Unit))),
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
