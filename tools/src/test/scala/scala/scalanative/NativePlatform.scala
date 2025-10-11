package scala.scalanative

import scala.scalanative.buildinfo.ScalaNativeBuildInfo

private[scalanative] object NativePlatform {
  def erasesEmptyTraitConstructor: Boolean =
    ScalaNativeBuildInfo.scalaVersion.startsWith("3.")
}
