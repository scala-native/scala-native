package salty.ir

sealed abstract class Name {
  override def toString: String = this match {
    case Name.No                               => ""
    case Name.Main                             => "@main"
    case Name.Local(id)                        => s"%$id"
    case Name.Module(id)                       => s"@m.$id"
    case Name.ModuleAccessor(module)           => s"$module.accessor"
    case Name.ModuleData(module)               => s"$module.data"
    case Name.Class(id)                        => s"@c.$id"
    case Name.Interface(id)                    => s"@i.$id"
    case Name.Primitive(id)                    => s"$id"
    case Name.Slice(n)                         => s"$n[]"
    case Name.Field(owner, field)              => s"$owner::$field"
    case Name.Constructor(owner, args)         => s"$owner<${args.mkString(", ")}>"
    case Name.Method(owner, method, args, ret) => s"$owner::$method<${args.mkString(", ")}; $ret>"
  }
}
object Name {
  final case object No extends Name
  final case object Main extends Name
  final case class Local(id: String) extends Name
  final case class Class(id: String) extends Name
  final case class ClassVtable(owner: Name) extends Name
  final case class ClassVtableData(owner: Name) extends Name
  final case class ClassData(owner: Name) extends Name
  final case class ClassRef(owner: Name) extends Name
  final case class Module(id: String) extends Name
  final case class ModuleAccessor(owner: Name) extends Name
  final case class ModuleData(owner: Name) extends Name
  final case class Interface(id: String) extends Name
  final case class Primitive(id: String) extends Name
  final case class Slice(name: Name) extends Name
  final case class Field(owner: Name, id: String) extends Name
  final case class Constructor(owner: Name, args: Seq[Name]) extends Name
  final case class Method(owner: Name, id: String, args: Seq[Name], ret: Name) extends Name
}
