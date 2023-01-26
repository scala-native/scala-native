package scala.scalanative.nscplugin

import java.net.URI

/** This trait allows to query all options to the ScalaNative Plugin
 *
 *  Also see the help text in ScalaNativePlugin for information about particular
 *  options.
 */
trait ScalaNativeOptions {
  import ScalaNativeOptions._

  /** Should static forwarders be emitted for non-top-level objects.
   *
   *  Scala/JVM does not do that and, we do not do it by default either, but
   *  this option can be used to opt in. This is necessary for implementations
   *  of JDK classes.
   */
  def genStaticForwardersForNonTopLevelObjects: Boolean

  /** Which source locations in source maps should be relativized (or where
   *  should they be mapped to)?
   */
  def sourceURIMaps: List[URIMap]
}

object ScalaNativeOptions {
  case class URIMap(from: URI, to: Option[URI])
}
