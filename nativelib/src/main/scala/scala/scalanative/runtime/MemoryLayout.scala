package scala.scalanative.runtime

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.runtime.Intrinsics.{castRawSizeToInt, sizeOf}
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

private[runtime] object MemoryLayout {

  /*  Even though it might seem non-idiomatic to use `def` instead of `final val`
   *  for the constants it actual can be faster at runtime. Vals would require
   *  a fieldload operation and loading the module instance. Def would be
   *  evaluated and inlined in the optimizer - it would result with replacing
   *  method call with a constant value.
   */
  @alwaysinline private def PtrSize = castRawSizeToInt(sizeOf[RawPtr])
  @alwaysinline private def IntSize = 4

  private def requiresEnabledMulithreading = throw new IllegalStateException(
    "Field available only in multithreading mode"
  )

  object Rtti {
    @alwaysinline def ClassOffset = 0
    @alwaysinline def LockWordOffset =
      if (isMultithreadingEnabled) PtrSize
      else requiresEnabledMulithreading
    @alwaysinline def IdOffset =
      if (isMultithreadingEnabled) LockWordOffset + PtrSize
      else PtrSize
    @deprecated(since = "0.5.6") @alwaysinline def TraitIdOffset = -1
    @alwaysinline def InterfacesCountOffset = IdOffset + IntSize
    @alwaysinline def InterfacesOffset = InterfacesCountOffset + IntSize
    @alwaysinline def NameOffset = InterfacesOffset + PtrSize

    @deprecated("Not a part of Rtti layout", since = "0.5.6") @alwaysinline
    def SizeOffset = NameOffset + PtrSize
    @deprecated("Not a part of Rtti layout", since = "0.5.6") @alwaysinline
    def IdRangeEndOffset = SizeOffset + IntSize
    @deprecated("Not a part of Rtti layout", since = "0.5.6") @alwaysinline
    def ReferenceMapOffset = IdRangeEndOffset + IntSize

    @alwaysinline def size = NameOffset + PtrSize
  }

  object ClassRtti {
    @alwaysinline def RttiOffset = 0
    @alwaysinline def SizeOffset = RttiOffset + Rtti.size
    // Remaining fields has optional or contain intrinsic data,
    // they should never be accessed in the runtime
  }

  object Object {
    @alwaysinline def RttiOffset = 0
    @alwaysinline def LockWordOffset =
      if (isMultithreadingEnabled) PtrSize
      else requiresEnabledMulithreading
    @alwaysinline def FieldsOffset =
      if (isMultithreadingEnabled) LockWordOffset + PtrSize
      else PtrSize
  }

  object Array {
    @alwaysinline def RttiOffset = 0
    @alwaysinline def LockWordOffset =
      if (isMultithreadingEnabled) PtrSize
      else requiresEnabledMulithreading
    @alwaysinline def LengthOffset =
      if (isMultithreadingEnabled) LockWordOffset + PtrSize
      else PtrSize
    @alwaysinline def StrideOffset = LengthOffset + IntSize
    @alwaysinline def ValuesOffset = StrideOffset + IntSize
  }

}
