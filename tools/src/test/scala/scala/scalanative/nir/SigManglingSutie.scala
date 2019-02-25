package scala.scalanative
package nir

import org.scalatest._

class SigManglingSuite extends FunSuite {
  Seq(
    Sig.Field("f"),
    Sig.Field("len"),
    Sig.Field("field"),
    Sig.Ctor(Seq.empty),
    Sig.Ctor(Seq(Type.Int)),
    Sig.Ctor(Seq(Rt.Object, Type.Int)),
    Sig.Method("bar", Seq()),
    Sig.Method("bar", Seq(Type.Unit)),
    Sig.Method("bar", Seq(Type.Int, Type.Unit)),
    Sig.Proxy("bar", Seq()),
    Sig.Proxy("bar", Seq(Type.Int)),
    Sig.Proxy("bar", Seq(Type.Int, Type.Int)),
    Sig.Extern("read"),
    Sig.Extern("malloc"),
    Sig.Generated("layout"),
    Sig.Generated("type"),
    Sig.Duplicate(Sig.Method("bar", Seq()), Seq()),
    Sig.Duplicate(Sig.Method("bar", Seq(Type.Unit)), Seq(Type.Unit))
  ).foreach { sig =>
    test(s"mangle/unmangle sig `${sig.toString}`") {
      val mangled = sig.mangle
      assert(mangled.nonEmpty, "empty mangle")
      val unmangled = Unmangle.unmangleSig(mangled)
      assert(unmangled == sig, "different unmangle")
      val remangled = unmangled.mangle
      assert(mangled == remangled, "different remangle")
    }
  }
}
