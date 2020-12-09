package scala.scalanative

import scala.scalanative.buildinfo.ScalaNativeBuildInfo._

private[scalanative] object NativePlatform {
  def scalaUsesImplClasses: Boolean = nativeScalaVersion.startsWith("2.11.")
}
