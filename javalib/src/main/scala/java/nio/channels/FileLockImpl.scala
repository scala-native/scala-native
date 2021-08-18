package java.nio.channels

import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix.fcntl._
import scala.scalanative.posix.fcntlOps._
import scala.scalanative.libc.stdio
import scalanative.unsafe._
import java.io.IOException
import java.io.FileDescriptor
import scala.scalanative.windows.FileApi
import scala.scalanative.unsigned._

// Works only between system processes. No thread support
private[java] final class FileLockImpl(
    channel: FileChannel,
    position: Long,
    size: Long,
    shared: Boolean,
    fd: FileDescriptor
) extends FileLock(channel, position, size, shared) {
  var released: Boolean = false

  override def isValid(): Boolean =
    !released && channel.isOpen()

  override def release(): Unit = {
    if (!channel.isOpen()) throw new ClosedChannelException()
    if (isWindows) {
      if (!FileApi.UnlockFile(
            fd.handle,
            position.toInt.toUInt,
            (position >> 32).toInt.toUInt,
            size.toInt.toUInt,
            (size >> 32).toInt.toUInt
          ))
        throw new IOException()
    } else {
      val fl = stackalloc[flock]
      fl.l_start = position
      fl.l_len = size
      fl.l_pid = 0
      fl.l_type = F_UNLCK
      fl.l_whence = stdio.SEEK_SET
      if (fcntl(fd.fd, F_SETLK, fl) != 0)
        throw new IOException()
    }
    released = true
  }
}
