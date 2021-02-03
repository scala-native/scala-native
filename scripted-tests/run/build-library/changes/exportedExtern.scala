import scala.scalanative.unsafe._

@extern
object exportedExtern {
  @export def foo(): Int = 0
}
