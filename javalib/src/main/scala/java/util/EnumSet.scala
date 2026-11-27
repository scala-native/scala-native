package java.util

import java.lang.{_Enum => Enum}

final class EnumSet[E <: Enum[E]] private (
    elementType: Class[E],
    values: Set[E]
) extends AbstractSet[E]
    with Cloneable
    with Serializable {
  // Unsupported requires reflection
  // def this(elementType: Class[E], universe: Array[Enum[E]]) = ???

  override def iterator(): Iterator[E] = values.iterator()
  override def size(): Int = values.size()
  override def isEmpty(): Boolean = values.isEmpty()
  override def contains(o: Any): Boolean = values.contains(o)
  override def toArray(): Array[AnyRef] = values.toArray()
  override def toArray[T <: AnyRef](a: Array[T]): Array[T] = values.toArray(a)
  override def add(e: E): Boolean = values.add(e)
  override def remove(o: Any): Boolean = values.remove(o)
  override def containsAll(c: Collection[_]): Boolean = values.containsAll(c)
  override def addAll(c: Collection[_ <: E]): Boolean = values.addAll(c)
  override def removeAll(c: Collection[_]): Boolean = values.removeAll(c)
  override def retainAll(c: Collection[_]): Boolean = values.retainAll(c)
  override def clear(): Unit = values.clear()
  override def equals(o: Any): Boolean = values.equals(o)
  override def hashCode(): Int = values.hashCode()

  override protected[util] def clone(): EnumSet[E] =
    new EnumSet[E](elementType, new HashSet[E](values))

  private def complement(): EnumSet[E] = {
    val result = EnumSet.noneOf(elementType)
    val universe = EnumSet.universe(elementType)
    var i = 0
    while (i < universe.length) {
      if (!values.contains(universe(i))) result.add(universe(i))
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
    universe(elementType) // checks elementType like the JDK
    new EnumSet[E](elementType, new HashSet[E]())
  }

  def allOf[E <: Enum[E]](elementType: Class[E]): EnumSet[E] = {
    val result = new EnumSet[E](elementType, new HashSet[E]())
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
    new EnumSet[E](e.getDeclaringClass(), new HashSet[E]())
}
