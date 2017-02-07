package scala.scalanative
package optimizer

import tools.Mode

sealed trait Driver {

  /** Companion of all the passes in the driver's pipeline. */
  def passes: Seq[PassCompanion]

  /** Take all passes including the given one. */
  def takeUpTo(pass: PassCompanion): Driver

  /** Take all passes including the given one. */
  def takeBefore(pass: PassCompanion): Driver

  /** Append a pass to the pipeline. */
  def append(pass: PassCompanion): Driver
}

object Driver {

  private val fastOptPasses = Seq(
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
    pass.DynmethodLowering,
    pass.MainInjection,
    pass.ExternHoisting,
    pass.ModuleLowering,
    pass.RuntimeTypeInfoInjection,
    pass.BoxingLowering,
    pass.AsLowering,
    pass.IsLowering,
    pass.MethodLowering,
    pass.TraitLowering,
    pass.ClassLowering,
    pass.StringLowering,
    pass.ConstLowering,
    pass.UnitLowering,
    pass.ThrowLowering,
    pass.NothingLowering,
    pass.AllocLowering,
    pass.SizeofLowering,
    pass.CopyPropagation,
    pass.DeadBlockElimination
  )

  /** Create driver with default pipeline for this configuration. */
  def apply(config: tools.Config): Driver = {
    val optPasses = config.mode match {
      case Mode.Debug   => fastOptPasses
      case Mode.Release => fullOptPasses
    }
    new Impl(optPasses ++ loweringPasses)
  }

  /** Create an empty pass-lesss driver. */
  def empty: Driver =
    new Impl(Seq.empty)

  private final class Impl(val passes: Seq[PassCompanion]) extends Driver {
    def takeUpTo(pass: PassCompanion): Driver =
      takeBefore(pass).append(pass)

    def takeBefore(pass: PassCompanion): Driver =
      new Impl(passes takeWhile (_ != pass))

    def append(pass: PassCompanion): Driver =
      new Impl(passes :+ pass)
  }
}
