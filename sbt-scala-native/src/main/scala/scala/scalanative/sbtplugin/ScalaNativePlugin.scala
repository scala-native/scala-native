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
      taskKey[build.NativeConfig](
        "User configuration for the native build, NativeConfig"
      )

    val nativeLink =
      taskKey[File]("Generates native binary without running it.")

  }

  override def globalSettings: Seq[Setting[_]] =
    ScalaNativePluginInternal.scalaNativeGlobalSettings

  override def projectSettings: Seq[Setting[_]] =
    ScalaNativePluginInternal.scalaNativeProjectSettings
}
