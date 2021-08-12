package scala.scalanative.meta

import scala.scalanative.unsafe._
import scala.scalanative.runtime.RawSize

/** Constants resolved at link-time from NativeConfig, can be conditionally
 *  discard some parts of NIR instructions when linking
 */
object LinktimeInfo {
  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.isWindows")
  def isWindows: Boolean = resolved

  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.is32")
  def is32: Boolean = resolved

  @resolvedAtLinktime("scala.scalanative.meta.linktimeinfo.sizeOfPtr")
  def sizeOfPtr: RawSize = resolved
}
