package salty.ir

import salty.ir.{Desc => D, Tags => T}

sealed abstract class NullaryFactory(desc: D) {
  def apply(attrs: Attr*): Node=
    Node(desc, Array(), attrs)
  def unapply(n: Node): Boolean =
    n.desc eq desc
}

sealed abstract class UnaryFactory(desc: D) {
  def apply(n: Node, attrs: Attr*): Node =
    Node(desc, Array(n), attrs)
  def unapply(n: Node): Option[Node] =
    if (n.desc eq desc)
      Some(n.at(0).dep)
    else
      None
  object deps {
    def unapply(n: Node): Option[Dep] =
      if (n.desc eq desc)
        Some(n.at(0))
      else
        None
  }
}

sealed abstract class BinaryFactory(desc: D) {
  def apply(left: Node, right: Node, attrs: Attr*): Node =
    Node(desc, Array(left, right), attrs)
  def unapply(n: Node): Option[(Node, Node)] =
    if (n.desc eq desc)
      Some((n.at(0).dep, n.at(1).dep))
    else
      None
  object deps {
    def unapply(n: Node): Option[(Dep, Dep)] =
      if (n.desc eq desc)
        Some((n.at(0), n.at(1)))
      else
        None
  }
}

sealed abstract class TernaryFactory(desc: D) {
  def apply(left: Node, middle: Node, right: Node, attrs: Attr*): Node =
    Node(desc, Array(left, middle, right), attrs)
  def unapply(n: Node): Option[(Node, Node, Node)] =
    if (n.desc eq desc)
      Some((n.at(0).dep, n.at(1).dep, n.at(2).dep))
    else
      None
  object deps {
    def unapply(n: Node): Option[(Dep, Dep, Dep)] =
      if (n.desc eq desc)
        Some((n.at(0), n.at(1), n.at(2)))
      else
        None
  }
}

private[ir] sealed abstract class SeqNodeFactory(desc: D) {
  def apply(nodes: Seq[Node], attrs: Attr*): Node =
    Node(desc, Array(nodes), attrs)
  def unapply(n: Node): Option[Seq[Node]] =
    if (n.desc eq desc)
      Some(n.multiAt(0).deps)
    else
      None
  object deps {
    def unapply(n: Node): Option[MultiDep] =
      if (n.desc eq desc)
        Some(n.multiAt(0))
      else
        None
  }
}

private[ir] sealed abstract class NodeSeqNodeFactory(desc: D) {
  def apply(node: Node, nodes: Seq[Node], attrs: Attr*): Node =
    Node(desc, Array(node, nodes), attrs)
  def unapply(n: Node): Option[(Node, Seq[Node])] =
    if (n.desc eq desc)
      Some((n.at(0).dep, n.multiAt(1).deps))
    else
      None
  object deps {
    def unapply(n: Node): Option[(Dep, MultiDep)] =
      if (n.desc eq desc)
        Some((n.at(0), n.multiAt(1)))
      else
        None
  }
}

private[ir] sealed abstract class NodeSeqNodeNodeFactory(desc: D) {
  def apply(node1: Node, nodes: Seq[Node], node2: Node, attrs: Attr*): Node =
    Node(desc, Array(node1, nodes, node2), attrs)
  def unapply(n: Node): Option[(Node, Seq[Node], Node)] =
    if (n.desc eq desc)
      Some((n.at(0).dep, n.multiAt(1).deps, n.at(2).dep))
    else
      None
  object deps {
    def unapply(n: Node): Option[(Dep, MultiDep, Dep)] =
      if (n.desc eq desc)
        Some((n.at(0), n.multiAt(1), n.at(2)))
      else
        None
  }
}

private[ir] sealed abstract class NodeSeqNodeNodeNodeFactory(desc: D) {
  def apply(node1: Node, nodes: Seq[Node], node2: Node, node3: Node, attrs: Attr*): Node =
    Node(desc, Array(node1, nodes, node2, node3), attrs)
  def unapply(n: Node): Option[(Node, Seq[Node], Node, Node)] =
    if (n.desc eq desc)
      Some((n.at(0).dep, n.multiAt(1).deps, n.at(2).dep, n.at(3).dep))
    else
      None
  object deps {
    def unapply(n: Node): Option[(Dep, MultiDep, Dep, Dep)] =
      if (n.desc eq desc)
        Some((n.at(0), n.multiAt(1), n.at(2), n.at(3)))
      else
        None
  }
}

private[ir] sealed abstract class NodeNodeSeqNodeFactory(desc: D) {
  def apply(node1: Node, node2: Node, nodes: Seq[Node], attrs: Attr*): Node =
    Node(desc, Array(node1, node2, nodes), attrs)
  def unapply(n: Node): Option[(Node, Node, Seq[Node])] =
    if (n.desc eq desc)
      Some((n.at(0).dep, n.at(1).dep, n.multiAt(2).deps))
    else
      None
  object deps {
    def unapply(n: Node): Option[(Dep, Dep, MultiDep)] =
      if (n.desc eq desc)
        Some((n.at(0), n.at(1), n.multiAt(2)))
      else
        None
  }
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


object EfPhi      extends NodeSeqNodeFactory(D.EfPhi)
object Call       extends NodeNodeSeqNodeFactory(D.Call)
object Load       extends BinaryFactory(D.Load)
object Store      extends TernaryFactory(D.Store)
object Elem       extends NodeSeqNodeFactory(D.Elem)
object StructElem extends BinaryFactory(D.StructElem)
object Param      extends UnaryFactory(D.Param)
object Phi        extends NodeSeqNodeFactory(D.Phi)
object Alloc      extends UnaryFactory(D.Alloc)
object Alloca     extends UnaryFactory(D.Alloca)

object Equals      extends TernaryFactory(D.Equals)
object Hash        extends BinaryFactory(D.Hash)
object FieldElem   extends TernaryFactory(D.FieldElem)
object MethodElem  extends TernaryFactory(D.MethodElem)
object SliceElem   extends TernaryFactory(D.SliceElem)
object GetClass    extends BinaryFactory(D.GetClass)
object SliceLength extends BinaryFactory(D.SliceLength)
object ClassAlloc  extends BinaryFactory(D.ClassAlloc)
object SliceAlloc  extends TernaryFactory(D.SliceAlloc)
object Is          extends BinaryFactory(D.If)
object As          extends BinaryFactory(D.As)
object Box         extends BinaryFactory(D.Box)
object Unbox       extends BinaryFactory(D.Unbox)

object Lit {
  object Struct extends NodeSeqNodeFactory(D.Lit.Struct)

  object Null  extends NullaryFactory(D.Lit.Null)
  object Unit  extends NullaryFactory(D.Lit.Unit)
  object True  extends NullaryFactory(D.Lit.True)
  object False extends NullaryFactory(D.Lit.False)
  object Zero  extends UnaryFactory(D.Lit.Zero)
  object Size  extends UnaryFactory(D.Lit.Size)
  object I8 {
    def apply(v: Byte): Node =
      Node(D.Lit.I8(v))
    def unapply(n: Node): Option[Byte] =
      n.desc match {
        case D.Lit.I8(v) => Some(v)
        case _           => None
      }
  }
  object I16 {
    def apply(v: Short): Node =
      Node(D.Lit.I16(v))
    def unapply(n: Node): Option[Short] =
      n.desc match {
        case D.Lit.I16(v) => Some(v)
        case _            => None
      }
  }
  object I32 {
    def apply(v: Int): Node =
      Node(D.Lit.I32(v))
    def unapply(n: Node): Option[Int] =
      n.desc match {
        case D.Lit.I32(v) => Some(v)
        case _            => None
      }
  }
  object I64 {
    def apply(v: Long): Node =
      Node(D.Lit.I64(v))
    def unapply(n: Node): Option[Long] =
      n.desc match {
        case D.Lit.I64(v) => Some(v)
        case _            => None
      }
  }
  object F32 {
    def apply(v: Float): Node =
      Node(D.Lit.F32(v))
    def unapply(n: Node): Option[Float] =
      n.desc match {
        case D.Lit.F32(v) => Some(v)
        case _            => None
      }
  }
  object F64 {
    def apply(v: Double): Node =
      Node(D.Lit.F64(v))
    def unapply(n: Node): Option[Double] =
      n.desc match {
        case D.Lit.F64(v) => Some(v)
        case _            => None
      }
  }
  object Str {
    def apply(v: String): Node =
      Node(D.Lit.Str(v))
    def unapply(n: Node): Option[String] =
      n.desc match {
        case D.Lit.Str(v) => Some(v)
        case _            => None
      }
  }
}

object Defn {
  object Global    extends BinaryFactory(D.Defn.Global)
  object Constant  extends BinaryFactory(D.Defn.Global)
  object Define    extends NodeSeqNodeNodeFactory(D.Defn.Define)
  object Declare   extends NodeSeqNodeFactory(D.Defn.Declare)
  object Extern    extends NullaryFactory(D.Defn.Extern)
  object Struct    extends SeqNodeFactory(D.Defn.Struct)
  object Ptr       extends UnaryFactory(D.Defn.Ptr)
  object Function  extends NodeSeqNodeFactory(D.Defn.Function)

  object Class     extends NodeSeqNodeFactory(D.Defn.Class)
  object Interface extends SeqNodeFactory(D.Defn.Interface)
  object Module    extends NodeSeqNodeNodeFactory(D.Defn.Module)
  object Method    extends NodeSeqNodeNodeNodeFactory(D.Defn.Method)
  object Field     extends BinaryFactory(D.Defn.Field)
  object Slice     extends UnaryFactory(D.Defn.Slice)
}
