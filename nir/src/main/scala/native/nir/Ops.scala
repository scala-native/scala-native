package native
package nir

import native.util.unreachable

sealed abstract class Op {
  final def resty = this match {
    case    Op.Undefined
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
    case Op.Elem(ty, _, _)                    => Type.Ptr(ty)
    case Op.Extract(ty, _, _)                 => ??? // ty @ index
    case Op.Insert(ty, _, _, _)               => ty
    case Op.Alloc(ty)                         => Type.Ptr(ty)
    case Op.Alloca(ty)                        => Type.Ptr(ty)
    case Op.Size(ty)                          => Type.Size
    case Op.Bin(_, ty, _, _)                  => ty
    case Op.Comp(_, _, _, _)                  => Type.Bool
    case Op.Conv(_, ty, _)                    => ty

    case Op.FieldElem(ty, _, _)  => Type.Ptr(ty)
    case Op.MethodElem(ty, _, _) => Type.Ptr(ty)
    case Op.AllocClass(ty)       => ty
    case Op.AllocArray(ty, _)    => Type.ArrayClass(ty)
    case Op.Equals(_, _)         => Type.Bool
    case Op.HashCode(_)          => Type.I32
    case Op.GetClass(_)          => Type.ClassClass
    case Op.AsInstanceOf(ty, _)  => ty
    case Op.IsInstanceOf(_, _)   => Type.Bool
    case Op.ArrayLength(_)       => Type.I32
    case Op.ArrayElem(ty, _, _)  => ty
    case Op.Box(ty, _)           => ty
    case Op.Unbox(ty, _)         => ty.unboxed
    case Op.MonitorEnter(_)      => Type.Unit
    case Op.MonitorExit(_)       => Type.Unit
    case Op.StringConcat(_, _)   => Type.StringClass
    case Op.ToString(_, _)       => Type.StringClass
    case Op.FromString(ty, _, _) => ty
  }
}
object Op {
  //control-flow
  final case object Undefined                                                extends Op
  final case class Ret    (value: Val)                                       extends Op
  final case class Throw  (value: Val)                                       extends Op
  final case class Jump   (next: Next)                                       extends Op
  final case class If     (value: Val, thenp: Next, elsep: Next)             extends Op
  final case class Switch (value: Val, default: Next, cases: Seq[Case])      extends Op
  final case class Invoke (ptr: Val, args: Seq[Val], succ: Next, fail: Next) extends Op

  //compute
  final case class Call   (ty: Type, ptr: Val, args: Seq[Val])          extends Op
  final case class Load   (ty: Type, ptr: Val)                          extends Op
  final case class Store  (ty: Type, ptr: Val, value: Val)              extends Op
  final case class Elem   (ty: Type, ptr: Val, indexes: Seq[Val])       extends Op
  final case class Extract(ty: Type, aggr: Val, index: Val)             extends Op
  final case class Insert (ty: Type, aggr: Val, value: Val, index: Val) extends Op
  final case class Alloc  (ty: Type)                                    extends Op
  final case class Alloca (ty: Type)                                    extends Op
  final case class Size   (ty: Type)                                    extends Op
  final case class Bin    (bin: nir.Bin, ty: Type, l: Val, r: Val)      extends Op
  final case class Comp   (comp: nir.Comp, ty: Type, l: Val, r: Val)    extends Op
  final case class Conv   (conv: nir.Conv, ty: Type, value: Val)        extends Op

  //scala
  final case class FieldElem   (ty: Type, name: Global, value: Val) extends Op
  final case class MethodElem  (ty: Type, name: Global, value: Val) extends Op
  final case class AllocClass  (ty: Type)                           extends Op
  final case class AllocArray  (ty: Type, length: Val)              extends Op
  final case class Equals      (l: Val, r: Val)                     extends Op
  final case class HashCode    (value: Val)                         extends Op
  final case class GetClass    (value: Val)                         extends Op
  final case class AsInstanceOf(ty: Type, value: Val)               extends Op
  final case class IsInstanceOf(ty: Type, value: Val)               extends Op
  final case class ArrayLength (value: Val)                         extends Op
  final case class ArrayElem   (ty: Type, value: Val, index: Val)   extends Op
  final case class Box         (ty: Type, value: Val)               extends Op
  final case class Unbox       (ty: Type, value: Val)               extends Op
  final case class MonitorEnter(value: Val)                         extends Op
  final case class MonitorExit (value: Val)                         extends Op
  final case class StringConcat(l: Val, r: Val)                     extends Op
  final case class ToString    (v: Val, radix: Val)                 extends Op
  final case class FromString  (ty: Type, s: Val, radix: Val)       extends Op
}
