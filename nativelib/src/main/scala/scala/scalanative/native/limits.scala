package scala.scalanative
package native

@extern
object limits {
  @name("scalanative_path_max")
  def PATH_MAX: CInt = extern
}
