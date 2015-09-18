package salty
package ir

sealed abstract class Edge {
  def node: Node
}
object Edge {
  final case class Val(node: ir.Instr.Val) extends Edge
  final case class Ef(node: ir.Instr.Ef) extends Edge
  final case class Cf(node: ir.Instr.Cf) extends Edge
  final case class Rel(node: ir.Defn) extends Edge
  final case class Ref(node: ir.Defn) extends Edge
  final case class Instr(node: ir.Instr) extends Edge
  final case class Type(node: ir.Type) extends Edge

  def of(node: ir.Node): Seq[Edge] = node match {
    case ty: ir.Type     => of(ty)
    case instr: ir.Instr => of(instr)
    case defn: ir.Defn   => of(defn)
  }
  def of(ty: ir.Type): Seq[Edge] = ty match {
    case ir.Type.Ref(ty)   => Seq(Edge.Type(ty))
    case ir.Type.Slice(ty) => Seq(Edge.Type(ty))
    case ir.Type.Of(defn)  => Seq(Edge.Ref(defn))
    case _                 => Seq()
  }
  def of(instr: ir.Instr): Seq[Edge] = instr match {
    case ir.Instr.Tag(ty)           => Seq(Edge.Type(ty))
    case _: ir.Instr.Const          => Seq()

    case ir.Instr.Start()           => Seq()
    case ir.Instr.If(cf, v)         => Seq(Edge.Cf(cf), Edge.Val(v))
    case ir.Instr.Switch(cf, v)     => Seq(Edge.Cf(cf), Edge.Val(v))
    case ir.Instr.Try(cf)           => Seq(Edge.Cf(cf))
    case ir.Instr.CaseTrue(cf)      => Seq(Edge.Cf(cf))
    case ir.Instr.CaseFalse(cf)     => Seq(Edge.Cf(cf))
    case ir.Instr.CaseConst(cf, c)  => Seq(Edge.Cf(cf), Edge.Val(c))
    case ir.Instr.CaseDefault(cf)   => Seq(Edge.Cf(cf))
    case ir.Instr.CaseException(cf) => Seq(Edge.Cf(cf))
    case ir.Instr.Return(cf, ef, v) => Seq(Edge.Cf(cf), Edge.Ef(ef), Edge.Val(v))
    case ir.Instr.Throw(cf, ef, v)  => Seq(Edge.Cf(cf), Edge.Ef(ef), Edge.Val(v))
    case ir.Instr.Undefined(cf, ef )=> Seq(Edge.Cf(cf), Edge.Ef(ef))
    case ir.Instr.Merge(cfs)        => cfs.map(Edge.Cf)
    case ir.Instr.End(cfs)          => cfs.map(Edge.Cf)

    case ir.Instr.EfPhi(cf, efs)          => Edge.Cf(cf) +: efs.map(Edge.Ef)
    case ir.Instr.Equals(ef, left, right) => Seq(Edge.Ef(ef), Edge.Val(left), Edge.Val(right))
    case ir.Instr.Call(ef, ptr, args)     => Edge.Ef(ef) +: Edge.Val(ptr) +: args.map(Edge.Val)
    case ir.Instr.Load(ef, ptr)           => Seq(Edge.Ef(ef), Edge.Val(ptr))
    case ir.Instr.Store(ef, ptr, value)   => Seq(Edge.Ef(ef), Edge.Val(ptr), Edge.Val(value))

    case ir.Instr.Add (left, right) => Seq(Edge.Val(left), Edge.Val(right))
    case ir.Instr.Sub (left, right) => Seq(Edge.Val(left), Edge.Val(right))
    case ir.Instr.Mul (left, right) => Seq(Edge.Val(left), Edge.Val(right))
    case ir.Instr.Div (left, right) => Seq(Edge.Val(left), Edge.Val(right))
    case ir.Instr.Mod (left, right) => Seq(Edge.Val(left), Edge.Val(right))
    case ir.Instr.Shl (left, right) => Seq(Edge.Val(left), Edge.Val(right))
    case ir.Instr.Lshr(left, right) => Seq(Edge.Val(left), Edge.Val(right))
    case ir.Instr.Ashr(left, right) => Seq(Edge.Val(left), Edge.Val(right))
    case ir.Instr.And (left, right) => Seq(Edge.Val(left), Edge.Val(right))
    case ir.Instr.Or  (left, right) => Seq(Edge.Val(left), Edge.Val(right))
    case ir.Instr.Xor (left, right) => Seq(Edge.Val(left), Edge.Val(right))
    case ir.Instr.Eq  (left, right) => Seq(Edge.Val(left), Edge.Val(right))
    case ir.Instr.Neq (left, right) => Seq(Edge.Val(left), Edge.Val(right))
    case ir.Instr.Lt  (left, right) => Seq(Edge.Val(left), Edge.Val(right))
    case ir.Instr.Lte (left, right) => Seq(Edge.Val(left), Edge.Val(right))
    case ir.Instr.Gt  (left, right) => Seq(Edge.Val(left), Edge.Val(right))
    case ir.Instr.Gte (left, right) => Seq(Edge.Val(left), Edge.Val(right))

    case ir.Instr.Trunc   (value, ty) => Seq(Edge.Val(value), Edge.Type(ty))
    case ir.Instr.Zext    (value, ty) => Seq(Edge.Val(value), Edge.Type(ty))
    case ir.Instr.Sext    (value, ty) => Seq(Edge.Val(value), Edge.Type(ty))
    case ir.Instr.Fptrunc (value, ty) => Seq(Edge.Val(value), Edge.Type(ty))
    case ir.Instr.Fpext   (value, ty) => Seq(Edge.Val(value), Edge.Type(ty))
    case ir.Instr.Fptoui  (value, ty) => Seq(Edge.Val(value), Edge.Type(ty))
    case ir.Instr.Fptosi  (value, ty) => Seq(Edge.Val(value), Edge.Type(ty))
    case ir.Instr.Uitofp  (value, ty) => Seq(Edge.Val(value), Edge.Type(ty))
    case ir.Instr.Sitofp  (value, ty) => Seq(Edge.Val(value), Edge.Type(ty))
    case ir.Instr.Ptrtoint(value, ty) => Seq(Edge.Val(value), Edge.Type(ty))
    case ir.Instr.Inttoptr(value, ty) => Seq(Edge.Val(value), Edge.Type(ty))
    case ir.Instr.Bitcast (value, ty) => Seq(Edge.Val(value), Edge.Type(ty))
    case ir.Instr.Cast    (value, ty) => Seq(Edge.Val(value), Edge.Type(ty))

    case ir.Instr.Is(value, ty)      => Seq(Edge.Val(value), Edge.Type(ty))
    case ir.Instr.Alloc(ty)          => Seq(Edge.Type(ty))
    case ir.Instr.Salloc(ty, n)      => Seq(Edge.Type(ty), Edge.Val(n))
    case ir.Instr.Phi(merge, values) => Edge.Cf(merge) +: values.map(Edge.Val)
    case ir.Instr.Box(value, ty)     => Seq(Edge.Val(value), Edge.Type(ty))
    case ir.Instr.Unbox(value, ty)   => Seq(Edge.Val(value), Edge.Type(ty))
    case ir.Instr.Length(value)      => Seq(Edge.Val(value))
    case ir.Instr.Elem(ptr, value)   => Seq(Edge.Val(ptr), Edge.Val(value))
    case ir.Instr.Class(ty)          => Seq(Edge.Type(ty))
    case ir.Instr.Param(_, ty)       => Seq(Edge.Type(ty))
    case ir.Instr.ValueOf(defn)      => Seq(Edge.Ref(defn))
    case ir.Instr.ExceptionOf(cf)    => Seq(Edge.Cf(cf))
    case ir.Instr.TagOf(value)       => Seq(Edge.Val(value))
  }
  def of(defn: ir.Defn): Seq[Edge] = {
    val reledges = defn.rels.map(rel => Edge.Rel(rel.defn))
    defn match {
      case _: Defn.Class | _: Defn.Interface | _: Defn.Module | _: Defn.Extern =>
        reledges
      case Defn.Declare(_, ty, in, _) =>
        Edge.Type(ty) +: (in.map(Edge.Instr) ++ reledges)
      case Defn.Define(_, ty, in, out, _) =>
        Edge.Type(ty) +: Edge.Instr(out) +: (in.map(Edge.Instr) ++ reledges)
      case Defn.Field(_, ty, _) =>
        Edge.Type(ty) +: reledges
    }
  }
}
