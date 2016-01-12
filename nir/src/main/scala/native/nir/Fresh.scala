package native
package nir

final class Fresh(scope: String) {
  private var i: Int = 0
  def apply() = {
    val res = Local(scope, i)
    i += 1
    res
  }
}
