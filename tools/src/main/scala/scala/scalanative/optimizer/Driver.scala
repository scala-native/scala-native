package scala.scalanative
package optimizer

import build.Mode

sealed trait Driver {

  /** Companion of all the passes in the driver's pipeline. */
  def passes: Seq[AnyPassCompanion]

  /** Linker event reporter. */
  def linkerReporter: linker.Reporter

  /** Optimizer event reporter. */
  def optimizerReporter: optimizer.Reporter

  /** Create a copy with given passes. */
  def withPasses(value: Seq[AnyPassCompanion]): Driver

  /** Create a copy of driver with given linker reporter. */
  def withLinkerReporter(value: linker.Reporter): Driver

  /** Create a copy of driver with given linker reporter. */
  def withOptimizerReporter(value: optimizer.Reporter): Driver
}

object Driver {

  private val injectionPasses: Seq[InjectCompanion] = Seq(
    inject.Main,
    inject.TraitDispatchTables,
    inject.HasTrait,
    inject.RuntimeTypeInformation,
    inject.ClassStruct,
    inject.ObjectArrayId,
    inject.ModuleArray
  )

  private val fastOptPasses = Seq(
    pass.DeadBlockElimination,
    pass.GlobalBoxingElimination,
    pass.UnitSimplification,
    pass.DeadCodeElimination
  )

  private val fullOptPasses = fastOptPasses ++ Seq(
    pass.BlockParamReduction,
    pass.CfChainsSimplification,
    pass.DeadBlockElimination,
    pass.BasicBlocksFusion,
    pass.Canonicalization,
    pass.PartialEvaluation,
    pass.InstCombine,
    pass.ConstantFolding,
    pass.GlobalValueNumbering
  )

  private val loweringPasses = Seq(
    pass.SRemOverflow,
    pass.DynmethodLowering,
    pass.ExternHoisting,
    pass.ModuleLowering,
    pass.TypeValueLowering,
    pass.BoxingLowering,
    pass.AsLowering,
    pass.IsLowering,
    pass.MethodLowering,
    pass.TraitLowering,
    pass.ClassLowering,
    pass.StringLowering,
    pass.UnitLowering,
    pass.NothingLowering,
    pass.AllocLowering,
    pass.SizeofLowering,
    pass.CopyPropagation,
    pass.DeadCodeElimination
  )

  /** Create driver with default pipeline for this configuration. */
  def default(mode: Mode): Driver = {
    val optPasses = mode match {
      case Mode.Debug   => fastOptPasses
      case Mode.Release => fullOptPasses
    }
    empty.withPasses(injectionPasses ++ optPasses ++ loweringPasses)
  }

  /** Create an empty pass-lesss driver. */
  def empty: Driver =
    new Impl(Seq.empty, linker.Reporter.empty, optimizer.Reporter.empty)

  private final case class Impl(passes: Seq[AnyPassCompanion],
                                linkerReporter: linker.Reporter,
                                optimizerReporter: optimizer.Reporter)
      extends Driver {
    def withPasses(value: Seq[AnyPassCompanion]): Driver =
      copy(passes = value)

    def withLinkerReporter(value: linker.Reporter): Driver =
      copy(linkerReporter = value)

    def withOptimizerReporter(value: optimizer.Reporter): Driver =
      copy(optimizerReporter = value)
  }
}
