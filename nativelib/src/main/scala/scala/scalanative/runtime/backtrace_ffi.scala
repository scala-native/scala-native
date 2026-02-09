package scala.scalanative.runtime

import scala.scalanative.unsafe._

@extern
private[runtime] object backtrace_ffi {
  type PcInfoResult =
    CStruct3[CString, CString, CInt] // filename, symname, lineno

  @name("scalanative_backtrace_init")
  def init(filename: CString, threaded: CInt): CInt = extern

  @name("scalanative_backtrace_pcinfo")
  def pcinfo(pc: CSize, result: Ptr[PcInfoResult]): CInt = extern

  @name("scalanative_backtrace_collect")
  def collect(skip: CInt, buffer: Ptr[CSize], maxFrames: CInt): CInt = extern
}
