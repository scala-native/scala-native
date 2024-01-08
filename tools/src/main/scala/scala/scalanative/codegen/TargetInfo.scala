package scala.scalanative.codegen

import scala.scalanative.build.{Config, Discover}

/** Information abut the target for which code is being compiled.
 *
 *  @param targetTriple
 *    The LLVM "triple" describing the target.
 *  @param targetsWindows
 *    `true` iff the target is Windows.
 *  @param is32Bit
 *    `true` iff the target has a 32-bit architecture.
 *  @param isMultithreadingEnabled
 *    `true` iff the target supports multithreading.
 *  @param useOpaquePointers
 *    `true` iff the LLVM IR generated for the target uses opaque pointers.
 */
private[scalanative] case class TargetInfo(
    targetTriple: Option[String],
    targetsWindows: Boolean,
    is32Bit: Boolean,
    isMultithreadingEnabled: Boolean,
    useOpaquePointers: Boolean
) {

  /** The size of a pointer on the target, in bytes. */
  val sizeOfPtr = if (is32Bit) 4 else 8

  /** The size of a pointer on the target, in bits. */
  val sizeOfPtrBits = sizeOfPtr * 8

}

object TargetInfo {

  /** Creates an instance for the given compiler configuration. */
  def apply(config: Config): TargetInfo = TargetInfo(
    targetTriple = config.compilerConfig.targetTriple,
    targetsWindows = config.targetsWindows,
    is32Bit = config.compilerConfig.is32BitPlatform,
    isMultithreadingEnabled = config.compilerConfig.multithreadingSupport,
    useOpaquePointers =
      Discover.features.opaquePointers(config.compilerConfig).isAvailable
  )

}
