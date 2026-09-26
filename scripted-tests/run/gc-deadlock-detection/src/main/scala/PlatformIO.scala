import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

/** Platform-specific I/O operations for pipe-based tests.
 *
 *  Uses LinktimeInfo.isWindows to conditionally compile the correct
 *  implementation for each PlatformInfo.
 */
object PlatformIO {

  /** Creates a pipe and returns (readFd, writeFd) as CInt values. On Windows,
   *  these are indices into an internal handle array. On POSIX, these are file
   *  descriptors.
   */
  def createPipe(pipeFds: Ptr[CInt]): CInt = {
    if (LinktimeInfo.isWindows) {
      WindowsPipeIO.createPipe(pipeFds)
    } else {
      PosixPipeIO.pipe(pipeFds)
    }
  }

  /** Close a pipe endpoint */
  def closePipe(fd: CInt): CInt = {
    if (LinktimeInfo.isWindows) {
      WindowsPipeIO.closePipe(fd)
    } else {
      PosixPipeIO.close(fd)
    }
  }
}

// =============================================================================
// POSIX Pipe I/O (Unix/Linux/macOS)
// =============================================================================
@extern
private object PosixPipeIO {
  def pipe(pipeFds: Ptr[CInt]): CInt = extern
  def close(fd: CInt): CInt = extern
}

// =============================================================================
// Windows Pipe I/O
// =============================================================================
@extern
@link("kernel32")
private object WindowsPipeIOExtern {
  @name("CreatePipe")
  def CreatePipe(
      readPipePtr: Ptr[Ptr[Byte]],
      writePipePtr: Ptr[Ptr[Byte]],
      securityAttributes: Ptr[Byte],
      size: CUnsignedInt
  ): Boolean = extern

  @name("CloseHandle")
  def CloseHandle(handle: Ptr[Byte]): Boolean = extern
}

private object WindowsPipeIO {
  import WindowsPipeIOExtern._

  // Store handles for later access - simple approach for test code
  private var handles: Array[Ptr[Byte]] = new Array[Ptr[Byte]](64)
  private var nextIndex: Int = 0

  def createPipe(pipeFds: Ptr[CInt]): CInt = {
    val readHandle = stackalloc[Ptr[Byte]]()
    val writeHandle = stackalloc[Ptr[Byte]]()

    if (CreatePipe(readHandle, writeHandle, null, 0.toUInt)) {
      val readIdx = nextIndex
      handles(readIdx) = !readHandle
      nextIndex += 1

      val writeIdx = nextIndex
      handles(writeIdx) = !writeHandle
      nextIndex += 1

      pipeFds(0) = readIdx
      pipeFds(1) = writeIdx
      0
    } else {
      -1
    }
  }

  def closePipe(fd: CInt): CInt = {
    val handle = handles(fd)
    if (handle != null) {
      handles(fd) = null
      if (CloseHandle(handle)) 0 else -1
    } else {
      -1
    }
  }

  def getHandle(fd: CInt): Ptr[Byte] = handles(fd)
}

// =============================================================================
// Native read() - platform-specific extern definitions
// =============================================================================

/** POSIX read() WITHOUT @blocking - causes GC deadlock */
@extern
object NativeBlockingPosix {
  def read(fd: CInt, buf: Ptr[Byte], count: CSize): CSSize = extern
}

/** POSIX read() WITH @blocking - GC can proceed */
@extern
object NativeBlockingCorrectPosix {
  @blocking
  def read(fd: CInt, buf: Ptr[Byte], count: CSize): CSSize = extern
}

/** Windows ReadFile WITHOUT @blocking - causes GC deadlock */
@extern
@link("kernel32")
object NativeBlockingWindows {
  @name("ReadFile")
  def ReadFile(
      fileHandle: Ptr[Byte],
      buffer: Ptr[Byte],
      bytesToRead: CUnsignedInt,
      bytesReadPtr: Ptr[CUnsignedInt],
      overlapped: Ptr[Byte]
  ): Boolean = extern
}

/** Windows ReadFile WITH @blocking - GC can proceed */
@extern
@link("kernel32")
object NativeBlockingCorrectWindows {
  @name("ReadFile")
  @blocking
  def ReadFile(
      fileHandle: Ptr[Byte],
      buffer: Ptr[Byte],
      bytesToRead: CUnsignedInt,
      bytesReadPtr: Ptr[CUnsignedInt],
      overlapped: Ptr[Byte]
  ): Boolean = extern
}

/** Unified read API that selects the correct platform implementation */
object NativeRead {

  /** Read WITHOUT @blocking - causes GC deadlock */
  def readBlocking(fd: CInt, buf: Ptr[Byte], count: CSize): CSSize = {
    if (LinktimeInfo.isWindows) {
      val handle = WindowsPipeIO.getHandle(fd)
      val bytesRead = stackalloc[CUnsignedInt]()
      if (NativeBlockingWindows.ReadFile(
            handle,
            buf,
            count.toUInt,
            bytesRead,
            null
          )) {
        (!bytesRead).toLong.toCSSize
      } else {
        (-1L).toCSSize
      }
    } else {
      NativeBlockingPosix.read(fd, buf, count)
    }
  }

  /** Read WITH @blocking - GC can proceed */
  def readBlockingCorrect(fd: CInt, buf: Ptr[Byte], count: CSize): CSSize = {
    if (LinktimeInfo.isWindows) {
      val handle = WindowsPipeIO.getHandle(fd)
      val bytesRead = stackalloc[CUnsignedInt]()
      if (NativeBlockingCorrectWindows.ReadFile(
            handle,
            buf,
            count.toUInt,
            bytesRead,
            null
          )) {
        (!bytesRead).toLong.toCSSize
      } else {
        (-1L).toCSSize
      }
    } else {
      NativeBlockingCorrectPosix.read(fd, buf, count)
    }
  }
}
