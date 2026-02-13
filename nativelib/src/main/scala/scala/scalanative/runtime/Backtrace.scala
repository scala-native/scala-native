package scala.scalanative.runtime

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

private[runtime] object Backtrace {
  case class Position(linkageName: CString, filename: String, line: Int)
  object Position {
    final val empty = Position(null, null, 0)
  }

  private var initialized: Boolean = false

  private[runtime] def ensureInitialized(): Unit = {
    if (!initialized) {
      initialized = true
      Zone.acquire { implicit z =>
        val cFilename =
          if (ExecInfo.filename != null) toCString(ExecInfo.filename)
          else null
        val threaded = if (LinktimeInfo.isMultithreadingEnabled) 1 else 0
        backtrace_ffi.init(cFilename, threaded)
      }
    }
  }

  def decodePosition(pc: Long): Position = {
    ensureInitialized()
    val result: Ptr[backtrace_ffi.PcInfoResult] =
      fromRawPtr(Intrinsics.stackalloc[backtrace_ffi.PcInfoResult]())
    val ret = backtrace_ffi.pcinfo(
      pc.toUSize,
      result
    )
    if (ret != 0) return Position.empty

    val cFilename = result._1
    val cSymname = result._2
    val lineno = result._3

    val filename =
      if (cFilename != null) {
        val full = fromCString(cFilename)
        val lastFwd = full.lastIndexOf('/')
        val lastBck = full.lastIndexOf('\\')
        val lastSep = Math.max(lastFwd, lastBck)
        if (lastSep >= 0) full.substring(lastSep + 1)
        else full
      } else null
    // libbacktrace's internal symbol table data is never freed
    // https://github.com/ianlancetaylor/libbacktrace/blob/b9e40069c0b47a722286b94eb5231f7f05c08713/backtrace.h#L69-L87
    // so this pointer remains valid for the entire process lifetime.
    val symname: CString = cSymname

    Position(symname, filename, lineno)
  }
}
