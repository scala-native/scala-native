package java.nio.file

import java.io.IOException
import attribute.BasicFileAttributes

trait FileVisitor[T] {
  def postVisitDirectory(dir: T, error: IOException): FileVisitResult
  def preVisitDirectory(dir: T,
                        attributes: BasicFileAttributes): FileVisitResult

  def visitFile(file: T, attributes: BasicFileAttributes): FileVisitResult
  def visitFileFailed(file: T, error: IOException): FileVisitResult
}

class SimpleFileVisitor[T] protected () extends FileVisitor[T] {
  override def postVisitDirectory(dir: T,
                                  error: IOException): FileVisitResult =
    FileVisitResult.CONTINUE

  override def preVisitDirectory(
      dir: T,
      attributes: BasicFileAttributes): FileVisitResult =
    FileVisitResult.CONTINUE

  override def visitFile(file: T,
                         attributes: BasicFileAttributes): FileVisitResult =
    FileVisitResult.CONTINUE

  override def visitFileFailed(file: T, error: IOException): FileVisitResult =
    FileVisitResult.CONTINUE

}
