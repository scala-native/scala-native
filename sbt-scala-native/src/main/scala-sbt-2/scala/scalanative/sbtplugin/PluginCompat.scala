package scala.scalanative.sbtplugin

import sbt.*
import sbt.librarymanagement.LibraryManagementCodec.{given, *}
import sbt.librarymanagement.UpdateReport

import java.nio.file.{Path => NioPath, Paths}

import scala.scalanative.build.*

import sjsonnew.BasicJsonProtocol.{given, *}
import sjsonnew.{Builder, HashWriter, JsonFormat, Unbuilder}
import xsbti.{FileConverter, HashedVirtualFileRef}

private[scalanative] object PluginCompat:
  type FileRef = HashedVirtualFileRef

  private final case class FileRefData(
      id: String,
      contentHash: String,
      sizeBytes: Long
  )

  private given JsonFormat[FileRefData] =
    sjsonnew.BasicJsonProtocol.caseClass3(
      FileRefData.apply,
      data => Some((data.id, data.contentHash, data.sizeBytes))
    )(
      "id",
      "contentHash",
      "sizeBytes"
    )

  given JsonFormat[FileRef] with
    override def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): FileRef =
      val data = summon[JsonFormat[FileRefData]].read(jsOpt, unbuilder)
      HashedVirtualFileRef.of(data.id, data.contentHash, data.sizeBytes)

    override def write[J](obj: FileRef, builder: Builder[J]): Unit =
      summon[JsonFormat[FileRefData]].write(
        FileRefData(
          id = obj.id(),
          contentHash = obj.contentHashStr(),
          sizeBytes = obj.sizeBytes()
        ),
        builder
      )

  def toNioPath(out: FileRef)(using conv: FileConverter): NioPath =
    conv.toPath(out)

  inline def toFile(out: FileRef)(using conv: FileConverter): File =
    toNioPath(out).toFile()

  def toFileRef(path: NioPath)(using conv: FileConverter): FileRef =
    conv.toVirtualFile(path)

  inline def toFileRef(file: File)(using conv: FileConverter): FileRef =
    toFileRef(file.toPath())

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
