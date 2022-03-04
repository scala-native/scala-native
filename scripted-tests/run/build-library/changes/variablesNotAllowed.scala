import scala.scalanative.unsafe._

object variablesNotAllowed {
  @exported var foo: Int = _
}
