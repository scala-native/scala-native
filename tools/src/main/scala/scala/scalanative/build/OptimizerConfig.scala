package scala.scalanative.build

/** An object describing how to configure the Scala Native Optimizer. */
sealed trait OptimizerConfig {

  /** The maximum inline depth during the optimization phase. If set to None
   *  inline depth would not be checked.
   */
  def maxInlineDepth: Option[Int]

  /** The maximum caller and callee size during the optimization phase. If set
   *  to None default value would be used.
   */
  def maxCallerSize: Option[Int]

  /** The maximum callee size that directly does inline. If set to None default
   *  value would be used
   */
  def maxInlineSize: Option[Int]

  /** Create a new config with the given max inline depth. */
  def withMaxInlineDepth(value: Int): OptimizerConfig

  /** Create a new config with the max caller size. */
  def withMaxCallerSize(value: Int): OptimizerConfig

  /** Create a new config with the max inline size. */
  def withMaxInlineSize(value: Int): OptimizerConfig

  private[scalanative] def show(indent: String): String

}

object OptimizerConfig {
  def empty: OptimizerConfig =
    Impl(
      maxInlineDepth = None,
      maxCallerSize = None,
      maxInlineSize = None
    )

  private final case class Impl(
      maxInlineDepth: Option[Int],
      maxCallerSize: Option[Int],
      maxInlineSize: Option[Int]
  ) extends OptimizerConfig {

    /** Create a new config with the given max inline depth. */
    override def withMaxInlineDepth(value: Int): OptimizerConfig =
      copy(maxInlineDepth = Option(value))

    /** Create a new config with the max caller size. */
    override def withMaxCallerSize(value: Int): OptimizerConfig =
      copy(maxCallerSize = Option(value))

    /** Create a new config with the max inline size. */
    override def withMaxInlineSize(value: Int): OptimizerConfig =
      copy(maxInlineSize = Option(value))

    override def toString: String = show(indent = " ")
    override private[scalanative] def show(indent: String): String =
      s"""
          |$indent- maxInlineDepth: $maxInlineDepth
          |$indent- maxInlineSize:  ${maxInlineSize.getOrElse("default")}
          |$indent- maxCallerSize:  ${maxCallerSize.getOrElse("default")}
          |""".stripMargin
  }
}
