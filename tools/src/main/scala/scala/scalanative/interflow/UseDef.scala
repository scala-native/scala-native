package scala.scalanative
package interflow

import scala.collection.mutable
import scalanative.util.unreachable
import scalanative.linker.Ref

object UseDef {

  sealed abstract class Def {
    def id: nir.Local
    def deps: mutable.UnrolledBuffer[Def]
    def uses: mutable.UnrolledBuffer[Def]
    var alive: Boolean = false
  }

  final case class InstDef(
      id: nir.Local,
      deps: mutable.UnrolledBuffer[Def],
      uses: mutable.UnrolledBuffer[Def]
  ) extends Def

  final case class BlockDef(
      id: nir.Local,
      deps: mutable.UnrolledBuffer[Def],
      uses: mutable.UnrolledBuffer[Def],
      params: Seq[Def]
  ) extends Def

  private class CollectLocalValDeps extends nir.Transform {
    val deps = mutable.UnrolledBuffer.empty[nir.Local]

    override def onVal(value: nir.Val) = {
      value match {
        case v @ nir.Val.Local(n, _) =>
          deps += n
        case _ =>
          ()
      }
      super.onVal(value)
    }

    override def onNext(next: nir.Next) = {
      next match {
        case next if next ne nir.Next.None =>
          deps += next.id
        case _ =>
          ()
      }
      super.onNext(next)
    }

    override def onType(ty: nir.Type): nir.Type = ty

  }

  private def collect(inst: nir.Inst): Seq[nir.Local] = {
    val collector = new CollectLocalValDeps
    collector.onInst(inst)
    collector.deps.distinct.toSeq
  }

  private def isPure(inst: nir.Inst) = inst match {
    case nir.Inst.Let(_, nir.Op.Call(_, nir.Val.Global(name, _), _), _) =>
      Allowlist.pure.contains(name)
    case nir.Inst.Let(_, nir.Op.Module(name), _) =>
      Allowlist.pure.contains(name)
    case nir.Inst.Let(_, op, _) if op.isPure =>
      true
    case _ =>
      false
  }

  def apply(cfg: nir.ControlFlow.Graph): Map[nir.Local, Def] = {
    val defs = mutable.Map.empty[nir.Local, Def]
    val blocks = cfg.all

    def enterBlock(n: nir.Local, params: Seq[nir.Local]) = {
      params.foreach(enterInst)
      val deps = mutable.UnrolledBuffer.empty[Def]
      val uses = mutable.UnrolledBuffer.empty[Def]
      val paramDefs = params.map(defs)
      assert(!defs.contains(n))
      defs += ((n, BlockDef(n, deps, uses, paramDefs)))
    }
    def enterInst(n: nir.Local) = {
      val deps = mutable.UnrolledBuffer.empty[Def]
      val uses = mutable.UnrolledBuffer.empty[Def]
      assert(!defs.contains(n), s"duplicate local ids: $n")
      defs += ((n, InstDef(n, deps, uses)))
    }
    def deps(n: nir.Local, deps: Seq[nir.Local]) = {
      val ndef = defs(n)
      deps.foreach { dep =>
        val ddef = defs(dep)
        ddef.uses += ndef
        ndef.deps += ddef
      }
    }
    def traceAlive(ndef: Def): Unit = {
      val todo = mutable.Queue(ndef)
      while (todo.nonEmpty) {
        val ndef = todo.dequeue()
        if (!ndef.alive) {
          ndef.alive = true
          todo ++= ndef.deps
        }
      }
    }

    // enter definitions
    blocks.foreach { block =>
      enterBlock(block.id, block.params.map(_.id))
      block.insts.foreach {
        case nir.Inst.Let(n, _, unwind) =>
          enterInst(n)
          unwind match {
            case nir.Next.None =>
              ()
            case nir.Next.Unwind(nir.Val.Local(exc, _), _) =>
              enterInst(exc)
            case _ =>
              util.unreachable
          }
        case nir.Inst.Throw(_, nir.Next.Unwind(nir.Val.Local(exc, _), _)) =>
          enterInst(exc)
        case nir.Inst.Unreachable(nir.Next.Unwind(nir.Val.Local(exc, _), _)) =>
          enterInst(exc)
        case _ => ()
      }
    }

    // enter deps & uses
    blocks.foreach { block =>
      block.insts.foreach {
        case inst: nir.Inst.Let =>
          deps(inst.id, collect(inst))
          if (!isPure(inst)) deps(block.id, Seq(inst.id))
        case inst: nir.Inst.Cf =>
          deps(block.id, collect(inst))
        case inst =>
          unreachable
      }
    }

    traceAlive(defs(cfg.entry.id))

    defs.toMap
  }

  def eliminateDeadCode(insts: Seq[nir.Inst]): Seq[nir.Inst] = {
    val fresh = nir.Fresh(insts)
    val cfg = nir.ControlFlow.Graph(insts)
    val usedef = UseDef(cfg)
    val buf = new nir.Buffer()(fresh)

    cfg.all.foreach { block =>
      if (usedef(block.id).alive) {
        buf += block.label
        block.insts.foreach {
          case inst @ nir.Inst.Let(n, _, _) =>
            if (usedef(n).alive) buf += inst
          case inst: nir.Inst.Cf =>
            buf += inst
          case _ =>
            ()
        }
      }
    }

    buf.toSeq
  }

}
