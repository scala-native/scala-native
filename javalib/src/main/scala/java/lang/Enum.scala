package java.lang

abstract class Enum[E <: Enum[E]] protected (_name: String, _ordinal: Int)
    extends Comparable[E]
    with java.io.Serializable {
  def name(): String              = _name
  def ordinal(): Int              = _ordinal
  override def toString(): String = _name
  final def compareTo(o: E): Int  = _ordinal.compareTo(o.ordinal)
  @inline
  override final def equals(that: Any): scala.Boolean = super.equals(that)
  @inline
  override final def hashCode(): Int = super.hashCode()
}
