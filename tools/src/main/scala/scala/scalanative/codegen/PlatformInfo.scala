package scala.scalanative.codegen

import scala.scalanative.build.{Config, Discover}

private[scalanative] case class PlatformInfo(
    targetTriple: Option[String],
    targetsWindows: Boolean,
    useOpaquePointers: Boolean
) {
  val sizeOfPtr = 8
  val sizeOfPtrBits = sizeOfPtr * 8
}
object PlatformInfo {
  def apply(config: Config): PlatformInfo = PlatformInfo(
    targetTriple = config.compilerConfig.targetTriple,
    targetsWindows = config.targetsWindows,
    useOpaquePointers =
      Discover.features.opaquePointers(config.compilerConfig).isAvailable
  )
}
