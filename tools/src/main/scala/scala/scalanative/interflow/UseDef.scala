package scala.scalanative
package interflow

import scala.collection.mutable
import scalanative.nir._
import scalanative.util.unreachable
import scalanative.linker.{Result, Ref}

object UseDef {
  sealed abstract class Def {
    def name: Local
    def deps: mutable.UnrolledBuffer[Def]
    def uses: mutable.UnrolledBuffer[Def]
    var alive: Boolean = false
  }

  final case class InstDef(
      name: Local,
      deps: mutable.UnrolledBuffer[Def],
      uses: mutable.UnrolledBuffer[Def]
  ) extends Def

  final case class BlockDef(
      name: Local,
      deps: mutable.UnrolledBuffer[Def],
      uses: mutable.UnrolledBuffer[Def],
      params: Seq[Def]
  ) extends Def

  private class CollectLocalValDeps extends Transform {
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

    override def onType(ty: Type): Type = ty

  }

  private def collect(inst: Inst): Seq[Local] = {
    val collector = new CollectLocalValDeps
    collector.onInst(inst)
    collector.deps.distinct.toSeq
  }

  private def isPure(inst: Inst) = inst match {
    case Inst.Let(_, Op.Call(_, Val.Global(name, _), _), _) =>
      Whitelist.pure.contains(name)
    case Inst.Let(_, Op.Module(name), _) =>
      Whitelist.pure.contains(name)
    case Inst.Let(_, op, _) if op.isPure =>
      true
    case _ =>
      false
  }

  def apply(cfg: ControlFlow.Graph): Map[Local, Def] = {
    val defs = mutable.Map.empty[Local, Def]
    val blocks = cfg.all

    def enterBlock(n: Local, params: Seq[Local]) = {
      params.foreach(enterInst)
      val deps = mutable.UnrolledBuffer.empty[Def]
      val uses = mutable.UnrolledBuffer.empty[Def]
      val paramDefs = params.map(defs)
      assert(!defs.contains(n))
      defs += ((n, BlockDef(n, deps, uses, paramDefs)))
    }
    def enterInst(n: Local) = {
      val deps = mutable.UnrolledBuffer.empty[Def]
      val uses = mutable.UnrolledBuffer.empty[Def]
      assert(!defs.contains(n), s"duplicate local ids: $n")
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
      enterBlock(block.name, block.params.map(_.name))
      block.insts.foreach {
        case Inst.Let(n, _, unwind) =>
          enterInst(n)
          unwind match {
            case Next.None =>
              ()
            case Next.Unwind(Val.Local(exc, _), _) =>
              enterInst(exc)
            case _ =>
              util.unreachable
          }
        case Inst.Throw(_, Next.Unwind(Val.Local(exc, _), _)) =>
          enterInst(exc)
        case Inst.Unreachable(Next.Unwind(Val.Local(exc, _), _)) =>
          enterInst(exc)
        case _ =>
          ()
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
        case inst =>
          unreachable
      }
    }

    traceAlive(defs(cfg.entry.name))

    defs.toMap
  }

  def eliminateDeadCode(insts: Seq[Inst]): Seq[Inst] = {
    val fresh = Fresh(insts)
    val cfg = ControlFlow.Graph(insts)
    val usedef = UseDef(cfg)
    val buf = new nir.Buffer()(fresh)

    cfg.all.foreach { block =>
      if (usedef(block.name).alive) {
        buf += block.label
        block.insts.foreach {
          case inst @ Inst.Let(n, _, _) =>
            if (usedef(n).alive) buf += inst
          case inst: Inst.Cf =>
            buf += inst
          case _ =>
            ()
        }
      }
    }

    buf.toSeq
  }
}
