package scala.scalanative
package optimizer
package pass

import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import nir._, Shows._, Inst.Let

/**
 * Inline monomorphic call sites
 */
class MonomorphicInlining(dispatchInfo: Map[String, Seq[String]])(implicit top: Top) extends Pass {
  import MonomorphicInlining._

  override def preInst = {
    case inst @ Let(n, Op.Method(_, MethodRef(_: Class, meth)))
        if meth.isVirtual =>

      val instname = s"${n.scope}.${n.id}"
      val key = s"$instname:${meth.name.id}"

      dispatchInfo get key getOrElse Seq() match {

        case Seq(mono) =>
          val ClassRef(clss) = Global.Top(mono)
          Seq(
            Let(n, Op.Copy(Val.Global(Global.Top(s"${clss.name.id}::${meth.name.id}"), Type.Ptr)))
          )

        case _ =>
          Seq(inst)
      }
  }
}

object MonomorphicInlining extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    config.profileDispatchInfo match {
      case Some(info) =>
        new MonomorphicInlining(Map("src.3:foobar_unit" -> Seq("B")))(top)
      case None =>
        EmptyPass
    }
}
