package scala.scalanative.windows

import scala.scalanative.unsafe._

@extern
@link("kernel32")
object ProcessMonitorApi {

  import HandleApi.Handle

  def ProcessMonitorQueueCreate(): Handle = extern

  def ProcessMonitorQueueRegister(
      iocp: Handle,
      process: Handle,
      pid: DWord
  ): Boolean = extern

  def ProcessMonitorQueuePull(iocp: Handle, timeoutMillis: DWord): DWord =
    extern

}
