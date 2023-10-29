package scala.scalanative
package runtime

import scala.scalanative.unsafe.{CString, CFuncPtr2, extern, name}
import scala.scalanative.unsafe.CInt
import scala.scalanative.unsafe.CSize

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

  @name("scalanative_little_endian")
  def littleEndian(): Boolean = extern

  @name("scalanative_set_os_props")
  def setOSProps(addProp: CFuncPtr2[CString, CString, Unit]): Unit =
    extern

  @name("scalanative_wide_char_size")
  final def SizeOfWChar: CSize = extern

  @name("scalanative_platform_is_msys")
  def isMsys(): Boolean = extern
}
