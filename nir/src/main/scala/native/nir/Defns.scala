package native
package nir

sealed abstract class Defn { def name: Name }
object Defn {
  final case class Var(attrs: Seq[Attr],
                       name: Name,
                       ty: Type,
                       value: Val) extends Defn
  final case class Declare(attrs: Seq[Attr],
                           name: Name,
                           ty: Type) extends Defn
  final case class Define(attrs: Seq[Attr],
                          name: Name,
                          ty: Type,
                          blocks: Seq[Block]) extends Defn
  final case class Struct(attrs: Seq[Attr],
                          name: Name,
                          members: Seq[Defn]) extends Defn

  // scala
  final case class Interface(attrs: Seq[Attr],
                             name: Name,
                             interfaces: Seq[Type],
                             members: Seq[Defn]) extends Defn
  final case class Class(attrs: Seq[Attr],
                         name: Name,
                         parent: Type,
                         interfaces: Seq[Type],
                         members: Seq[Defn]) extends Defn
  final case class Module(attrs: Seq[Attr],
                          name: Name,
                          parent: Type,
                          interfaces: Seq[Type],
                          members: Seq[Defn]) extends Defn
}
