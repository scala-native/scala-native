package scala.scalanative.posix.sys

import scalanative.native._, Nat.{Digit, _1, _6}
import scalanative.posix.sys.time.timeval
import scalanative.posix.sys.types.{suseconds_t, time_t}

@extern
object select {

  /// Note Well!
  ///
  /// See comments and code in resources/select.c. That file does a
  /// compile time check to ensure that the size of fd_sets allocated using,
  /// say "stackalloc[fd_set]" is greater than or equal to the size that
  /// C code, say FD_ZERO, will touch. Insurance well worth the cheap cost.

  // CLongLong instead of proper CLongInt allows same code to work on
  // both 32 & 64 architectures.
  private[this] type _16 = Digit[_1, _6] // on size change, see also select.c.
  type fd_set            = CStruct1[CArray[CLongLong, _16]]

  @name("scalanative_select")
  def select(nfds: CInt,
             readfds: Ptr[fd_set],
             writefds: Ptr[fd_set],
             exceptfds: Ptr[fd_set],
             timeout: Ptr[timeval]): CInt = extern

  @name("scalanative_FD_SETSIZE")
  def FD_SETSIZE: CInt = extern

  @name("scalanative_FD_CLR")
  def FD_CLR(fd: CInt, set: Ptr[fd_set]): Unit = extern

  @name("scalanative_FD_ISSET")
  def FD_ISSET(fd: CInt, set: Ptr[fd_set]): CInt = extern

  @name("scalanative_FD_SET")
  def FD_SET(fd: CInt, set: Ptr[fd_set]): Unit = extern

  @name("scalanative_FD_ZERO")
  def FD_ZERO(set: Ptr[fd_set]): Unit = extern
}

object SelectFdSet {
  import select.{FD_SET, FD_SETSIZE, FD_ZERO, fd_set}

  // create methods _must_ be used within a Zone

  def createZeroed()(implicit z: Zone): Ptr[fd_set] = {
    // Determine allocation size for full set at runtime.
    // Works on both 32 & 64 bit hardware. Robust to FD_SETSIZE > 1024.
    // Encapsulate/hide/centralize tricky math, tricky allocation, & ugly cast.

    val nBytes   = (FD_SETSIZE.toDouble / 8).ceil.toInt
    val fdsetPtr = z.alloc(nBytes).cast[Ptr[fd_set]]

    // N.B.: Must zero manually!
    // scalanative.native package.sys alloc, which zeros memory, can not be
    // used in nativelib because it is a macro.
    // Zone.alloc returns arbitrary memory, which may not be zeroed.
    FD_ZERO(fdsetPtr)
    fdsetPtr
  }

  def create(osFd: CInt)(implicit z: Zone): Ptr[fd_set] = {
    val fdsetPtr = this.createZeroed()
    FD_SET(osFd, fdsetPtr) // let OS handle negative or invalid fd
    fdsetPtr
  }
}
