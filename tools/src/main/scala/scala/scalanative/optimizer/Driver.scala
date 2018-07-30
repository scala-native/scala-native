package scala.scalanative
package optimizer

import build.Mode

sealed trait Driver {

  /** Companion of all the passes in the driver's pipeline. */
  def passes: Seq[PassCompanion]

  /** Linker event reporter. */
  def linkerReporter: linker.Reporter

  /** Optimizer event reporter. */
  def optimizerReporter: optimizer.Reporter

  /** Create a copy with given passes. */
  def withPasses(value: Seq[PassCompanion]): Driver

  /** Create a copy of driver with given linker reporter. */
  def withLinkerReporter(value: linker.Reporter): Driver

  /** Create a copy of driver with given linker reporter. */
  def withOptimizerReporter(value: optimizer.Reporter): Driver
}

object Driver {

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

  /** Create driver with default pipeline for this configuration. */
  def default(mode: Mode): Driver = {
    val optPasses = mode match {
      case Mode.Debug   => fastOptPasses
      case Mode.Release => fullOptPasses
    }
    empty.withPasses(optPasses)
  }

  /** Create an empty pass-lesss driver. */
  def empty: Driver =
    new Impl(Seq.empty, linker.Reporter.empty, optimizer.Reporter.empty)

  private final case class Impl(passes: Seq[PassCompanion],
                                linkerReporter: linker.Reporter,
                                optimizerReporter: optimizer.Reporter)
      extends Driver {
    def withPasses(value: Seq[PassCompanion]): Driver =
      copy(passes = value)

    def withLinkerReporter(value: linker.Reporter): Driver =
      copy(linkerReporter = value)

    def withOptimizerReporter(value: optimizer.Reporter): Driver =
      copy(optimizerReporter = value)
  }
}
