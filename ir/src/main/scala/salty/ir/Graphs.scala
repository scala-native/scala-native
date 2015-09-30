package salty
package ir

import salty.ir.{Tags => T, Schema => Sc}

sealed class Node private[ir] (var desc: Desc, var slots: Array[Any]) {
  private[ir] var epoch: Int = 0

  final override def toString = {
    val name = desc.toString
    val slots = this.slots.map {
      case n: Node     => n.toString
      case seq: Seq[_] => seq.mkString(", ")
    }.mkString("; ")
    s"$name { $slots }"
  }

  final def type_==(other: Node): Boolean =
    (this, other) match {
      case (Extern(name1), Extern(name2)) =>
        name1 == name2
      case (Type(shape1, deps1), Type(shape2, deps2)) =>
        shape1 == shape2 && deps1.zip(deps2).forall { case (l, r) => l type_== r }
      case _ =>
        this eq other
    }

  // TODO: iterator
  final def edges: Seq[(Sc, Node)] =
    desc.schema.zip(slots).flatMap {
      case (Sc.Many(sc), nodes) => nodes.asInstanceOf[Seq[Node]].map { n => (sc, n) }
      case (sc         , node)  => Seq((sc, node.asInstanceOf[Node]))
      case _                    => throw new Exception("schema violation")
    }

  private[ir] def at(index: Int): Slot[Node]          = new Slot[Node](this, index)
  private[ir] def manyAt(index: Int): Slot[Seq[Node]] = new Slot[Seq[Node]](this, index)
}
object Node {
  private var lastEpoch = 0
  private[ir] def nextEpoch = {
    lastEpoch += 1
    lastEpoch
  }

  private[ir] def apply(desc: Desc, slots: Array[Any]) =
    new Node(desc, slots)
}

final class Slot[T](node: Node, index: Int) {
  def :=(value: T) = node.slots(index) = value
  def get: T = node.slots(index).asInstanceOf[T]
}
object Slot {
  implicit def slot2value[T](slot: Slot[T]): T = slot.get
}

sealed abstract class Prim(name: Name) extends Node(Desc.Primitive(name), Array())
object Prim {
  final case object Null    extends Prim(Name.Simple("null"))
  final case object Nothing extends Prim(Name.Simple("nothing"))
  final case object Unit    extends Prim(Name.Simple("unit"))
  final case object Bool    extends Prim(Name.Simple("bool"))

  sealed abstract case class I(width: Int) extends Prim(Name.Simple(s"i$width"))
  final object I8  extends I(8)
  final object I16 extends I(16)
  final object I32 extends I(32)
  final object I64 extends I(64)

  sealed abstract case class F(width: Int) extends Prim(Name.Simple(s"f$width"))
  final object F32 extends F(32)
  final object F64 extends F(64)
}

sealed abstract class Schema
object Schema {
  final case object Val              extends Schema
  final case object Cf               extends Schema
  final case object Ef               extends Schema
  final case object Ref              extends Schema
  final case class  Many(of: Schema) extends Schema
}

sealed abstract class Desc(val schema: Schema*)
object Desc {
  sealed trait Plain extends Desc
  object Plain {
    def unapply(tag: Int): Option[Plain] = ???
  }

  sealed trait Cf
  sealed trait Termn extends Cf
  sealed trait Ef
  sealed trait Val
  sealed trait Const extends Val
  sealed trait Defn

  final case object Start             extends Desc(                    ) with Plain with Cf with Ef
  final case class  Label(name: Name) extends Desc(Sc.Many(Sc.Cf)      )            with Cf
  final case object If                extends Desc(Sc.Cf, Sc.Val       ) with Plain with Cf
  final case object Switch            extends Desc(Sc.Cf, Sc.Val       ) with Plain with Cf
  final case object Try               extends Desc(Sc.Cf               ) with Plain with Cf
  final case object CaseTrue          extends Desc(Sc.Cf               ) with Plain with Cf
  final case object CaseFalse         extends Desc(Sc.Cf               ) with Plain with Cf
  final case object CaseConst         extends Desc(Sc.Cf, Sc.Val       ) with Plain with Cf
  final case object CaseDefault       extends Desc(Sc.Cf               ) with Plain with Cf
  final case object CaseException     extends Desc(Sc.Cf               ) with Plain with Cf
  final case object Merge             extends Desc(Sc.Many(Sc.Cf)      ) with Plain with Cf
  final case object Return            extends Desc(Sc.Cf, Sc.Ef, Sc.Val) with Plain with Termn
  final case object Throw             extends Desc(Sc.Cf, Sc.Ef, Sc.Val) with Plain with Termn
  final case object Undefined         extends Desc(Sc.Cf, Sc.Ef        ) with Plain with Termn
  final case object End               extends Desc(Sc.Many(Sc.Cf)      ) with Plain with Cf

  final case object EfPhi  extends Desc(Sc.Cf, Sc.Many(Sc.Ef))          with Plain with Ef
  final case object Equals extends Desc(Sc.Ef, Sc.Val, Sc.Val)          with Plain with Ef with Val
  final case object Call   extends Desc(Sc.Ef, Sc.Val, Sc.Many(Sc.Val)) with Plain with Ef with Val
  final case object Load   extends Desc(Sc.Ef, Sc.Val)                  with Plain with Ef with Val
  final case object Store  extends Desc(Sc.Ef, Sc.Val, Sc.Val)          with Plain with Ef with Val

  final case object Add  extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Sub  extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Mul  extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Div  extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Mod  extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Shl  extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Lshr extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Ashr extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object And  extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Or   extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Xor  extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Eq   extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Neq  extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Lt   extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Lte  extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Gt   extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Gte  extends Desc(Sc.Val, Sc.Val) with Plain with Val

  final case object Trunc    extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Zext     extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Sext     extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Fptrunc  extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Fpext    extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Fptoui   extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Fptosi   extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Uitofp   extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Sitofp   extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Ptrtoint extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Inttoptr extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Bitcast  extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Cast     extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Box      extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Unbox    extends Desc(Sc.Val, Sc.Ref) with Plain with Val

  final case object Phi                 extends Desc(Sc.Cf, Sc.Many(Sc.Val)) with Plain with Val
  final case object Is                  extends Desc(Sc.Val, Sc.Ref        ) with Plain with Val
  final case object Alloc               extends Desc(Sc.Ref                ) with Plain with Val
  final case object Salloc              extends Desc(Sc.Ref, Sc.Val        ) with Plain with Val
  final case object Length              extends Desc(Sc.Val                ) with Plain with Val
  final case object Elem                extends Desc(Sc.Val, Sc.Val        ) with Plain with Val
  final case class  Param(name: Name)   extends Desc(Sc.Ref                )            with Val
  final case object ValueOf             extends Desc(Sc.Val                ) with Plain with Val
  final case object ExceptionOf         extends Desc(Sc.Cf                 ) with Plain with Val
  final case object TagOf               extends Desc(Sc.Val                ) with Plain with Val

  final case object Unit                extends Desc() with Plain with Const
  final case object Null                extends Desc() with Plain with Const
  final case object True                extends Desc() with Plain with Const
  final case object False               extends Desc() with Plain with Const
  final case class  I8(value: Byte)     extends Desc()            with Const
  final case class  I16(value: Short)   extends Desc()            with Const
  final case class  I32(value: Int)     extends Desc()            with Const
  final case class  I64(value: Long)    extends Desc()            with Const
  final case class  F32(value: Float)   extends Desc()            with Const
  final case class  F64(value: Double)  extends Desc()            with Const
  final case class  Str(value: String)  extends Desc()            with Const
  final case object Tag                 extends Desc() with Plain with Const

  final case class Class(name: Name)     extends Desc(Sc.Many(Sc.Ref)                                 ) with Defn
  final case class Interface(name: Name) extends Desc(Sc.Many(Sc.Ref)                                 ) with Defn
  final case class Module(name: Name)    extends Desc(Sc.Many(Sc.Ref)                                 ) with Defn
  final case class Declare(name: Name)   extends Desc(Sc.Ref, Sc.Many(Sc.Val), Sc.Many(Sc.Ref)        ) with Defn
  final case class Define(name: Name)    extends Desc(Sc.Ref, Sc.Many(Sc.Val), Sc.Val, Sc.Many(Sc.Ref)) with Defn
  final case class Field(name: Name)     extends Desc(Sc.Ref, Sc.Many(Sc.Ref)                         ) with Defn
  final case class Extern(name: Name)    extends Desc(                                                ) with Defn
  final case class Type(shape: Shape)    extends Desc(Sc.Many(Sc.Ref)                                 ) with Defn
  final case class Primitive(name: Name) extends Desc(                                                ) with Defn
}

sealed abstract class Name {
  final override def toString = this match {
    case Name.No           => ""
    case Name.Simple(id)   => id
    case Name.Nested(l, r) => s"$l::$r"
  }
}
object Name {
  final case object No extends Name
  final case class Simple(id: String) extends Name
  final case class Nested(parent: Name, child: Name) extends Name
}

sealed abstract class Shape {
  final def holes: Int = this match {
    case Shape.Hole         => 1
    case Shape.Ref(shape)   => shape.holes
    case Shape.Slice(shape) => shape.holes
  }

  final override def toString = this match {
    case Shape.Hole         => "•"
    case Shape.Ref(shape)   => s"$shape!"
    case Shape.Slice(shape) => s"$shape[]"
  }
}
object Shape {
  final case object Hole extends Shape
  final case class Ref(of: Shape) extends Shape
  final case class Slice(of: Shape) extends Shape
  // TODO: Func(ret, args)
  // TODO: Struct(fields)
  // TODO: Array(t, n)
}

final case class Scope(entries: Map[Name, Node])
