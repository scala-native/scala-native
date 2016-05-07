package scala.scalanative
package sbtplugin

import sbt._

object ScalaNativePlugin extends AutoPlugin {
  val autoImport = AutoImport

  object AutoImport {
    val nativeVersion = nir.Versions.current

    val nativeVerbose = settingKey[Boolean](
      "Enable verbose tool logging.")

    val nativeClang = settingKey[File](
      "Location of the clang++ compiler.")

    val nativeClangOptions = settingKey[Seq[String]](
      "Additional options that are passed to clang.")

    val nativeEmitDependencyGraphPath = settingKey[Option[File]](
      "If non-empty, emit linker graph to the given file path.")
  }

  override def projectSettings =
    ScalaNativePluginInternal.projectSettings
}
