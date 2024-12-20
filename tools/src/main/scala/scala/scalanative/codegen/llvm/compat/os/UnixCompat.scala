package scala.scalanative
package codegen.llvm
package compat.os

import scala.scalanative.codegen.llvm.AbstractCodeGen
import scala.scalanative.nir.Defn.Define.DebugInfo
import scala.scalanative.util.ShowBuilder

private[codegen] class UnixCompat(codegen: AbstractCodeGen)
    extends OsCompat(codegen) {

  import codegen.{pointerType => ptrT}

  val ehWrapperTy = "@_ZTIPv"
  val excRecTy = s"{ $ptrT, i32 }"
  val beginCatch = "@scalanative_begin_catch"
  // val beginCatch = "@__cxa_begin_catch"
  val endCatch = "@scalanative_end_catch"
  // val endCatch = "@__cxa_end_catch"
  val catchSig =
    if (useOpaquePointers) s"$ptrT $ehWrapperTy"
    else s"i8* $ehWrapperTy"
  val landingpad =
    s"landingpad $excRecTy catch $catchSig"

  // protected val osPersonalityType: String = "@__gxx_personality_v0"
  protected val osPersonalityType: String = "@scalanative_personality"

  override def genBlockAlloca(block: nir.ControlFlow.Block)(implicit
      sb: ShowBuilder
  ): Unit =
    ()

  def genLandingPad(
      unwind: nir.Next.Unwind
  )(implicit
      fresh: nir.Fresh,
      pos: nir.SourcePosition,
      sb: ShowBuilder
  ): Unit = {
    import sb._
    val nir.Next.Unwind(nir.Val.Local(excname, _), next) = unwind

    val excpad = "_" + excname.id + ".landingpad"
    val excsucc = excpad + ".succ"

    val exc = "%_" + excname.id
    val rec, r0, id = "%_" + fresh().id
    val w0, w1, w2 = "%_" + fresh().id

    def line(s: String) = { newline(); str(s) }

    line(s"$excpad:")
    indent()
    line(s"$rec = $landingpad")
    line(s"$r0 = extractvalue $excRecTy $rec, 0")
    line(s"br label %$excsucc")
    unindent()

    line(s"$excsucc:")
    indent()
    line(s"$exc = call $ptrT $beginCatch($ptrT $r0)")
    line(s"call void $endCatch()")
    line("br ")
    codegen.genNext(next)
    unindent()

    // line(s"$excfail:")
    // indent()
    // line(s"resume $excRecTy $rec")
    // unindent()
  }

  def genPrelude()(implicit builder: ShowBuilder): Unit = {
    import builder._
    // line(s"declare i32 @llvm.eh.typeid.for($ptrT)")
    line(s"declare i32 $osPersonalityType(...)")
    line(s"declare $ptrT $beginCatch($ptrT)")
    line(s"declare void $endCatch()")
    line(s"$ehWrapperTy = external constant ptr")
    // line(s"$ehWrapperTy = external constant { $ptrT, $ptrT, $ptrT }")
  }

}
