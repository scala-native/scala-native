package scala.scalanative.windows.util

import scala.scalanative.unsafe._
import scala.scalanative.windows.HandleApi.Handle
import scala.scalanative.windows.ProcessThreadsApi._
import scala.scalanative.windows._

object HelperMethods {
  def withUserToken[T](desiredAccess: DWord)(fn: Handle => T): T = {
    val tokenHandle = stackalloc[Handle]
    def getProcessToken =
      OpenProcessToken(GetCurrentProcess(), desiredAccess, tokenHandle)
    def getThreadToken =
      OpenThreadToken(GetCurrentThread(),
                      desiredAccess,
                      openAsSelf = true,
                      tokenHandle)
    if (getProcessToken || getThreadToken) {
      try {
        fn(!tokenHandle)
      } finally {
        HandleApi.CloseHandle(!tokenHandle)
      }
    } else {
      throw new RuntimeException("Cannot get user token")
    }
  }
}
