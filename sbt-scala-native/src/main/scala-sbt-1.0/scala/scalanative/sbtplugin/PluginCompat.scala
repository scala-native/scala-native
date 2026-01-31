package scala.scalanative.sbtplugin
import sbt.*

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

import xsbti.FileConverter

private[scalanative] object PluginCompat {
  type FileRef = java.io.File
  type Out = java.io.File
  type NioPath = java.nio.file.Path

  val sbtVersionBaseSettings = Seq[Setting[_]](
    Keys.crossVersion := ScalaNativeCrossVersion.binary,
    platformDepsCrossVersion := ScalaNativeCrossVersion.binary
  )

  def crossScalaNative(module: ModuleID): ModuleID =
    module.cross(ScalaNativeCrossVersion.binary)

  def crossJVM(module: ModuleID): ModuleID = module

  def toNioPath(a: Attributed[File])(implicit conv: FileConverter): NioPath =
    a.data.toPath()

  def toFile(a: Attributed[File])(implicit conv: FileConverter): File =
    a.data

  def toNioPaths(cp: Seq[Attributed[File]]): Vector[NioPath] =
    cp.map(_.data.toPath()).toVector

  def toFiles(cp: Seq[Attributed[File]]): Vector[File] =
    cp.map(_.data).toVector

  // This adds `Def.uncached(...)`
  implicit class DefOp(singleton: Def.type) {
    def uncached[A1](a: A1): A1 = a
  }

  implicit def seqStringFormat: sjsonnew.JsonFormat[Seq[String]] =
    sjsonnew.BasicJsonProtocol.seqFormat[String](using
      sjsonnew.BasicJsonProtocol.StringJsonFormat
    )

  def classpathEntryToModuleID(entry: Attributed[File]): Option[ModuleID] =
    entry.metadata
      .get(Keys.moduleID.key)
}
