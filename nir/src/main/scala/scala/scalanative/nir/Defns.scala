package scala.scalanative
package nir

sealed abstract class Defn {
  def name: Global
  def attrs: Attrs

  final def show: String = nir.Show(this)
}

object Defn {
  // low-level
  final case class Var(attrs: Attrs, name: Global, ty: Type, rhs: Val)
      extends Defn
  final case class Const(attrs: Attrs, name: Global, ty: Type, rhs: Val)
      extends Defn
  final case class Declare(attrs: Attrs, name: Global, ty: Type) extends Defn
  final case class Define(attrs: Attrs,
                          name: Global,
                          ty: Type,
                          insts: Seq[Inst])
      extends Defn
  final case class Struct(attrs: Attrs, name: Global, tys: Seq[Type])
      extends Defn

  // high-level
  final case class Trait(attrs: Attrs, name: Global, traits: Seq[Global])
      extends Defn
  final case class Class(attrs: Attrs,
                         name: Global,
                         parent: Option[Global],
                         traits: Seq[Global])
      extends Defn
  final case class Module(attrs: Attrs,
                          name: Global,
                          parent: Option[Global],
                          traits: Seq[Global])
      extends Defn
}
