package java.util

import java.nio.file.WindowsException

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.ErrorHandlingApi._
import scala.scalanative.windows.FileApiExt._
import scala.scalanative.windows.HandleApi._
import scala.scalanative.windows.HandleApiExt._
import scala.scalanative.windows.ProcessThreadsApi._
import scala.scalanative.windows.SecurityBaseApi._
import scala.scalanative.windows._
import scala.scalanative.windows.winnt._

import winnt.AccessToken._

/** Windows implementation specific helper methods, not available in public API
 *  (javalib does not contain them in published jar) Not made `java` package
 *  private only because of usage inside `scala.scalanative.nio.fs`
 */
object WindowsHelperMethods {
  // Extended paths must be absolute and normalized before adding the prefix.
  def toWindowsPath(path: String)(implicit z: Zone): CWString = {
    val absolute = new java.io.File(path).getAbsolutePath()
    // Directory creation reserves room for an 8.3 file name within MAX_PATH.
    if (absolute.length < MAX_PATH.toInt - 12 || absolute.startsWith(
          "\\\\?\\"
        ) ||
        absolute.startsWith("\\\\.\\"))
      toCWideStringUTF16LE(path)
    else {
      val filename = toCWideStringUTF16LE(absolute)
      val capacity = FileApi.GetFullPathNameW(filename, 0.toUInt, null, null)
      if (capacity == 0.toUInt) throw WindowsException.onPath(path)
      val buffer = alloc[WChar](capacity)
      val length = FileApi.GetFullPathNameW(filename, capacity, buffer, null)
      if (length == 0.toUInt || length >= capacity)
        throw WindowsException.onPath(path)
      val normalized =
        fromCWideString(buffer, java.nio.charset.StandardCharsets.UTF_16LE)
      val extended =
        if (normalized.startsWith("\\\\"))
          "\\\\?\\UNC\\" + normalized.substring(2)
        else "\\\\?\\" + normalized
      toCWideStringUTF16LE(extended)
    }
  }

  def fromWindowsPath(path: String): String =
    if (path.startsWith("\\\\?\\UNC\\")) "\\\\" + path.substring(8)
    else path.stripPrefix("\\\\?\\")

  def withUserToken[T](desiredAccess: DWord)(fn: Handle => T): T = {
    val tokenHandle = stackalloc[Handle]()
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

  def withImpersonatedToken[T](fn: Handle => T): T = {
    withUserToken(
      TOKEN_IMPERSONATE | TOKEN_READ | TOKEN_DUPLICATE
    ) { tokenHandle =>
      val impersonatedToken = stackalloc[Handle]()
      if (!DuplicateToken(
            tokenHandle,
            SecurityImpersonation,
            impersonatedToken
          )) {
        throw new RuntimeException("Cannot impersonate access token")
      }

      try {
        fn(!impersonatedToken)
      } finally HandleApi.CloseHandle(!impersonatedToken)
    }
  }

  def withTokenInformation[T, R](
      token: Handle,
      informationClass: TokenInformationClass
  )(fn: Ptr[T] => R)(implicit z: Zone): R = {
    val dataSize = alloc[DWord]()
    !dataSize = 0.toUInt

    if (!GetTokenInformation(
          token,
          informationClass,
          information = null,
          informationLength = !dataSize,
          returnLength = dataSize
        )) {
      GetLastError() match {
        case ErrorCodes.ERROR_INSUFFICIENT_BUFFER => ()
        case errCode                              =>
          throw WindowsException(
            s"Cannot determinate size for token informaiton $informationClass",
            errCode
          )
      }
    }

    val data: Ptr[Byte] = alloc[Byte](!dataSize)
    if (!GetTokenInformation(
          token,
          informationClass,
          information = data,
          informationLength = !dataSize,
          returnLength = dataSize
        )) {
      throw WindowsException(
        s"Failed to get data for token informaiton $informationClass"
      )
    }

    fn(data.asInstanceOf[Ptr[T]])
  }

  def withFileOpen[T](
      path: String,
      access: DWord,
      shareMode: DWord = FILE_SHARE_ALL,
      disposition: DWord = OPEN_EXISTING,
      attributes: DWord = FILE_ATTRIBUTE_NORMAL,
      allowInvalidHandle: Boolean = false
  )(fn: Handle => T)(implicit z: Zone): T = {
    val handle = FileApi.CreateFileW(
      toWindowsPath(path),
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
      throw WindowsException.onPath(path)
    }
  }
}
