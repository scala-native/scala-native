package scala.scalanative
package sbtplugin

import sbt._

object ScalaNativePlugin extends AutoPlugin {
  val autoImport = AutoImport

  object AutoImport {
    val nativeVersion = nir.Versions.current

    val nativeCompile = inputKey[Unit](
      "Compiles to native code.")

    val nativeRun = inputKey[Unit](
      "Runs compiled native code.")
  }

  override def projectSettings =
    ScalaNativePluginInternal.projectSettings
}
