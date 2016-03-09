package scala.runtime

object BoxedUnit {
  override def equals(other: Any) = this eq other
  override def hashCode = 0
  override def toString = "()"
}
