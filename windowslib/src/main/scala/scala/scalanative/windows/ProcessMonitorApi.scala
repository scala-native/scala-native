package scala.scalanative.windows

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

@extern
@link("kernel32")
object ProcessMonitorApi {

  import HandleApi.Handle
  import MinWinBaseApi.OVERLAPPED

  def ProcessMonitorQueueCreate(): Handle = extern

  def CreateIoCompletionPort(
      fileHandle: Handle,
      existingIocp: Handle,
      key: Ptr[ULong],
      numThreads: DWord
  ): Handle = extern

  def GetQueuedCompletionStatus(
      iocp: Handle,
      lpNumberOfBytesTransferred: Ptr[DWord],
      lpCompletionKey: CVoidPtr,
      lpOverlapped: Ptr[Ptr[OVERLAPPED]],
      dwMilliseconds: DWord
  ): Boolean = extern

  def PostQueuedCompletionStatus(
      iocp: Handle,
      dwNumberOfBytesTransferred: DWord,
      dwCompletionKey: CVoidPtr,
      lpOverlapped: Ptr[OVERLAPPED]
  ): Boolean = extern

  def ProcessMonitorQueueRegister(
      iocp: Handle,
      process: Handle,
      pid: DWord
  ): Boolean = extern

  def ProcessMonitorQueuePull(iocp: Handle, timeoutMillis: DWord): DWord =
    extern

}
