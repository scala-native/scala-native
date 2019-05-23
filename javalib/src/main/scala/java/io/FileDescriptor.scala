package java.io

import scala.scalanative.unsigned._
import scala.scalanative.unsafe._
import scala.scalanative.posix.{fcntl, unistd}

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

  private[io] def openReadOnly(file: File): FileDescriptor =
    Zone { implicit z =>
      val fd = fcntl.open(toCString(file.getPath), fcntl.O_RDONLY, 0.toUInt)
      if (fd == -1) {
        throw new FileNotFoundException("No such file " + file.getPath)
      }
      new FileDescriptor(fd, true)
    }
}
