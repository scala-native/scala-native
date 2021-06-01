package java.util

import scala.scalanative.unsafe._
import java.io.IOException
import scala.scalanative.windows.ProcessThreadsApi._
import scala.scalanative.windows.HandleApi._
import scala.scalanative.windows.HandleApiExt._
import scala.scalanative.windows.FileApiExt._
import scala.scalanative.windows._

private[java] object WindowsHelperMethods {
  def withUserToken[T](desiredAccess: DWord)(fn: Handle => T): T = {
    val tokenHandle = stackalloc[Handle]
    def getProcessToken =
      OpenProcessToken(GetCurrentProcess(), desiredAccess, tokenHandle)
    def getThreadToken =
      OpenThreadToken(
        GetCurrentThread(),
        desiredAccess,
        openAsSelf = true,
        tokenHandle
      )
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

  def withFileOpen[T](path: String,
                      access: DWord,
                      shareMode: DWord = FILE_SHARE_ALL,
                      disposition: DWord = OPEN_EXISTING,
                      attributes: DWord = FILE_ATTRIBUTE_NORMAL,
                      allowInvalidHandle: Boolean = false)(fn: Handle => T)(
      implicit z: Zone): T = {
    val handle = FileApi.CreateFileW(
      toCWideStringUTF16LE(path),
      desiredAccess = access,
      shareMode = shareMode,
      securityAttributes = null,
      creationDisposition = disposition,
      flagsAndAttributes = attributes,
      templateFile = null
    )
    if (handle != INVALID_HANDLE_VALUE || allowInvalidHandle) {
      try { fn(handle) }
      finally CloseHandle(handle)
    } else {
      throw new IOException(
        s"Cannot open file ${path}: ${ErrorHandlingApi.GetLastError()}")
    }
  }
}
