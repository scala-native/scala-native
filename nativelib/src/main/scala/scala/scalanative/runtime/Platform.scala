package scala.scalanative
package runtime

import scala.scalanative.unsafe.{
  CInt,
  CLong,
  CBool,
  CString,
  CFuncPtr2,
  CFuncPtr3,
  Ptr,
  extern,
  name
}

@extern
object Platform {
  @name("scalanative_platform_is_mac")
  def isMac(): Boolean = extern

  @name("scalanative_platform_is_windows")
  def isWindows(): Boolean = extern

  @name("scalanative_little_endian")
  def littleEndian(): Boolean = extern

  @name("scalanative_set_os_props")
  def setOSProps(addProp: CFuncPtr2[CString, CString, Unit]): Unit =
    extern

  @name("scalanative_platform_get_all_env")
  def getAllEnv(obj: RawPtr, addEnv: CFuncPtr3[RawPtr, CString, CString, Unit]): Int = extern

  @name("scalanative_platform_thread_sleep")
  def thread_sleep(millis: CLong, nanos: CInt): CBool = extern
}
