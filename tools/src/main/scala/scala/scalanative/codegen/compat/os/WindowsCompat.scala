package scala.scalanative.codegen.compat.os

import scala.scalanative.codegen.AbstractCodeGen
import scala.scalanative.nir.ControlFlow.Block
import scala.scalanative.nir.{Fresh, Next, Position, Val}
import scala.scalanative.util.ShowBuilder
import scala.scalanative.codegen.llvm.GenIdx
import scala.scalanative.codegen.llvm.DebugInformationSection
import scala.scalanative.nir.Defn
import scala.scalanative.nir.Global

private[codegen] class WindowsCompat(protected val codegen: AbstractCodeGen)
    extends OsCompat {
  import codegen.{pointerType => ptrT}
  val ehWrapperTy = "\"??_R0?AVExceptionWrapper@scalanative@@@8\""
  val ehWrapperName = "c\".?AVExceptionWrapper@scalanative@@\\00\""
  val ehClass = "%\"class.scalanative::ExceptionWrapper\""
  val typeInfo = "\"??_7type_info@@6B@\""
  val stdExceptionClass = "\"class.std::exception\""
  val stdExceptionData = "struct.__std_exception_data"
  val typeDescriptor = "%rtti.TypeDescriptor34"
  val ehVar = "%eslot"

  override protected val osPersonalityType: String = "@__CxxFrameHandler3"

  override def genBlockAlloca(block: Block)(implicit sb: ShowBuilder): Unit = {
    import sb._
    if (block.pred.isEmpty) {
      newline()
      str(s"$ehVar = alloca $ehClass*, align 8")
    }
  }

  override def genPrelude()(implicit sb: ShowBuilder): Unit = {
    import sb._
    def PtrRef = if (useOpaquePointers) ptrT else s"$ptrT*"
    line(s"declare i32 @llvm.eh.typeid.for($ptrT)")
    line(s"declare i32 $osPersonalityType(...)")
    line(s"$typeDescriptor = type { $PtrRef, $ptrT, [35 x i8] }")
    line(s"%$stdExceptionData = type { $ptrT, i8 }")
    if (useOpaquePointers)
      line(s"%$stdExceptionClass = type { $ptrT, %$stdExceptionData }")
    else
      line(s"%$stdExceptionClass = type { i32 (...)**, %$stdExceptionData }")
    line(s"$ehClass = type { %$stdExceptionClass, $ptrT }")
    line(s"@$typeInfo = external constant $ptrT")
    line(s"$$$ehWrapperTy = comdat any")
    line(
      s"@$ehWrapperTy = linkonce_odr global $typeDescriptor { $PtrRef @$typeInfo, $ptrT null, [35 x i8] $ehWrapperName }, comdat"
    )
  }

  override def genLandingPad(
      unwind: Next.Unwind
  )(implicit
      fresh: Fresh,
      pos: Position,
      sb: ShowBuilder,
      dwf: DebugInformationSection.Builder
  ): Unit = {
    import codegen._
    import sb._
    val Next.Unwind(Val.Local(excname, _), next) = unwind

    val excpad = s"_${excname.id}.landingpad"
    val excsucc = excpad + ".succ"

    val exc = "%_" + excname.id
    val rec, w1, w2, cpad = "%_" + fresh().id

    def line(s: String) = { newline(); str(s) }

    line(s"$excpad:")
    indent()
    line(s"$rec = catchswitch within none [label %$excsucc] unwind to caller")
    unindent()

    line(s"$excsucc:")
    indent()
    if (useOpaquePointers) {
      line(
        s"$cpad = catchpad within $rec [ptr @$ehWrapperTy, i32 8, ptr $ehVar]"
      )
      line(s"$w1 = load ptr, ptr $ehVar, align 8")
      line(s"$w2 = getelementptr inbounds $ehClass, ptr $w1, i32 0, i32 1")
      line(s"$exc = load ptr, ptr $w2, align 8")
      line(s"catchret from $cpad to ")
    } else {
      line(
        s"$cpad = catchpad within $rec [$typeDescriptor* @$ehWrapperTy, i32 8, $ehClass** $ehVar]"
      )
      line(s"$w1 = load $ehClass*, $ehClass** $ehVar, align 8")
      line(
        s"$w2 = getelementptr inbounds $ehClass, $ehClass* $w1, i32 0, i32 1"
      )
      line(s"$exc = load i8*, i8** $w2, align 8")
      line(s"catchret from $cpad to ")
    }
    genNext(next)
    unindent()
  }
}
