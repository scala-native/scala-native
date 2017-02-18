package scala.scalanative
package optimizer
package analysis

import scala.collection.mutable
import ClassHierarchy.Top
import ClassHierarchyExtractors._
import nir._
import util.unreachable

object UseDef {
  sealed abstract class Def {
    def name: Local
    def deps: mutable.UnrolledBuffer[Def]
    def uses: mutable.UnrolledBuffer[Def]
    var alive: Boolean = false
  }

  final case class InstDef(name: Local,
                           deps: mutable.UnrolledBuffer[Def],
                           uses: mutable.UnrolledBuffer[Def])
      extends Def

  final case class BlockDef(name: Local,
                            deps: mutable.UnrolledBuffer[Def],
                            uses: mutable.UnrolledBuffer[Def],
                            params: Seq[Def])
      extends Def

  private class CollectLocalValDeps extends Pass {
    val deps = mutable.UnrolledBuffer.empty[Local]

    override def onVal(value: Val) = {
      value match {
        case v @ Val.Local(n, _) =>
          deps += n
        case _ =>
          ()
      }
      super.onVal(value)
    }

    override def onNext(next: Next) = {
      next match {
        case next if next ne Next.None =>
          deps += next.name
        case _ =>
          ()
      }
      super.onNext(next)
    }
  }

  private def collect(inst: Inst): Seq[Local] = {
    val collector = new CollectLocalValDeps
    collector.onInst(inst)
    collector.deps.distinct
  }

  private def isPure(inst: Inst)(implicit top: Top) = inst match {
    case Inst.Let(_, Op.Call(_, Val.Global(Ref(node), _), _, _)) =>
      node.attrs.isPure
    case Inst.Let(_, Op.Module(Ref(node), _)) =>
      node.attrs.isPure
    case Inst.Let(_, _: Op.Pure) =>
      true
    case _ =>
      false
  }

  def apply(cfg: ControlFlow.Graph)(implicit top: Top): Map[Local, Def] = {
    val defs   = mutable.Map.empty[Local, Def]
    val blocks = cfg.all

    def enterBlock(n: Local, params: Seq[Local]) = {
      params.foreach(enterInst)
      val deps      = mutable.UnrolledBuffer.empty[Def]
      val uses      = mutable.UnrolledBuffer.empty[Def]
      val paramDefs = params.map(defs)
      defs += ((n, BlockDef(n, deps, uses, paramDefs)))
    }
    def enterInst(n: Local) = {
      val deps = mutable.UnrolledBuffer.empty[Def]
      val uses = mutable.UnrolledBuffer.empty[Def]
      defs += ((n, InstDef(n, deps, uses)))
    }
    def deps(n: Local, deps: Seq[Local]) = {
      val ndef = defs(n)
      deps.foreach { dep =>
        val ddef = defs(dep)
        ddef.uses += ndef
        ndef.deps += ddef
      }
    }
    def alive(ndef: Def): Unit =
      if (!ndef.alive) {
        ndef.alive = true
        ndef.deps.foreach(alive)
      }

    // enter definitions
    blocks.foreach { block =>
      enterBlock(block.name, block.params.map(_.name))
      block.insts.foreach {
        case Inst.Let(n, _) => enterInst(n)
        case _              => ()
      }
    }

    // enter deps & uses
    blocks.foreach { block =>
      block.insts.foreach {
        case inst: Inst.Let =>
          deps(inst.name, collect(inst))
          if (!isPure(inst)) deps(block.name, Seq(inst.name))
        case inst: Inst.Cf =>
          deps(block.name, collect(inst))
        case Inst.None =>
          ()
        case inst =>
          unreachable
      }
    }

    // trace live code
    alive(defs(cfg.entry.name))

    defs.toMap
  }
}
