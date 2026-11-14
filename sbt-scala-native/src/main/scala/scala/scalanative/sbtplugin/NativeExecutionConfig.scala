package scala.scalanative
package sbtplugin

/** Configuration of how the built binaries are executed by the sbt plugin, e.g.
 *  by `run` and `test` tasks. Does not affect the build.
 */
sealed trait NativeExecutionConfig {

  /** Command used to execute binaries built for a target that cannot be
   *  executed directly on the host, e.g. QEMU user mode emulator or a launcher
   *  for iOS simulator. The path of the binary and its arguments are appended
   *  to it. Empty when the binary should be executed directly.
   */
  def targetRunner: Seq[String]

  /** Create a new config with given command used to execute built binaries. */
  def withTargetRunner(value: Seq[String]): NativeExecutionConfig
}

object NativeExecutionConfig {

  /** Default config, binaries are executed directly */
  def empty: NativeExecutionConfig = Impl(targetRunner = Seq.empty)

  /** Config using the target runner defined in SCALANATIVE_TARGET_RUNNER env
   *  variable, arguments are delimited by whitespaces.
   */
  def discover(): NativeExecutionConfig = empty.withTargetRunner(
    sys.env
      .get("SCALANATIVE_TARGET_RUNNER")
      .toList
      .flatMap(_.trim().split("\\s+"))
      .filter(_.nonEmpty)
  )

  private final case class Impl(targetRunner: Seq[String])
      extends NativeExecutionConfig {
    def withTargetRunner(value: Seq[String]): NativeExecutionConfig =
      copy(targetRunner = value)

    override def toString: String =
      s"NativeExecutionConfig(targetRunner: ${targetRunner.mkString("[", ", ", "]")})"
  }
}
