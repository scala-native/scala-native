package scala.scalanative
package nir

import util.unreachable

sealed abstract class Op {
  final def resty: Type = this match {
    case Op.Call(Type.Function(_, ret), _, _) => ret
    case Op.Call(_, _, _)                     => unreachable
    case Op.Load(ty, _, _)                    => ty
    case Op.Store(_, _, _, _)                 => Type.Unit
    case Op.Elem(_, _, _)                     => Type.Ptr
    case Op.Extract(aggr, indexes)            => aggr.ty.elemty(indexes.map(Val.Int(_)))
    case Op.Insert(aggr, _, _)                => aggr.ty
    case Op.Stackalloc(ty, _)                 => Type.Ptr
    case Op.Bin(_, ty, _, _)                  => ty
    case Op.Comp(_, _, _, _)                  => Type.Bool
    case Op.Conv(_, ty, _)                    => ty
    case Op.Select(_, v, _)                   => v.ty

    case Op.Classalloc(n)           => Type.Class(n)
    case Op.Fieldload(ty, _, _)     => ty
    case Op.Fieldstore(ty, _, _, _) => Type.Unit
    case Op.Method(_, _)            => Type.Ptr
    case Op.Dynmethod(_, _)         => Type.Ptr
    case Op.Module(n)               => Type.Module(n)
    case Op.As(ty, _)               => ty
    case Op.Is(_, _)                => Type.Bool
    case Op.Copy(v)                 => v.ty
    case Op.Sizeof(_)               => Type.Size
    case Op.Closure(ty, _, _)       => ty
    case Op.Box(ty, _)              => ty
    case Op.Unbox(ty, _)            => Type.unbox(ty)
    case Op.Var(ty)                 => Type.Var(ty)
    case Op.Varload(slot)           => val Type.Var(ty) = slot.ty; ty
    case Op.Varstore(slot, _)       => Type.Unit
    case Op.Arrayalloc(ty, _)       => Type.Array(ty)
    case Op.Arrayload(ty, _, _)     => ty
    case Op.Arraystore(_, _, _, _)  => Type.Unit
    case Op.Arraylength(_)          => Type.Int
  }

  final def show: String = nir.Show(this)
}
object Op {
  sealed abstract class Pure extends Op

  // low-level
  final case class Call(ty: Type, ptr: Val, args: Seq[Val])      extends Op
  final case class Load(ty: Type, ptr: Val, isVolatile: Boolean) extends Op
  final case class Store(ty: Type, ptr: Val, value: Val, isVolatile: Boolean)
      extends Op
  final case class Elem(ty: Type, ptr: Val, indexes: Seq[Val])      extends Pure
  final case class Extract(aggr: Val, indexes: Seq[Int])            extends Pure
  final case class Insert(aggr: Val, value: Val, indexes: Seq[Int]) extends Pure
  final case class Stackalloc(ty: Type, n: Val)                     extends Op
  final case class Bin(bin: nir.Bin, ty: Type, l: Val, r: Val)      extends Pure
  final case class Comp(comp: nir.Comp, ty: Type, l: Val, r: Val)   extends Pure
  final case class Conv(conv: nir.Conv, ty: Type, value: Val)       extends Pure
  final case class Select(cond: Val, thenv: Val, elsev: Val)        extends Pure

  def Load(ty: Type, ptr: Val): Load =
    Load(ty, ptr, isVolatile = false)
  def Store(ty: Type, ptr: Val, value: Val): Store =
    Store(ty, ptr, value, isVolatile = false)

  // high-level
  final case class Classalloc(name: Global)                    extends Op
  final case class Fieldload(ty: Type, obj: Val, name: Global) extends Op
  final case class Fieldstore(ty: Type, obj: Val, name: Global, value: Val)
      extends Op
  final case class Method(obj: Val, signature: String)             extends Op
  final case class Dynmethod(obj: Val, signature: String)          extends Op
  final case class Module(name: Global)                            extends Op
  final case class As(ty: Type, obj: Val)                          extends Op
  final case class Is(ty: Type, obj: Val)                          extends Op
  final case class Copy(value: Val)                                extends Op
  final case class Sizeof(ty: Type)                                extends Op
  final case class Closure(ty: Type, fun: Val, captures: Seq[Val]) extends Op
  final case class Box(ty: Type, obj: Val)                         extends Op
  final case class Unbox(ty: Type, obj: Val)                       extends Op
  final case class Var(ty: Type)                                   extends Op
  final case class Varload(slot: Val)                              extends Op
  final case class Varstore(slot: Val, value: Val)                 extends Op
  final case class Arrayalloc(ty: Type, init: Val)                 extends Op
  final case class Arrayload(ty: Type, arr: Val, idx: Val)         extends Op
  final case class Arraystore(ty: Type, arr: Val, idx: Val, value: Val)
      extends Op
  final case class Arraylength(arr: Val) extends Op
}
