package java.nio.file

import java.io.IOException
import attribute.BasicFileAttributes

trait FileVisitor[T] {
  def postVisitDirectory(dir: Path, error: IOException): FileVisitResult
  def preVisitDirectory(dir: Path,
                        attributes: BasicFileAttributes): FileVisitResult

  def visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult
  def visitFileFailed(file: Path, error: IOException): FileVisitResult
}

class SimpleFileVisitor[T] extends FileVisitor[T] {
  def postVisitDirectory(dir: Path, error: IOException): FileVisitResult =
    FileVisitResult.CONTINUE

  def preVisitDirectory(dir: Path,
                        attributes: BasicFileAttributes): FileVisitResult =
    FileVisitResult.CONTINUE

  def visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult =
    FileVisitResult.CONTINUE

  def visitFileFailed(file: Path, error: IOException): FileVisitResult =
    FileVisitResult.CONTINUE

}

sealed trait FileVisitResult
object FileVisitResult {
  case object CONTINUE      extends FileVisitResult
  case object SKIP_SIBLINGS extends FileVisitResult
  case object SKIP_SUBTREE  extends FileVisitResult
  case object TERMINATE     extends FileVisitResult
}
