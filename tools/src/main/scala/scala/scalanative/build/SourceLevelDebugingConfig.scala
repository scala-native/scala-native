package scala.scalanative.build

import java.nio.file.Path

sealed trait SourceLevelDebuggingConfig {

  /** Shall toolchain enable mechanism for generation for source level debugging
   *  metadata.
   */
  def enabled: Boolean
  def enabled(state: Boolean): SourceLevelDebuggingConfig

  def enableAll: SourceLevelDebuggingConfig
  def disableAll: SourceLevelDebuggingConfig

  /** Shall function contain additional information about source definition.
   *  Enables source positions in stacktraces, but introduces a runtime penalty
   *  for symbols deserialization
   */
  def generateFunctionSourcePositions: Boolean
  def generateFunctionSourcePositions(
      state: Boolean
  ): SourceLevelDebuggingConfig

  /** Shall generate a metadata for local variables, allows to check state of
   *  local variables in debugger. Recommended usage of LLDB with disabled
   *  optimizations.
   */
  def generateLocalVariables: Boolean
  def generateLocalVariables(state: Boolean): SourceLevelDebuggingConfig

  /** List of custom source roots used to map symbols find in binary file (NIR)
   *  with orignal Scala sources
   */
  def customSourceRoots: Seq[Path]
  def withCustomSourceRoots(paths: Seq[Path]): SourceLevelDebuggingConfig

  private[scalanative] def show(indent: String): String

}

object SourceLevelDebuggingConfig {
  def disabled: SourceLevelDebuggingConfig = Impl(false, false, false, Nil)
  def enabled: SourceLevelDebuggingConfig = Impl(true, true, true, Nil)

  private final case class Impl(
      enabled: Boolean,
      private val genFunctionSourcePositions: Boolean,
      private val genLocalVariables: Boolean,
      customSourceRoots: Seq[Path]
  ) extends SourceLevelDebuggingConfig {
    override def enabled(state: Boolean): SourceLevelDebuggingConfig =
      copy(enabled = state)
    override def enableAll: SourceLevelDebuggingConfig = copy(
      enabled = true,
      genFunctionSourcePositions = true,
      genLocalVariables = true
    )
    override def disableAll: SourceLevelDebuggingConfig =
      copy(
        enabled = false,
        genFunctionSourcePositions = false,
        genLocalVariables = false
      )

    override def generateFunctionSourcePositions: Boolean =
      enabled && genFunctionSourcePositions
    override def generateFunctionSourcePositions(
        state: Boolean
    ): SourceLevelDebuggingConfig =
      copy(genFunctionSourcePositions = state)

    override def generateLocalVariables: Boolean = enabled && genLocalVariables
    override def generateLocalVariables(
        state: Boolean
    ): SourceLevelDebuggingConfig =
      copy(genLocalVariables = state)

    override def withCustomSourceRoots(
        paths: Seq[Path]
    ): SourceLevelDebuggingConfig =
      copy(customSourceRoots = paths)

    override def toString: String = show(indent = " ")
    override private[scalanative] def show(indent: String): String = {
      val state = if (enabled) "Enabled" else "Disabled"
      val sourceRoots = customSourceRoots.mkString("[", ", ", "]")
      s"""SourceLevelDebuggingConfig[${state}]
        |$indent- customSourceRoots:       ${sourceRoots}
        |$indent- generateFunctionSourcePositions: $generateFunctionSourcePositions
        |$indent- generateLocalVariables:  $generateLocalVariables
        |$indent)""".stripMargin
    }
  }
}
