package scala.scalanative.codegen

import scala.scalanative.build.{Config, Discover, TargetTriple}

private[scalanative] case class PlatformInfo(
    targetTriple: Option[String],
    targetsWindows: Boolean,
    is32Bit: Boolean,
    isMultithreadingEnabled: Boolean,
    useOpaquePointers: Boolean,
    useGCYieldPointTraps: Boolean,
    useCxxExceptions: Boolean,
    alignmentOf64BitPrimitives: Int
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
    useGCYieldPointTraps = config.useTrapBasedGCYieldPoints,
    useCxxExceptions = config.usingCppExceptions,
    alignmentOf64BitPrimitives = alignmentOf64BitPrimitives(
      config.compilerConfig.configuredOrDetectedTriple
    )
  )

  /** Only the i386 System V ABI aligns i64 and f64 to 4 bytes. */
  private def alignmentOf64BitPrimitives(triple: TargetTriple): Int = {
    import TargetTriple._
    if (triple.arch == Arch.x86 && triple.os != OS.Win32) 4 else 8
  }
}
