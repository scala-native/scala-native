package java.util

import java.lang.{_Enum => Enum}

/** Backed by an [[EnumMap]], so it iterates in ordinal order and rejects
 *  elements of other enums like the JDK.
 */
final class EnumSet[E <: Enum[E]] private (
    elementType: Class[E],
    map: EnumMap[E, java.lang.Boolean]
) extends AbstractSet[E]
    with Cloneable
    with Serializable {
  private def values: Set[E] = map.keySet()

  override def iterator(): Iterator[E] = values.iterator()
  override def size(): Int = map.size()
  override def isEmpty(): Boolean = map.isEmpty()
  override def contains(o: Any): Boolean = map.containsKey(o)
  override def add(e: E): Boolean = map.put(e, java.lang.Boolean.TRUE) == null
  override def remove(o: Any): Boolean = map.remove(o) != null
  override def clear(): Unit = map.clear()

  override protected[util] def clone(): EnumSet[E] =
    new EnumSet[E](elementType, new EnumMap[E, java.lang.Boolean](map))

  private def complement(): EnumSet[E] = {
    val result = EnumSet.noneOf(elementType)
    val universe = EnumSet.universe(elementType)
    var i = 0
    while (i < universe.length) {
      if (!contains(universe(i))) result.add(universe(i))
      i += 1
    }
    result
  }
}

object EnumSet {
  // The constants of elementType, resolved at link time by
  // Class.getEnumConstants
  private def universe[E <: Enum[E]](elementType: Class[E]): Array[E] = {
    val constants = elementType.getEnumConstants()
    if (constants == null)
      throw new ClassCastException(s"$elementType not an enum")
    constants
  }

  def noneOf[E <: Enum[E]](elementType: Class[E]): EnumSet[E] = {
    universe(elementType) // ClassCastException for a non-enum, like the JDK
    new EnumSet[E](elementType, new EnumMap[E, java.lang.Boolean](elementType))
  }

  def allOf[E <: Enum[E]](elementType: Class[E]): EnumSet[E] = {
    val result = noneOf(elementType)
    val constants = universe(elementType)
    var i = 0
    while (i < constants.length) {
      result.add(constants(i))
      i += 1
    }
    result
  }

  def copyOf[E <: Enum[E]](s: EnumSet[E]): EnumSet[E] =
    s.clone().asInstanceOf[EnumSet[E]]

  def copyOf[E <: Enum[E]](c: Collection[E]): EnumSet[E] = c match {
    case c: EnumSet[E] => copyOf(c)
    case c             =>
      if (c.isEmpty()) throw new IllegalArgumentException("Collection is empty")
      val i = c.iterator()
      val set = EnumSet.of(i.next())
      while (i.hasNext()) {
        set.add(i.next())
      }
      set
  }

  def complementOf[E <: Enum[E]](s: EnumSet[E]): EnumSet[E] = s.complement()

  def of[E <: Enum[E]](e: E): EnumSet[E] = {
    val s = emptySetOf(e)
    s.add(e)
    s
  }

  def of[E <: Enum[E]](e1: E, e2: E): EnumSet[E] = {
    val s = emptySetOf(e1)
    s.add(e1)
    s.add(e2)
    s
  }

  def of[E <: Enum[E]](e1: E, e2: E, e3: E): EnumSet[E] = {
    val s = emptySetOf(e1)
    s.add(e1)
    s.add(e2)
    s.add(e3)
    s
  }

  def of[E <: Enum[E]](e1: E, e2: E, e3: E, e4: E): EnumSet[E] = {
    val s = emptySetOf(e1)
    s.add(e1)
    s.add(e2)
    s.add(e3)
    s.add(e4)
    s
  }

  def of[E <: Enum[E]](e1: E, e2: E, e3: E, e4: E, e5: E): EnumSet[E] = {
    val s = emptySetOf(e1)
    s.add(e1)
    s.add(e2)
    s.add(e3)
    s.add(e4)
    s.add(e5)
    s
  }

  def of[E <: Enum[E]](first: E, rest: Array[E]): EnumSet[E] = {
    val s = emptySetOf(first)
    s.add(first)
    rest.foreach(s.add)
    s
  }

  def range[E <: Enum[E]](from: E, to: E): EnumSet[E] = {
    if (from.compareTo(to) > 0)
      throw new IllegalArgumentException(s"$from > $to")
    val result = emptySetOf(from)
    val constants = universe(from.getDeclaringClass())
    var i = from.ordinal()
    while (i <= to.ordinal()) {
      result.add(constants(i))
      i += 1
    }
    result
  }

  @inline
  private def emptySetOf[E <: Enum[E]](e: E): EnumSet[E] =
    noneOf(e.getDeclaringClass())
}
