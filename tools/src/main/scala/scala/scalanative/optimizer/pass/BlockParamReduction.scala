package scala.scalanative
package optimizer
package pass

import nir._, Inst.Let
import sema._

class BlockParamReduction extends Pass {
  import BlockParamReduction._

  override def onInsts(insts: Seq[Inst]): Seq[Inst] = {
    val fresh = Fresh(insts)
    val cfg   = ControlFlow.Graph(insts)

    val suppliedArgs = SuppliedArguments(cfg)
    val changeParams = new ParamChanger(suppliedArgs)

    changeParams.onInsts(insts)
  }
}

object BlockParamReduction extends PassCompanion {
  def apply(config: build.Config, linked: linker.Result) =
    new BlockParamReduction

  class ParamChanger(val suppliedArgs: SuppliedArguments) extends Transform {
    /* On block labels, copy the known argument values, and remove the
     * corresponding parameters
     */
    override def onInsts(insts: Seq[Inst]) = insts.flatMap {
      case Inst.Label(name, params) =>
        val paramVals = suppliedArgs.paramValues(name)

        val newParams = params.zip(paramVals).collect {
          case (param, None) => param
        }
        val argCopies = params.zip(paramVals).collect {
          case (param, Some(value)) =>
            Let(param.name, Op.Copy(value), Next.None)
        }

        val newLabel = Inst.Label(name, newParams)
        newLabel +: argCopies

      case cf: Inst.Cf =>
        Seq(super.onInst(cf))

      case inst =>
        Seq(inst)
    }

    // On nexts, ignore the deleted parameters of the target block
    override def onNext(next: Next) = next match {
      case Next.Label(name, args) =>
        val newArgs = args.zip(suppliedArgs.paramValues(name)).collect {
          case (arg, None) => arg
        }
        Next.Label(name, newArgs)

      case _ =>
        next
    }
  }
}
