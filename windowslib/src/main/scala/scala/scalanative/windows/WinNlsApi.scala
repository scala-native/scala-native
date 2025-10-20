package scala.scalanative.windows

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

@link("kernel32")
@extern()
object WinNlsApi {
  type LCType = Int

  def GetLocaleInfoEx(
      localeName: CWString,
      lcType: LCType,
      buffer: CWString,
      bufferSize: UInt
  ): Int = extern

  @name("scalanative_locale_name_invariant")
  def LOCALE_NAME_INVARIANT: CWString = extern
  @name("scalanative_locale_name_system_default")
  def LOCALE_NAME_SYSTEM_DEFAULT: CWString = extern
  @name("scalanative_locale_name_user_default")
  def LOCALE_NAME_USER_DEFAULT: CWString = extern

  @name("scalanative_locale_siso639langname")
  def LOCALE_SISO639LANGNAME: LCType = extern

  @name("scalanative_locale_siso639langname2")
  def LOCALE_SISO639LANGNAME2: LCType = extern

  @name("scalanative_locale_siso3166ctryname")
  def LOCALE_SISO3166CTRYNAME: LCType = extern

  @name("scalanative_locale_siso3166ctryname2")
  def LOCALE_SISO3166CTRYNAME2: LCType = extern
}
