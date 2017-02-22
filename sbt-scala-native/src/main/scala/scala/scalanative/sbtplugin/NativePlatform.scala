package scala.scalanative
package sbtplugin

import sbt._
import sbtcrossproject._

case object NativePlatform extends Platform {
  def identifier: String = "native"
  def sbtSuffix: String  = "Native"
  def enable(project: Project): Project =
    project.enablePlugins(ScalaNativePlugin)
  val crossBinary: CrossVersion = ScalaNativeCrossVersion.binary
  val crossFull: CrossVersion   = ScalaNativeCrossVersion.full
}
