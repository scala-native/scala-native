package java.util

class HashSet[E] private[util] (private[util] var inner: HashMap[E, AnyRef])
    extends AbstractSet[E]
    with Set[E]
    with Cloneable
    with Serializable {

  def this(initialCapacity: Int, loadFactor: Float) =
    this(new HashMap[E, AnyRef](initialCapacity, loadFactor))

  def this(initialCapacity: Int) =
    this(new HashMap[E, AnyRef](initialCapacity))

  def this() =
    this(new HashMap[E, AnyRef]())

  def this(c: Collection[_ <: E]) = {
    this(Math.max((c.size() / 0.75f).toInt + 1, 16))
    addAll(c)
  }

  override def contains(o: Any): Boolean =
    inner.containsKey(o)

  override def remove(o: Any): Boolean =
    inner.remove(o) ne null

  override def add(e: E): Boolean =
    inner.put(e, HashSet.Present) eq null

  override def clear(): Unit = inner.clear()

  override def size(): Int = inner.size()

  override def isEmpty(): Boolean = inner.isEmpty()

  def iterator(): Iterator[E] = inner.keyIterator()

  override def clone(): AnyRef = {
    val result = super.clone().asInstanceOf[HashSet[E]]
    result.inner = inner.clone().asInstanceOf[HashMap[E, AnyRef]]
    result
  }
}

object HashSet {
  private val Present: AnyRef = new Object

  // Since: Java 19
  def newHashSet[E](numElements: Int): HashSet[E] = {
    if (numElements < 0) {
      throw new IllegalArgumentException(
        s"Negative number of elements: ${numElements}"
      )
    }

    val loadFactor = 0.75f // as defined in JVM method description.

    val desiredCapacity = Math.ceil(numElements * (1.0f / loadFactor)).toInt

    val clampedCapacity = Math.clamp(desiredCapacity, 0, Integer.MAX_VALUE)

    new HashSet[E](clampedCapacity.toInt, loadFactor)
  }
}
