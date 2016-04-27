package scala.scalanative

package object native {
  def extern: Nothing = runtime.undefined

  def cast[T](any: Any): T = runtime.undefined
}
