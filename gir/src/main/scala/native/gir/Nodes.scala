package native
package gir

import scala.collection.mutable

// TODO: store offsets in desc
// TODO: ensure that all mutability is private[gir]
sealed class Node private[gir] (
  private[gir] var _desc:    Desc,
  private[gir] var _slots:   Array[Slot]       = Array.empty,
  private[gir] var _offsets: Array[Int]        = Array.empty,
  private[gir] var _epoch:   Int               = 0,
  private[gir] var _uses:    mutable.Set[Slot] = mutable.Set.empty,
  private[gir] var _attrs:   Array[Attr]       = Array.empty
) {
  private def length(index: Int): Int =
    if (index + 1 < _offsets.length)
      _offsets(index + 1) - _offsets(index)
    else
      _slots.length - _offsets(index)

  private[gir] def at(index: Int): Dep =
    _slots(_offsets(index))

  private[gir] def multiAt(index: Int): MultiDep =
    new MultiDep(this, length(index), _offsets(index))

  final def desc: Desc = _desc

  final def attrs: Seq[Attr] =
    _attrs.toSeq

  lazy val name: Name =
    _attrs.collectFirst { case n: Name => n }.getOrElse(Name.No)

  final def uses: Iterator[Use] = _uses.toIterator

  final def deps: Iterator[Dep] = _slots.toIterator

  // TODO: cycles
  final def type_==(other: Node): Boolean =
    (this, other) match {
      case (Defn.Extern(), Defn.Extern()) =>
        this.name == other.name
      case (Defn.Struct(args1), Defn.Struct(args2)) =>
        args1.zip(args2).forall { case (l, r) => l type_== r }
      case (Defn.Ptr(arg1), Defn.Ptr(arg2)) =>
        arg1 type_== arg2
      case (Defn.Function(ret1, args1), Defn.Function(ret2, args2)) =>
        (ret1 type_== ret2) &&
        (args1.zip(args2).forall { case (l, r) => l type_== r })
      case (Defn.ArrayClass(arg1), Defn.ArrayClass(arg2)) =>
        arg1 type_== arg2
      case _ =>
        this eq other
    }

  final def copy(attrs: Seq[Attr] = _attrs.clone()) = {
    val n = new Node(_desc)
    n._slots = _slots.map(s => new Slot(s.schema, n, s.dep))
    n._offsets = _offsets.clone
    n._attrs = attrs.toArray
    n
  }

  final override def toString = s"$desc $name"
}
object Node {
  private[gir] var lastEpoch = 0
  private[gir] def nextEpoch = {
    lastEpoch += 1
    lastEpoch
  }

  private[gir] def apply(desc: Desc): Node =
    new Node(desc)

  private[gir] def apply(desc: Desc, attrs: Seq[Attr]): Node =
    new Node(desc, _attrs = attrs.toArray)

  private[gir] def apply(desc: Desc, deps: Array[Any], attrs: Seq[Attr]): Node = {
    val node    = Node(desc, attrs)
    val slots   = new mutable.ArrayBuffer[Slot]
    val offsets = new mutable.ArrayBuffer[Int]
    var offset  = 0
    deps.zip(desc.schema).foreach {
      case (seq: Seq[_], Desc.Many(schema)) =>
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
