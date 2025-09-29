package scala.scalanative.windows

import java.nio.charset.StandardCharsets

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

@extern()
object ErrorHandlingApi {
  def GetLastError(): UInt = extern
}

object ErrorHandlingApiOps {
  def errorMessage(errCode: DWord): String = {
    import WinBaseApi._
    import WinBaseApiExt._

    val msgBuffer = stackalloc[CWString]()
    FormatMessageW(
      flags = FORMAT_MESSAGE_ALLOCATE_BUFFER |
        FORMAT_MESSAGE_FROM_SYSTEM |
        FORMAT_MESSAGE_IGNORE_INSERTS,
      source = null,
      messageId = errCode,
      languageId = DefaultLanguageId,
      buffer = msgBuffer,
      size = 0.toUInt,
      arguments = null
    )
    fromCWideString(!msgBuffer, StandardCharsets.UTF_16LE)
      .stripSuffix(System.lineSeparator())
  }
}
