package scala.scalanative
package sbtplugin

import scalanative.tools
import ScalaNativePluginInternal._

import sbt._

object ScalaNativePlugin extends AutoPlugin {
  override def requires: Plugins = plugins.JvmPlugin

  val autoImport = AutoImport

  object AutoImport extends NativeCross {

    val ScalaNativeCrossVersion = sbtplugin.ScalaNativeCrossVersion

    val nativeVersion = nir.Versions.current

    val nativeClang =
      settingKey[Option[File]]("Location of the clang compiler.")

    val nativeClangPP =
      settingKey[Option[File]]("Location of the clang++ compiler.")

    val nativeCompileOptions =
      settingKey[Seq[String]](
        "Additional options are passed to clang during compilation.")

    val nativeLinkingOptions =
      settingKey[Seq[String]](
        "Additional options that are pased to clang during linking.")

    val nativeLink =
      taskKey[File]("Generates native binary without running it.")

    val nativeSharedLibrary =
      settingKey[Boolean]("Will create a shared library instead of a program with a main method.")

    val nativeMode =
      settingKey[String]("Compilation mode, either \"debug\" or \"release\".")

    val nativeGC =
      settingKey[String]("GC choice, either \"none\" or \"boehm\".")
  }

  override def projectSettings: Seq[Setting[_]] = (
    ScalaNativePluginInternal.projectSettings ++
      scalaNativeEcosystemSettings
  )
}
