package scala.scalanative
package testinterface

private[scalanative] object TargetRunner {

  /** Command used to execute the built binaries, defined in
   *  SCALANATIVE_TARGET_RUNNER env variable (whitespace delimited). The path of
   *  the binary and its arguments are appended to it. Allows to run binaries
   *  that cannot be executed directly on the host (e.g. using an emulator or a
   *  simulator launcher) or to wrap them with additional tools.
   */
  def fromEnv(): Seq[String] =
    sys.env
      .get("SCALANATIVE_TARGET_RUNNER")
      .toList
      .flatMap(_.trim().split("\\s+"))
      .filter(_.nonEmpty)
}
