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

sealed abstract class Builtin(desc: Desc) extends Node(desc) {
  override lazy val name: Name = this match {
    case Builtin.Unit    => Name.Builtin("unit")
    case Builtin.Bool    => Name.Builtin("bool")
    case Builtin.I(w)    => Name.Builtin(s"i$w")
    case Builtin.F(w)    => Name.Builtin(s"f$w")
    case Builtin.Nothing => Name.Builtin("nothing")
    case Builtin.Null    => Name.Builtin("null")
    case Builtin.AnyRef  => Name.Class("java.lang.Object")
  }
}
object Builtin {
  final case object Unit extends Builtin(Desc.Builtin.Unit)
  final case object Bool extends Builtin(Desc.Builtin.Bool)

  sealed abstract case class I(width: Int) extends Builtin(width match {
    case 8  => Desc.Builtin.I8
    case 16 => Desc.Builtin.I16
    case 32 => Desc.Builtin.I32
    case 64 => Desc.Builtin.I64
  })
  final object I8  extends I(8)
  final object I16 extends I(16)
  final object I32 extends I(32)
  final object I64 extends I(64)

  sealed abstract case class F(width: Int) extends Builtin(width match {
    case 32 => Desc.Builtin.F32
    case 64 => Desc.Builtin.F64
  })
  final object F32 extends F(32)
  final object F64 extends F(64)

  final case object Nothing extends Builtin(Desc.Builtin.Nothing)
  final case object Null    extends Builtin(Desc.Builtin.Null)
  final case object AnyRef  extends Builtin(Desc.Builtin.AnyRef) {
    def resolve(name: Name): Option[Node] = name match {
      case `initName`      => Some(initMethod)
      case `cloneName`     => Some(cloneMethod)
      case `equalsName`    => Some(equalsMethod)
      case `finalizeName`  => Some(finalizeMethod)
      case `getClassName`  => Some(getClassMethod)
      case `hashCodeName`  => Some(hashCodeMethod)
      case `notifyName`    => Some(notifyMethod)
      case `notifyAllName` => Some(notifyAllMethod)
      case `toStringName`  => Some(toStringMethod)
      case `wait0Name`     => Some(wait0Method)
      case `wait1Name`     => Some(wait1Method)
      case `wait2Name`     => Some(wait2Method)
      case _               => None
    }

    lazy val initName: Name = Name.Constructor(name, Seq())
    lazy val initMethod: Node =
      Defn.Method(Unit, Seq(), End(Seq(Return(Empty, Empty, Unit))), this, initName)

    lazy val cloneName: Name = Name.Method(name, "clone", Seq(), name)
    lazy val cloneMethod: Node = ???

    lazy val equalsName: Name = Name.Method(name, "equals", Seq(name), Bool.name)
    lazy val equalsMethod: Node = ???

    lazy val finalizeName: Name = Name.Method(name, "finalize", Seq(), Unit.name)
    lazy val finalizeMethod: Node = ???

    lazy val getClassName: Name = Name.Method(name, "getClass", Seq(), Name.Class("java.lang.Class"))
    lazy val getClassMethod: Node = ???

    lazy val hashCodeName: Name = Name.Method(name, "hashCode", Seq(), I32.name)
    lazy val hashCodeMethod: Node = ???

    lazy val notifyName: Name = Name.Method(name, "notify", Seq(), Unit.name)
    lazy val notifyMethod: Node = ???

    lazy val notifyAllName: Name = Name.Method(name, "notifyAll", Seq(), Unit.name)
    lazy val notifyAllMethod: Node = ???

    lazy val toStringName: Name = Name.Method(name, "toString", Seq(), Name.Class("java.lang.String"))
    lazy val toStringMethod: Node = ???

    lazy val wait0Name: Name = Name.Method(name, "wait", Seq(), Unit.name)
    lazy val wait0Method: Node = ???

    lazy val wait1Name: Name = Name.Method(name, "wait", Seq(I64.name), Unit.name)
    lazy val wait1Method: Node = ???

    lazy val wait2Name: Name = Name.Method(name, "wait", Seq(I64.name, I32.name), Unit.name)
    lazy val wait2Method: Node = ???
  }
}

final case object Empty extends Node(Desc.Empty)
