import scala.scalanative.unsafe._

@extern
object exportedExtern {
  @exported def foo(): Int = 0
}
