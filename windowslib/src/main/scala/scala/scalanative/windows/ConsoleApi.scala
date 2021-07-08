package scala.scalanative.windows

import scala.scalanative.unsafe._
import scala.scalanative.windows.HandleApi.Handle

@extern()
object ConsoleApi {
  def GetStdHandle(handleNum: DWord): Handle = extern
  def SetStdHandle(stdHandle: DWord, handle: Handle): Boolean = extern

  @name("scalanative_win32_console_std_in_handle")
  final def STD_INPUT_HANDLE: DWord = extern

  @name("scalanative_win32_console_std_out_handle")
  final def STD_OUTPUT_HANDLE: DWord = extern

  @name("scalanative_win32_console_std_err_handle")
  final def STD_ERROR_HANDLE: DWord = extern
}

object ConsoleApiExt {
  import ConsoleApi._

  def stdIn: Handle = GetStdHandle(STD_INPUT_HANDLE)
  def stdOut: Handle = GetStdHandle(STD_OUTPUT_HANDLE)
  def stdErr: Handle = GetStdHandle(STD_ERROR_HANDLE)
}
