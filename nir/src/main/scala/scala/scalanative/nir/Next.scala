package scala.scalanative
package nir

sealed abstract class Next {
  def name: Local

  final def show: String = nir.Show(this)
}
object Next {
  final case object None extends Next {
    def name: Local =
      throw new UnsupportedOperationException
  }
  final case class Unwind(exc: Val.Local, next: Next) extends Next {
    def name: Local = next.name
  }
  final case class Case(value: Val, next: Next) extends Next {
    def name: Local = next.name
  }
  final case class Label(name: Local, args: Seq[Val]) extends Next

  def apply(name: Local): Label =
    Label(name, Seq())
  def Case(value: Val, name: Local): Case =
    Case(value, Next(name))
}
