// Classes defined in this file are registered inside Scala 3 compiler,
// compiling them in javalib would lead to fatal error of compiler. They need
// to be defined with a different name and renamed when generating NIR name

package java.lang

abstract class _Enum[E <: _Enum[E]] protected (_name: String, _ordinal: Int)
    extends Comparable[E]
    with java.io.Serializable {
  def name(): String = _name
  def ordinal(): Int = _ordinal
  override def toString(): String = _name
  final def compareTo(o: E): Int = _ordinal.compareTo(o.ordinal())
}
