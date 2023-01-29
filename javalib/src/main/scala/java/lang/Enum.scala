package java.lang

abstract class _Enum[E <: _Enum[E]] protected (_name: String, _ordinal: Int)
    extends Comparable[E]
    with java.io.Serializable {
  def name(): String = _name
  def ordinal(): Int = _ordinal
  override def toString(): String = _name
  final def compareTo(o: E): Int = _ordinal.compareTo(o.ordinal())
}
