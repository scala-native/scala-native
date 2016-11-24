package scala.scalanative
package io

import scala.collection.mutable
import scala.collection.JavaConverters._
import java.io.File
import java.net.URI
import java.nio.ByteBuffer
import java.nio.file._
import java.nio.channels._

sealed trait VirtualDirectory {

  /** Check if file with given path is in the directory. */
  def contains(path: Path): Boolean =
    files.exists(_.path == path)

  /** Get file from the directory. */
  def get(path: Path): Option[VirtualFile] =
    files.collectFirst { case f if f.path == path => f }

  /** Create a new file or return existing one. */
  def create(path: Path): VirtualFile

  /** Reads a contents of file with given path. */
  def read(path: Path): ByteBuffer

  /** Replaces contents of file with given value. */
  def write(path: Path, buffer: ByteBuffer): Unit

  /** List all files in this directory. */
  def files: Seq[VirtualFile]

  /** Dispose of virtual directoy. */
  def close(): Unit
}

object VirtualDirectory {

  /** Map-backed virtual directory. */
  def virtual(): VirtualDirectory =
    new MapDirectory()

  /** Either real, non-virtual directory or real jar-backed virtual directory. */
  def real(file: File): VirtualDirectory = {
    val exists = file.exists
    val isDir  = file.isDirectory
    val isJar  = file.getAbsolutePath.endsWith(".jar")

    (exists, isDir, isJar) match {
      case (true, true, false) =>
        new LocalDirectory(file.toPath)
      case (true, false, true) =>
        new JarDirectory(file.toPath)
      case _ =>
        throw new Exception(
          s"unrecognized nir path entry: ${file.getAbsolutePath}")
    }
  }

  /** Root file system directory. */
  val root: VirtualDirectory = real(new File("/"))

  /** Empty directory that contains no files. */
  val empty: VirtualDirectory = EmptyDirectory

  private final class MapDirectory extends VirtualDirectory {
    private val entries  = mutable.Map.empty[Path, VirtualFile]
    private val contents = mutable.Map.empty[Path, ByteBuffer]

    override def files: Seq[VirtualFile] =
      entries.values.toSeq

    override def create(path: Path): VirtualFile = {
      val f = VirtualFile(this, path)
      entries(path) = f
      contents(path) = ByteBuffer.allocate(0)
      f
    }

    override def read(path: Path): ByteBuffer = {
      contents(path)
    }

    override def write(path: Path, buffer: ByteBuffer): Unit = {
      contents(path) = cloneBuffer(buffer)
    }

    override def close(): Unit = {
      entries.clear()
      contents.clear()
    }
  }

  private trait NioDirectory extends VirtualDirectory {
    protected def resolve(path: Path): Path = path

    protected def open(path: Path) =
      FileChannel.open(path,
                       StandardOpenOption.CREATE,
                       StandardOpenOption.WRITE,
                       StandardOpenOption.TRUNCATE_EXISTING)

    override def read(path: Path): ByteBuffer = {
      val bytes  = Files.readAllBytes(resolve(path))
      val buffer = ByteBuffer.wrap(bytes)
      buffer
    }

    override def write(path: Path, buffer: ByteBuffer): Unit = {
      val channel = open(resolve(path))
      try channel.write(buffer)
      finally channel.close
    }
  }

  private final class LocalDirectory(path: Path) extends NioDirectory {
    override protected def resolve(path: Path): Path =
      this.path.resolve(path)

    override def files: Seq[VirtualFile] =
      Files
        .walk(path, Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS)
        .iterator()
        .asScala
        .map(fp => VirtualFile(this, path.relativize(fp)))
        .toSeq

    override def create(path: Path): VirtualFile = {
      val channel = open(resolve(path))
      channel.close()
      VirtualFile(this, path)
    }

    override def close(): Unit = ()
  }

  private final class JarDirectory(path: Path) extends NioDirectory {
    private val fileSystem: FileSystem =
      FileSystems.newFileSystem(URI.create(s"jar:${path.toUri}"),
                                Map("create" -> "false").asJava)

    override def files: Seq[VirtualFile] = {
      val roots = fileSystem.getRootDirectories.asScala.toSeq

      roots.flatMap { path =>
        Files
          .walk(path, Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS)
          .iterator()
          .asScala
      }.map(VirtualFile(this, _))
    }

    override def create(path: Path): VirtualFile =
      throw new UnsupportedOperationException(
        "Can't create files in jar directory.")

    override def close(): Unit = fileSystem.close()
  }

  private final object EmptyDirectory extends VirtualDirectory {
    override def files = Seq.empty

    override def create(path: Path): VirtualFile =
      throw new Exception("Can't create files in empty directory.")

    override def read(path: Path): ByteBuffer =
      throw new UnsupportedOperationException("Can't read from jar directory.")

    override def write(path: Path, buffer: ByteBuffer): Unit =
      throw new UnsupportedOperationException("Can't write to jar directory.")

    override def close(): Unit = ()
  }
}
