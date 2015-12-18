package native
package nir

sealed abstract class Name
object Name {
  final case object None                                               extends Name
  final case class Fresh      (id: Int)                                extends Name
  final case class Local      (id: String)                             extends Name
  final case class Foreign    (id: String)                             extends Name
  final case class Prim       (id: String)                             extends Name
  final case class Nested     (owner: Name, member: Name)              extends Name
  final case class Class      (id: String)                             extends Name
  final case class Module     (id: String)                             extends Name
  final case class Interface  (id: String)                             extends Name
  final case class Field      (id: String)                             extends Name
  final case class Constructor(args: Seq[Name])                        extends Name
  final case class Method     (id: String, args: Seq[Name], ret: Name) extends Name
  final case class Accessor   (owner: Name)                            extends Name
  final case class Data       (owner: Name)                            extends Name
  final case class Vtable     (owner: Name)                            extends Name
  final case class Array      (name: Name)                             extends Name
}
