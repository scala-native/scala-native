package java.nio.channels

import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix.fcntl.*
import scala.scalanative.posix.fcntlOps.*
import scala.scalanative.libc.stdio
import scalanative.unsafe.*
import java.io.IOException
import java.io.FileDescriptor
import scala.scalanative.windows.FileApi
import scala.scalanative.unsigned.*

// Works only between system processes. No thread support.
// Although in JVM locks are held on behalf the entire Java
// Virtual Machine, and are not suitable for controlling access
// to a file between different threads of the same JVM (which is
// effectively what is being done here, with a process instead of
// JVM), it should be still checked if multiple filelocks from
// different threads (of the same JVM/process) overlap, according
// to the java docs.
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
      val fl = stackalloc[flock]()
      fl.l_start = position.toSize
      fl.l_len = size.toSize
      fl.l_pid = 0
      fl.l_type = F_UNLCK
      fl.l_whence = stdio.SEEK_SET
      if (fcntl(fd.fd, F_SETLK, fl) != 0)
        throw new IOException()
    }
    released = true
  }
}
