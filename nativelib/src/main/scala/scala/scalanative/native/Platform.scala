package scala.scalanative
package native

@extern
object Platform {
  @name("scalanative_platform_is_windows")
  def isWindows(): Boolean = extern
}
