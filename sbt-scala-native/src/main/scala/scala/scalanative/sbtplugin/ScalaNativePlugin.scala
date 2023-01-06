package scala.scalanative
package sbtplugin

import ScalaNativePluginInternal._

import sbt._

object ScalaNativePlugin extends AutoPlugin {
  override def requires: Plugins = plugins.JvmPlugin

  object autoImport {

    val ScalaNativeCrossVersion = sbtplugin.ScalaNativeCrossVersion

    val nativeVersion = nir.Versions.current

    /** Declares `Tag`s which may be used to limit the concurrency of build
     *  tasks.
     *
     *  For example, the following snippet can be used to limit the number of
     *  linking tasks which are able to run at once:
     *
     *  {{{
     *  Global / concurrentRestrictions += Tags.limit(NativeTags.Link, 2)
     *  }}}
     */
    object NativeTags {

      /** This tag is applied to the [[nativeLink]] task. */
      val Link = Tags.Tag("native-link")
    }

    val nativeConfig =
      settingKey[build.NativeConfig](
        "User configuration for the native build, NativeConfig"
      )

    val nativeLink =
      taskKey[File]("Generates native binary without running it.")

    // raw settings minimal sbt projects

    val nativeClang =
      settingKey[File]("Location of the clang compiler.")

    val nativeClangPP =
      settingKey[File]("Location of the clang++ compiler.")

    val nativeCompileOptions =
      settingKey[Seq[String]](
        "Additional options are passed to clang during compilation."
      )

    val nativeLinkingOptions =
      settingKey[Seq[String]](
        "Additional options that are passed to clang during linking."
      )

    val nativeLinkStubs =
      settingKey[Boolean]("Whether to link `@stub` methods, or ignore them.")

    val nativeMode =
      taskKey[String](
        "Compilation mode, either \"debug\", \"release-size\", \"release-fast\", or \"release-full\"."
      )

    val nativeGC =
      settingKey[String](
        "GC choice, either \"none\", \"boehm\", \"immix\" or \"commix\"."
      )

    val nativeLTO =
      settingKey[String](
        "LTO variant used for release mode, either \"none\", \"thin\", or \"full\" (legacy)."
      )

    val nativeCheck =
      settingKey[Boolean]("Shall native toolchain check NIR during linking?")

    val nativeDump =
      settingKey[Boolean](
        "Shall native toolchain dump intermediate NIR to disk during linking?"
      )
  }

  override def globalSettings: Seq[Setting[_]] =
    ScalaNativePluginInternal.scalaNativeGlobalSettings

  override def projectSettings: Seq[Setting[_]] =
    ScalaNativePluginInternal.scalaNativeProjectSettings
}
