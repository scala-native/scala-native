package scala.scalanative
package optimizer

import tools.Mode

sealed trait Driver {

  /** The compilation mode */
  def mode: Mode

  /** Companion of all the passes in the driver's pipeline. */
  private[scalanative] def passes: Seq[AnyPassCompanion]

  /** Create a copy with given passes. */
  private[scalanative] def withPasses(passes: Seq[AnyPassCompanion]): Driver
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
  def apply(mode: tools.Mode): Driver = {
    val optPasses = mode match {
      case Mode.Debug   => fastOptPasses
      case Mode.Release => fullOptPasses
    }
    new Impl(mode, injectionPasses ++ optPasses ++ loweringPasses)
  }

  /** Create an empty pass-lesss driver. */
  def empty: Driver =
    new Impl(Mode.default, Seq.empty)

  private final class Impl(val mode: Mode, val passes: Seq[AnyPassCompanion])
      extends Driver {
    def withPasses(passes: Seq[AnyPassCompanion]): Driver =
      new Impl(mode, passes)
  }

}
