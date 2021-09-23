package scala.scalanative.windows

import scala.scalanative.unsafe._

@link("Kernel32")
@extern()
object WinNlsApi {
  type LCType = Int
  def GetLocaleInfoEx(
      localeName: CWString,
      lcType: LCType,
      buffer: CWString,
      bufferSize: Int
  ): Int = extern

  @name("scalanative_win32_LocaleName_Invariant")
  def LOCALE_NAME_INVARIANT = extern
  @name("scalanative_win32_LocaleName_SystemDefault")
  def LOCALE_NAME_SYSTEM_DEFAULT = extern
  @name("scalanative_win32_LocaleName_UserDefault")
  def LOCALE_NAME_USER_DEFAULT = extern
}
