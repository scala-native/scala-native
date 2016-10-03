package scala.scalanative
package sbtplugin

import sbt._

object ScalaNativePlugin extends AutoPlugin {
  val autoImport = AutoImport

  object AutoImport {
    val nativeVersion = nir.Versions.current

    val nativeVerbose = settingKey[Boolean]("Enable verbose tool logging.")

    val nativeClang = settingKey[File]("Location of the clang++ compiler.")

    val nativeClangOptions =
      settingKey[Seq[String]]("Additional options that are passed to clang.")

    val nativeEmitDependencyGraphPath = settingKey[Option[File]](
      "If non-empty, emit linker graph to the given file path.")

    val nativeLibraryLinkage = settingKey[Map[String, String]](
      "Given a native library, provide the linkage kind (static or dynamic). " +
        "If key is not present in the map, dynamic is picked as a default.")

    val nativeLink =
      taskKey[File]("Generates native binary without running it.")

    val nativeSharedLibrary = settingKey[Boolean](
      "Will create a shared library instead of a program with a main method.")
  }

  override def projectSettings =
    ScalaNativePluginInternal.projectSettings
}
