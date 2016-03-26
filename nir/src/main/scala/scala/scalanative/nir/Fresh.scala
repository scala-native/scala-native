package scala.scalanative
package nir

final class Fresh(scope: String) {
  private var i: Int = 0
  def apply() = {
    val res = Local(scope, i)
    i += 1
    res
  }
}
object Fresh {
  def apply(scope: String) = new Fresh(scope)
}
