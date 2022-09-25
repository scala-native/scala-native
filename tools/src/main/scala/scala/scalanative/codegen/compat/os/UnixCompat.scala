package scala.scalanative.codegen.compat.os

import scala.scalanative.codegen.AbstractCodeGen
import scala.scalanative.nir.ControlFlow.Block
import scala.scalanative.nir._
import scala.scalanative.util.ShowBuilder
import scala.scalanative.codegen.dwarf.GenIdx
import scala.scalanative.codegen.dwarf.DwarfSection

private[codegen] class UnixCompat(codeGen: AbstractCodeGen) extends OsCompat {
  val ehWrapperTy = "@_ZTIN11scalanative16ExceptionWrapperE"
  val excRecTy = "{ i8*, i32 }"
  val beginCatch = "@__cxa_begin_catch"
  val endCatch = "@__cxa_end_catch"
  val landingpad =
    s"landingpad $excRecTy catch i8* bitcast ({ i8*, i8*, i8* }* $ehWrapperTy to i8*)"
  val typeid =
    s"call i32 @llvm.eh.typeid.for(i8* bitcast ({ i8*, i8*, i8* }* $ehWrapperTy to i8*))"

  protected val osPersonalityType: String = "@__gxx_personality_v0"

  override def genBlockAlloca(block: Block)(implicit sb: ShowBuilder): Unit =
    ()

  def genLandingPad(
      unwind: Next.Unwind
  )(implicit
      fresh: Fresh,
      pos: Position,
      sb: ShowBuilder,
      gidx: GenIdx,
      dwf: DwarfSection.Builder[Global]
  ): Unit = {
    import sb._
    val Next.Unwind(Val.Local(excname, _), next) = unwind

    val excpad = "_" + excname.id + ".landingpad"
    val excsucc = excpad + ".succ"
    val excfail = excpad + ".fail"

    val exc = "%_" + excname.id
    val rec, r0, r1, id, cmp = "%_" + fresh().id
    val w0, w1, w2 = "%_" + fresh().id

    def line(s: String) = { newline(); str(s) }

    line(s"$excpad:")
    indent()
    line(s"$rec = $landingpad")
    line(s"$r0 = extractvalue $excRecTy $rec, 0")
    line(s"$r1 = extractvalue $excRecTy $rec, 1")
    line(s"$id = $typeid")
    line(s"$cmp = icmp eq i32 $r1, $id")
    line(s"br i1 $cmp, label %$excsucc, label %$excfail")
    unindent()

    line(s"$excsucc:")
    indent()
    line(s"$w0 = call i8* $beginCatch(i8* $r0)")
    line(s"$w1 = bitcast i8* $w0 to i8**")
    line(s"$w2 = getelementptr i8*, i8** $w1, i32 1")
    line(s"$exc = load i8*, i8** $w2")
    line(s"call void $endCatch()")
    codeGen.genInst(Inst.Jump(next))
    unindent()

    line(s"$excfail:")
    indent()
    line(s"resume $excRecTy $rec")
    unindent()
  }

  def genPrelude()(implicit builder: ShowBuilder): Unit = {
    import builder._
    line("declare i32 @llvm.eh.typeid.for(i8*)")
    line(s"declare i32 $osPersonalityType(...)")
    line(s"declare i8* $beginCatch(i8*)")
    line(s"declare void $endCatch()")
    line(s"$ehWrapperTy = external constant { i8*, i8*, i8* }")
  }
}
