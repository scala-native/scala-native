package scala.scalanative
package sbtplugin

import sbt._

object ScalaNativePlugin extends AutoPlugin {
  val autoImport = AutoImport

  object AutoImport {
    val nativeVersion = nir.Versions.current

    val nativeVerbose = settingKey[Boolean](
      "Enable verbose tool logging.")
  }

  override def projectSettings =
    ScalaNativePluginInternal.projectSettings
}
