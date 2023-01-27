package scala.scalanative.meta

import scala.scalanative.unsafe._
import scala.scalanative.runtime.RawSize

/** Constants resolved at link-time from NativeConfig, can be conditionally
 *  discard some parts of NIR instructions when linking
 */
object LinktimeInfo {
  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.debugMode")
  def debugMode: Boolean = resolved

  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.releaseMode")
  def releaseMode: Boolean = resolved

  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.isWindows")
  def isWindows: Boolean = resolved

  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.isLinux")
  def isLinux: Boolean = resolved

  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.isMac")
  def isMac: Boolean = resolved

  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.isFreeBSD")
  def isFreeBSD: Boolean = resolved

  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.is32BitPlatform")
  def is32BitPlatform: Boolean = resolved

  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.sizeOfPtr")
  def sizeOfPtr: RawSize = resolved

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
}
