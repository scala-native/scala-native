package scala.scalanative.sbtplugin

import sbt._

object ScalaNativePlugin extends AutoPlugin {
  val autoImport = AutoImport

  object AutoImport {
    val compileNative = inputKey[Unit](
      "Compiles to native code.")
  }

  override def projectSettings =
    ScalaNativePluginInternal.projectSettings
}
