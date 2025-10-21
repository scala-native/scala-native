package scala.scalanative
package nir

import org.junit.Assert._
import org.junit.Test

import Sig.Scope.Private

class GlobalManglingSuite {

  @Test def mangling(): Unit = Seq(
    Global.Top("foo"),
    Global.Top("foo.bar.Baz"),
    Global.Top("1"),
    Global.Top("-1bar"),
    Global.Member(Global.Top("1"), Sig.Field("2")),
    Global.Member(Global.Top("-1bar"), Sig.Field("-2foo")),
    Global.Member(Global.Top("foo"), Sig.Field("field")),
    Global.Member(
      Global.Top("foo"),
      Sig.Field("field", Private(Global.Top("foo")))
    ),
    Global.Member(Global.Top("foo"), Sig.Ctor(Seq.empty)),
    Global.Member(Global.Top("foo"), Sig.Ctor(Seq(Type.Int))),
    Global.Member(Global.Top("foo"), Sig.Method("bar", Seq(Type.Unit))),
    Global.Member(
      Global.Top("foo"),
      Sig.Method("bar", Seq(Type.Unit), Private(Global.Top("foo")))
    ),
    Global.Member(
      Global.Top("foo"),
      Sig.Method("bar", Seq(Type.Int, Type.Unit), Private(Global.Top("foo")))
    ),
    Global.Member(
      Global.Top("foo"),
      Sig.Method("bar", Seq(Type.Int, Type.Unit))
    ),
    Global.Member(Global.Top("foo"), Sig.Proxy("bar", Seq(Type.Int))),
    Global.Member(Global.Top("foo"), Sig.Proxy("bar", Seq(Type.Int, Type.Int))),
    Global.Member(Global.Top("foo"), Sig.Extern("malloc")),
    Global.Member(Global.Top("foo"), Sig.Generated("type"))
  ).foreach { g =>
    val clue = "`${g.toString}` "
    val mangled = g.mangle
    assertTrue(s"$clue empty mangle", mangled.nonEmpty)

    val unmangled = Unmangle.unmangleGlobal(mangled)
    assertEquals(s"$clue different unmangle", g, unmangled)

    val remangled = unmangled.mangle
    assertEquals(s"$clue different remangle", mangled, remangled)
  }

}
