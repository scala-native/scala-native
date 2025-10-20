package scala.scalanative
package build

import java.io.IOException
import java.nio.file.{
  AccessDeniedException,
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
import java.nio.file.attribute.DosFileAttributes
import scala.util.control.NonFatal

/** Internal I/O utilities. */
private[scalanative] object IO {

  implicit class RichPath(val path: Path) extends AnyVal {
    def abs: String = path.toAbsolutePath.toString.norm
  }
  implicit class RichString(val s: String) extends AnyVal {
    // commands issued in shell environments require forward slash
    // clang and llvm command line tools accept forward slash
    def norm: String = s.replace('\\', '/')
  }

  /** Write bytes to given file. */
  def write(file: Path, bytes: Array[Byte]): Unit = {
    import java.nio.file.StandardOpenOption.*
    Files.createDirectories(file.getParent)
    Files.write(file, bytes, CREATE, WRITE)
  }

  /** Write string to given file. */
  def write(path: Path, content: String): Unit =
    write(path, content.getBytes())

  // Read fully content of given file if it exists
  def readFully(path: Path): Option[String] = {
    if (!Files.exists(path)) None
    else {
      val source = scala.io.Source.fromFile(path.toFile())
      try Some(source.mkString)
      catch { case _: Exception => None }
      finally source.close()
    }
  }

  /** Finds all files starting in `base` that match `pattern`. */
  def getAll(base: Path, pattern: String): Seq[Path] = {
    val out = collection.mutable.ArrayBuffer.empty[Path]
    val matcher = FileSystems.getDefault.getPathMatcher(pattern)
    val visitor = new SimpleFileVisitor[Path] {
      override def preVisitDirectory(
          directory: Path,
          attributes: BasicFileAttributes
      ): FileVisitResult =
        FileVisitResult.CONTINUE

      override def postVisitDirectory(
          directory: Path,
          exception: IOException
      ): FileVisitResult =
        FileVisitResult.CONTINUE

      override def visitFile(
          file: Path,
          attributes: BasicFileAttributes
      ): FileVisitResult = {
        if (matcher.matches(file)) out += file
        FileVisitResult.CONTINUE
      }

      override def visitFileFailed(
          file: Path,
          exception: IOException
      ): FileVisitResult =
        FileVisitResult.CONTINUE
    }
    Files.walkFileTree(
      base,
      EnumSet.of(FileVisitOption.FOLLOW_LINKS),
      Int.MaxValue,
      visitor
    )
    out.toSeq
  }

  /** Does a `pattern` match starting at base */
  def existsInDir(base: Path, pattern: String): Boolean = {
    var out = false
    val matcher = base.getFileSystem.getPathMatcher(pattern)
    val visitor = new SimpleFileVisitor[Path] {
      override def preVisitDirectory(
          directory: Path,
          attributes: BasicFileAttributes
      ): FileVisitResult =
        FileVisitResult.CONTINUE

      override def postVisitDirectory(
          directory: Path,
          exception: IOException
      ): FileVisitResult =
        FileVisitResult.CONTINUE

      override def visitFile(
          file: Path,
          attributes: BasicFileAttributes
      ): FileVisitResult = {
        if (matcher.matches(file)) {
          out = true
          FileVisitResult.TERMINATE
        } else {
          FileVisitResult.CONTINUE
        }
      }

      override def visitFileFailed(
          file: Path,
          exception: IOException
      ): FileVisitResult =
        FileVisitResult.CONTINUE
    }
    Files.walkFileTree(
      base,
      EnumSet.of(FileVisitOption.FOLLOW_LINKS),
      Int.MaxValue,
      visitor
    )
    out
  }

  /** Look for a zip entry path string using a matcher function */
  def existsInJar(path: Path, matcher: String => Boolean): Boolean = {
    import java.util.zip.ZipFile
    val zf = new ZipFile(path.toFile)
    val it = zf.entries()
    while (it.hasMoreElements()) {
      if (matcher(it.nextElement().getName()))
        return true
    }
    false
  }

  /** Deletes recursively `directory` and all its content. */
  def deleteRecursive(directory: Path): Unit = {
    // On Windows the file permissions / locks are slow leading to AccessDeniedException
    // we might need to revisit the directory to ensure it is deleted
    var shouldRetry = false
    var remainingRetries = 3
    def tryDelete(path: Path, isRetry: Boolean = false): Unit =
      try Files.deleteIfExists(path)
      catch {
        case _: AccessDeniedException if Platform.isWindows && !isRetry =>
          if (Files.notExists(path)) ()
          else
            try {
              val attrs = Files.readAttributes(path, classOf[DosFileAttributes])
              if (attrs.isReadOnly()) {
                Files.setAttribute(path, "dos:readonly", false)
              }
              tryDelete(path, isRetry = true)
            } catch { case NonFatal(_) => shouldRetry = true }
        case NonFatal(_) => shouldRetry = true
      }

    while (Files.exists(directory) && remainingRetries > 0) {
      // If retrying the cleanup give OS a bit of time to close any pending locks
      if (shouldRetry) Thread.sleep(50)
      shouldRetry = false
      Files.walkFileTree(
        directory,
        new SimpleFileVisitor[Path]() {
          override def visitFile(
              file: Path,
              attrs: BasicFileAttributes
          ): FileVisitResult = {
            tryDelete(file)
            FileVisitResult.CONTINUE
          }
          override def postVisitDirectory(
              dir: Path,
              exc: IOException
          ): FileVisitResult = {
            tryDelete(dir)
            FileVisitResult.CONTINUE
          }
        }
      )
      remainingRetries -= 1
    }
  }

  /** Compute a SHA-1 hash of `path`. */
  def sha1(path: Path, bufSize: Int = 1024): Array[Byte] = {
    val digest = MessageDigest.getInstance("SHA-1")
    val stream = Files.newInputStream(path)
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
      val stream = Files.newInputStream(file)
      val digestStream = new DigestInputStream(stream, digest)
      val buf = new Array[Byte](bufSize)
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

  /** Copy recursively to existing target directory
   *
   *  Note: We need source.relativize(file) for copying to and from UNIX FS to
   *  get a relative path. We can't use the following code because you can't
   *  resolve across filesystems like UNIX FS to ZIP FS: val dest =
   *  target.resolve(source.relativize(file))
   */
  private def copyRecursive(source: Path, target: Path): Path = {
    Files.walkFileTree(
      source,
      new SimpleFileVisitor[Path]() {
        override def visitFile(
            file: Path,
            attrs: BasicFileAttributes
        ): FileVisitResult = {
          val dest =
            Paths.get(target.toString, source.relativize(file).toString())
          Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING)
          FileVisitResult.CONTINUE
        }

        override def preVisitDirectory(
            dir: Path,
            attrs: BasicFileAttributes
        ): FileVisitResult = {
          val dest =
            Paths.get(target.toString, source.relativize(dir).toString())
          Files.createDirectories(dest)
          FileVisitResult.CONTINUE
        }
      }
    )
  }
}
