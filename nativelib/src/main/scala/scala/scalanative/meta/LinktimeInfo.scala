package scala.scalanative.meta

import scala.scalanative.unsafe._

/** Constants resolved at link-time from NativeConfig,
 * can be conditionally discard some parts of NIR instructions when linking */
object LinktimeInfo {
  @resolvedAtLinktime("scalanative.meta.linktimeinfo.isWindows")
  def isWindows: Boolean = resolved
}
