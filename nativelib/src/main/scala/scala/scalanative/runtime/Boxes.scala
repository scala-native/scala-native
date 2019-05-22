package scala.scalanative
package runtime

import scalanative.unsigned._
import scalanative.unsafe._

object Boxes {
  @inline def boxToUByte(v: Byte): UByte    = new UByte(v)
  @inline def boxToUShort(v: Short): UShort = new UShort(v)
  @inline def boxToUInt(v: Int): UInt       = new UInt(v)
  @inline def boxToULong(v: Long): ULong    = new ULong(v)
  @inline def boxToPtr[T](v: RawPtr): Ptr[T] =
    if (v == null) null else new Ptr[T](v)
  @inline def boxToCArray[T, N <: Nat](v: RawPtr): CArray[T, N] =
    if (v == null) null else new CArray[T, N](v)
  @inline def boxToCFuncRawPtr(v: RawPtr): CFuncRawPtr =
    if (v == null) null else new CFuncRawPtr(v)
  @inline def boxToCVarArgList(v: RawPtr): CVarArgList =
    if (v == null) null else new CVarArgList(v)

  @inline def unboxToUByte(o: java.lang.Object): Byte =
    if (o == null) 0.toByte
    else o.asInstanceOf[UByte].underlying
  @inline def unboxToUShort(o: java.lang.Object): Short =
    if (o == null) 0.toShort
    else o.asInstanceOf[UShort].underlying
  @inline def unboxToUInt(o: java.lang.Object): Int =
    if (o == null) 0.toInt
    else o.asInstanceOf[UInt].underlying
  @inline def unboxToULong(o: java.lang.Object): Long =
    if (o == null) 0.toLong
    else o.asInstanceOf[ULong].underlying
  @inline def unboxToPtr(o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[Ptr[_]].rawptr
  @inline def unboxToCArray(o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CArray[_, _]].rawptr
  @inline def unboxToCFuncRawPtr(o: java.lang.Object): RawPtr =
    if (o == null) {
      null
    } else if (o.isInstanceOf[CFuncRawPtr]) {
      o.asInstanceOf[CFuncRawPtr].rawptr
    } else {
      Intrinsics.resolveCFuncPtr(o.asInstanceOf[CFuncPtr])
    }
  @inline def unboxToCVarArgList(o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CVarArgList].rawptr
}
