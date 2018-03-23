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

  /** Write byts to given file. */
  def write(file: Path, bytes: Array[Byte]): Unit = {
    Files.createDirectories(file.getParent)
    Files.write(file, bytes)
  }

  /** Finds all files in `base` that match `pattern`. */
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
    val buf          = new Array[Byte](bufSize)
    while (digestStream.read(buf, 0, bufSize) == 1024) {}
    digest.digest()
  }

  /** Unzip all members of the ZIP archive `archive` to `target`. */
  def unzip(archive: Path, target: Path): Unit = {
    Files.createDirectories(target)

    val zipFS = FileSystems.newFileSystem(archive, null)
    try {
      val rootDirectories = zipFS.getRootDirectories().iterator
      while (rootDirectories.hasNext) {
        val root = rootDirectories.next()
        Files.walkFileTree(
          root,
          new SimpleFileVisitor[Path]() {
            override def visitFile(
                file: Path,
                attrs: BasicFileAttributes): FileVisitResult = {
              val dest = Paths.get(target.toString, file.toString)
              Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING)
              FileVisitResult.CONTINUE
            }

            override def preVisitDirectory(
                dir: Path,
                attrs: BasicFileAttributes): FileVisitResult = {
              val dest = Paths.get(target.toString, dir.toString)
              Files.createDirectories(dest)
              FileVisitResult.CONTINUE
            }
          }
        )
      }
    } finally zipFS.close()
  }
}
