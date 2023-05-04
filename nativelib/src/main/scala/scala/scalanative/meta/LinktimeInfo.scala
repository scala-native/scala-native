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
  def isMac: Boolean = target.vendor == "apple" && target.os == "darwin"

  @resolvedAtLinktime
  def isFreeBSD: Boolean = target.os == "freebsd"

  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.is32BitPlatform")
  def is32BitPlatform: Boolean = resolved

  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.asanEnabled")
  def asanEnabled: Boolean = resolved

  @resolvedAtLinktime(
    "scala.scalanative.meta.linktimeinfo.isWeakReferenceSupported"
  )
  def isWeakReferenceSupported: Boolean = resolved

  @resolvedAtLinktime(
    "scala.scalanative.meta.linktimeinfo.isMultithreadingEnabled"
  )
  def isMultithreadingEnabled: Boolean = resolved

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
}
