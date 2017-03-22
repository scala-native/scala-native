package java.nio.channels

import java.util.concurrent.{ExecutorService, Future}
import java.nio.ByteBuffer
import java.nio.file.{OpenOption, Path}
import java.nio.file.attribute.FileAttribute

import java.util.Set

abstract class AsynchronousFileChannel extends AsynchronousChannel {
  def size(): Long
  def truncate(size: Long): AsynchronousFileChannel
  def force(metaData: Boolean): Unit
  def lock[A](position: Long,
              size: Long,
              shared: Boolean,
              attachment: A,
              handler: CompletionHandler[FileLock, _ >: A]): Unit
  final def lock[A](attachment: A,
                    handler: CompletionHandler[FileLock, _ >: A]): Unit =
    lock(0L, Long.MaxValue, false, attachment, handler)
  def lock(position: Long, size: Long, shared: Boolean): Future[FileLock]
  final def lock(): Future[FileLock] =
    lock(0L, Long.MaxValue, false)
  def tryLock(position: Long, size: Long, shared: Boolean): FileLock
  final def tryLock(): FileLock =
    tryLock(0L, Long.MaxValue, false)
  def read[A](dst: ByteBuffer,
              position: Long,
              attachment: A,
              handler: CompletionHandler[Integer, _ >: A]): Unit
  def read(dst: ByteBuffer, position: Long): Future[Integer]
  def write[A](src: ByteBuffer,
               position: Long,
               attachment: A,
               handler: CompletionHandler[Integer, _ >: A]): Unit
  def write(src: ByteBuffer, position: Long)
}

object AsynchronousFileChannel {
  // TODO:
  def open(file: Path,
           options: Set[_ <: OpenOption],
           executor: ExecutorService,
           attrs: Array[FileAttribute[_]]): AsynchronousFileChannel =
    ???

  // TODO:
  def open(file: Path, options: Array[OpenOption]): AsynchronousFileChannel =
    ???
}
