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
  def isMac: Boolean =
    target.vendor == "apple" &&
      (target.os == "darwin" || target.os == "macosx")

  @resolvedAtLinktime
  def isFreeBSD: Boolean = target.os == "freebsd"

  @resolvedAtLinktime
  def isOpenBSD: Boolean = target.os == "openbsd"

  @resolvedAtLinktime
  def isNetBSD: Boolean = target.os == "netbsd"

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

  @resolvedAtLinktime()
  def isContinuationsSupported: Boolean =
    (isLinux || isMac || isFreeBSD || isOpenBSD || isNetBSD) &&
      (target.arch != "arm" && !is32BitPlatform)

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
