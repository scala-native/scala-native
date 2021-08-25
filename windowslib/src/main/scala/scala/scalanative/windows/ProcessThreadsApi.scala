package scala.scalanative.windows

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.HandleApi.Handle

@link("Advapi32")
@link("Kernel32")
@extern()
object ProcessThreadsApi {
  def GetCurrentProcess(): Handle = extern
  def GetCurrentProcessToken(): Handle = extern
  def GetCurrentThread(): Handle = extern
  def OpenThreadToken(
      thread: Handle,
      desiredAccess: DWord,
      openAsSelf: Boolean,
      tokenHandle: Ptr[Handle]
  ): Boolean = extern
  def OpenProcessToken(
      process: Handle,
      desiredAccess: DWord,
      tokenHandle: Ptr[Handle]
  ): Boolean = extern
}
