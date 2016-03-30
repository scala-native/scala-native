package scala.scalanative.sbtplugin

import sbt._

object ScalaNativePlugin extends AutoPlugin {
  val autoImport = AutoImport

  object AutoImport {
    val link = inputKey[Unit](
      "Links NIR and compiles to LLVM IR.")

    val filterOutScalaLibraries = settingKey[Boolean](
      "Filter out scala-{compiler,reflect,library,parser-combinators}.")
  }

  override def projectSettings =
    ScalaNativePluginInternal.projectSettings
}
