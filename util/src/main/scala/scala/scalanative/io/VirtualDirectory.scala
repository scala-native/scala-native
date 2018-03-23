package scala.scalanative
package io

import scala.collection.mutable
import scala.collection.JavaConverters._
import java.net.URI
import java.nio.ByteBuffer
import java.nio.file._
import java.nio.channels._
import scalanative.util.{acquire, defer, Scope}

sealed trait VirtualDirectory {

  /** Check if file with given path is in the directory. */
  def contains(path: Path): Boolean =
    files.contains(path)

  /** Reads a contents of file with given path. */
  def read(path: Path): ByteBuffer

  /** Replaces contents of file with given value. */
  def write(path: Path, buffer: ByteBuffer): Unit

  /** List all files in this directory. */
  def files: Seq[Path]
}

object VirtualDirectory {

  /** Real, non-virtual directory on local file system. */
  def local(file: Path): VirtualDirectory = {
    def absolute = file.toAbsolutePath
    assert(Files.exists(file), s"Local directory doesn't exist: $absolute")
    assert(Files.isDirectory(file), s"Not a directory: $absolute")

    new LocalDirectory(file)
  }

  /** Virtual directory that represents contents of the jar file. */
  def jar(file: Path)(implicit in: Scope): VirtualDirectory = {
    val absolute = file.toAbsolutePath
    assert(Files.exists(file), s"Jar doesn't exist: $absolute")
    assert(absolute.toString.endsWith(".jar"), s"Not a jar: $absolute")

    new JarDirectory(file)
  }

  /** Virtual directory based on either local directory or a jar. */
  def real(file: Path)(implicit in: Scope): VirtualDirectory =
    if (Files.isDirectory(file)) {
      local(file)
    } else if (file.toString.endsWith(".jar")) {
      jar(file)
    } else {
      throw new UnsupportedOperationException(
        "Neither a jar, nor a directory: " + file)
    }

  /** Empty directory that contains no files. */
  val empty: VirtualDirectory = EmptyDirectory

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

    override def files: Seq[Path] =
      Files
        .walk(path, Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS)
        .iterator()
        .asScala
        .map(fp => path.relativize(fp))
        .toSeq
  }

  private final class JarDirectory(path: Path)(implicit in: Scope)
      extends NioDirectory {
    private val fileSystem: FileSystem =
      acquire {
        val uri = URI.create(s"jar:${path.toUri}")
        try {
          FileSystems.newFileSystem(uri, Map("create" -> "false").asJava)
        } catch {
          case e: FileSystemAlreadyExistsException =>
            FileSystems.getFileSystem(uri)
        }
      }

    override def files: Seq[Path] = {
      val roots = fileSystem.getRootDirectories.asScala.toSeq

      roots
        .flatMap { path =>
          Files
            .walk(path, Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS)
            .iterator()
            .asScala
        }
    }
  }

  private final object EmptyDirectory extends VirtualDirectory {
    override def files = Seq.empty

    override def read(path: Path): ByteBuffer =
      throw new UnsupportedOperationException(
        "Can't read from empty directory.")

    override def write(path: Path, buffer: ByteBuffer): Unit =
      throw new UnsupportedOperationException("Can't write to empty directory.")
  }
}
