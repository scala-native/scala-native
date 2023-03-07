package scala.scalanative
package nir

import scala.util.hashing.MurmurHash3
import util.unreachable

sealed abstract class Op {
  self: Product =>
  override lazy val hashCode = MurmurHash3.productHash(self)

  final def resty: Type = this match {
    case Op.Call(Type.Function(_, ret), _, _) => ret
    case Op.Call(_, _, _)                     => unreachable
    case Op.Load(ty, _, _)                    => ty
    case Op.Store(_, _, _, _)                 => Type.Unit
    case Op.Elem(_, _, _)                     => Type.Ptr
    case Op.Extract(aggr, indexes) => aggr.ty.elemty(indexes.map(Val.Int(_)))
    case Op.Insert(aggr, _, _)     => aggr.ty
    case Op.Stackalloc(ty, _)      => Type.Ptr
    case Op.Bin(_, ty, _, _)       => ty
    case Op.Comp(_, _, _, _)       => Type.Bool
    case Op.Conv(_, ty, _)         => ty
    case Op.Fence(_)               => Type.Unit

    case Op.Classalloc(n)       => Type.Ref(n, exact = true, nullable = false)
    case Op.Fieldload(ty, _, _) => ty
    case Op.Fieldstore(ty, _, _, _) => Type.Unit
    case Op.Field(_, _)             => Type.Ptr
    case Op.Method(_, _)            => Type.Ptr
    case Op.Dynmethod(_, _)         => Type.Ptr
    case Op.Module(n) => Type.Ref(n, exact = true, nullable = false)
    case Op.As(ty, _) => ty
    case Op.Is(_, _)  => Type.Bool
    case Op.Copy(v)   => v.ty
    case Op.SizeOf(_) | Op.AlignmentOf(_) => Type.Size
    case Op.Box(refty: Type.RefKind, _) =>
      val nullable = Type.isPtrBox(refty)
      Type.Ref(refty.className, exact = true, nullable = nullable)
    case Op.Unbox(ty, _)      => Type.unbox(ty)
    case Op.Var(ty)           => Type.Var(ty)
    case Op.Varload(slot)     => val Type.Var(ty) = slot.ty: @unchecked; ty
    case Op.Varstore(slot, _) => Type.Unit
    case Op.Arrayalloc(ty, _) =>
      Type.Ref(Type.toArrayClass(ty), exact = true, nullable = false)
    case Op.Arrayload(ty, _, _)    => ty
    case Op.Arraystore(_, _, _, _) => Type.Unit
    case Op.Arraylength(_)         => Type.Int
    case _ =>
      throw new Exception(s"nir/Ops#resty ${this} not in set of expected Ops.")
  }

  final def show: String = nir.Show(this)

  /** Op is pure if it doesn't have any side-effects, including:
   *
   *  * doesn't throw exceptions * doesn't perform any unsafe reads or writes
   *  from the memory * doesn't call foreign code
   *
   *  Recomputing pure op will always yield to the same result.
   */
  final def isPure: Boolean = this match {
    case _: Op.Elem | _: Op.Extract | _: Op.Insert | _: Op.Comp | _: Op.Conv |
        _: Op.Is | _: Op.Copy | _: Op.SizeOf =>
      true
    // Division and modulo on integers is only pure if
    // divisor is a canonical non-zero value.
    case Op.Bin(Bin.Sdiv | Bin.Udiv | Bin.Srem | Bin.Urem, _: Type.I, _, div) =>
      if (div.isCanonical && !div.isZero) {
        true
      } else {
        false
      }
    case _: Op.Bin =>
      true
    case _ =>
      false
  }

  /** Op is idempotent if re-evaluation of the operation with the same arguments
   *  is going to produce the same results, without any extra side effects as
   *  long as previous evaluation did not throw.
   */
  final def isIdempotent: Boolean = this match {
    case op if op.isPure =>
      true
    // Division and modulo are non-pure but idempotent.
    case op: Op.Bin =>
      true
    case _: Op.Field | _: Op.Method | _: Op.Dynmethod | _: Op.Module |
        _: Op.Box | _: Op.Unbox | _: Op.Arraylength =>
      true
    case _ =>
      false
  }

  final def isCommutative: Boolean = this match {
    case Op.Bin(bin, _, _, _) =>
      import Bin._
      bin match {
        case Iadd | Imul | And | Or | Xor | Fadd | Fmul =>
          true
        case Isub | Fsub | Sdiv | Udiv | Fdiv | Srem | Urem | Frem | Shl |
            Lshr | Ashr =>
          false
      }
    case Op.Comp(comp, _, _, _) =>
      import Comp._
      comp match {
        case Ieq | Ine => true
        case _         => false
      }
    case _ =>
      false
  }
}
object Op {
  // low-level
  final case class Call(ty: Type, ptr: Val, args: Seq[Val]) extends Op
  final case class Load(ty: Type, ptr: Val, syncAttrs: Option[SyncAttrs] = None)
      extends Op
  final case class Store(
      ty: Type,
      ptr: Val,
      value: Val,
      syncAttrs: Option[SyncAttrs] = None
  ) extends Op
  final case class Elem(ty: Type, ptr: Val, indexes: Seq[Val]) extends Op
  final case class Extract(aggr: Val, indexes: Seq[Int]) extends Op
  final case class Insert(aggr: Val, value: Val, indexes: Seq[Int]) extends Op
  final case class Stackalloc(ty: Type, n: Val) extends Op
  final case class Bin(bin: nir.Bin, ty: Type, l: Val, r: Val) extends Op
  final case class Comp(comp: nir.Comp, ty: Type, l: Val, r: Val) extends Op
  final case class Conv(conv: nir.Conv, ty: Type, value: Val) extends Op
  final case class Fence(syncAttrs: SyncAttrs) extends Op

  // high-level
  final case class Classalloc(name: Global) extends Op
  final case class Fieldload(ty: Type, obj: Val, name: Global) extends Op
  final case class Fieldstore(ty: Type, obj: Val, name: Global, value: Val)
      extends Op
  final case class Field(obj: Val, name: Global) extends Op
  final case class Method(obj: Val, sig: Sig) extends Op
  final case class Dynmethod(obj: Val, sig: Sig) extends Op
  final case class Module(name: Global) extends Op
  final case class As(ty: Type, obj: Val) extends Op
  final case class Is(ty: Type, obj: Val) extends Op
  final case class Copy(value: Val) extends Op
  final case class SizeOf(ty: Type) extends Op
  final case class AlignmentOf(ty: Type) extends Op
  final case class Box(ty: Type, obj: Val) extends Op
  final case class Unbox(ty: Type, obj: Val) extends Op
  final case class Var(ty: Type) extends Op
  final case class Varload(slot: Val) extends Op
  final case class Varstore(slot: Val, value: Val) extends Op
  final case class Arrayalloc(ty: Type, init: Val) extends Op
  final case class Arrayload(ty: Type, arr: Val, idx: Val) extends Op
  final case class Arraystore(ty: Type, arr: Val, idx: Val, value: Val)
      extends Op
  final case class Arraylength(arr: Val) extends Op
}
