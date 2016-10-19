package scala.scalanative
package compiler
package analysis

import scala.collection.mutable
import ClassHierarchy.Top
import ClassHierarchyExtractors._
import nir._
import util.unreachable

object UseDef {
  final case class Def(name: Local,
                       deps: mutable.UnrolledBuffer[Def],
                       uses: mutable.UnrolledBuffer[Def],
                       var alive: Boolean = false)

  private class CollectLocalValDeps extends Pass {
    val deps = mutable.UnrolledBuffer.empty[Local]

    override def preVal = {
      case v @ Val.Local(n, _) =>
        deps += n
        v
    }

    override def preNext = {
      case next =>
        deps += next.name
        next
    }
  }

  private def collect(inst: Inst): Seq[Local] = {
    val collector = new CollectLocalValDeps
    collector(inst)
    collector.deps.distinct
  }

  private def isPure(inst: Inst)(implicit top: Top) = inst match {
    case Inst.Let(_, Op.Call(_, Val.Global(Ref(node), _), _)) =>
      node.attrs.isPure
    case Inst.Let(_, Op.Module(Ref(node))) =>
      node.attrs.isPure
    case Inst.Let(_, _: Op.Pure) =>
      true
    case _ =>
      false
  }

  def apply(cfg: ControlFlow.Graph)(implicit top: Top): Map[Local, Def] = {
    val defs   = mutable.Map.empty[Local, Def]
    val blocks = cfg.all

    def enter(n: Local) = {
      val deps = mutable.UnrolledBuffer.empty[Def]
      val uses = mutable.UnrolledBuffer.empty[Def]
      defs += ((n, Def(n, deps, uses)))
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
      enter(block.name)
      block.params.foreach { param =>
        enter(param.name)
      }
      block.insts.foreach {
        case Inst.Let(n, _) => enter(n)
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
