package scala.scalanative.codegen

import scala.scalanative.build.{Config, Discover}

private[scalanative] case class PlatformInfo(
    targetTriple: Option[String],
    targetsWindows: Boolean,
    is32Bit: Boolean,
    isMultithreadingEnabled: Boolean,
    useOpaquePointers: Boolean,
    useGCYieldPointTraps: Boolean
) {
  val sizeOfPtr = if (is32Bit) 4 else 8
  val sizeOfPtrBits = sizeOfPtr * 8
}
private[scalanative] object PlatformInfo {
  def apply(config: Config): PlatformInfo = PlatformInfo(
    targetTriple = config.compilerConfig.targetTriple,
    targetsWindows = config.targetsWindows,
    is32Bit = config.compilerConfig.is32BitPlatform,
    isMultithreadingEnabled = config.compilerConfig.multithreadingSupport,
    useOpaquePointers =
      Discover.features.opaquePointers(config.compilerConfig).isAvailable,
    useGCYieldPointTraps = config.useTrapBasedGCYieldPoints
  )
}
