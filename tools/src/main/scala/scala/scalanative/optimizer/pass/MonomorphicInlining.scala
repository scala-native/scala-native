package scala.scalanative
package optimizer
package pass

import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import scala.io.Source

import nir._, Shows._, Inst.Let

/**
 * Inline monomorphic call sites
 */
class MonomorphicInlining(dispatchInfo: Map[String, Seq[Int]])(implicit top: Top) extends Pass {
  import MonomorphicInlining._

  private def findImpl(meth: Method, clss: Class): String =
    if (meth.in == clss) {
      assert(meth.isConcrete, s"Method ${meth.name.id} belongs to a type observed at runtime. The method should be concrete!")
      s"${clss.name.id}::${meth.name.id}"
    }
    else {
      clss.allmethods.filter(m => m.isConcrete && m.name.id == meth.name.id) match {
        case Seq() =>
          ???
        case Seq(m) =>
            m.in match {
              case c: Class if c.isModule =>
                val className = c.name.id.drop("module.".length)
                s"$className::${m.name.id}"
              case other =>
                s"${m.in.name.id}::${m.name.id}"
            }

        case many =>
          many find (_.in == clss) match {
            case Some(m) =>
              s"${clss.name.id}::${m.name.id}"
            case None =>
              ???
          }
      }
    }

  override def preInst = {
    case inst @ Let(n, Op.Method(_, MethodRef(_: Class, meth)))
        if meth.isVirtual =>

      val instname = s"${n.scope}.${n.id}"
      val key = s"$instname:${meth.name.id}"

      dispatchInfo get key getOrElse Seq() flatMap (top classWithId _) match {

        case Seq(clss) =>
          val implName = findImpl(meth, clss)
          Seq(Let(n, Op.Copy(Val.Global(Global.Top(implName), Type.Ptr))))

        case _ =>
          Seq(inst)
      }
  }
}

object MonomorphicInlining extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    config.profileDispatchInfo match {
      case Some(info) if info.exists =>
        val dispatchInfo =
          analysis.DispatchInfoParser(Source.fromFile(info).mkString)
        new MonomorphicInlining(dispatchInfo)(top)
      case None =>
        EmptyPass
    }
}
