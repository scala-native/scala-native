package native
package ir

sealed abstract class Attr
sealed abstract class PersistentAttr extends Attr
abstract class TransientAttr extends Attr

sealed abstract class Name extends PersistentAttr {
  override def toString: String = this match {
    case Name.No                               => ""
    case Name.Main                             => "main"
    case Name.Prim(id)                         => id
    case Name.Local(id)                        => s"$id"
    case Name.Module(id)                       => s"module.$id"
    case Name.Class(id)                        => s"class.$id"
    case Name.Interface(id)                    => s"interface.$id"
    case Name.Accessor(owner)                  => s"$owner.accessor"
    case Name.Data(owner)                      => s"$owner.data"
    case Name.Vtable(owner)                    => s"$owner.vtable"
    case Name.VtableConstant(owner)            => s"$owner.vtable.constant"
    case Name.Array(n)                         => s"$n[]"
    case Name.Field(owner, field)              => s"$owner::$field"
    case Name.Constructor(owner, args)         => s"$owner<${args.mkString(", ")}>"
    case Name.Method(owner, method, args, ret) => s"$owner::$method<${args.mkString(", ")}; $ret>"
    case Name.Foreign(owner, id)               => s"$owner::$id"
  }
}
object Name {
  final case object No extends Name
  final case object Main extends Name
  final case class Prim(id: String) extends Name
  final case class Local(id: String) extends Name
  final case class Class(id: String) extends Name
  final case class Module(id: String) extends Name
  final case class Interface(id: String) extends Name
  final case class Accessor(owner: Name) extends Name
  final case class Data(owner: Name) extends Name
  final case class Vtable(owner: Name) extends Name
  final case class VtableConstant(owner: Name) extends Name
  final case class Array(name: Name) extends Name
  final case class Field(owner: Name, id: String) extends Name
  final case class Constructor(owner: Name, args: Seq[Name]) extends Name
  final case class Method(owner: Name, id: String, args: Seq[Name], ret: Name) extends Name
  final case class Foreign(owner: Name, id: String) extends Name
}
