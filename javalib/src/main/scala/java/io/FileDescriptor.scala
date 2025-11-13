package java.io

import java.io.FileDescriptor._

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix.{fcntl, unistd}
import scala.scalanative.runtime.{Intrinsics, fromRawPtr}
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.FileApi._
import scala.scalanative.windows.FileApiExt._
import scala.scalanative.windows.HandleApi._
import scala.scalanative.windows.HandleApiExt._
import scala.scalanative.windows.winnt.AccessRights._
import scala.scalanative.windows.{ConsoleApiExt, DWord}

final class FileDescriptor private[java] (
    private var fileHandle: FileHandle,
    readOnly: Boolean
) {
  def this() = this(
    fileHandle = FileHandle.Invalid,
    readOnly = true
  )

  // ScalaNative private constructors
  private[java] def this(fd: Int, readOnly: Boolean = false) =
    this(FileHandle(fd), readOnly)

  override def toString(): String =
    s"FileDescriptor($fd, readOnly=$readOnly)"

  /* Unix file descriptor underlying value */
  @alwaysinline private[java] def fd: Int = fileHandle.fd

  /* Windows file handler underlying value */
  @alwaysinline private[java] def handle: Handle = fileHandle.handle

  def sync(): Unit = {
    def throwSyncFailed(): Unit = {
      throw new SyncFailedException("sync failed")
    }

    def isStdOrInvalidFileDescriptor: Boolean = {
      if (isWindows) {
        handle == INVALID_HANDLE_VALUE ||
        this == FileDescriptor.in ||
        this == FileDescriptor.out ||
        this == FileDescriptor.err
      } else fd <= 2
    }

    if (isStdOrInvalidFileDescriptor) throwSyncFailed()
    else {
      if (!readOnly) {
        val hasSucceded =
          if (isWindows) FlushFileBuffers(handle)
          else unistd.fsync(fd) == 0

        if (!hasSucceded) {
          throwSyncFailed()
        }
      }
    }
  }

  @alwaysinline def valid(): Boolean = fileHandle.valid()

  // Not in the Java API. Called by java.nio.channels.FileChannelImpl.scala
  private[java] def close(): Unit = {
    val prevHandle = fileHandle
    fileHandle = FileHandle.Invalid
    prevHandle.close()
  }

}

object FileDescriptor {
  // Universal type allowing to store references to both Unix integer based,
  // and Windows pointer based file handles
  private[java] class FileHandle(val value: Long) extends AnyVal {
    /* Unix file descriptor underlying value */
    @alwaysinline def fd: Int = value.toInt

    /* Windows file handler underlying value */
    @alwaysinline def handle: Handle =
      fromRawPtr[Byte](Intrinsics.castLongToRawPtr(value))

    def close(): Unit =
      if (valid())
        if (isWindows) CloseHandle(handle) else unistd.close(fd)

    @alwaysinline def valid(): Boolean = value != FileHandle.Invalid.value
  }

  private[java] object FileHandle {
    def apply(handle: Handle): FileHandle = new FileHandle(handle.toLong)
    def apply(unixFd: Int): FileHandle = new FileHandle(unixFd.toLong)
    val Invalid: FileHandle =
      if (isWindows) FileHandle(INVALID_HANDLE_VALUE) else FileHandle(-1)
  }

  private[java] val none: FileDescriptor = new FileDescriptor()

  val in: FileDescriptor = {
    val handle =
      if (isWindows) FileHandle(ConsoleApiExt.stdIn)
      else FileHandle(unistd.STDIN_FILENO)
    new FileDescriptor(handle, readOnly = false)
  }

  val out: FileDescriptor = {
    val handle =
      if (isWindows) FileHandle(ConsoleApiExt.stdOut)
      else FileHandle(unistd.STDOUT_FILENO)
    new FileDescriptor(handle, readOnly = false)
  }

  val err: FileDescriptor = {
    val handle =
      if (isWindows) FileHandle(ConsoleApiExt.stdErr)
      else FileHandle(unistd.STDERR_FILENO)
    new FileDescriptor(handle, readOnly = false)
  }

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
        FileHandle(handle)
      } else {
        val fd = fcntl.open(toCString(file.getPath()), fcntl.O_RDONLY, 0.toUInt)
        if (fd == -1) {
          fail()
        }
        FileHandle(fd)
      }

      new FileDescriptor(fileHandle, true)
    }
}
