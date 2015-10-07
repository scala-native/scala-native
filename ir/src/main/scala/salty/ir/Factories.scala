package salty.ir

import salty.ir.{Desc => D, Tags => T}
import salty.ir.Node.Slot

sealed abstract class NullaryFactory(desc: D) extends (() => Node) {
  def apply(): Node=
    Node(desc, Array())
  def unapply(n: Node): Boolean =
    n.desc eq desc
}

sealed abstract class UnaryFactory(desc: D) extends (Node => Node) {
  def apply(n: Node): Node =
    Node(desc, Array(n))
  def unapply(n: Node): Option[Slot[Node]] =
    if (n.desc eq desc)
      Some(n.at(0))
    else
      None
}

sealed abstract class BinaryFactory(desc: D) extends ((Node, Node) => Node) {
  def apply(left: Node, right: Node): Node =
    Node(desc, Array(left, right))
  def unapply(n: Node): Option[(Slot[Node], Slot[Node])] =
    if (n.desc eq desc)
      Some((n.at(0), n.at(1)))
    else
      None
}

sealed abstract class TernaryFactory(desc: D) extends ((Node, Node, Node) => Node) {
  def apply(left: Node, middle: Node, right: Node): Node =
    Node(desc, Array(left, middle, right))
  def unapply(n: Node): Option[(Slot[Node], Slot[Node], Slot[Node])] =
    if (n.desc eq desc)
      Some((n.at(0), n.at(1), n.at(2)))
    else
      None
}

object Start         extends NullaryFactory(D.Start)
object Label {
  def apply(id: String, cfs: Seq[Node]): Node =
    Node(D.Label(id), Array(cfs))
  def unapply(n: Node): Option[(String, Slot[Seq[Node]])] =
    n.desc match {
      case D.Label(id) => Some((id, n.manyAt(0)))
      case _           => None
    }
}
object If            extends BinaryFactory(D.If)
object Switch        extends BinaryFactory(D.Switch)
object Try           extends UnaryFactory(D.Try)
object CaseTrue      extends UnaryFactory(D.CaseTrue)
object CaseFalse     extends UnaryFactory(D.CaseFalse)
object CaseConst     extends BinaryFactory(D.CaseConst)
object CaseDefault   extends UnaryFactory(D.CaseDefault)
object CaseException extends UnaryFactory(D.CaseException)
object Merge {
  def apply(cfs: Seq[Node]): Node =
    Node(D.Merge, Array(cfs))
  def unapply(n: Node): Option[Slot[Seq[Node]]] =
    if (n.desc eq D.Merge)
      Some(n.manyAt(0))
    else
      None
}
object Return        extends TernaryFactory(D.Return)
object Throw         extends TernaryFactory(D.Throw)
object Undefined     extends BinaryFactory(D.Undefined)
object End {
  def apply(cfs: Seq[Node]): Node =
    Node(D.End, Array(cfs))
  def unapply(n: Node): Option[Slot[Seq[Node]]] =
    if (n.desc eq D.End)
      Some(n.manyAt(0))
    else
      None
}

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
object As       extends BinaryFactory(D.As      )
object Box      extends BinaryFactory(D.Box     )
object Unbox    extends BinaryFactory(D.Unbox   )

object EfPhi {
  def apply(cf: Node, efs: Seq[Node]): Node =
    Node(D.EfPhi, Array(cf, efs))
  def unapply(n: Node): Option[(Slot[Node], Slot[Seq[Node]])] =
    if (n.desc eq D.EfPhi)
      Some((n.at(0), n.manyAt(1)))
    else
      None
}
object Call {
  def apply(ef: Node, funptr: Node, args: Seq[Node]): Node =
    Node(D.Call, Array(ef, funptr, args))
  def unapply(n: Node): Option[(Slot[Node], Slot[Node], Slot[Seq[Node]])] =
    if (n.desc eq D.Call)
      Some((n.at(0), n.at(1), n.manyAt(2)))
    else
      None

}
object Load   extends BinaryFactory(D.Load)
object Store  extends TernaryFactory(D.Store)
object Equals extends TernaryFactory(D.Equals)
object Hash   extends BinaryFactory(D.Hash)

object Phi {
  def apply(cf: Node, values: Seq[Node]): Node =
    Node(D.Phi, Array(cf, values))
  def unapply(n: Node): Option[(Slot[Node], Slot[Seq[Node]])] =
    if (n.desc eq D.Phi)
      Some((n.at(0), n.manyAt(1)))
    else
      None
}
object Param {
  def apply(id: String, ty: Node): Node =
    Node(D.Param(id), Array(ty))
  def unapply(n: Node): Option[(String, Slot[Node])] =
    n.desc match {
      case D.Param(id) => Some((id, n.at(0)))
      case _           => None
    }
}
object Alloc    extends UnaryFactory(D.Alloc)
object Alloca   extends UnaryFactory(D.Alloca)
object Allocs   extends BinaryFactory(D.Allocs)
object Is       extends BinaryFactory(D.If)
object Length   extends UnaryFactory(D.Length)
object Elem     extends BinaryFactory(D.Elem)
object GetClass extends UnaryFactory(D.GetClass)

object Null  extends NullaryFactory(D.Null)
object Unit  extends NullaryFactory(D.Unit)
object True  extends NullaryFactory(D.True)
object False extends NullaryFactory(D.False)
object I8    {
  def apply(v: Byte): Node =
    Node(D.I8(v), Array())
  def unapply(n: Node): Option[Byte] =
    n.desc match {
      case D.I8(v) => Some(v)
      case _       => None
    }
}
object I16   {
  def apply(v: Short): Node =
    Node(D.I16(v), Array())
  def unapply(n: Node): Option[Short] =
    n.desc match {
      case D.I16(v) => Some(v)
      case _        => None
    }
}
object I32   {
  def apply(v: Int): Node =
    Node(D.I32(v), Array())
  def unapply(n: Node): Option[Int] =
    n.desc match {
      case D.I32(v) => Some(v)
      case _        => None
    }
}
object I64   {
  def apply(v: Long): Node =
    Node(D.I64(v), Array())
  def unapply(n: Node): Option[Long] =
    n.desc match {
      case D.I64(v) => Some(v)
      case _        => None
    }
}
object F32   {
  def apply(v: Float): Node =
    Node(D.F32(v), Array())
  def unapply(n: Node): Option[Float] =
    n.desc match {
      case D.F32(v) => Some(v)
      case _        => None
    }
}
object F64   {
  def apply(v: Double): Node =
    Node(D.F64(v), Array())
  def unapply(n: Node): Option[Double] =
    n.desc match {
      case D.F64(v) => Some(v)
      case _        => None
    }
}
object Str   {
  def apply(v: String): Node =
    Node(D.Str(v), Array())
  def unapply(n: Node): Option[String] =
    n.desc match {
      case D.Str(v) => Some(v)
      case _        => None
    }
}

object Class {
  def apply(name: Name, rels: Seq[Node]): Node =
    Node(D.Class(name), Array(rels))
  def unapply(n: Node): Option[(Name, Slot[Seq[Node]])] =
    n.desc match {
      case D.Class(name) => Some((name, n.manyAt(0)))
      case _             => None
    }
}
object Interface {
  def apply(name: Name, rels: Seq[Node]): Node =
    Node(D.Interface(name), Array(rels))
  def unapply(n: Node): Option[(Name, Slot[Seq[Node]])] =
    n.desc match {
      case D.Interface(name) => Some((name, n.manyAt(0)))
      case _                 => None
    }
}
object Module {
  def apply(name: Name, rels: Seq[Node]): Node =
    Node(D.Module(name), Array(rels))
  def unapply(n: Node): Option[(Name, Slot[Seq[Node]])] =
    n.desc match {
      case D.Module(name) => Some((name, n.manyAt(0)))
      case _              => None
    }
}
object Declare {
  def apply(name: Name, ty: Node, params: Seq[Node], rels: Seq[Node]): Node =
    Node(D.Declare(name),
         Array(ty, params, rels))
  def unapply(n: Node): Option[(Name, Slot[Node], Slot[Seq[Node]], Slot[Seq[Node]])] =
    n.desc match {
      case D.Declare(name) =>
        Some((name, n.at(0), n.manyAt(1), n.manyAt(2)))
      case _ =>
        None
    }
}
object Define {
  def apply(name: Name, ty: Node, params: Seq[Node], end: Node, rels: Seq[Node]): Node =
    Node(D.Define(name),
         Array(ty, params, end, rels))
  def unapply(n: Node): Option[(Name, Slot[Node], Slot[Seq[Node]], Slot[Node], Slot[Seq[Node]])] =
    n.desc match {
      case D.Define(name) =>
        Some((name, n.at(0), n.manyAt(1), n.at(2), n.manyAt(3)))
      case _ =>
        None
    }
}
object Field {
  def apply(name: Name, ty: Node, rels: Seq[Node]): Node =
    Node(D.Field(name), Array(ty, rels))
  def unapply(n: Node): Option[(Name, Slot[Node], Slot[Seq[Node]])] =
    n.desc match {
      case D.Field(name) => Some((name, n.at(0), n.manyAt(1)))
      case _             => None
    }
}
object Extern {
  def apply(name: Name): Node =
    Node(D.Extern(name), Array())
  def unapply(n: Node): Option[Name] =
    n.desc match {
      case D.Extern(name) => Some(name)
      case _              => None
    }
}
object Type {
  def apply(shape: Shape, holes: Seq[Node]): Node =
    Node(D.Type(shape), Array(holes))
  def unapply(n: Node): Option[(Shape, Slot[Seq[Node]])] =
    n.desc match {
      case D.Type(shape) => Some((shape, n.manyAt(0)))
      case _             => None
    }
}
