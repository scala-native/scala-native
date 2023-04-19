package scala.scalanative.windows

import scala.scalanative.unsafe._
import HandleApi.Handle
import WinBaseApi.SecurityAttributes

@extern()
object NamedPipeApi {
  def CreatePipe(
      readPipePtr: Ptr[Handle],
      writePipePtr: Ptr[Handle],
      securityAttributes: Ptr[SecurityAttributes],
      size: DWord
  ): Boolean = extern

  def PeekNamedPipe(
      pipe: Handle,
      buffer: Ptr[Byte],
      bufferSize: DWord,
      bytesRead: Ptr[DWord],
      totalBytesAvailable: Ptr[DWord],
      bytesLeftThisMessage: Ptr[DWord]
  ): Boolean = extern
}
