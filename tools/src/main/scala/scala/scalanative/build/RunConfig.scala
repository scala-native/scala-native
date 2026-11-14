package scala.scalanative
package build

/** Configuration of how the built binaries are executed by the build tool,
 *  e.g. when running the application or its tests. Does not affect the build.
 */
sealed trait RunConfig {

  /** Command used to execute binaries built for a target that cannot be
   *  executed directly on the host, e.g. QEMU user mode emulator or a launcher
   *  for iOS simulator. The path of the binary and its arguments are appended
   *  to it. Empty when the binary should be executed directly.
   */
  def targetRunner: Seq[String]

  /** Create a new config with given command used to execute built binaries. */
  def withTargetRunner(value: Seq[String]): RunConfig
}

object RunConfig {

  /** Default config, binaries are executed directly */
  def empty: RunConfig = Impl(targetRunner = Seq.empty)

  private final case class Impl(targetRunner: Seq[String]) extends RunConfig {
    def withTargetRunner(value: Seq[String]): RunConfig =
      copy(targetRunner = value)

    override def toString: String =
      s"RunConfig(targetRunner: ${targetRunner.mkString("[", ", ", "]")})"
  }
}
