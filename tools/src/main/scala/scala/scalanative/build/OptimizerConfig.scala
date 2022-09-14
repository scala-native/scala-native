package scala.scalanative.build

/** An object describing how to configure the Scala Native Optimizer. */
sealed trait OptimizerConfig {

  /** The maximum inline depth during the optimization phase */
  def maxInlineDepth: Option[Int]

  /** The maximum caller and callee size during the optimization phase */
  def maxCallerSize: Int

  /** The maximum callee size that directly does inline */
  def maxInlineSize: Int

  /** Create a new config with the given max inline depth. */
  def withMaxInlineDepth(value: Int): OptimizerConfig

  /** Create a new config with the max caller size. */
  def withMaxCallerSize(value: Int): OptimizerConfig

  /** Create a new config with the max inline size. */
  def withMaxInlineSize(value: Int): OptimizerConfig

}

object OptimizerConfig {
  def empty: OptimizerConfig =
    Impl(
      maxInlineDepth = None,
      maxCallerSize = 8196,
      maxInlineSize = 8
    )

  private final case class Impl(
      maxInlineDepth: Option[Int],
      maxCallerSize: Int,
      maxInlineSize: Int
  ) extends OptimizerConfig {

    /** Create a new config with the given max inline depth. */
    override def withMaxInlineDepth(value: Int): OptimizerConfig =
      copy(maxInlineDepth = Option(value))

    /** Create a new config with the max caller size. */
    override def withMaxCallerSize(value: Int): OptimizerConfig =
      copy(maxCallerSize = value)

    /** Create a new config with the max inline size. */
    override def withMaxInlineSize(value: Int): OptimizerConfig =
      copy(maxInlineSize = value)

    override def toString: String = {
      s"""OptimizerConfig(
         | - maxInlineDepth:     $maxInlineDepth
         | - maxCallerSize:      $maxCallerSize
         | - maxInlineSize:      $maxInlineSize
         |)""".stripMargin
    }
  }
}
