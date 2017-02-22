package scala.scalanative
package nir

import util.unreachable

sealed abstract class Op {
  final def resty: Type = this match {
    case Op.Call(Type.Function(_, ret), _, _, _) => ret
    case Op.Call(_, _, _, _)                     => unreachable
    case Op.Load(ty, _)                          => ty
    case Op.Store(_, _, _)                       => Type.Unit
    case Op.Elem(_, _, _)                        => Type.Ptr
    case Op.Extract(aggr, indexes)               => aggr.ty.elemty(indexes.map(Val.Int(_)))
    case Op.Insert(aggr, _, _)                   => aggr.ty
    case Op.Stackalloc(ty, _)                    => Type.Ptr
    case Op.Bin(_, ty, _, _)                     => ty
    case Op.Comp(_, _, _, _)                     => Type.Bool
    case Op.Conv(_, ty, _)                       => ty
    case Op.Select(_, v, _)                      => v.ty

    case Op.Classalloc(n)     => Type.Class(n)
    case Op.Field(_, _)       => Type.Ptr
    case Op.Method(_, _)      => Type.Ptr
    case Op.Dynmethod(_, _)   => Type.Ptr
    case Op.Module(n, _)      => Type.Module(n)
    case Op.As(ty, _)         => ty
    case Op.Is(_, _)          => Type.Bool
    case Op.Copy(v)           => v.ty
    case Op.Sizeof(_)         => Type.Long
    case Op.Closure(ty, _, _) => ty
    case Op.Box(ty, _)        => ty
    case Op.Unbox(ty, _)      => Type.unbox(ty)
  }

  final def show: String = nir.Show(this)
}
object Op {
  sealed abstract class Pure extends Op
  sealed abstract class Unwind extends Op {
    def unwind: Next
  }

  // low-level
  final case class Call(ty: Type, ptr: Val, args: Seq[Val], unwind: Next)
      extends Unwind
  final case class Load(ty: Type, ptr: Val)                    extends Op
  final case class Store(ty: Type, ptr: Val, value: Val)       extends Op
  final case class Elem(ty: Type, ptr: Val, indexes: Seq[Val]) extends Pure
  final case class Extract(aggr: Val, indexes: Seq[Int])       extends Pure
  final case class Insert(aggr: Val, value: Val, indexes: Seq[Int])
      extends Pure
  final case class Stackalloc(ty: Type, n: Val)                   extends Op
  final case class Bin(bin: nir.Bin, ty: Type, l: Val, r: Val)    extends Pure
  final case class Comp(comp: nir.Comp, ty: Type, l: Val, r: Val) extends Pure
  final case class Conv(conv: nir.Conv, ty: Type, value: Val)     extends Pure
  final case class Select(cond: Val, thenv: Val, elsev: Val)      extends Pure

  // high-level
  final case class Classalloc(name: Global)                        extends Op
  final case class Field(obj: Val, name: Global)                   extends Op
  final case class Method(obj: Val, name: Global)                  extends Op
  final case class Dynmethod(obj: Val, signature: String)          extends Op
  final case class Module(name: Global, unwind: Next)              extends Unwind
  final case class As(ty: Type, obj: Val)                          extends Op
  final case class Is(ty: Type, obj: Val)                          extends Op
  final case class Copy(value: Val)                                extends Op
  final case class Sizeof(ty: Type)                                extends Op
  final case class Closure(ty: Type, fun: Val, captures: Seq[Val]) extends Op
  final case class Box(ty: Type, obj: Val)                         extends Op
  final case class Unbox(ty: Type, obj: Val)                       extends Op
}
