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

  /** ABI alignment of i64 and f64, needs to match LLVM data layout of the
   *  target. Only the i386 System V ABI aligns them to 4 bytes, other 32-bit
   *  ABIs (ARM EABI, Windows x86, MIPS O32, PowerPC) use natural alignment.
   */
  private def alignmentOf64BitPrimitives(triple: TargetTriple): Int = {
    import TargetTriple._
    if (isArch64Bit(triple.arch)) 8
    else if (triple.arch == Arch.x86 && triple.os != OS.Win32) 4
    else 8
  }
}
