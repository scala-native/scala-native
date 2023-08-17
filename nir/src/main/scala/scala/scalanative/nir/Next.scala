package scala.scalanative
package nir

sealed abstract class Next {
  def id: Local

  final def show: String = nir.Show(this)
}
object Next {
  case object None extends Next {
    def id: Local = throw new UnsupportedOperationException
  }
  final case class Unwind(exc: Val.Local, next: Next) extends Next {
    def id: Local = next.id
  }
  final case class Case(value: Val, next: Next) extends Next {
    def id: Local = next.id
  }
  final case class Label(id: Local, args: Seq[Val]) extends Next

  def apply(id: Local): Label = Label(id, Seq.empty)
  def Case(value: Val, id: Local): Case = Case(value, Next(id))
}
