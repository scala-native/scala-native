package scala.scalanative
package nir

sealed abstract class Next {
  def name: Local

  final def show: String = nir.Show(this)
}
object Next {
  final case class Succ(name: Local)                  extends Next
  final case class Fail(name: Local)                  extends Next
  final case class Case(value: Val, name: Local)      extends Next
  final case class Label(name: Local, args: Seq[Val]) extends Next

  def apply(name: Local) = Label(name, Seq())
}
