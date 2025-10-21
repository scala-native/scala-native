package scala.scalanative
package nir

import org.junit.Assert._
import org.junit.Test

import Sig.Scope._
class SigManglingSuite {
  val fieldNames =
    Seq("f", "len", "field", "-field", "2", "-", "-2field", "2-field")
  val scopes = Seq(
    Sig.Scope.Public,
    Sig.Scope.Private(Global.Top("foo"))
  )

  val methodArgs = Seq(
    Seq.empty,
    Seq(Type.Unit),
    Seq(Type.Int, Type.Unit)
  )

  val fields = for {
    scope <- scopes
    field <- fieldNames
  } yield Sig.Field(field, scope)

  val methods = for {
    scope <- scopes
    args <- methodArgs
  } yield Sig.Method("bar", args, scope)

  val proxies = methodArgs.map(Sig.Proxy("bar", _))

  @Test def sigMangling(): Unit = {
    fields ++
      methods ++
      proxies ++
      Seq(
        Sig.Ctor(Seq.empty),
        Sig.Ctor(Seq(Type.Int)),
        Sig.Ctor(Seq(Rt.Object, Type.Int)),
        Sig.Extern("read"),
        Sig.Extern("malloc"),
        Sig.Generated("layout"),
        Sig.Generated("type"),
        Sig.Duplicate(Sig.Method("bar", Seq.empty), Seq.empty),
        Sig.Duplicate(Sig.Method("bar", Seq(Type.Unit)), Seq(Type.Unit))
      )
  }.foreach { sig =>
    val clue = "`${sig.toString}`"
    val mangled = sig.mangle
    assertTrue(s"$clue empty mangle ", mangled.nonEmpty)

    val unmangled = Unmangle.unmangleSig(mangled)
    assertEquals(s"$clue - different unmangle", sig, unmangled)

    val remangled = unmangled.mangle
    assertEquals(s"$clue different remangle", mangled, remangled)
  }

}
