import scala.scalanative.unsafe._

object Lib {
  @exported("answer")
  def answer(): CInt = 42
}
