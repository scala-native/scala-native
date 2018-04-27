package scala.scalanative
package runtime

import scala.scalanative.native.{CString, extern, name}

@extern
object Platform {
  @name("scalanative_platform_is_windows")
  def isWindows(): Boolean = extern

  @name("scalanative_windows_get_user_lang")
  def windowsGetUserLang(): CString = extern

  @name("scalanative_windows_get_user_country")
  def windowsGetUserCountry(): CString = extern

  @name("scalanative_little_endian")
  def littleEndian(): Boolean = extern
}

object CrossPlatform {
  @inline final def cross3264[A, B](thirtyTwo: => A, sixtyFour: => B): A = {
    thirtyTwo
  }

  type Cross3264[ThirtyTwo, SixtyFour] = ThirtyTwo
}