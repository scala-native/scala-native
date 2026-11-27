package scala.scalanative.windows

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.HandleApi.Handle

@extern()
object ConsoleApi {
  def GetStdHandle(handleNum: DWord): Handle = extern
  def SetStdHandle(stdHandle: DWord, handle: Handle): Boolean = extern
  def GetConsoleMode(consoleHandle: Handle, mode: Ptr[DWord]): Boolean = extern
  def SetConsoleMode(consoleHandle: Handle, mode: DWord): Boolean = extern

  @name("scalanative_std_input_handle")
  final def STD_INPUT_HANDLE: DWord = extern

  @name("scalanative_std_output_handle")
  final def STD_OUTPUT_HANDLE: DWord = extern

  @name("scalanative_std_error_handle")
  final def STD_ERROR_HANDLE: DWord = extern
}

object ConsoleApiExt {
  import ConsoleApi._

  // Input console modes, see SetConsoleMode
  final val ENABLE_PROCESSED_INPUT = 0x0001.toUInt
  final val ENABLE_LINE_INPUT = 0x0002.toUInt
  final val ENABLE_ECHO_INPUT = 0x0004.toUInt

  def stdIn: Handle = GetStdHandle(STD_INPUT_HANDLE)
  def stdOut: Handle = GetStdHandle(STD_OUTPUT_HANDLE)
  def stdErr: Handle = GetStdHandle(STD_ERROR_HANDLE)
}
