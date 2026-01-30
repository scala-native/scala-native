package scala.scalanative.sbtplugin

import sbt.*
import sbt.librarymanagement.LibraryManagementCodec.{given, *}
import sbt.librarymanagement.UpdateReport

import java.nio.file.{Path => NioPath, Paths}

import scala.scalanative.build.*

import sjsonnew.BasicJsonProtocol.{given, *}
import sjsonnew.{Builder, HashWriter, JsonFormat, Unbuilder}
import xsbti.{FileConverter, HashedVirtualFileRef, VirtualFile}

private[scalanative] object PluginCompat:
  type FileRef = HashedVirtualFileRef
  type Out = VirtualFile

  def toNioPath(a: Attributed[HashedVirtualFileRef])(using
      conv: FileConverter
  ): NioPath =
    conv.toPath(a.data)

  inline def toFile(a: Attributed[HashedVirtualFileRef])(using
      conv: FileConverter
  ): File =
    toNioPath(a).toFile()

  def toNioPaths(cp: Seq[Attributed[HashedVirtualFileRef]])(using
      conv: FileConverter
  ): Vector[NioPath] =
    cp.map(toNioPath).toVector

  inline def toFiles(cp: Seq[Attributed[HashedVirtualFileRef]])(using
      conv: FileConverter
  ): Vector[File] =
    toNioPaths(cp).map(_.toFile())

  val sbtVersionBaseSettings = Seq[Setting[?]](
    Keys.platform := ScalaNativePlatform.current
  )

  def crossScalaNative(module: ModuleID): ModuleID =
    module.platform(ScalaNativePlatform.current)

  def crossJVM(module: ModuleID): ModuleID =
    module.platform(sbt.Platform.jvm)

  def classpathEntryToModuleID(entry: Attributed[HashedVirtualFileRef])(using
      conv: FileConverter
  ): Option[ModuleID] =
    import sjsonnew.support.scalajson.unsafe.*
    entry.metadata
      .get(Keys.moduleIDStr)
      .map(Parser.parseUnsafe(_))
      .map(Converter.fromJsonUnsafe[ModuleID](_))
