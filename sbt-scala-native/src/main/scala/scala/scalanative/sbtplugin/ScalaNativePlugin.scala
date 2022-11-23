package scala.scalanative
package sbtplugin

import ScalaNativePluginInternal._

import sbt._

object ScalaNativePlugin extends AutoPlugin {
  override def requires: Plugins = plugins.JvmPlugin

  object autoImport {

    val ScalaNativeCrossVersion = sbtplugin.ScalaNativeCrossVersion

    val nativeVersion = nir.Versions.current

    val nativeConfig =
      taskKey[build.NativeConfig](
        "User configuration for the native build, NativeConfig"
      )

    val nativeClang =
      taskKey[File]("Location of the clang compiler.")

    val nativeClangPP =
      taskKey[File]("Location of the clang++ compiler.")

    val nativeCompileOptions =
      taskKey[Seq[String]](
        "Additional options are passed to clang during compilation."
      )

    val nativeLinkingOptions =
      taskKey[Seq[String]](
        "Additional options that are passed to clang during linking."
      )

    val nativeLinkStubs =
      taskKey[Boolean]("Whether to link `@stub` methods, or ignore them.")

    val nativeLink =
      taskKey[File]("Generates native binary without running it.")

    val nativeMode =
      taskKey[String](
        "Compilation mode, either \"debug\", \"release-fast\", or \"release-full\"."
      )

    val nativeGC =
      taskKey[String](
        "GC choice, either \"none\", \"boehm\", \"immix\" or \"commix\"."
      )

    val nativeLTO =
      taskKey[String](
        "LTO variant used for release mode, either \"none\", \"thin\", or \"full\" (legacy)."
      )

    val nativeCheck =
      taskKey[Boolean]("Shall native toolchain check NIR during linking?")

    val nativeDump =
      taskKey[Boolean](
        "Shall native toolchain dump intermediate NIR to disk during linking?"
      )
  }

  override def globalSettings: Seq[Setting[_]] =
    ScalaNativePluginInternal.scalaNativeGlobalSettings

  override def projectSettings: Seq[Setting[_]] =
    ScalaNativePluginInternal.scalaNativeProjectSettings
}
