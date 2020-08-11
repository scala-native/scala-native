package scala.scalanative.build

import java.io.IOException
import java.nio.file.{
  Files,
  FileSystems,
  FileVisitOption,
  FileVisitResult,
  Path,
  Paths,
  SimpleFileVisitor,
  StandardCopyOption
}
import java.nio.file.attribute.BasicFileAttributes
import java.util.EnumSet
import java.util.zip.{ZipEntry, ZipInputStream}
import java.security.{DigestInputStream, MessageDigest}

/** Internal I/O utilities. */
private[scalanative] object IO {

  implicit class RichPath(val path: Path) extends AnyVal {
    def abs: String = path.toAbsolutePath.toString
  }

  /** Write bytes to given file. */
  def write(file: Path, bytes: Array[Byte]): Unit = {
    Files.createDirectories(file.getParent)
    Files.write(file, bytes)
  }

  /** Finds all files starting in `base` that match `pattern`. */
  def getAll(base: Path, pattern: String): Seq[Path] = {
    val out     = collection.mutable.ArrayBuffer.empty[Path]
    val matcher = FileSystems.getDefault.getPathMatcher(pattern)
    val visitor = new SimpleFileVisitor[Path] {
      override def preVisitDirectory(
          directory: Path,
          attributes: BasicFileAttributes): FileVisitResult =
        FileVisitResult.CONTINUE

      override def postVisitDirectory(directory: Path,
                                      exception: IOException): FileVisitResult =
        FileVisitResult.CONTINUE

      override def visitFile(
          file: Path,
          attributes: BasicFileAttributes): FileVisitResult = {
        if (matcher.matches(file)) out += file
        FileVisitResult.CONTINUE
      }

      override def visitFileFailed(file: Path,
                                   exception: IOException): FileVisitResult =
        FileVisitResult.CONTINUE
    }
    Files.walkFileTree(base,
                       EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                       Int.MaxValue,
                       visitor)
    out
  }

  /** Does a `pattern` match starting at base */
  def existsInDir(base: Path, pattern: String): Boolean = {
    var out     = false
    val matcher = FileSystems.getDefault.getPathMatcher(pattern)
    val visitor = new SimpleFileVisitor[Path] {
      override def preVisitDirectory(
          directory: Path,
          attributes: BasicFileAttributes): FileVisitResult =
        FileVisitResult.CONTINUE

      override def postVisitDirectory(directory: Path,
                                      exception: IOException): FileVisitResult =
        FileVisitResult.CONTINUE

      override def visitFile(
          file: Path,
          attributes: BasicFileAttributes): FileVisitResult = {
        if (matcher.matches(file)) {
          out = true
          FileVisitResult.TERMINATE
        } else {
          FileVisitResult.CONTINUE
        }
      }

      override def visitFileFailed(file: Path,
                                   exception: IOException): FileVisitResult =
        FileVisitResult.CONTINUE
    }
    Files.walkFileTree(base,
                       EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                       Int.MaxValue,
                       visitor)
    out
  }

  /** Look for a zip entry path string using a matcher function */
  def existsInJar(path: Path, matcher: String => Boolean): Boolean = {
    import java.util.zip.ZipFile
    import scala.collection.JavaConverters._
    val zf = new ZipFile(path.toFile)
    val it = zf.entries().asScala
    it.exists(e => matcher(e.getName))
  }

  /** Deletes recursively `directory` and all its content. */
  def deleteRecursive(directory: Path): Unit = {
    if (Files.exists(directory)) {
      Files.walkFileTree(
        directory,
        new SimpleFileVisitor[Path]() {
          override def visitFile(
              file: Path,
              attrs: BasicFileAttributes): FileVisitResult = {
            Files.delete(file)
            FileVisitResult.CONTINUE
          }
          override def postVisitDirectory(dir: Path,
                                          exc: IOException): FileVisitResult = {
            Files.delete(dir)
            FileVisitResult.CONTINUE
          }
        }
      )
    }
  }

  /** Compute a SHA-1 hash of `path`. */
  def sha1(path: Path, bufSize: Int = 1024): Array[Byte] = {
    val digest       = MessageDigest.getInstance("SHA-1")
    val stream       = Files.newInputStream(path)
    val digestStream = new DigestInputStream(stream, digest)
    try {
      val buf = new Array[Byte](bufSize)
      while (digestStream.read(buf, 0, bufSize) != -1) {}
      digest.digest()
    } finally {
      digestStream.close()
    }
  }

  /** Compute a SHA-1 hash of `files`. */
  def sha1files(files: Seq[Path], bufSize: Int = 1024): Array[Byte] = {
    val digest = MessageDigest.getInstance("SHA-1")
    files.foreach { file =>
      val stream       = Files.newInputStream(file)
      val digestStream = new DigestInputStream(stream, digest)
      val buf          = new Array[Byte](bufSize)
      try {
        while (digestStream.read(buf, 0, bufSize) != -1) {}
      } finally {
        digestStream.close()
      }
    }
    digest.digest()
  }

  /** Unzip all members of the ZIP archive `archive` to `target`. */
  def unzip(archive: Path, target: Path): Unit = {
    Files.createDirectories(target)
    val zipFS = FileSystems.newFileSystem(archive, null: ClassLoader)
    try {
      val rootDirectories = zipFS.getRootDirectories().iterator
      while (rootDirectories.hasNext) {
        val root = rootDirectories.next()
        copyRecursive(root, target)
      }
    } finally zipFS.close()
  }

  /** Copy source directory and contents to target directory. */
  def copyDirectory(source: Path, target: Path): Unit = {
    Files.createDirectories(target)
    copyRecursive(source, target)
  }

  /**
   * Copy recursively to existing target directory
   *
   * Note: We need source.relativize(file) for copying
   * to and from UNIX FS to get a relative path. We can't
   * use the following code because you can't resolve
   * across filesystems like UNIX FS to ZIP FS:
   * val dest = target.resolve(source.relativize(file))
   */
  private def copyRecursive(source: Path, target: Path): Path = {
    Files.walkFileTree(
      source,
      new SimpleFileVisitor[Path]() {
        override def visitFile(file: Path,
                               attrs: BasicFileAttributes): FileVisitResult = {
          val dest =
            Paths.get(target.toString, source.relativize(file).toString())
          Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING)
          FileVisitResult.CONTINUE
        }

        override def preVisitDirectory(
            dir: Path,
            attrs: BasicFileAttributes): FileVisitResult = {
          val dest =
            Paths.get(target.toString, source.relativize(dir).toString())
          Files.createDirectories(dest)
          FileVisitResult.CONTINUE
        }
      }
    )
  }
}
