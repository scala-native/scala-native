package scala.scalanative.meta

import scala.scalanative.unsafe._
import scala.scalanative.runtime.RawSize

/** Constants resolved at link-time from NativeConfig, can be conditionally
 *  discard some parts of NIR instructions when linking
 */
object LinktimeInfo {
  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.isWindows")
  def isWindows: Boolean = resolved

  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.is32BitPlatform")
  def is32BitPlatform: Boolean = resolved

  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.sizeOfPtr")
  def sizeOfPtr: RawSize = resolved

  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.asanEnabled")
  def asanEnabled: Boolean = resolved
}
