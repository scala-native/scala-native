package java.util

class ArrayList[E] private (private[ArrayList] var values: Array[AnyRef])
    extends AbstractList[E]
    with RandomAccess
    with Cloneable
    with Serializable { self =>

  def this(initialCapacity: Int) = this(new Array[AnyRef](initialCapacity))

  def this() = this(new Array[AnyRef](16))

  def this(c: Collection[_ <: E]) = {
    this(new Array[AnyRef](c.size))
    addAll(c)
  }

  var _size: Int = 0
  def size: Int  = _size

  override def isEmpty: Boolean = _size == 0

  override def iterator(): Iterator[E] = super.iterator()

  override def contains(that: Any): Boolean = {
    indexOf(that) >= 0
  }

  override def indexOf(that: Any): Int = {
    var i = 0
    while (i < _size) {
      val value = values(i)
      if (value != null) {
        if (value equals that)
          return i
      } else if (that == null)
        return i
      i += 1
    }
    -1
  }

  override def lastIndexOf(that: Any): Int = {
    var i = _size - 1
    while (i > 0) {
      val value = values(i)
      if (value != null) {
        if (value equals that)
          return i
      } else if (that == null)
        return i
      i -= 1
    }
    -1
  }

  def trimToSize(): Unit =
    if (values.length > _size)
      values = Arrays.copyOf(values, _size)

  def ensureCapacity(minCapacity: Int): Unit =
    if (minCapacity > values.length)
      values = Arrays.copyOf(values, minCapacity)

  override def clone(): AnyRef = new ArrayList(Arrays.copyOf(values, _size))

  override def toArray: Array[AnyRef] = Arrays.copyOf(values, _size)

  override def toArray[F <: AnyRef](array: Array[F]): Array[F] =
    if (_size > array.length) {
      Arrays.copyOf(values.asInstanceOf[Array[E with AnyRef]],
                    _size,
                    array.getClass.asInstanceOf[Class[_ <: Array[F]]])
    } else {
      System.arraycopy(values, 0, array, 0, _size)
      if (array.length > _size)
        array(_size) = null.asInstanceOf[F] // see tests
      array
    }

  def get(index: Int): E = {
    checkIndexInBounds(index)
    values(index).asInstanceOf[E]
  }

  override def set(index: Int, element: E): E = {
    val e = get(index)
    values(index) = element.asInstanceOf[AnyRef]
    e
  }

  override def add(e: E): Boolean = {
    values(_size) = e.asInstanceOf[AnyRef]
    _size += 1
    true
  }

  override def add(index: Int, element: E): Unit = {
    checkIndexOnBounds(index)
    System.arraycopy(values, index, values, index + 1, size - index)
    values(index) = element.asInstanceOf[AnyRef]
    _size += 1
  }

  override def remove(index: Int): E = {
    checkIndexInBounds(index)
    val elem = values(index)
    remove0(index)
    elem.asInstanceOf[E]
  }

  override def remove(that: Any): Boolean = {
    var i     = 0
    var found = false
    while (i < _size && !found) {
      if (values(i) equals that) {
        found = true
        remove0(i)
      }
      i += 1
    }
    found
  }

  // Shared code between different removes.
  private def remove0(index: Int): Unit = {
    // if it is the last element, we don't need to move any elements behind it!
    if (index - 1 != _size)
      System.arraycopy(values, index + 1, values, index, _size - index)
    _size -= 1
    values(_size) = null
  }

  // Removes all values, but doesn't reallocate or resize the underlying array.
  override def clear(): Unit = {
    var i = 0
    while (i < _size) {
      values(i) = null
      i += 1
    }
    _size = 0
  }

  override def addAll(c: Collection[_ <: E]): Boolean = {
    c match {
      case other: ArrayList[_] =>
        val otherSize = other.size
        if (otherSize > 0) {
          ensureCapacity(_size + otherSize)
          System.arraycopy(other.values, 0, values, _size, otherSize)
          true
        } else {
          false
        }
      case _ => super.addAll(c)
    }
  }

  override def addAll(index: Int, c: Collection[_ <: E]): Boolean = {
    c match {
      case other: ArrayList[_] =>
        val otherSize = other.size
        if (otherSize > 0) {
          ensureCapacity(_size + otherSize)
          System
            .arraycopy(values, index, values, otherSize + index, _size - index)
          System.arraycopy(other.values, 0, values, index, otherSize)
          true
        } else {
          false
        }
      case _ => super.addAll(index, c)
    }
  }

  // Workaround for #375
  override def subList(fromIndex: Int, toIndex: Int): List[E] =
    super.subList(fromIndex, toIndex)

  override def listIterator(): ListIterator[E] = super.listIterator(0)

  override def listIterator(index: Int): ListIterator[E] =
    super.listIterator(index)
}
