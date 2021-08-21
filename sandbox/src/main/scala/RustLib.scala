import scalanative.unsafe._

@extern
object RustLib {
  @name("rust_hello")
  def hello(): Unit = extern

  @name("rust_hello2")
  def hello2(): Unit = extern

  def isEven(v: Int): Boolean = extern
}
