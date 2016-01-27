package native
package nir

import native.util.unreachable

sealed abstract class Op {
  final def resty = this match {
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
    case Op.Elem(ty, _, _)                    => Type.Ptr(ty)
    case Op.Extract(ty, _, _)                 => ??? // ty @ index
    case Op.Insert(ty, _, _, _)               => ty
    case Op.Alloca(ty)                        => Type.Ptr(ty)
    case Op.Bin(_, ty, _, _)                  => ty
    case Op.Comp(_, _, _, _)                  => Type.Bool
    case Op.Conv(_, ty, _)                    => ty

    case Op.ObjAlloc        (ty)       => ty
    case Op.ObjFieldElem    (ty, _, _) => Type.Ptr(ty)
    case Op.ObjMethodElem   (ty, _, _) => Type.Ptr(ty)
    case Op.ObjAs           (ty, _)    => ty
    case Op.ObjIs           (_, _)     => Type.Bool
    case Op.ArrAlloc        (ty, _)    => Type.ArrayClass(ty)
    case Op.ArrLength       (_)        => Type.I32
    case Op.ArrElem         (ty, _, _) => Type.Ptr(ty)
    case Op.ClassOf         (_)        => Type.ClassClass
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
  final case class Elem   (ty: Type, ptr: Val, indexes: Seq[Val])       extends Op
  final case class Extract(ty: Type, aggr: Val, index: Val)             extends Op
  final case class Insert (ty: Type, aggr: Val, value: Val, index: Val) extends Op
  final case class Alloca (ty: Type)                                    extends Op
  final case class Bin    (bin: nir.Bin, ty: Type, l: Val, r: Val)      extends Op
  final case class Comp   (comp: nir.Comp, ty: Type, l: Val, r: Val)    extends Op
  final case class Conv   (conv: nir.Conv, ty: Type, value: Val)        extends Op

  // high-level
  final case class ObjAlloc     (ty: Type)                         extends Op
  final case class ObjFieldElem (ty: Type, name: Global, obj: Val) extends Op
  final case class ObjMethodElem(ty: Type, name: Global, obj: Val) extends Op
  final case class ObjAs        (ty: Type, obj: Val)               extends Op
  final case class ObjIs        (ty: Type, obj: Val)               extends Op
  final case class ArrAlloc     (ty: Type, length: Val)            extends Op
  final case class ArrLength    (value: Val)                       extends Op
  final case class ArrElem      (ty: Type, value: Val, index: Val) extends Op
  final case class ClassOf      (ty: Type)                         extends Op
}
