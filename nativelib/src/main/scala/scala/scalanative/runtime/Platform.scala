package scala.scalanative
package runtime

import scala.scalanative.native._

@extern
object Platform {
  @name("scalanative_platform_is_windows")
  def isWindows(): Boolean = extern

  @name("scalanative_windows_get_user_lang")
  def windowsGetUserLang(): CString = extern

  @name("scalanative_windows_get_user_country")
  def windowsGetUserCountry(): CString = extern

}
