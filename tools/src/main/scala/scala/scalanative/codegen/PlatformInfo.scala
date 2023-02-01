package scala.scalanative.codegen

import scala.scalanative.build.Config

private[scalanative] case class PlatformInfo(
    targetTriple: Option[String],
    targetsWindows: Boolean
) {
  val sizeOfPtr = 8
  val sizeOfPtrBits = sizeOfPtr * 8
}
object PlatformInfo {
  def apply(config: Config): PlatformInfo = PlatformInfo(
    targetTriple = config.compilerConfig.targetTriple,
    targetsWindows = config.targetsWindows
  )
}
