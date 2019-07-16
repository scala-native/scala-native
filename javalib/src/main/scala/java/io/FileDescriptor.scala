package java.io

import scalanative.unsigned._
import scalanative.unsafe._
import scalanative.io.FcntlHelpers.fcntlOpenOrThrow
import scalanative.libc.{errno, string}
import scalanative.posix.{fcntl, unistd}

/** Wraps a UNIX file descriptor */
final class FileDescriptor private[java] (private[java] val fd: Int,
                                          val readOnly: Boolean = false) {

  def this() = this(-1)

  // inspired by Apache Harmony including filedesc.c
  def sync(): Unit =
    if (fd <= 2) throwSyncFailed()
    else if (!readOnly) {
      unistd.fsync(fd) match {
        case 0 => ()
        case _ => throwSyncFailed()
      }
    }

  def valid(): Boolean = fcntl.fcntl(fd, fcntl.F_GETFD, 0) != -1

  private def throwSyncFailed(): Unit =
    throw new SyncFailedException("sync failed")

}

object FileDescriptor {
  val in: FileDescriptor  = new FileDescriptor(unistd.STDIN_FILENO)
  val out: FileDescriptor = new FileDescriptor(unistd.STDOUT_FILENO)
  val err: FileDescriptor = new FileDescriptor(unistd.STDERR_FILENO)

  private[io] def openReadOnly(file: File): FileDescriptor = {
    val fd = fcntlOpenOrThrow(file.getPath, fcntl.O_RDONLY, 0.toUInt)
    new FileDescriptor(fd, true)
  }
}
