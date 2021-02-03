import scala.scalanative.unsafe._

class ExportInClass() {
  @export
  def foo(l: Int): Int = l
}

