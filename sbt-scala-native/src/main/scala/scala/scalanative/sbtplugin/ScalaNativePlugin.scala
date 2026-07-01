package scala.scalanative
package sbtplugin

import sbt._

import sjsonnew.{JsonFormat, HashWriter}
import sjsonnew.BasicJsonProtocol.{mapFormat, given}

object ScalaNativePlugin extends AutoPlugin {
  override def requires: Plugins = plugins.JvmPlugin

  object autoImport {
    type NativeLinkResult = PluginCompat.FileRef

    val ScalaNativeCrossVersion = sbtplugin.ScalaNativeCrossVersion
    val ScalaNativePlatform: String = sbtplugin.ScalaNativePlatform.current

    private[sbtplugin] val nativeOrgName = "org.scala-native"
    val nativeVersion = nir.Versions.current

    def scalalibVersion(scalaVersion: String, nativeVersion: String): String =
      s"$scalaVersion+$nativeVersion"

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

    val nativeDiscoverEnv =
      settingKey[build.Discover.Env](
        "System environment used to discover NativeConfig"
      )

    val nativeConfig =
      taskKey[build.NativeConfig](
        "User configuration for the native build, NativeConfig"
      )

    val nativeLink =
      taskKey[NativeLinkResult]("Generates native binary without running it.")

    val nativeLinkReleaseFast =
      taskKey[NativeLinkResult](
        "Generates native binary in release-fast configuration without running it."
      )

    val nativeLinkReleaseFull =
      taskKey[NativeLinkResult](
        "Generates native binary in release-full configuration without running it."
      )

    implicit def nativeConfigJsonFormat: JsonFormat[build.NativeConfig] =
      NativeConfigJsonFormats.NativeConfigCodec

    implicit def nativeDiscoverEnvJsonWriter1: HashWriter[build.Discover.Env *: EmptyTuple] = ???

    implicit def nativeDiscoverEnvJsonWriter: HashWriter[build.Discover.Env] =
      new HashWriter[build.Discover.Env] {
        override def write[J](obj: build.Discover.Env, builder: sjsonnew.Builder[J]): Unit = {
          val mapValue = Map(
            "SCALANATIVE_GC" -> obj.`SCALANATIVE_GC`,
            "SCALANATIVE_INCLUDE_DIRS" -> obj.`SCALANATIVE_INCLUDE_DIRS`,
            "SCALANATIVE_LIB_DIRS" -> obj.`SCALANATIVE_LIB_DIRS`,
            "SCALANATIVE_LTO" -> obj.`SCALANATIVE_LTO`,
            "SCALANATIVE_MODE" -> obj.`SCALANATIVE_MODE`,
            "SCALANATIVE_OPTIMIZE" -> obj.`SCALANATIVE_OPTIMIZE`,
            "LLVM_BIN" -> obj.`LLVM_BIN`
          )

          mapFormat[String, Option[String]].write(mapValue, builder)
        }

        override def addField[J](name: String, obj: build.Discover.Env, builder: sjsonnew.Builder[J]): Unit = {
          builder.addFieldName(name)
          write(obj, builder)
        }
      }
  }

  override def globalSettings: Seq[Setting[_]] =
    ScalaNativePluginInternal.scalaNativeGlobalSettings

  override def projectSettings: Seq[Setting[_]] =
    ScalaNativePluginInternal.scalaNativeProjectSettings
}
