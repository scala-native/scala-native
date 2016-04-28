package scala.scalanative
package nir

import util.unreachable

sealed abstract class Op {
  final def resty: Type = this match {
    case Op.Call(Type.Function(_, ret), _, _) => ret
    case Op.Call(_, _, _)                     => unreachable
    case Op.Load(ty, _)                       => ty
    case Op.Store(_, _, _)                    => Type.Unit
    case Op.Elem(ty, _, _)                    => Type.Ptr // todo: ty @ index
    case Op.Extract(aggr, indexes)            => aggr.ty.elemty(indexes)
    case Op.Insert(aggr, _, _)                => aggr.ty
    case Op.Alloca(ty)                        => Type.Ptr
    case Op.Bin(_, ty, _, _)                  => ty
    case Op.Comp(_, _, _, _)                  => Type.Bool
    case Op.Conv(_, ty, _)                    => ty

    case Op.Alloc(ty)         => ty
    case Op.Field(ty, _, _)   => Type.Ptr
    case Op.Method(ty, _, _)  => Type.Ptr
    case Op.Module(n)         => Type.Module(n)
    case Op.As(ty, _)         => ty
    case Op.Is(_, _)          => Type.Bool
    case Op.Copy(v)           => v.ty
    case Op.SizeOf(_)         => Type.Size
    case Op.TypeOf(_)         => Rt.Type
    case Op.Closure(ty, _, _) => ty
  }
}
object Op {
  // low-level
  final case class Call(ty: Type, ptr: Val, args: Seq[Val]) extends Op
  final case class Load(ty: Type, ptr: Val)                 extends Op
  final case class Store(ty: Type, ptr: Val, value: Val)    extends Op
  // TODO: ty should be a pointee type, not result elem type
  final case class Elem(ty: Type, ptr: Val, indexes: Seq[Val])      extends Op
  final case class Extract(aggr: Val, indexes: Seq[Int])            extends Op
  final case class Insert(aggr: Val, value: Val, indexes: Seq[Int]) extends Op
  final case class Alloca(ty: Type)                                 extends Op
  final case class Bin(bin: nir.Bin, ty: Type, l: Val, r: Val)      extends Op
  final case class Comp(comp: nir.Comp, ty: Type, l: Val, r: Val)   extends Op
  final case class Conv(conv: nir.Conv, ty: Type, value: Val)       extends Op

  // high-level
  final case class Alloc(ty: Type)                                 extends Op
  final case class Field(ty: Type, obj: Val, name: Global)         extends Op
  final case class Method(ty: Type, obj: Val, name: Global)        extends Op
  final case class Module(name: Global)                            extends Op
  final case class As(ty: Type, obj: Val)                          extends Op
  final case class Is(ty: Type, obj: Val)                          extends Op
  final case class Copy(value: Val)                                extends Op
  final case class SizeOf(ty: Type)                                extends Op
  final case class TypeOf(ty: Type)                                extends Op
  final case class Closure(ty: Type, fun: Val, captures: Seq[Val]) extends Op
}
