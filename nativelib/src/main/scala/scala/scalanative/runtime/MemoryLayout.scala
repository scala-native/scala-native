package scala.scalanative.runtime

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.runtime.Intrinsics.castRawSizeToInt
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

object MemoryLayout {

  /*  Even though it might seem non-idiomatic to use `def` instead of `final val`
   *  for the constants it actual can be faster at runtime. Vals would require
   *  a fieldload operation and loading the module instance. Def would be
   *  evaluated and inlined in the optimizer - it would result with replacing
   *  method call with a constant value.
   */

  @alwaysinline private def PtrSize = castRawSizeToInt(sizeOfPtr)
  @alwaysinline private def IntSize = 4

  private[scalanative] object Rtti {
    @alwaysinline def ClassOffset = 0
    @alwaysinline def IdOffset = ClassOffset + PtrSize
    @alwaysinline def TraitIdOffset = IdOffset + IntSize
    @alwaysinline def NameOffset = TraitIdOffset + IntSize
    @alwaysinline def SizeOffset = NameOffset + PtrSize
    @alwaysinline def IdRangeEndOffset = SizeOffset + IntSize
    @alwaysinline def ReferenceMapOffset = IdRangeEndOffset + IntSize
  }

  private[scalanative] object Object {
    @alwaysinline def RttiOffset = 0
    @alwaysinline def FieldsOffset = RttiOffset + PtrSize
  }

  private[scalanative] object Array {
    @alwaysinline def RttiOffset = 0
    @alwaysinline def LengthOffset = RttiOffset + PtrSize
    @alwaysinline def PaddingOffset = LengthOffset + IntSize
    @alwaysinline def ValuesOffset = PaddingOffset + IntSize
  }

}
