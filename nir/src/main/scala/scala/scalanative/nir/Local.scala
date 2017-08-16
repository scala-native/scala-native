package scala.scalanative
package nir

final case class Local(id: Int) {
  final def show: String = nir.Show(this)
  final override def equals(other: Any): Boolean = {
    this.eq(other.asInstanceOf[AnyRef]) || (other match {
      case other: Local => id == other.id
      case _            => false
    })
  }
  final override def hashCode: Int = id
}
