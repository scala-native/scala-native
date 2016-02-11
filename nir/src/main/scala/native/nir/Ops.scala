package native
package nir

import native.util.unreachable

sealed abstract class Op {
  final def resty: Type = this match {
    case    Op.Unreachable
       | _: Op.Ret
       | _: Op.Throw
       | _: Op.Jump
       | _: Op.If
       | _: Op.Switch
       | _: Op.Invoke => Type.Nothing

    case Op.Call(Type.Function(_, ret), _, _) => ret
    case Op.Call(_, _, _)                     => unreachable
    case Op.Load(ty, _)                       => ty
    case Op.Store(_, _, _)                    => Type.Unit
    case Op.Elem(ty, _, _)                    => Type.Ptr(ty) // todo: ty @ index
    case Op.Extract(ty, _, _)                 => ??? // todo: ty @ index
    case Op.Insert(ty, _, _, _)               => ty
    case Op.Alloca(ty)                        => Type.Ptr(ty)
    case Op.Bin(_, ty, _, _)                  => ty
    case Op.Comp(_, _, _, _)                  => Type.Bool
    case Op.Conv(_, ty, _)                    => ty

    case Op.Alloc(ty)         => ty
    case Op.Field(ty, _, _)   => Type.Ptr(ty)
    case Op.Method(ty, _, _)  => Type.Ptr(ty)
    case Op.Module(n)         => Type.ModuleClass(n)
    case Op.As(ty, _)         => ty
    case Op.Is(_, _)          => Type.Bool
    case Op.ArrAlloc(ty, _)   => Type.ArrayClass(ty)
    case Op.ArrLength(_)      => Type.I32
    case Op.ArrElem(ty, _, _) => Type.Ptr(ty)
    case Op.Copy(v)           => v.ty
  }

  final def vals: Seq[Val] = this match {
    case Op.Unreachable              => Seq()
    case Op.Ret(v)                   => Seq(v)
    case Op.Jump(n)                  => n.args
    case Op.If(v, n1, n2)            => v +: n1.args ++: n2.args
    case Op.Switch(v, n, cases)      => v +: n.args ++: cases.flatMap(_.next.args)
    case Op.Invoke(_, v, vs, n1, n2) => v +: vs ++: n1.args ++: n2.args
    case Op.Throw(v)                 => Seq(v)

    case Op.Call(_, v, vs)        => v +: vs
    case Op.Load(_, v)            => Seq(v)
    case Op.Store(_, v1, v2)      => Seq(v1, v2)
    case Op.Elem(_, v, vs)        => v +: vs
    case Op.Extract(_, v1, v2)    => Seq(v1, v2)
    case Op.Insert(_, v1, v2, v3) => Seq(v1, v2, v3)
    case Op.Alloca(_)             => Seq()
    case Op.Bin(_, _, v1, v2)     => Seq(v1, v2)
    case Op.Comp(_, _, v1, v2)    => Seq(v1, v2)
    case Op.Conv(_, _, v)         => Seq(v)

    case Op.Alloc(_)           => Seq()
    case Op.Field(_, v, _)     => Seq(v)
    case Op.Method(_, v, _)    => Seq(v)
    case Op.Module(n)          => Seq()
    case Op.As(_, v)           => Seq(v)
    case Op.Is(_, v)           => Seq(v)
    case Op.ArrAlloc(_, v)     => Seq(v)
    case Op.ArrLength(v)       => Seq(v)
    case Op.ArrElem(_, v1, v2) => Seq(v1, v2)
    case Op.Copy(v)            => Seq(v)
  }
}
object Op {
  // low-level control-flow
  final case object Unreachable                                                        extends Op
  final case class Ret    (value: Val)                                                 extends Op
  final case class Jump   (next: Next)                                                 extends Op
  final case class If     (value: Val, thenp: Next, elsep: Next)                       extends Op
  final case class Switch (value: Val, default: Next, cases: Seq[Case])                extends Op
  final case class Invoke (ty: Type, ptr: Val, args: Seq[Val], succ: Next, fail: Next) extends Op

  // high-level control-flow
  final case class Throw  (value: Val)                                                 extends Op

  // low-level
  final case class Call   (ty: Type, ptr: Val, args: Seq[Val])          extends Op
  final case class Load   (ty: Type, ptr: Val)                          extends Op
  final case class Store  (ty: Type, ptr: Val, value: Val)              extends Op
  // TODO: ty should be a pointee type, not result elem type
  final case class Elem   (ty: Type, ptr: Val, indexes: Seq[Val])       extends Op
  final case class Extract(ty: Type, aggr: Val, index: Val)             extends Op
  final case class Insert (ty: Type, aggr: Val, value: Val, index: Val) extends Op
  final case class Alloca (ty: Type)                                    extends Op
  final case class Bin    (bin: nir.Bin, ty: Type, l: Val, r: Val)      extends Op
  final case class Comp   (comp: nir.Comp, ty: Type, l: Val, r: Val)    extends Op
  final case class Conv   (conv: nir.Conv, ty: Type, value: Val)        extends Op

  // high-level
  final case class Alloc    (ty: Type)                         extends Op
  final case class Field    (ty: Type, obj: Val, name: Global) extends Op
  final case class Method   (ty: Type, obj: Val, name: Global) extends Op
  final case class Module   (name: Global)                     extends Op
  final case class As       (ty: Type, obj: Val)               extends Op
  final case class Is       (ty: Type, obj: Val)               extends Op
  final case class ArrAlloc (ty: Type, length: Val)            extends Op
  final case class ArrLength(value: Val)                       extends Op
  final case class ArrElem  (ty: Type, value: Val, index: Val) extends Op
  final case class Copy     (value: Val)                       extends Op
}
