package salty.ir

import salty.ir.{Desc => D, Tags => T}

sealed abstract class NullaryFactory(desc: D) {
  def apply(name: Name = Name.No): Node=
    Node(desc, name, Array())
  def unapply(n: Node): Boolean =
    n.desc eq desc
}

sealed abstract class UnaryFactory(desc: D) {
  def apply(n: Node, name: Name = Name.No): Node =
    Node(desc, name, Array(n))
  def unapply(n: Node): Option[Slot] =
    if (n.desc eq desc)
      Some(n.at(0))
    else
      None
}

sealed abstract class BinaryFactory(desc: D) {
  def apply(left: Node, right: Node, name: Name = Name.No): Node =
    Node(desc, name, Array(left, right))
  def unapply(n: Node): Option[(Slot, Slot)] =
    if (n.desc eq desc)
      Some((n.at(0), n.at(1)))
    else
      None
}

sealed abstract class TernaryFactory(desc: D) {
  def apply(left: Node, middle: Node, right: Node, name: Name = Name.No): Node =
    Node(desc, name, Array(left, middle, right))
  def unapply(n: Node): Option[(Slot, Slot, Slot)] =
    if (n.desc eq desc)
      Some((n.at(0), n.at(1), n.at(2)))
    else
      None
}

private[ir] sealed abstract class SeqNodeFactory(desc: D) {
  def apply(nodes: Seq[Node], name: Name = Name.No): Node =
    Node(desc, name, Array(nodes))
  def unapply(n: Node): Option[MultiSlot] =
    if (n.desc eq desc)
      Some(n.multiAt(0))
    else
      None
}

private[ir] sealed abstract class NodeSeqNodeFactory(desc: D) {
  def apply(node: Node, nodes: Seq[Node], name: Name = Name.No): Node =
    Node(desc, name, Array(node, nodes))
  def unapply(n: Node): Option[(Slot, MultiSlot)] =
    if (n.desc eq desc)
      Some((n.at(0), n.multiAt(1)))
    else
      None
}

private[ir] sealed abstract class NodeSeqNodeNodeFactory(desc: D) {
  def apply(node1: Node, nodes: Seq[Node], node2: Node, name: Name = Name.No): Node =
    Node(desc, name, Array(node1, nodes, node2))
  def unapply(n: Node): Option[(Slot, MultiSlot, Slot)] =
    if (n.desc eq desc)
      Some((n.at(0), n.multiAt(1), n.at(2)))
    else
      None
}

private[ir] sealed abstract class NodeSeqNodeNodeNodeFactory(desc: D) {
  def apply(node1: Node, nodes: Seq[Node], node2: Node, node3: Node, name: Name = Name.No): Node =
    Node(desc, name, Array(node1, nodes, node2, node3))
  def unapply(n: Node): Option[(Slot, MultiSlot, Slot, Slot)] =
    if (n.desc eq desc)
      Some((n.at(0), n.multiAt(1), n.at(2), n.at(3)))
    else
      None
}

private[ir] sealed abstract class NodeNodeSeqNodeFactory(desc: D) {
  def apply(node1: Node, node2: Node, nodes: Seq[Node], name: Name = Name.No): Node =
    Node(desc, name, Array(node1, node2, nodes))
  def unapply(n: Node): Option[(Slot, Slot, MultiSlot)] =
    if (n.desc eq desc)
      Some((n.at(0), n.at(1), n.multiAt(2)))
    else
      None
}

object Label         extends SeqNodeFactory(D.Label)
object If            extends BinaryFactory(D.If)
object Switch        extends BinaryFactory(D.Switch)
object Try           extends UnaryFactory(D.Try)
object CaseTrue      extends UnaryFactory(D.CaseTrue)
object CaseFalse     extends UnaryFactory(D.CaseFalse)
object CaseConst     extends BinaryFactory(D.CaseConst)
object CaseDefault   extends UnaryFactory(D.CaseDefault)
object CaseException extends UnaryFactory(D.CaseException)
object Merge         extends SeqNodeFactory(D.Merge)
object Return        extends TernaryFactory(D.Return)
object Throw         extends TernaryFactory(D.Throw)
object Undefined     extends BinaryFactory(D.Undefined)
object End           extends SeqNodeFactory(D.End)

object Add  extends BinaryFactory(D.Add )
object Sub  extends BinaryFactory(D.Sub )
object Mul  extends BinaryFactory(D.Mul )
object Div  extends BinaryFactory(D.Div )
object Mod  extends BinaryFactory(D.Mod )
object Shl  extends BinaryFactory(D.Shl )
object Lshr extends BinaryFactory(D.Lshr)
object Ashr extends BinaryFactory(D.Ashr)
object And  extends BinaryFactory(D.And )
object Or   extends BinaryFactory(D.Or  )
object Xor  extends BinaryFactory(D.Xor )
object Eq   extends BinaryFactory(D.Eq  )
object Neq  extends BinaryFactory(D.Neq )
object Lt   extends BinaryFactory(D.Lt  )
object Lte  extends BinaryFactory(D.Lte )
object Gt   extends BinaryFactory(D.Gt  )
object Gte  extends BinaryFactory(D.Gte )

object Trunc    extends BinaryFactory(D.Trunc   )
object Zext     extends BinaryFactory(D.Zext    )
object Sext     extends BinaryFactory(D.Sext    )
object Fptrunc  extends BinaryFactory(D.Fptrunc )
object Fpext    extends BinaryFactory(D.Fpext   )
object Fptoui   extends BinaryFactory(D.Fptoui  )
object Fptosi   extends BinaryFactory(D.Fptosi  )
object Uitofp   extends BinaryFactory(D.Uitofp  )
object Sitofp   extends BinaryFactory(D.Sitofp  )
object Ptrtoint extends BinaryFactory(D.Ptrtoint)
object Inttoptr extends BinaryFactory(D.Inttoptr)
object Bitcast  extends BinaryFactory(D.Bitcast )


object EfPhi  extends NodeSeqNodeFactory(D.EfPhi)
object Call   extends NodeNodeSeqNodeFactory(D.Call)
object Load   extends BinaryFactory(D.Load)
object Store  extends TernaryFactory(D.Store)
object Param  extends UnaryFactory(D.Param)
object Phi    extends NodeSeqNodeFactory(D.Phi)
object Alloc  extends UnaryFactory(D.Alloc)
object Alloca extends UnaryFactory(D.Alloca)

object Equals     extends TernaryFactory(D.Equals)
object Hash       extends BinaryFactory(D.Hash)
object FieldElem  extends TernaryFactory(D.FieldElem)
object MethodElem extends TernaryFactory(D.MethodElem)
object SliceElem  extends TernaryFactory(D.SliceElem)
object GetClass   extends BinaryFactory(D.GetClass)
object Length     extends BinaryFactory(D.Length)
object Is         extends BinaryFactory(D.If)
object As         extends BinaryFactory(D.As)
object Box        extends BinaryFactory(D.Box)
object Unbox      extends BinaryFactory(D.Unbox)
object Allocs     extends BinaryFactory(D.Allocs)

object Null  extends NullaryFactory(D.Null)
object Unit  extends NullaryFactory(D.Unit)
object True  extends NullaryFactory(D.True)
object False extends NullaryFactory(D.False)
object I8 {
  def apply(v: Byte): Node =
    Node(D.I8(v))
  def unapply(n: Node): Option[Byte] =
    n.desc match {
      case D.I8(v) => Some(v)
      case _       => None
    }
}
object I16 {
  def apply(v: Short): Node =
    Node(D.I16(v))
  def unapply(n: Node): Option[Short] =
    n.desc match {
      case D.I16(v) => Some(v)
      case _        => None
    }
}
object I32 {
  def apply(v: Int): Node =
    Node(D.I32(v))
  def unapply(n: Node): Option[Int] =
    n.desc match {
      case D.I32(v) => Some(v)
      case _        => None
    }
}
object I64 {
  def apply(v: Long): Node =
    Node(D.I64(v))
  def unapply(n: Node): Option[Long] =
    n.desc match {
      case D.I64(v) => Some(v)
      case _        => None
    }
}
object F32 {
  def apply(v: Float): Node =
    Node(D.F32(v))
  def unapply(n: Node): Option[Float] =
    n.desc match {
      case D.F32(v) => Some(v)
      case _        => None
    }
}
object F64 {
  def apply(v: Double): Node =
    Node(D.F64(v))
  def unapply(n: Node): Option[Double] =
    n.desc match {
      case D.F64(v) => Some(v)
      case _        => None
    }
}
object Str {
  def apply(v: String): Node =
    Node(D.Str(v))
  def unapply(n: Node): Option[String] =
    n.desc match {
      case D.Str(v) => Some(v)
      case _        => None
    }
}

object Primitive extends NullaryFactory(D.Primitive)
object Global    extends BinaryFactory(D.Global)
object Define    extends NodeSeqNodeNodeFactory(D.Define)
object Declare   extends NodeSeqNodeFactory(D.Declare)
object Extern    extends NullaryFactory(D.Extern)
object Type {
  def apply(shape: Shape, nodes: Seq[Node], name: Name = Name.No): Node =
    Node(D.Type(shape), name, Array(nodes))
  def unapply(n: Node): Option[(Shape, MultiSlot)] =
    n.desc match {
      case D.Type(shape) => Some((shape, n.multiAt(0)))
      case _             => None
    }
}

object Class     extends NodeSeqNodeFactory(D.Class)
object Interface extends SeqNodeFactory(D.Interface)
object Module    extends NodeSeqNodeNodeFactory(D.Module)
object Method    extends NodeSeqNodeNodeNodeFactory(D.Method)
object Field     extends BinaryFactory(D.Field)

// Helper extractors

object Ref {
  def apply(node: Node): Node =
    Type(Shape.Ref(Shape.Hole), Seq(node))
  def unapply(node: Node): Option[Slot] = node match {
    case Type(Shape.Ref(Shape.Hole), mslot) => Some(mslot(0))
    case _                                  => None
  }
}
object Slice {
  def apply(node: Node): Node =
    Type(Shape.Slice(Shape.Hole), Seq(node))
  def unapply(node: Node): Option[Slot] = node match {
    case Type(Shape.Slice(Shape.Hole), mslot) => Some(mslot(0))
    case _                                    => None
  }
}
