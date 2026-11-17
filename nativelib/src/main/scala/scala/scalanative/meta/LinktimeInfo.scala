package scala.scalanative.meta

import scala.scalanative.unsafe._

/** Constants resolved at link-time from NativeConfig, can be conditionally
 *  discard some parts of NIR instructions when linking
 */
object LinktimeInfo {

  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.debugMode")
  def debugMode: Boolean = resolved

  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.releaseMode")
  def releaseMode: Boolean = resolved

  @resolvedAtLinktime
  def isWindows: Boolean = target.os == "windows"

  @resolvedAtLinktime
  def isLinux: Boolean = target.os == "linux"

  @resolvedAtLinktime
  def isAndroid: Boolean = target.env == "android"

  /** True for all Darwin based Apple platforms: macOS, iOS, tvOS, watchOS and
   *  visionOS. They share the kernel interfaces, the C library and, for a given
   *  CPU architecture, the C ABI. Equivalent of `__APPLE__` in C.
   */
  @resolvedAtLinktime
  def isApple: Boolean = target.vendor == "apple"

  /** True only when targeting macOS, see [[isApple]] for a check covering all
   *  Apple platforms. Equivalent of `TARGET_OS_OSX` in C.
   */
  @resolvedAtLinktime
  def isMac: Boolean =
    isApple && (target.os == "darwin" || target.os == "macosx")

  /** True when targeting iOS, either a physical device, the simulator or Mac
   *  Catalyst. Equivalent of `TARGET_OS_IOS` in C.
   */
  @resolvedAtLinktime
  def isIOS: Boolean = isApple && target.os == "ios"

  /** True when targeting tvOS, either a physical device or the simulator.
   *  Equivalent of `TARGET_OS_TV` in C.
   */
  @resolvedAtLinktime
  def isTvOS: Boolean = isApple && target.os == "tvos"

  /** True when targeting watchOS, either a physical device or the simulator.
   *  Equivalent of `TARGET_OS_WATCH` in C.
   */
  @resolvedAtLinktime
  def isWatchOS: Boolean = isApple && target.os == "watchos"

  /** True when targeting a simulator of one of Apple platforms. Equivalent of
   *  `TARGET_OS_SIMULATOR` in C.
   */
  @resolvedAtLinktime
  def isAppleSimulator: Boolean = isApple && target.env == "simulator"

  @resolvedAtLinktime
  def isFreeBSD: Boolean = target.os == "freebsd"

  @resolvedAtLinktime
  def isOpenBSD: Boolean = target.os == "openbsd"

  @resolvedAtLinktime
  def isNetBSD: Boolean = target.os == "netbsd"

  @resolvedAtLinktime
  def isMusl: Boolean = target.env == "musl"

  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.is32BitPlatform")
  def is32BitPlatform: Boolean = resolved

  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.enabledSanitizer")
  def enabledSanitizer: String = resolved

  @resolvedAtLinktime()
  def asanEnabled: Boolean = enabledSanitizer == "address"

  @resolvedAtLinktime(
    "scala.scalanative.meta.linktimeinfo.isWeakReferenceSupported"
  )
  def isWeakReferenceSupported: Boolean = resolved

  /** `Continuations.multi` */
  @resolvedAtLinktime()
  def isMultiShotContinuationsSupported: Boolean =
    (isLinux || isApple || isFreeBSD || isOpenBSD || isNetBSD) &&
      !is32BitPlatform &&
      (target.arch == "x86_64" || target.arch == "aarch64")

  /** `Continuations` and `Continuations.single`. */
  @resolvedAtLinktime()
  def isContinuationsSupported: Boolean = isMultiShotContinuationsSupported

  @resolvedAtLinktime()
  def isVirtualThreadsSupported: Boolean =
    isMultithreadingEnabled && isContinuationsSupported

  @resolvedAtLinktime(
    "scala.scalanative.meta.linktimeinfo.isMultithreadingEnabled"
  )
  def isMultithreadingEnabled: Boolean = resolved

  // Referenced in nscplugin and codegen
  @resolvedAtLinktime(
    "scala.scalanative.meta.linktimeinfo.contendedPaddingWidth"
  )
  def contendedPaddingWidth: Int = resolved

  @resolvedAtLinktime(
    "scala.scalanative.meta.linktimeinfo.runtimeVersion"
  )
  def runtimeVersion: String = resolved

  @resolvedAtLinktime(
    "scala.scalanative.meta.linktimeinfo.garbageCollector"
  )
  def garbageCollector: String = resolved
  object gc {
    @resolvedAtLinktime def isBoehm: Boolean = garbageCollector == "boehm"
    @resolvedAtLinktime def isImmix: Boolean = garbageCollector == "immix"
    @resolvedAtLinktime def isCommix: Boolean = garbageCollector == "commix"
    @resolvedAtLinktime def isNone: Boolean = garbageCollector == "none"
  }

  object target {
    @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.target.arch")
    def arch: String = resolved
    @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.target.vendor")
    def vendor: String = resolved
    @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.target.os")
    def os: String = resolved
    @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.target.env")
    def env: String = resolved
  }

  object sourceLevelDebuging {
    @resolvedAtLinktime(
      "scala.scalanative.meta.linktimeinfo.debugMetadata.enabled"
    )
    def enabled: Boolean = resolved

    @resolvedAtLinktime(
      "scala.scalanative.meta.linktimeinfo.debugMetadata.generateFunctionSourcePositions"
    )
    def generateFunctionSourcePositions: Boolean = resolved

  }
}
