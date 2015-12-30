package native
package nir

sealed abstract class Defn { def name: Global }
object Defn {
  final case class Var(attrs: Seq[Attr],
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
                          members: Seq[Defn]) extends Defn

  // scala
  final case class Interface(attrs: Seq[Attr],
                             name: Global,
                             interfaces: Seq[Type],
                             members: Seq[Defn]) extends Defn
  final case class Class(attrs: Seq[Attr],
                         name: Global,
                         parent: Type,
                         interfaces: Seq[Type],
                         members: Seq[Defn]) extends Defn
  final case class Module(attrs: Seq[Attr],
                          name: Global,
                          parent: Type,
                          interfaces: Seq[Type],
                          members: Seq[Defn]) extends Defn
}
