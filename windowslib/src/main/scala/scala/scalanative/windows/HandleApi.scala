package scala.scalanative.windows

import scala.scalanative.runtime.fromRawPtr
import scala.scalanative.runtime.Intrinsics.{castLongToRawPtr}
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.HandleApi.Handle

@extern
object HandleApi {
  type Handle = Ptr[Byte]

  @blocking
  def CloseHandle(handle: Handle): Boolean = extern

  def DuplicateHandle(
      sourceProcess: Handle,
      source: Handle,
      targetProcess: Handle,
      target: Ptr[Handle],
      desiredAccess: DWord,
      inheritHandle: Boolean,
      options: DWord
  ): Boolean = extern

  def GetHandleInformation(handle: Handle, flags: Ptr[DWord]): Boolean = extern

  def SetHandleInformation(handle: Handle, mask: DWord, flags: DWord): Boolean =
    extern
}

object HandleApiExt {
  final val INVALID_HANDLE_VALUE: Handle = fromRawPtr(castLongToRawPtr(-1))
  final val HANDLE_FLAG_INHERIT = 0x00000001.toUInt
  final val HANDLE_FLAG_PROTECT_FROM_CLOSE = 0x00000002.toUInt
  final val DUPLICATE_CLOSE_SOURCE = 0x00000001.toUInt
  final val DUPLICATE_SAME_ACCESS = 0x00000002.toUInt
}
