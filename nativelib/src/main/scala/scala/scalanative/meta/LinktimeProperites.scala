package scala.scalanative.meta

import scala.scalanative.unsafe._

/** Constants resolved at link-time from NativeConfig,
 * can be conditionally discard some parts of NIR instructions when linking */
object LinktimeProperites {
  @resolvedAtLinktime(withName = "isWindows")
  def isWindows: Boolean = resolved
}
