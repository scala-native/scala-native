package scala.scalanative
package compiler
package analysis

import scala.collection.mutable
import ClassHierarchy.Top
import ClassHierarchyExtractors._
import nir._

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

  private def deps(inst: Inst): Seq[Local] = {
    val collect = new CollectLocalValDeps
    collect(inst)
    collect.deps.distinct
  }

  private def deps(cf: Cf): Seq[Local] = {
    val collect = new CollectLocalValDeps
    collect(cf)
    collect.deps.distinct
  }

  private def isPure(op: Op)(implicit top: Top) = op match {
    case Op.Call(_, Val.Global(Ref(node), _), _) =>
      node.attrs.isPure

    case Op.Module(Ref(node)) =>
      node.attrs.isPure

    case _: Op.Pure =>
      true

    case _ =>
      false
  }

  def apply(blocks: Seq[Block])(implicit top: Top): Map[Local, Def] = {
    val defs = mutable.Map.empty[Local, Def]

    def enterDef(n: Local) = {
      val deps = mutable.UnrolledBuffer.empty[Def]
      val uses = mutable.UnrolledBuffer.empty[Def]
      defs += ((n, Def(n, deps, uses)))
    }
    def enterDeps(n: Local, deps: Seq[Local]) = {
      val ndef = defs(n)
      deps.foreach { dep =>
        val ddef = defs(dep)
        ddef.uses += ndef
        ndef.deps += ddef
      }
    }
    def alive(n: Local): Unit = aliveDef(defs(n))
    def aliveDef(ndef: Def): Unit =
      if (!ndef.alive) {
        ndef.alive = true
        ndef.deps.foreach(aliveDef)
      }

    // enter definitions
    blocks.foreach { block =>
      enterDef(block.name)
      block.params.foreach { param =>
        enterDef(param.name)
      }
      block.insts.foreach {
        case Inst(n, _) => enterDef(n)
        case _          => ()
      }
    }

    // enter deps & uses
    blocks.foreach { block =>
      block.insts.foreach {
        case inst @ Inst(n, _) => enterDeps(n, deps(inst))
      }
      enterDeps(block.name, deps(block.cf))
    }

    // trace live code
    blocks.foreach { block =>
      block.insts.foreach {
        case Inst(n, op) =>
          if (!isPure(op)) alive(n)
      }
      deps(block.cf).foreach(alive)
    }

    defs.toMap
  }
}
