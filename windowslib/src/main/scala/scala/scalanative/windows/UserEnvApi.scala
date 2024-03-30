package scala.scalanative.windows

import scala.scalanative.unsafe._
import scala.scalanative.windows.HandleApi.Handle

@link("userenv")
@extern()
object UserEnvApi {
  def GetUserProfileDirectoryA(
      token: Handle,
      profileDir: CString,
      size: Ptr[DWord]
  ): Boolean = extern
  def GetUserProfileDirectoryW(
      token: Handle,
      profileDir: CWString,
      size: Ptr[DWord]
  ): Boolean = extern

}
