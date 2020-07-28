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
      settingKey[build.NativeConfig]("Configuration of the Scala Native plugin")

    @deprecated("Use nativeConfig setting instead", "0.4.3")
    val nativeClang =
      settingKey[File]("Location of the clang compiler.")

    @deprecated("Use nativeConfig setting instead", "0.4.3")
    val nativeClangPP =
      settingKey[File]("Location of the clang++ compiler.")

    @deprecated("Use nativeConfig setting instead", "0.4.3")
    val nativeCompileOptions =
      settingKey[Seq[String]](
        "Additional options are passed to clang during compilation.")

    @deprecated("Use nativeConfig setting instead", "0.4.3")
    val nativeLinkingOptions =
      settingKey[Seq[String]](
        "Additional options that are passed to clang during linking.")

    @deprecated("Use nativeConfig setting instead", "0.4.3")
    val nativeLinkStubs =
      settingKey[Boolean]("Whether to link `@stub` methods, or ignore them.")

    val nativeLink =
      taskKey[File]("Generates native binary without running it.")

    @deprecated("Use nativeConfig setting instead", "0.4.3")
    val nativeMode =
      settingKey[String]("Compilation mode, either \"debug\" or \"release\".")

    @deprecated("Use nativeConfig setting instead", "0.4.3")
    val nativeGC =
      settingKey[String](
        "GC choice, either \"none\", \"boehm\", \"immix\" or \"commix\".")

    @deprecated("Use nativeConfig setting instead", "0.4.3")
    val nativeLTO =
      settingKey[String](
        "LTO variant used for release mode (either \"none\", \"thin\" or \"full\").")

    @deprecated("Use nativeConfig setting instead", "0.4.3")
    val nativeCheck =
      settingKey[Boolean]("Shall native toolchain check NIR during linking?")

    @deprecated("Use nativeConfig setting instead", "0.4.3")
    val nativeDump =
      settingKey[Boolean](
        "Shall native toolchain dump intermediate NIR to disk during linking?")
  }

  @deprecated("use autoImport instead", "0.3.7")
  val AutoImport = autoImport

  override def globalSettings: Seq[Setting[_]] =
    ScalaNativePluginInternal.scalaNativeGlobalSettings

  override def projectSettings: Seq[Setting[_]] =
    ScalaNativePluginInternal.scalaNativeProjectSettings
}
