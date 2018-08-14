package scala.scalanative
package optimizer
package pass

import nir._, Inst._
import sema._, ControlFlow.Block

class CfChainsSimplification(implicit top: sema.Top) extends Pass {
  import CfChainsSimplification._

  override def onInsts(insts: Seq[Inst]): Seq[Inst] = {
    val fresh  = Fresh(insts)
    val cfg    = ControlFlow.Graph(insts)
    val usedef = UseDef(cfg)
    val method = MethodInfo(cfg, usedef)

    cfg.all.flatMap { b =>
      (b.label +: b.insts.dropRight(1)) ++ simplifyCf(b.insts.last)(method,
                                                                    fresh)
    }
  }

  private def simplifyCf(cfInst: Inst)(implicit method: MethodInfo,
                                       fresh: Fresh): Seq[Inst] = {
    var nonCf     = Seq.empty[Inst]
    var currentCf = cfInst
    var continue  = true

    while (continue) {
      val wholeOptSeq = simplifyCfOnce(currentCf)
      val newCf       = wholeOptSeq.last

      // stop when convergence has been reached
      continue = (newCf != currentCf)
      nonCf ++= wholeOptSeq.dropRight(1)
      currentCf = newCf
    }

    nonCf :+ currentCf
  }

  private def simplifyCfOnce(cfInst: Inst)(implicit method: MethodInfo,
                                           fresh: Fresh): Seq[Inst] = {
    val simpleRes = cfInst match {

      // If the target block of this jump is only a comprised of
      // a single Cf instruction, replace our jump with this next Cf
      case Jump(Next.Label(targetName, args)) =>
        val targetBlock = method.cfg.find(targetName)
        targetBlock.insts match {

          case Seq(nextCf: Cf) =>
            val nextBlockParams = targetBlock.params.map(_.name)

            /* Ensures that the parameters of the target block are only used locally.
             * If this is not the case, this parameter has to be defined, and can't be ignored
             * This test is enough because we know there is only one instruction,
             * which is a Cf
             */
            val canSkip = nextBlockParams.forall { param =>
              val paramUses = method.usedef(param).uses.toSeq
              paramUses.forall(_.name == targetName)
            }

            if (canSkip) {
              val evaluation = nextBlockParams.zip(args).toMap
              val replacer   = new ArgumentReplacer(evaluation)
              replacer.onInst(nextCf)
            } else {
              cfInst
            }

          case _ => cfInst
        }

      case If(Val.True, next, _) =>
        Jump(next)

      case If(Val.False, _, next) =>
        Jump(next)

      case If(cond, thenp, elsep) =>
        If(cond, simplifyIfBranch(thenp), simplifyIfBranch(elsep))

      case Switch(value, default, Seq()) =>
        Jump(default)

      case Switch(value, default, cases) if (staticValue(value)) =>
        val next = cases
          .collectFirst {
            case Next.Case(caseVal, targetName) if (caseVal == value) =>
              Next.Label(targetName, Seq.empty)
          }
          .getOrElse(default)
        Jump(next)

      case Switch(value, default, cases) =>
        Switch(value,
               simplifySwitchCase(default),
               cases.map(simplifySwitchCase(_)))

      case _ => cfInst
    }

    fixIf(simpleRes)
  }

  /* This is necessary to prevent a problem with LLVM, as its phi-functions
   * can't handle two distinct CFG-edges going from the same source block to the
   * same destination block
   */
  private def fixIf(inst: Inst)(implicit fresh: Fresh): Seq[Inst] = {
    inst match {
      // The problem only occurs when the two destination blocks are the same
      case If(cond,
              thenNext @ Next.Label(thenName, thenArgs),
              Next.Label(elseName, elseArgs)) if (thenName == elseName) =>
        // if both branches provide the same arguments, we simply have a jump
        if (thenArgs == elseArgs) {
          Seq(Jump(thenNext))
        }
        // otherwise, we change the `if` to a select (for the argument values)
        // followed by a jump
        else {
          val (newArgs, selects) = thenArgs
            .zip(elseArgs)
            .map {
              case (thenV, elseV) =>
                val freshVar = fresh()
                val selectInst =
                  Let(freshVar, Op.Select(cond, thenV, elseV), Next.None)
                (Val.Local(freshVar, thenV.ty), selectInst)
            }
            .unzip

          selects :+ Jump(Next.Label(thenName, newArgs))
        }

      case _ => Seq(inst)
    }
  }

  /* To simplify a normal `if` branch, imagine it is a simple `jump`, and try to optimize
   * the latter. After that, keep the most optimized `jump` instruction, and get its next
   */
  private def simplifyIfBranch(branch: Next)(implicit method: MethodInfo,
                                             fresh: Fresh): Next = {
    var newBranch       = branch
    var currentCf: Inst = Jump(branch)
    var continue        = true

    while (continue) {
      val optSeq = simplifyCfOnce(currentCf)
      optSeq match {
        // if we have more than one instruction, we can't use the result
        case Seq(Jump(next)) => newBranch = next
        case _               =>
      }
      val newCf = optSeq.last

      // stop when there is more than one instruction, or when convergence is reached
      continue = (optSeq.size == 1 && newCf != currentCf)
      currentCf = newCf
    }

    newBranch
  }

  /* To simplify a switch case, imagine it is a simple `jump`, and try to optimize
   * the latter. After that, keep the most optimized `jump` instruction that has no
   * parameters (not allowed in Next.Case), and get its target block
   */
  private def simplifySwitchCase(swCase: Next)(implicit method: MethodInfo,
                                               fresh: Fresh): Next = {
    swCase match {
      case Next.Case(value, name) => {
        var newLocalJump    = name
        var currentCf: Inst = Jump(Next.Label(name, Seq.empty))
        var continue        = true

        while (continue) {
          val optSeq = simplifyCfOnce(currentCf)
          optSeq match {
            // Can only use the result when there is one instruction and no parameters
            case Seq(Jump(Next.Label(newLocal, Seq()))) =>
              newLocalJump = newLocal
            case _ =>
          }
          val newCf = optSeq.last

          // stop when there is more than one instruction, or when convergence is reached
          continue = (optSeq.size == 1 && currentCf != newCf)
          currentCf = newCf
        }

        Next.Case(value, newLocalJump)
      }

      case _ => swCase
    }
  }

  def staticValue(value: Val): Boolean = {
    value match {
      case _: Val.Byte | _: Val.Short | _: Val.Int | _: Val.Long |
          _: Val.Float | _: Val.Double =>
        true
      case _ => false
    }
  }

}

object CfChainsSimplification extends PassCompanion {
  override def apply(config: build.Config, top: sema.Top) =
    new CfChainsSimplification()(top)

  /** The ArgumentReplacer is used to replace the arguments of a Cf instruction
   * by its concrete evaluation
   */
  class ArgumentReplacer(evaluation: Map[Local, Val]) extends Transform {

    override def onVal(value: Val) = value match {
      case local @ Val.Local(name, _) =>
        evaluation.getOrElse(name, local)
      case _ =>
        super.onVal(value)
    }
  }

  case class MethodInfo(val cfg: ControlFlow.Graph,
                        val usedef: Map[Local, UseDef.Def])

}
