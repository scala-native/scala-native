package scala.scalanative.build

/** An object describing how to configure the Scala Native Optimizer. */
sealed trait OptimizerConfig {

  /** The maximum inline depth during the optimization phase.
   */
  def maxInlineDepth: Int

  /** The maximum number of instructions allowed in the caller function */
  def maxCallerSize: Int

  /** The maximum number of instructions allowed in the inlined function */
  def maxCalleeSize: Int

  /** The maximum number of instructions defined in function classifing it as a
   *  small function. Small functions are always inlined in relea value would be
   *  used
   */
  def smallFunctionSize: Int

  /** Create a new config with the given max inline depth. */
  def withMaxInlineDepth(value: Int): OptimizerConfig

  /** Create a new config with the max caller size. */
  def withMaxCallerSize(value: Int): OptimizerConfig

  /** Create a new config with the max callee size. */
  def withMaxCalleeSize(value: Int): OptimizerConfig

  /** Create a new config with the small function size. */
  def withSmallFunctionSize(value: Int): OptimizerConfig

  private[scalanative] def show(indent: String): String

}

object OptimizerConfig {
  def empty: OptimizerConfig =
    Impl(
      maxInlineDepth = 32,
      maxCallerSize = 1024,
      maxCalleeSize = 256,
      smallFunctionSize = 12
    )

  private final case class Impl(
      maxInlineDepth: Int,
      maxCallerSize: Int,
      maxCalleeSize: Int,
      smallFunctionSize: Int
  ) extends OptimizerConfig {

    override def withMaxInlineDepth(value: Int): OptimizerConfig =
      copy(maxInlineDepth = value)

    override def withMaxCallerSize(value: Int): OptimizerConfig =
      copy(maxCallerSize = value)

    override def withMaxCalleeSize(value: Int): OptimizerConfig =
      copy(maxCalleeSize = value)

    override def withSmallFunctionSize(value: Int): OptimizerConfig =
      copy(smallFunctionSize = value)

    override def toString: String = show(indent = " ")
    override private[scalanative] def show(indent: String): String =
      s"""
          |$indent- maxInlineDepth:    $maxInlineDepth functions
          |$indent- smallFunctionSize: $smallFunctionSize instructions
          |$indent- maxCallerSize:     $maxCallerSize instructions
          |$indent- maxCalleeSize:     $maxCalleeSize instructions
          |""".stripMargin
  }
}
