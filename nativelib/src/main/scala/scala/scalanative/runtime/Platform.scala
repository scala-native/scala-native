package scala.scalanative
package runtime

import scala.scalanative.native.{CString, extern, name}

@extern
object Platform {
  @name("scalanative_platform_is_mac")
  def isMac(): Boolean = extern

  @name("scalanative_platform_is_windows")
  def isWindows(): Boolean = extern

  @name("scalanative_windows_get_user_lang")
  def windowsGetUserLang(): CString = extern

  @name("scalanative_windows_get_user_country")
  def windowsGetUserCountry(): CString = extern

  @name("scalanative_little_endian")
  def littleEndian(): Boolean = extern
}
