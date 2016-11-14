package som

class Set[E <: AnyRef](_size: Int = Constants.INITIAL_SIZE) {
  private val items = new Vector[E](_size)

  def size() = items.size()

  def forEach(fn: E => Unit): Unit = items.forEach(fn)

  def hasSome(fn: E => Boolean): Boolean = items.hasSome(fn)

  def getOne(fn: E => Boolean): E = items.getOne(fn)

  def add(obj: E): Unit = {
    if (!contains(obj)) {
      items.append(obj);
    }
  }

  def collect[T <: AnyRef](fn: E => T): Vector[T] = {
    val coll = new Vector[T]();
    forEach { e =>
      coll.append(fn(e))
    }
    coll
  }

  def contains(obj: E): Boolean = hasSome(e => e.equals(obj))

  def removeAll(): Unit = items.removeAll()
}
