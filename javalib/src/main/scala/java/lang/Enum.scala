package java.lang

abstract class _Enum[E <: _Enum[E]] protected (_name: String, _ordinal: Int)
    extends Comparable[E]
    with java.io.Serializable {
  def name(): String = _name
  def ordinal(): Int = _ordinal
  override def toString(): String = _name
  final def compareTo(o: E): Int = _ordinal.compareTo(o.ordinal())

  // A constant with a body is an instance of a subclass of its enum
  final def getDeclaringClass(): Class[E] = {
    val cls: Class[_] = getClass()
    val parent: Class[_] = cls.getSuperclass()
    (if (parent eq classOf[_Enum[_]]) cls else parent).asInstanceOf[Class[E]]
  }
}

object _Enum {
  def valueOf[T <: _Enum[T]](enumClass: Class[T], name: String): T = {
    if (name == null) throw new NullPointerException("Name is null")
    val constants = enumClass.getEnumConstants()
    if (constants == null)
      throw new IllegalArgumentException(
        s"${enumClass.getName()} is not an enum class"
      )
    var i = 0
    while (i < constants.length) {
      if (constants(i).name() == name) return constants(i)
      i += 1
    }
    throw new IllegalArgumentException(
      s"No enum constant ${enumClass.getName().replace('$', '.')}.$name"
    )
  }
}
