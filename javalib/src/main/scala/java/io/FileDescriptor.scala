package java.io

import scala.scalanative.posix.{fcntl, unistd}

/** Wraps a UNIX file descriptor */
final class FileDescriptor private[io] (private[io] val fd: Int,
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

  def valid(): Boolean = fcntl.fcntl(fd, fcntl.F_GETFD) != -1

  private def throwSyncFailed(): Unit =
    throw new SyncFailedException("sync failed")

}

object FileDescriptor {

  val in: FileDescriptor  = new FileDescriptor(unistd.STDIN_FILENO)
  val out: FileDescriptor = new FileDescriptor(unistd.STDOUT_FILENO)
  val err: FileDescriptor = new FileDescriptor(unistd.STDERR_FILENO)

}
