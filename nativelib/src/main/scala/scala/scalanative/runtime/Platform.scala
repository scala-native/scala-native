package scala.scalanative
package runtime

import scala.scalanative.unsafe.{CSize, CString, CFuncPtr2, extern, name}
import scala.scalanative.unsafe.CInt

@extern
object Platform {
  @name("scalanative_platform_is_freebsd")
  def isFreeBSD(): Boolean = extern

  @name("scalanative_platform_is_linux")
  def isLinux(): Boolean = extern

  @name("scalanative_platform_is_mac")
  def isMac(): Boolean = extern

  @name("scalanative_platform_probe_mac_x8664_is_arm64")
  def probeMacX8664IsArm64(): CInt = extern

  @name("scalanative_platform_is_windows")
  def isWindows(): Boolean = extern

  @deprecated("Use windows.WinNlsApi to retrieve locale info instead", "0.4.1")
  @name("scalanative_windows_get_user_lang")
  def windowsGetUserLang(): CString = extern

  @deprecated("Use windows.WinNlsApi to retrieve locale info instead", "0.4.1")
  @name("scalanative_windows_get_user_country")
  def windowsGetUserCountry(): CString = extern

  @name("scalanative_little_endian")
  def littleEndian(): Boolean = extern

  @name("scalanative_set_os_props")
  def setOSProps(addProp: CFuncPtr2[CString, CString, Unit]): Unit =
    extern

  @name("scalanative_wide_char_size")
  final def SizeOfWChar: CSize = extern
}
