package scala.scalanative
package codegen.llvm
package compat.os

import scala.scalanative.codegen.llvm.AbstractCodeGen
import scala.scalanative.nir.Defn.Define.DebugInfo
import scala.scalanative.util.ShowBuilder
import scala.scalanative.codegen.llvm.Metadata.DILocation

private[codegen] class UnixCompat(codegen: AbstractCodeGen)
    extends OsCompat(codegen) {

  import codegen.{pointerType => ptrT}

  val excRecTy = s"{ $ptrT, i32 }"

  final val scalanativeCatch = "@scalanative_catch"

  final val cppExceptionWrapperTy = "@_ZTIN11scalanative16ExceptionWrapperE"
  final val cxaBeginCatch = "@__cxa_begin_catch"
  final val cxaEndCatch = "@__cxa_end_catch"

  val usingCppExceptions = codegen.meta.buildConfig.usingCppExceptions

  protected val osPersonalityType: String =
    if (usingCppExceptions) "@__gxx_personality_v0"
    else "@scalanative_personality"

  override def genBlockAlloca(block: nir.ControlFlow.Block)(implicit
      sb: ShowBuilder
  ): Unit =
    ()

  def genLandingPad(
      unwind: nir.Next.Unwind
  )(implicit
      fresh: nir.Fresh,
      pos: nir.SourcePosition,
      scopeId: nir.ScopeId,
      sb: ShowBuilder,
      metaCtx: MetadataCodeGen.Context,
      scopes: MetadataCodeGen.DefnScopes
  ): Unit = {
    import sb._
    val nir.Next.Unwind(nir.Val.Local(excname, _), next) = unwind

    val excpad = "_" + excname.id + ".landingpad"
    val excsucc = excpad + ".succ"

    val exc = "%_" + excname.id
    val rec, r0, id = "%_" + fresh().id
    val w0, w1, w2 = "%_" + fresh().id

    def line(s: String) = { newline(); str(s) }

    def genScalaNativeLandingPad(): Unit = {
      line(s"$excpad:")
      indent()
      // Catch all exception
      line(s"$rec = landingpad $excRecTy catch $ptrT null")
      line(s"$r0 = extractvalue $excRecTy $rec, 0")
      line(s"br label %$excsucc")
      unindent()

      line(s"$excsucc:")
      indent()
      line(s"$exc = call $ptrT $scalanativeCatch($ptrT $r0)")
      codegen.dbg(",", codegen.toDILocation(pos, scopeId))
      line("br ")
      codegen.genNext(next)
      unindent()
    }

    def genCppLandingPad(): Unit = {
      val catchSig =
        if (useOpaquePointers) s"$ptrT $cppExceptionWrapperTy"
        else s"i8* bitcast ({ i8*, i8*, i8* }* $cppExceptionWrapperTy to i8*)"
      val r1, cmp = "%_" + fresh().id
      val excfail = excpad + ".fail"

      line(s"$excpad:")
      indent()
      line(s"$rec = landingpad $excRecTy catch $catchSig")
      line(s"$r0 = extractvalue $excRecTy $rec, 0")
      line(s"$r1 = extractvalue $excRecTy $rec, 1")
      line(s"$id = call i32 @llvm.eh.typeid.for($catchSig)")
      line(s"$cmp = icmp eq i32 $r1, $id")
      line(s"br i1 $cmp, label %$excsucc, label %$excfail")
      unindent()

      line(s"$excsucc:")
      indent()
      line(s"$w0 = call $ptrT $cxaBeginCatch($ptrT $r0)")
      if (useOpaquePointers) {
        line(s"$w2 = getelementptr ptr, ptr $w0, i32 1")
        line(s"$exc = load ptr, ptr $w2")
      } else {
        line(s"$w1 = bitcast i8* $w0 to i8**")
        line(s"$w2 = getelementptr i8*, i8** $w1, i32 1")
        line(s"$exc = load i8*, i8** $w2")
      }
      line(s"call void $cxaEndCatch()")
      str("br ")
      codegen.genNext(next)
      unindent()

      line(s"$excfail:")
      indent()
      line(s"resume $excRecTy $rec")
      unindent()
    }

    if (usingCppExceptions) genCppLandingPad()
    else genScalaNativeLandingPad()
  }

  def genPrelude()(implicit builder: ShowBuilder): Unit = {
    import builder._
    line(s"declare i32 $osPersonalityType(...)")
    if (usingCppExceptions) {
      line(s"declare i32 @llvm.eh.typeid.for($ptrT)")
      line(s"declare $ptrT $cxaBeginCatch($ptrT)")
      line(s"declare void $cxaEndCatch()")
      line(
        s"$cppExceptionWrapperTy = external constant { $ptrT, $ptrT, $ptrT }"
      )
    } else {
      line(s"declare $ptrT $scalanativeCatch($ptrT)")
    }
  }

}
