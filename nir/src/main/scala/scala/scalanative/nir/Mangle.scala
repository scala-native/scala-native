package scala.scalanative
package nir

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
    val sb = new scalanative.util.ShowBuilder
    import sb._

    def mangleGlobal(name: Global): Unit = name match {
      case name: Global.Top =>
        sb.str("T")
        mangleIdent(name.id)
      case name: Global.Member =>
        val ownerId = name.owner match {
          case owner: Global.Top => owner.id
          case _                 => util.unreachable
        }
        sb.str("M")
        mangleIdent(ownerId)
        mangleSig(name.sig)
      case _ =>
        util.unreachable
    }

    def mangleSig(sig: Sig): Unit =
      str(sig.mangle)

    def mangleUnmangledSig(sig: Sig.Unmangled): Unit = sig match {
      case Sig.Field(id) =>
        str("F")
        mangleIdent(id)
      case Sig.Ctor(types) =>
        str("R")
        types.foreach(mangleType)
        str("E")
      case Sig.Method(id, types) =>
        str("D")
        mangleIdent(id)
        types.foreach(mangleType)
        str("E")
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
      case Type.Vararg      => str("v")
      case Type.Ptr         => str("R_")
      case Type.Bool        => str("z")
      case Type.Char        => str("c")
      case Type.I(8, true)  => str("b")
      case Type.I(16, true) => str("s")
      case Type.I(32, true) => str("i")
      case Type.I(64, true) => str("j")
      case Type.Float       => str("f")
      case Type.Double      => str("d")
      case Type.Null        => str("l")
      case Type.Nothing     => str("n")
      case Type.Unit        => str("u")
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
        if (nullable) { str("L") }
        str("A")
        mangleType(ty)
        str("_")
      case Type.Ref(Global.Top(id), exact, nullable) =>
        if (nullable) { str("L") }
        if (exact) { str("X") }
        mangleIdent(id)
      case _ =>
        util.unreachable
    }

    def mangleIdent(id: String): Unit = {
      str(id.length)
      str(id)
    }

    override def toString = sb.toString
  }
}
