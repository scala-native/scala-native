// format: off

// BEWARE: This file is generated - direct edits will be lost.
// Do not edit this it directly other than to remove
// personally identifiable information in sourceLocation lines.
// All direct edits to this file will be lost the next time it
// is generated.
//
// To generate this file manually execute the python scripts/gyb.py
// script under the project root. For example, from the project root:
//
//   scripts/gyb.py \
//     nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb \
//     --line-directive '' \
//     -o /nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala
//
//  After executing the script, you may want to edit this file to remove
//  personally or build-system specific identifiable information.
//
//  The order elements in the output file depends upon the Python version
//  used to execute the gyb.py. Arrays.scala.gyb has a BEWARE: comment near
//  types.items() which gives details.
//
//  Python >= 3.6 should give a reproducible output order and reduce trivial
//  & annoying git differences.

package scala.scalanative
package runtime

import scalanative.unsigned._
import scalanative.unsafe._

object Boxes {
  @inline def boxToSize(v: RawSize): Size   = new Size(v)
  @inline def boxToUSize(v: RawSize): USize = new USize(v)

  @inline def boxToUnsignedInt(v: NewUInt): UnsignedInt = new UnsignedInt(v)
  @inline def unboxToUnsignedInt(v: java.lang.Object): NewUInt = 
    if(v == null) NewUInt.unsigned(0) 
    else v.asInstanceOf[UnsignedInt].value  

  @inline def unboxToSize(o: java.lang.Object): RawSize =
    if (o == null) Intrinsics.castIntToRawSize(0)
    else o.asInstanceOf[Size].rawSize
  @inline def unboxToUSize(o: java.lang.Object): RawSize =
    if (o == null) Intrinsics.castIntToRawSize(0)
    else o.asInstanceOf[USize].rawSize

    
  @inline def boxToUByte(v: Byte): UByte = new UByte(v)
  @inline def unboxToUByte(o: java.lang.Object): Byte =
    if (o == null) 0.toByte else o.asInstanceOf[UByte].underlying

  @inline def boxToUShort(v: Short): UShort = new UShort(v)
  @inline def unboxToUShort(o: java.lang.Object): Short =
    if (o == null) 0.toShort else o.asInstanceOf[UShort].underlying

  @inline def boxToUInt(v: Int): UInt = new UInt(v)
  @inline def unboxToUInt(o: java.lang.Object): Int =
    if (o == null) 0.toInt else o.asInstanceOf[UInt].underlying

  @inline def boxToULong(v: Long): ULong = new ULong(v)
  @inline def unboxToULong(o: java.lang.Object): Long =
    if (o == null) 0.toLong else o.asInstanceOf[ULong].underlying

  @inline def boxToPtr[T](v: RawPtr): Ptr[T] =
    if (v == null) null else new Ptr[T](v)
  @inline def unboxToPtr(o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[Ptr[_]].rawptr

  @inline def boxToCArray[T, N <: Nat](v: RawPtr): CArray[T, N] =
    if (v == null) null else new CArray[T, N](v)
  @inline def unboxToCArray(o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CArray[_, _]].rawptr

  @inline def boxToCVarArgList(v: RawPtr): CVarArgList =
    if (v == null) null else new CVarArgList(v)
  @inline def unboxToCVarArgList(o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CVarArgList].rawptr

  @inline def boxToCFuncPtr0[R](v: RawPtr): CFuncPtr0[R] =
    if (v == null) null else CFuncPtr0.fromRawPtr[R](v)
  @inline def unboxToCFuncPtr0[R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr1[T1, R](v: RawPtr): CFuncPtr1[T1, R] =
    if (v == null) null else CFuncPtr1.fromRawPtr[T1, R](v)
  @inline def unboxToCFuncPtr1[T1, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr2[T1, T2, R](v: RawPtr): CFuncPtr2[T1, T2, R] =
    if (v == null) null else CFuncPtr2.fromRawPtr[T1, T2, R](v)
  @inline def unboxToCFuncPtr2[T1, T2, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr3[T1, T2, T3, R](v: RawPtr): CFuncPtr3[T1, T2, T3, R] =
    if (v == null) null else CFuncPtr3.fromRawPtr[T1, T2, T3, R](v)
  @inline def unboxToCFuncPtr3[T1, T2, T3, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr4[T1, T2, T3, T4, R](v: RawPtr): CFuncPtr4[T1, T2, T3, T4, R] =
    if (v == null) null else CFuncPtr4.fromRawPtr[T1, T2, T3, T4, R](v)
  @inline def unboxToCFuncPtr4[T1, T2, T3, T4, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr5[T1, T2, T3, T4, T5, R](v: RawPtr): CFuncPtr5[T1, T2, T3, T4, T5, R] =
    if (v == null) null else CFuncPtr5.fromRawPtr[T1, T2, T3, T4, T5, R](v)
  @inline def unboxToCFuncPtr5[T1, T2, T3, T4, T5, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr6[T1, T2, T3, T4, T5, T6, R](v: RawPtr): CFuncPtr6[T1, T2, T3, T4, T5, T6, R] =
    if (v == null) null else CFuncPtr6.fromRawPtr[T1, T2, T3, T4, T5, T6, R](v)
  @inline def unboxToCFuncPtr6[T1, T2, T3, T4, T5, T6, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr7[T1, T2, T3, T4, T5, T6, T7, R](v: RawPtr): CFuncPtr7[T1, T2, T3, T4, T5, T6, T7, R] =
    if (v == null) null else CFuncPtr7.fromRawPtr[T1, T2, T3, T4, T5, T6, T7, R](v)
  @inline def unboxToCFuncPtr7[T1, T2, T3, T4, T5, T6, T7, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr8[T1, T2, T3, T4, T5, T6, T7, T8, R](v: RawPtr): CFuncPtr8[T1, T2, T3, T4, T5, T6, T7, T8, R] =
    if (v == null) null else CFuncPtr8.fromRawPtr[T1, T2, T3, T4, T5, T6, T7, T8, R](v)
  @inline def unboxToCFuncPtr8[T1, T2, T3, T4, T5, T6, T7, T8, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](v: RawPtr): CFuncPtr9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R] =
    if (v == null) null else CFuncPtr9.fromRawPtr[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](v)
  @inline def unboxToCFuncPtr9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](v: RawPtr): CFuncPtr10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R] =
    if (v == null) null else CFuncPtr10.fromRawPtr[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](v)
  @inline def unboxToCFuncPtr10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R](v: RawPtr): CFuncPtr11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R] =
    if (v == null) null else CFuncPtr11.fromRawPtr[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R](v)
  @inline def unboxToCFuncPtr11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R](v: RawPtr): CFuncPtr12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R] =
    if (v == null) null else CFuncPtr12.fromRawPtr[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R](v)
  @inline def unboxToCFuncPtr12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R](v: RawPtr): CFuncPtr13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R] =
    if (v == null) null else CFuncPtr13.fromRawPtr[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R](v)
  @inline def unboxToCFuncPtr13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R](v: RawPtr): CFuncPtr14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R] =
    if (v == null) null else CFuncPtr14.fromRawPtr[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R](v)
  @inline def unboxToCFuncPtr14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R](v: RawPtr): CFuncPtr15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R] =
    if (v == null) null else CFuncPtr15.fromRawPtr[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R](v)
  @inline def unboxToCFuncPtr15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R](v: RawPtr): CFuncPtr16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R] =
    if (v == null) null else CFuncPtr16.fromRawPtr[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R](v)
  @inline def unboxToCFuncPtr16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R](v: RawPtr): CFuncPtr17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R] =
    if (v == null) null else CFuncPtr17.fromRawPtr[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R](v)
  @inline def unboxToCFuncPtr17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R](v: RawPtr): CFuncPtr18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R] =
    if (v == null) null else CFuncPtr18.fromRawPtr[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R](v)
  @inline def unboxToCFuncPtr18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R](v: RawPtr): CFuncPtr19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R] =
    if (v == null) null else CFuncPtr19.fromRawPtr[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R](v)
  @inline def unboxToCFuncPtr19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R](v: RawPtr): CFuncPtr20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R] =
    if (v == null) null else CFuncPtr20.fromRawPtr[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R](v)
  @inline def unboxToCFuncPtr20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R](v: RawPtr): CFuncPtr21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R] =
    if (v == null) null else CFuncPtr21.fromRawPtr[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R](v)
  @inline def unboxToCFuncPtr21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

  @inline def boxToCFuncPtr22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R](v: RawPtr): CFuncPtr22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R] =
    if (v == null) null else CFuncPtr22.fromRawPtr[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R](v)
  @inline def unboxToCFuncPtr22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R](o: java.lang.Object): RawPtr =
    if (o == null) null else o.asInstanceOf[CFuncPtr].rawptr

}
