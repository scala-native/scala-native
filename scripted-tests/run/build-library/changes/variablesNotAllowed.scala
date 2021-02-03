import scala.scalanative.unsafe._

object variablesNotAllowed {
  @export var foo: Int = _
}