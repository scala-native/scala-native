import scala.scalanative.unsafe._

class ExportInClass() {
  @exported
  def foo(l: Int): Int = l
}
