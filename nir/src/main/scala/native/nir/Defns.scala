package native
package nir

sealed abstract class Defn { def name: Name }
object Defn {
  final case class Var     (name: Name, ty: Type, value: Val)         extends Defn
  final case class Declare (name: Name, ty: Type)                     extends Defn
  final case class Define  (name: Name, ty: Type, blocks: Seq[Block]) extends Defn
  final case class Struct  (name: Name, members: Seq[Defn])           extends Defn

  // scala
  final case class Interface(name: Name,
                             interfaces: Seq[Name],
                             members: Seq[Defn]) extends Defn
  final case class Class(name: Name,
                         parent: Name,
                         interfaces: Seq[Name],
                         members: Seq[Defn]) extends Defn
  final case class Module(name: Name,
                          parent: Name,
                          interfaces: Seq[Name],
                          members: Seq[Defn]) extends Defn
}
