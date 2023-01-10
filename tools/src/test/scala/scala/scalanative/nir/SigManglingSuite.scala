package scala.scalanative
package nir

import org.scalatest._
import org.scalatest.funsuite.AnyFunSuite
import Sig.Scope._
class SigManglingSuite extends AnyFunSuite {
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

  {
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
