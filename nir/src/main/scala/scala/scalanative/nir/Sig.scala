package scala.scalanative
package nir

sealed abstract class Sig {
  final def toProxy: Sig.Proxy = this match {
    case Sig.Method(id, types) =>
      Sig.Proxy(id, types.init)
    case _ =>
      util.unsupported(
        s"can't convert non-method sig ${this.mangle} to proxy sig")
  }
  final def show: String =
    Show(this)
  final lazy val mangle: String =
    Mangle(this)
  final override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: Sig => other.mangle == mangle
      case _          => false
    })
  final override lazy val hashCode: Int =
    mangle.##
  final override def toString: String =
    mangle
}
object Sig {
  final case class Field(id: String)                    extends Sig
  final case class Ctor(types: Seq[Type])               extends Sig
  final case class Method(id: String, types: Seq[Type]) extends Sig
  final case class Proxy(id: String, types: Seq[Type])  extends Sig
  final case class Extern(id: String)                   extends Sig
  final case class Generated(id: String)                extends Sig
}
