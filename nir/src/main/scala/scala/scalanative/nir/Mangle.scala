package scala.scalanative
package nir
import scala.scalanative.nir.Sig.Scope._
import scala.scalanative.util.ShowBuilder.InMemoryShowBuilder

object Mangle {
  def apply(ty: Type): String = {
    val impl = new Impl
    impl.mangleType(ty)
    impl.toString
  }

  def apply(name: Global): String = {
    val impl = new Impl
    impl.mangleGlobal(name)
    impl.toString
  }

  def apply(sig: Sig.Unmangled): String = {
    val impl = new Impl
    impl.mangleUnmangledSig(sig)
    impl.toString
  }

  private class Impl {
    val sb = new InMemoryShowBuilder
    import sb._

    def mangleGlobal(name: Global): Unit = name match {
      case name: Global.Top =>
        sb.str("T")
        mangleIdent(name.id)
      case name: Global.Member =>
        sb.str("M")
        mangleIdent(name.owner.id)
        mangleSig(name.sig)
      case _ =>
        util.unreachable
    }

    def mangleSig(sig: Sig): Unit =
      str(sig.mangle)

    def mangleSigScope(scope: Sig.Scope): Unit = {
      scope match {
        case Public            => str("O")
        case PublicStatic      => str("o")
        case Private(in)       => str("P"); mangleGlobal(in)
        case PrivateStatic(in) => str("p"); mangleGlobal(in)
      }
    }

    def mangleUnmangledSig(sig: Sig.Unmangled): Unit = sig match {
      case Sig.Field(id, scope) =>
        str("F")
        mangleIdent(id)
        mangleSigScope(scope)
      case Sig.Ctor(types) =>
        str("R")
        types.foreach(mangleType)
        str("E")
      case Sig.Clinit =>
        str("I")
        str("E")
      case Sig.Method(id, types, scope) =>
        str("D")
        mangleIdent(id)
        types.foreach(mangleType)
        str("E")
        mangleSigScope(scope)
      case Sig.Proxy(id, types) =>
        str("P")
        mangleIdent(id)
        types.foreach(mangleType)
        str("E")
      case Sig.Extern(id) =>
        str("C")
        mangleIdent(id)
      case Sig.Generated(id) =>
        str("G")
        mangleIdent(id)
      case Sig.Duplicate(sig, types) =>
        str("K")
        mangleSig(sig)
        types.foreach(mangleType)
        str("E")
    }

    def mangleType(ty: Type): Unit = ty match {
      case Type.Vararg               => str("v")
      case Type.Ptr                  => str("R_")
      case Type.Bool                 => str("z")
      case Type.Char                 => str("c")
      case Type.FixedSizeI(8, true)  => str("b")
      case Type.FixedSizeI(16, true) => str("s")
      case Type.FixedSizeI(32, true) => str("i")
      case Type.FixedSizeI(64, true) => str("j")
      case Type.Size                 => str("w")
      case Type.Float                => str("f")
      case Type.Double               => str("d")
      case Type.Null                 => str("l")
      case Type.Nothing              => str("n")
      case Type.Unit                 => str("u")
      case Type.ArrayValue(ty, n) =>
        str("A")
        mangleType(ty)
        str(n)
        str("_")
      case Type.StructValue(tys) =>
        str("S")
        tys.foreach(mangleType)
        str("E")
      case Type.Function(args, ret) =>
        str("R")
        args.foreach(mangleType)
        mangleType(ret)
        str("E")

      case Type.Array(ty, nullable) =>
        if (nullable) {
          str("L")
        }
        str("A")
        mangleType(ty)
        str("_")
      case Type.Ref(Global.Top(id), exact, nullable) =>
        if (nullable) {
          str("L")
        }
        if (exact) {
          str("X")
        }
        mangleIdent(id)
      case _ =>
        util.unreachable
    }

    def mangleIdent(id: String): Unit = {
      str(id.length)
      if (id.head.isDigit || id.head == '-') str('-')
      str(id)
    }

    override def toString = sb.toString
  }
}
