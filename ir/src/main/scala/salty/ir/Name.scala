package salty.ir

sealed abstract class Name {
  override def toString: String = this match {
    case Name.No                               => ""
    case Name.Local(id)                        => s"local $id"
    case Name.Class(id)                        => s"class $id"
    case Name.Module(id)                       => s"module $id"
    case Name.Interface(id)                    => s"interface $id"
    case Name.Primitive(id)                    => s"$id"
    case Name.Slice(n)                         => s"$n[]"
    case Name.Field(owner, field)              => s"$owner :: field `$field`"
    case Name.Method(owner, method, args, ret) => s"$owner :: method $method <${args.mkString(", ")}; $ret>"
  }
}
object Name {
  final case object No extends Name
  final case class Local(id: String) extends Name
  final case class Class(id: String) extends Name
  final case class Module(id: String) extends Name
  final case class Interface(id: String) extends Name
  final case class Primitive(id: String) extends Name
  final case class Slice(name: Name) extends Name
  final case class Field(owner: Name, id: String) extends Name
  final case class Method(owner: Name, id: String, args: Seq[Name], ret: Name) extends Name
}
