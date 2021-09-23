package scala.scalanative.windows

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

@link("Kernel32")
@extern()
object WinNlsApi {
  type LCType = Int
  def GetLocaleInfoEx(
      localeName: CWString,
      lcType: LCType,
      buffer: CWString,
      bufferSize: UInt
  ): Int = extern

  @name("scalanative_win32_LocaleName_Invariant")
  def LOCALE_NAME_INVARIANT: CWString = extern
  @name("scalanative_win32_LocaleName_SystemDefault")
  def LOCALE_NAME_SYSTEM_DEFAULT: CWString = extern
  @name("scalanative_win32_LocaleName_UserDefault")
  def LOCALE_NAME_USER_DEFAULT: CWString = extern

  @name("scalanative_win32_Locale_SISO_LangName")
  def LOCALE_SISO639LANGNAME: LCType = extern

  @name("scalanative_win32_Locale_SISO_LangName2")
  def LOCALE_SISO639LANGNAME2: LCType = extern

  @name("scalanative_win32_Locale_SISO_CountryName")
  def LOCALE_SISO3166CTRYNAME: LCType = extern

  @name("scalanative_win32_Locale_SISO_CountryName2")
  def LOCALE_SISO3166CTRYNAME2: LCType = extern
}
