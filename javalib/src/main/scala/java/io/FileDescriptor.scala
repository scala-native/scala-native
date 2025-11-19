package java.io

import java.io.FileDescriptor._

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.javalib.io._
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix.{fcntl, unistd}
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.ConsoleApiExt
import scala.scalanative.windows.FileApi._
import scala.scalanative.windows.FileApiExt._
import scala.scalanative.windows.HandleApi._
import scala.scalanative.windows.HandleApiExt._
import scala.scalanative.windows.winnt.AccessRights._

final class FileDescriptor private[java] (
    private val fileHandle: AtomicObjectHandle,
    readOnly: Boolean
) {
  def this() = this(
    fileHandle = AtomicObjectHandle(ObjectHandle.Invalid),
    readOnly = true
  )

  // ScalaNative private constructors
  private[java] def this(fd: Int, readOnly: Boolean = false) =
    this(AtomicObjectHandle(fd), readOnly)

  private[java] def this(fh: Handle, readOnly: Boolean) =
    this(AtomicObjectHandle(fh), readOnly)

  private[java] def this(fh: ObjectHandle, readOnly: Boolean) =
    this(AtomicObjectHandle(fh), readOnly)

  override def toString(): String =
    s"FileDescriptor($fd, readOnly=$readOnly)"

  @inline private[java] def get(): ObjectHandle = fileHandle.get()

  /* Unix file descriptor underlying value */
  @alwaysinline private[java] def fd: Int = get().asInt

  /* Windows file handler underlying value */
  @alwaysinline private[java] def handle: Handle = get().asHandle

  def sync(): Unit = {
    def isStdOrInvalidFileDescriptor: Boolean =
      if (isWindows) !valid() || this == in || this == out || this == err
      else fd <= 2

    val failed = isStdOrInvalidFileDescriptor || !readOnly && {
      if (isWindows) !FlushFileBuffers(handle) else unistd.fsync(fd) != 0
    }

    if (failed) throw new SyncFailedException("sync failed")
  }

  @alwaysinline def valid(): Boolean = get().valid()

  // Not in the Java API. Called by java.nio.channels.FileChannelImpl.scala
  private[java] def close(): Unit = fileHandle.close()

}

object FileDescriptor {

  private[java] val none: FileDescriptor = new FileDescriptor()

  val in: FileDescriptor =
    if (isWindows)
      new FileDescriptor(ConsoleApiExt.stdIn, readOnly = true)
    else
      new FileDescriptor(unistd.STDIN_FILENO, readOnly = true)

  val out: FileDescriptor =
    if (isWindows)
      new FileDescriptor(ConsoleApiExt.stdOut, readOnly = false)
    else
      new FileDescriptor(unistd.STDOUT_FILENO, readOnly = false)

  val err: FileDescriptor =
    if (isWindows)
      new FileDescriptor(ConsoleApiExt.stdErr, readOnly = false)
    else
      new FileDescriptor(unistd.STDERR_FILENO, readOnly = false)

  private[io] def openReadOnly(file: File): FileDescriptor =
    Zone.acquire { implicit z =>
      def fail() =
        throw new FileNotFoundException("No such file " + file.getPath())

      val fileHandle = if (isWindows) {
        val handle = CreateFileW(
          filename = toCWideStringUTF16LE(file.getPath()),
          desiredAccess = FILE_GENERIC_READ,
          shareMode = FILE_SHARE_READ | FILE_SHARE_WRITE,
          securityAttributes = null,
          creationDisposition = OPEN_EXISTING,
          flagsAndAttributes = 0.toUInt,
          templateFile = null
        )
        if (handle == INVALID_HANDLE_VALUE) {
          fail()
        }
        ObjectHandle(handle)
      } else {
        val fd = fcntl.open(toCString(file.getPath()), fcntl.O_RDONLY, 0.toUInt)
        if (fd == -1) {
          fail()
        }
        ObjectHandle(fd)
      }

      new FileDescriptor(fileHandle, true)
    }
}
