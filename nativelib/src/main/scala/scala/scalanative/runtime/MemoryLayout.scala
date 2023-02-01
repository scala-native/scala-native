package scala.scalanative.runtime

import scala.scalanative.annotation.alwaysinline

object MemoryLayout {

  /*  Even though it might seem non-idiomatic to use `def` instead of `final val`
   *  for the constants it actual can be faster at runtime. Vals would require
   *  a fieldload operation and loading the module instance. Def would be
   *  evaluated and inlined in the optimizer - it would result with replacing
   *  method call with a constant value.
   */

  @alwaysinline private def PtrSize = 8
  @alwaysinline private def IntSize = 4

  private[scalanative] object Rtti {
    @alwaysinline def ClassOffset = 0
    @alwaysinline def IdOffset = ClassOffset + PtrSize
    @alwaysinline def TraitIdOffset = IdOffset + IntSize
    @alwaysinline def NameOffset = TraitIdOffset + IntSize

    @alwaysinline def size = NameOffset + PtrSize
  }

  private[scalanative] object ClassRtti {
    @alwaysinline def RttiOffset = 0
    @alwaysinline def SizeOffset = RttiOffset + Rtti.size
    // Remaining fields has optional or contain intrinsic data,
    // they should never be accessed in the runtime
  }

  private[scalanative] object Object {
    @alwaysinline def RttiOffset = 0
    @alwaysinline def FieldsOffset = RttiOffset + PtrSize
  }

  private[scalanative] object Array {
    @alwaysinline def RttiOffset = 0
    @alwaysinline def LengthOffset = RttiOffset + PtrSize
    @alwaysinline def StrideOffset = LengthOffset + IntSize
    @alwaysinline def ValuesOffset = StrideOffset + IntSize
  }

}
