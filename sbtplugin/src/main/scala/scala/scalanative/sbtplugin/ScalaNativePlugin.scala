package scala.scalanative.sbtplugin

import sbt._

object ScalaNativePlugin {
  val autoImport = AutoImport

  object AutoImport {
    val link = inputKey[Unit](
      "Links NIR and compiles to LLVM IR.")
  }
}
