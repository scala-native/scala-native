package salty.ir

sealed abstract class Name {
  override def toString: String = this match {
    case Name.No                               => ""
    case Name.Class(id)                        => id
    case Name.Module(id)                       => id
    case Name.Interface(id)                    => id
    case Name.Primitive(id)                    => id
    case Name.Slice(n)                         => s"$n[]"
    case Name.Field(owner, field)              => s"$owner::$field"
    case Name.Method(owner, method, args, ret) => s"$owner::$method<${args.mkString(", ")}; $ret>"
  }

  def fullString = this match {
    case Name.No | _: Name.Slice => this.toString
    case _: Name.Class           => s"class $this"
    case _: Name.Module          => s"module $this"
    case _: Name.Interface       => s"interface $this"
    case _: Name.Primitive       => s"primitive $this"
    case _: Name.Field           => s"field $this"
    case _: Name.Method          => s"method $this"
  }
}
object Name {
  final case object No extends Name
  final case class Class(id: String) extends Name
  final case class Module(id: String) extends Name
  final case class Interface(id: String) extends Name
  final case class Primitive(id: String) extends Name
  final case class Slice(name: Name) extends Name
  final case class Field(owner: Name, id: String) extends Name
  final case class Method(owner: Name, id: String, args: Seq[Name], ret: Name) extends Name
}

final case class Scope(entries: Map[Name, Node])
