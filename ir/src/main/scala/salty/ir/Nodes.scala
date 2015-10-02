package salty
package ir

import salty.ir.{Schema => Sc}
import salty.ir.Node.Slot

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

  final class Slot[T](node: Node, index: Int) {
    def :=(value: T) = node.slots(index) = value
    def get: T = node.slots(index).asInstanceOf[T]
  }
  object Slot {
    implicit def slot2value[T](slot: Slot[T]): T = slot.get
  }
}

sealed abstract class Prim(name: Name) extends Node(Desc.Primitive(name), Array())
object Prim {
  final case object Null    extends Prim(Name.Primitive("null"))
  final case object Nothing extends Prim(Name.Primitive("nothing"))
  final case object Unit    extends Prim(Name.Primitive("unit"))
  final case object Bool    extends Prim(Name.Primitive("bool"))

  sealed abstract case class I(width: Int) extends Prim(Name.Primitive(s"i$width"))
  final object I8  extends I(8)
  final object I16 extends I(16)
  final object I32 extends I(32)
  final object I64 extends I(64)

  sealed abstract case class F(width: Int) extends Prim(Name.Primitive(s"f$width"))
  final object F32 extends F(32)
  final object F64 extends F(64)
}
