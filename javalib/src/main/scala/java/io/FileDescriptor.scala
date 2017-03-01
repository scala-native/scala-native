package java.io

import scala.scalanative.posix.unistd

/** Wraps a UNIX file descriptor */
final class FileDescriptor private[io] (private[io] val fd: Int) {

  def this() = this(-1)

  def sync(): Unit =
    unistd.fsync(fd) match {
      case 0 => ()
      case _ => throw new java.io.SyncFailedException("sync failed")
    }

  def valid(): Boolean = fd == -1
}

object FileDescriptor {

  val in: FileDescriptor  = new FileDescriptor(unistd.STDIN_FILENO)
  val out: FileDescriptor = new FileDescriptor(unistd.STDOUT_FILENO)
  val err: FileDescriptor = new FileDescriptor(unistd.STDERR_FILENO)

}
