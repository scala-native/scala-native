package scala.scalanative.tools

import java.io.IOException
import java.nio.file.{
  Files,
  FileSystems,
  FileVisitResult,
  Path,
  Paths,
  SimpleFileVisitor,
  StandardCopyOption
}
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.{ZipEntry, ZipInputStream}
import java.security.{DigestInputStream, MessageDigest}

object IO {

  /** Deletes recursively `directory` and all its content. */
  def deleteRecursive(directory: Path): Unit = {
    Files.walkFileTree(
      directory,
      new SimpleFileVisitor[Path]() {
        override def visitFile(file: Path,
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
