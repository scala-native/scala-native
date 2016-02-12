package native
package compiler
package pass

import scala.collection.mutable
import native.nir._
import native.util.ScopedVar, ScopedVar.scoped

/** Eliminates:
 *  - Val.String
 */
trait StringLowering extends Pass {
  private val i8_* = Type.Ptr(Type.I8)

  private val defns = mutable.Map.empty[String, Defn.Var]
  private val locals = new ScopedVar[Map[String, Local]]
  private var id = 0
  private def intrinsify(s: String): Val.Global = {
    if (!defns.contains(s)) {
      val g = Global(s".str.$id")
      val v = Val.Chars(s)
      val defn = Defn.Var(Seq(), g, v.ty, v)
      defns += (s -> defn)
      id += 1
    }
    val defn = defns(s)
    Val.Global(defn.name, Type.Ptr(defn.ty))
  }
  private def stringsOf(op: Op): Set[String] = op.vals.flatMap(stringsOf).toSet
  private def stringsOf(v: Val): Set[String] = v match {
    case Val.String(s)     => Set(s)
    case Val.Struct(_, vs) => vs.flatMap(stringsOf).toSet
    case Val.Array(_, vs)  => vs.flatMap(stringsOf).toSet
    case _                 => Set()
  }

  override def onPostCompilationUnit(defns: Seq[Defn]) = super.onPostCompilationUnit {
    defns ++ this.defns.values
  }

  override def onInstr(instr: Instr) = {
    val strings = stringsOf(instr.op)
    val collected = mutable.UnrolledBuffer.empty[(String, Local)]
    val preamble = strings.toSeq.flatMap { s =>
      val g  = intrinsify(s)
      val n1 = fresh()
      val n2 = fresh()
      collected += (s -> n2)
      Seq(Instr(n1, Op.Elem(Type.I8, g, Seq(Val.I32(0), Val.I32(0)))),
          Instr(n2, Intrinsic.call(Intrinsic.string_fromPtr, Val.Local(n1, i8_*), Val.I32(s.length))))
    }

    scoped(
      locals := collected.toMap
    ) {
      (preamble :+ instr).flatMap(super.onInstr)
    }
  }

  override def onVal(value: Val) = super.onVal(value match {
    case Val.String(v) => Val.Local(locals(v), i8_*)
    case _             => value
  })
}
