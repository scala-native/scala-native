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
  final case class Unwind(name: Local)                extends Next
  final case class Case(value: Val, name: Local)      extends Next
  final case class Label(name: Local, args: Seq[Val]) extends Next

  def apply(name: Local) = Label(name, Seq())
}
