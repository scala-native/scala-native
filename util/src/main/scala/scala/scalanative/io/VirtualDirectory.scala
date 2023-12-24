package scala.scalanative
package io

import java.io.Writer
import java.net.URI
import java.nio.ByteBuffer
import java.nio.file._
import java.nio.channels._
import java.util.HashMap
import scalanative.util.{Scope, acquire, defer}

sealed trait VirtualDirectory {

  /** A unique identifier for this directory */
  def uri: URI

  /** Check if file with given path is in the directory. */
  def contains(path: Path): Boolean =
    files.contains(path)

  /** Reads a contents of file with given path. */
  def read(path: Path): ByteBuffer

  /** Reads up to len bytes from the file with the given path. */
  def read(path: Path, len: Int): ByteBuffer

  /** Replaces contents of file with given value. */
  def write(path: Path, buffer: ByteBuffer): Unit

  /** Replaces contents of file using given writer. */
  def write(path: Path)(fn: Writer => Unit): Path

  /** List all files in this directory. */
  def files: Seq[Path]

  /** Merges content of source paths into single file in target */
  def merge(sources: Seq[Path], target: Path): Path

  /** Returns a Java NIO path matcher for given pattern based on underlying file
   *  system
   */
  def pathMatcher(pattern: String): PathMatcher
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
        "Neither a jar, nor a directory: " + file
      )
    }

  /** Empty directory that contains no files. */
  val empty: VirtualDirectory = EmptyDirectory

  private trait NioDirectory extends VirtualDirectory {
    protected def resolve(path: Path): Path = path

    protected def open(path: Path) =
      FileChannel.open(
        path,
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE,
        StandardOpenOption.TRUNCATE_EXISTING
      )

    override def read(path: Path): ByteBuffer = {
      val bytes = Files.readAllBytes(resolve(path))
      val buffer = ByteBuffer.wrap(bytes)
      buffer
    }

    override def read(path: Path, len: Int): ByteBuffer = {
      val stream = Files.newInputStream(resolve(path))
      try {
        val bytes = new Array[Byte](len)
        val read = stream.read(bytes)
        ByteBuffer.wrap(bytes, 0, read)
      } finally stream.close()
    }

    override def write(path: Path)(fn: Writer => Unit): Path = {
      val fullPath = resolve(path)
      Files.createDirectories(fullPath.getParent())
      val writer = Files.newBufferedWriter(fullPath)
      try fn(writer)
      finally writer.close()
      fullPath
    }

    override def write(path: Path, buffer: ByteBuffer): Unit = {
      val fullPath = resolve(path)
      Files.createDirectories(fullPath.getParent())
      val channel = open(fullPath)
      try channel.write(buffer)
      finally channel.close
    }

    override def merge(sources: Seq[Path], target: Path): Path = {
      val outputPath = resolve(target)
      Files.createDirectories(outputPath.getParent())
      val output = FileChannel.open(
        outputPath,
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE,
        StandardOpenOption.APPEND
      )
      try {
        sources.foreach { path =>
          val input = FileChannel.open(
            resolve(path),
            StandardOpenOption.READ,
            StandardOpenOption.DELETE_ON_CLOSE
          )
          try {
            input.transferTo(0, input.size(), output)
          } finally input.close()
        }
      } finally output.close()
      outputPath
    }
  }

  private final class LocalDirectory(path: Path) extends NioDirectory {

    def uri: URI = path.toUri

    override protected def resolve(path: Path): Path =
      this.path.resolve(path)

    override def files: Seq[Path] =
      jIteratorToSeq {
        Files
          .walk(path, Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS)
          .iterator()
      }.map(fp => path.relativize(fp))

    override def pathMatcher(pattern: String): PathMatcher =
      path.getFileSystem().getPathMatcher(pattern)
  }

  private final class JarDirectory(path: Path)(implicit in: Scope)
      extends NioDirectory {
    def uri: URI = URI.create(s"jar:${path.toUri}")
    private val fileSystem: FileSystem =
      acquire {
        try {
          val params = new HashMap[String, String]()
          params.put("create", "false")
          FileSystems.newFileSystem(uri, params)
        } catch {
          case e: FileSystemAlreadyExistsException =>
            FileSystems.getFileSystem(uri)
        }
      }

    override def pathMatcher(pattern: String): PathMatcher =
      fileSystem.getPathMatcher(pattern)

    override def files: Seq[Path] = {
      val roots = jIteratorToSeq(fileSystem.getRootDirectories.iterator())

      roots
        .flatMap { path =>
          jIteratorToSeq {
            Files
              .walk(path, Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS)
              .iterator()
          }
        }
    }
  }

  private object EmptyDirectory extends VirtualDirectory {

    val uri: URI = URI.create("")

    override def files = Seq.empty

    override def read(path: Path): ByteBuffer =
      throw new UnsupportedOperationException(
        "Can't read from empty directory."
      )

    override def read(path: Path, len: Int): ByteBuffer = read(path)

    override def write(path: Path)(fn: Writer => Unit): Path =
      throw new UnsupportedOperationException("Can't write to empty directory.")

    override def write(path: Path, buffer: ByteBuffer): Unit =
      throw new UnsupportedOperationException("Can't write to empty directory.")

    override def merge(sources: Seq[Path], target: Path): Path =
      throw new UnsupportedOperationException("Can't merge in empty directory.")

    override def pathMatcher(pattern: String): PathMatcher =
      throw new UnsupportedOperationException("Can't match in empty directory.")
  }
}
