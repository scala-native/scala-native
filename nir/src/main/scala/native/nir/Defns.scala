package native
package nir

sealed abstract class Defn {
  def name: Global
  def members: Seq[Defn] = Seq()
}
object Defn {
  final case class Var(attrs: Seq[Attr],
                       name: Global,
                       ty: Type,
                       value: Val) extends Defn
  final case class Const(attrs: Seq[Attr],
                         name: Global,
                         ty: Type,
                         value: Val) extends Defn
  final case class Declare(attrs: Seq[Attr],
                           name: Global,
                           ty: Type) extends Defn
  final case class Define(attrs: Seq[Attr],
                          name: Global,
                          ty: Type,
                          blocks: Seq[Block]) extends Defn
  final case class Struct(attrs: Seq[Attr],
                          name: Global,
                          fieldtys: Seq[Type]) extends Defn

  // scala
  final case class Trait(attrs: Seq[Attr],
                         name: Global,
                         traits: Seq[Global],
                         override val members: Seq[Defn]) extends Defn
  final case class Class(attrs: Seq[Attr],
                         name: Global,
                         parent: Global,
                         traits: Seq[Global],
                         override val members: Seq[Defn]) extends Defn
  final case class Module(attrs: Seq[Attr],
                          name: Global,
                          parent: Global,
                          traits: Seq[Global],
                          override val members: Seq[Defn]) extends Defn
}
