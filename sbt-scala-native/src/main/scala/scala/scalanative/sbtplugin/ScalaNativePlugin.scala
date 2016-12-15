package scala.scalanative
package sbtplugin

import scalanative.tools

import sbt._

object ScalaNativePlugin extends AutoPlugin {
  override def requires: Plugins = plugins.JvmPlugin

  val autoImport = AutoImport

  object AutoImport extends NativeCross {

    val ScalaNativeCrossVersion = sbtplugin.ScalaNativeCrossVersion

    val nativeVersion = nir.Versions.current

    val nativeClang = settingKey[File]("Location of the clang compiler.")

    val nativeClangPP = settingKey[File]("Location of the clang++ compiler.")

    val nativeClangOptions =
      settingKey[Seq[String]]("Additional options that are passed to clang.")

    val nativeLibraryLinkage = settingKey[Map[String, String]](
      "Given a native library, provide the linkage kind (static or dynamic). " +
        "If key is not present in the map, dynamic is picked as a default.")

    val nativeLink =
      taskKey[File]("Generates native binary without running it.")

    val nativeSharedLibrary = settingKey[Boolean](
      "Will create a shared library instead of a program with a main method.")

    val nativeProfileDispatch = settingKey[Boolean](
      "Gather information about types encountered in method dispatch at runtime.")

    val nativeProfileInfo = settingKey[Option[File]](
      "Where to store or find the profiling information.")

    val nativeInlineCachingMaxCandidates = settingKey[Int](
      "Maximum number of types observed at runtime to consider a call site " +
        "for inline caching.")
  }

  override def projectSettings: Seq[Setting[_]] = (
    ScalaNativePluginInternal.projectSettings ++
      ScalaNativePluginInternal.scalaNativeEcosystemSettings
  )
}
