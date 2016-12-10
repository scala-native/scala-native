package scala.scalanative
package optimizer

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

  /** Create driver with default pipeline. */
  def apply(): Driver =
    new Impl(
      Seq(pass.GlobalBoxingElimination,
          pass.DeadCodeElimination,
          pass.GlobalValueNumbering,
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
          pass.TryLowering,
          pass.AllocLowering,
          pass.SizeofLowering,
          pass.CopyPropagation,
          pass.DeadCodeElimination))

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
