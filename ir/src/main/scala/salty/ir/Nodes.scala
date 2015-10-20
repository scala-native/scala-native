package salty
package ir

import scala.collection.mutable
import salty.ir.{Schema => Sc}

// TODO: store offsets in desc
// TODO: ensure that all mutability is private[ir]
sealed class Node private[ir] (
              var desc:     Desc,
  private[ir] var _slots:   Array[Slot]       = Array.empty,
  private[ir] var _offsets: Array[Int]        = Array.empty,
  private[ir] var _epoch:   Int               = Node.lastEpoch,
  private[ir] var _uses:    mutable.Set[Slot] = mutable.Set.empty,
  private[ir] val _attrs:   Array[Attr]       = Array.empty
) {
  private def length(index: Int): Int =
    if (index + 1 < _offsets.length)
      _offsets(index + 1) - _offsets(index)
    else
      _slots.length - _offsets(index)

  private[ir] def at(index: Int): Slot =
    _slots(_offsets(index))

  private[ir] def multiAt(index: Int): MultiSlot =
    new MultiSlot(this, length(index), _offsets(index))

  final def attrs: Seq[Attr] =
    _attrs.toSeq

  lazy val name: Name =
    _attrs.collectFirst { case n: Name => n }.getOrElse(Name.No)

  final def uses: Seq[Slot] = _uses.toSeq

  final override def toString = s"Node($desc, ...)"

  // TODO: cycles
  final def type_==(other: Node): Boolean =
    (this, other) match {
      case (Defn.Extern(), Defn.Extern()) =>
        this.name == other.name
      case (Defn.Struct(args1), Defn.Struct(args2)) =>
        args1.nodes.zip(args2.nodes).forall { case (l, r) => l type_== r }
      case (Defn.Ptr(arg1), Defn.Ptr(arg2)) =>
        arg1 type_== arg2
      case (Defn.Function(ret1, args1), Defn.Function(ret2, args2)) =>
        (ret1 type_== ret2) && (args1.nodes.zip(args2.nodes).forall { case (l, r) => l type_== r })
      case (Defn.Slice(arg1), Defn.Slice(arg2)) =>
        arg1 type_== arg2
      case _ =>
        this eq other
    }

  // TODO: iterator
  final def edges: Seq[(Sc, Slot)] =
    desc.schema.zipWithIndex.flatMap {
      case (Sc.Many(sc), idx) => multiAt(idx).slots.map((sc, _))
      case (sc,          idx) => Seq((sc, at(idx)))
      case _                  => throw new Exception("schema violation")
    }

  final def deps: Iterator[Node] =
    _slots.toIterator.map(_.get)
}
object Node {
  private[ir] var lastEpoch = 0
  private[ir] def nextEpoch = {
    lastEpoch += 1
    lastEpoch
  }

  private[ir] def apply(desc: Desc): Node =
    new Node(desc)

  private[ir] def apply(desc: Desc, attrs: Seq[Attr]): Node =
    new Node(desc, _attrs = attrs.toArray)

  private[ir] def apply(desc: Desc, deps: Array[Any], attrs: Seq[Attr]): Node = {
    val node    = Node(desc, attrs)
    val slots   = new mutable.ArrayBuffer[Slot]
    val offsets = new mutable.ArrayBuffer[Int]
    var offset  = 0
    deps.zip(desc.schema).foreach {
      case (seq: Seq[_], Sc.Many(schema)) =>
        seq.asInstanceOf[Seq[Node]].foreach { n =>
          slots += new Slot(schema, node, n)
        }
        offsets += offset
        offset  += seq.length
      case (n: Node, schema) =>
        slots   += new Slot(schema, node, n)
        offsets += offset
        offset  += 1
      case _ =>
        throw new Exception("Schema violation.")
    }
    node._slots   = slots.toArray
    node._offsets = offsets.toArray
    node
  }
}

sealed abstract class Prim(desc: Desc) extends Node(desc) {
  override lazy val name: Name = this match {
    case Prim.Unit    => Name.Prim("unit")
    case Prim.Bool    => Name.Prim("bool")
    case Prim.I(w)    => Name.Prim(s"i$w")
    case Prim.F(w)    => Name.Prim(s"f$w")
    case Prim.Nothing => Name.Prim("nothing")
    case Prim.Null    => Name.Prim("null")
  }
}
object Prim {
  final case object Unit extends Prim(Desc.Prim.Unit)
  final case object Bool extends Prim(Desc.Prim.Bool)

  sealed abstract case class I(width: Int) extends Prim(width match {
    case 8  => Desc.Prim.I8
    case 16 => Desc.Prim.I16
    case 32 => Desc.Prim.I32
    case 64 => Desc.Prim.I64
  })
  final object I8  extends I(8)
  final object I16 extends I(16)
  final object I32 extends I(32)
  final object I64 extends I(64)

  sealed abstract case class F(width: Int) extends Prim(width match {
    case 32 => Desc.Prim.F32
    case 64 => Desc.Prim.F64
  })
  final object F32 extends F(32)
  final object F64 extends F(64)

  final case object Nothing extends Prim(Desc.Prim.Nothing)
  final case object Null    extends Prim(Desc.Prim.Null)
}

final case object Empty extends Node(Desc.Empty)
