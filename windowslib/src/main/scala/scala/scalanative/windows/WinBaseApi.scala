package scala.scalanative.windows

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import HandleApi.Handle
import scala.scalanative.windows.winnt._

@link("Advapi32")
@extern()
object WinBaseApi {
  import SecurityBaseApi._

  type CallbackContext     = Ptr[Byte]
  type WaitOrTimerCallback = CFuncPtr2[CallbackContext, Boolean, Unit]
  type LocalHandle         = Ptr[_]

  def FormatMessageA(flags: DWord,
                     source: Ptr[Byte],
                     messageId: DWord,
                     languageId: DWord,
                     buffer: Ptr[CWString],
                     size: DWord,
                     arguments: CVarArgList): DWord = extern

  def FormatMessageW(flags: DWord,
                     source: Ptr[Byte],
                     messageId: DWord,
                     languageId: DWord,
                     buffer: Ptr[CWString],
                     size: DWord,
                     arguments: CVarArgList): DWord                     = extern
  def GetCurrentDirectoryA(bufferLength: DWord, buffer: CString): DWord = extern
  def GetCurrentDirectoryW(bufferLength: DWord, buffer: CWString): DWord =
    extern

  @name("scalanative_win32_default_language")
  final def DefaultLanguageId: DWord = extern
}

object WinBaseApiExt {
  final val WT_EXECUTEDEFAULT            = 0x00000000.toUInt
  final val WT_EXECUTEIOTHREAD           = 0x00000001.toUInt
  final val WT_EXECUTEINPERSISTANTTHREAD = 0x00000080.toUInt
  final val WT_EXECUTEINWAITTHREAD       = 0x00000004.toUInt
  final val WT_EXECUTELONGFUNCTION       = 0x00000010.toUInt
  final val WT_EXECUTEONLYONCE           = 0x00000008.toUInt
  final val WT_TRANSFER_IMPERSONATION    = 0x00000100.toUInt

  final val MOVEFILE_REPLACE_EXISTING      = 0x1.toUInt
  final val MOVEFILE_COPY_ALLOWED          = 0x2.toUInt
  final val MOVEFILE_DELAY_UNTIL_REBOOT    = 0x4.toUInt
  final val MOVEFILE_WRITE_THROUGH         = 0x8.toUInt
  final val MOVEFILE_CREATE_HARDLINK       = 0x10.toUInt
  final val MOVEFILE_FAIL_IF_NOT_TRACKABLE = 0x20.toUInt

  final val SYMBOLIC_LINK_FLAG_FILE                      = 0.toUInt
  final val SYMBOLIC_LINK_FLAG_DIRECTORY                 = 0x01.toUInt
  final val SYMBOLIC_LINK_FLAG_ALLOW_UNPRIVILEGED_CREATE = 0x02.toUInt

  final val FORMAT_MESSAGE_ALLOCATE_BUFFER: DWord = 0x00000100.toUInt
  final val FORMAT_MESSAGE_IGNORE_INSERTS: DWord  = 0x00000200.toUInt
  final val FORMAT_MESSAGE_FROM_STRING: DWord     = 0x00000400.toUInt
  final val FORMAT_MESSAGE_FROM_HMODULE: DWord    = 0x00000800.toUInt
  final val FORMAT_MESSAGE_FROM_SYSTEM: DWord     = 0x00001000.toUInt
  final val FORMAT_MESSAGE_ARGUMENT_ARRAY: DWord  = 0x00002000.toUInt

}
