package scala.scalanative
package codegen

/** An extern symbol introduced by code generation, with no Scala owner. */
private[codegen] object Extern {
  private val owner = nir.Global.Top("__")

  def apply(id: String): nir.Global.Member =
    owner.member(nir.Sig.Extern(id))

  def unapply(global: nir.Global): Option[String] = global match {
    case nir.Global.Member(`owner`, sig) =>
      sig.unmangled match {
        case nir.Sig.Extern(id) => Some(id)
        case _                  => None
      }
    case _ => None
  }
}
