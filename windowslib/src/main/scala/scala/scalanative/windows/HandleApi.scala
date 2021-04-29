package scala.scalanative.windows

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

@extern
object HandleApi {
  type Handle = Ptr[Byte]

  @name("scalanative_win32_invalid_handle_value")
  def InvalidHandleValue: Handle = extern

  def CloseHandle(handle: Handle): Boolean = extern

  def GetHandleInformation(handle: Handle, flags: Ptr[DWord]): Boolean = extern

  def SetHandleInformation(handle: Handle, mask: DWord, flags: DWord): Boolean =
    extern
}

object HandleApiExt {
  final val HANDLE_FLAG_INHERIT            = 0x00000001.toUInt
  final val HANDLE_FLAG_PROTECT_FROM_CLOSE = 0x00000002.toUInt
}
