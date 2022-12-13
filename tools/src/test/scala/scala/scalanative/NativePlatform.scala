package scala.scalanative

import scala.scalanative.buildinfo.ScalaNativeBuildInfo._

private[scalanative] object NativePlatform {
  def erasesEmptyTraitConstructor: Boolean = nativeScalaVersion.startsWith("3.")
}
