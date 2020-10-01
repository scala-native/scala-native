// Ported from Scala.js.
// Also contains original work for Scala Native.

package java.util

/// ScalaNative Porting Note:
///
///     * Ported, with thanks & gratitude, from Scala.js ArrayDeque.scala
///       commit 9DC4D5b, dated 2018-10-12.
///       Also contains original work for Scala Native.
///
///     * Changes in Scala.js original commit E07F99D, dated 2019-07-30
///       were considered on 2020-05-19. The Scala.js changes to
///       ArrayDeque.scala were to use Objects.equals() in 3 places:
///       contains(), removeFirstOccurrence(), & removeLastOccurrence().
///       No corresponding change is needed here because the above
///       methods of this class are defined in terms of
///       inner.{contains,indexOf,lastIndexOf}. inner is a
///       java.util.ArrayList, whose methods already use the semantics of
///       Object.equals().
///
///     * ArrayList is the inner type, rather than js.Array.
///
///     * The order of method declarations is not alphabetical to reduce
///       churn versus Scala.js original.

class ArrayDeque[E] private (private val inner: ArrayList[E])
    extends AbstractCollection[E]
    with Deque[E]
    with Cloneable
    with Serializable {
  self =>

  private var status = 0

  def this() =
    this(new ArrayList[E](16))

  def this(initialCapacity: Int) = {
    // This is the JVM behavior for negative initialCapacity.
    this(new ArrayList[E](Math.max(0, initialCapacity)))
  }

  def this(c: Collection[_ <: E]) = {
    this(c.size())
    addAll(c)
  }

  override def add(e: E): Boolean = {
    offerLast(e)
    true
  }

  def addFirst(e: E): Unit =
    offerFirst(e)

  def addLast(e: E): Unit =
    offerLast(e)

  // shallow-copy
  override def clone(): ArrayDeque[E] =
    new ArrayDeque[E](inner.clone.asInstanceOf[ArrayList[E]])

  def offerFirst(e: E): Boolean = {
    if (e == null) {
      throw new NullPointerException()
    } else {
      inner.add(0, e)
      status += 1
      true
    }
  }

  def offerLast(e: E): Boolean = {
    if (e == null) {
      throw new NullPointerException()
    } else {
      inner.add(e)
      status += 1
      true
    }
  }

  def removeFirst(): E = {
    if (inner.isEmpty())
      throw new NoSuchElementException()
    else
      pollFirst()
  }

  def removeLast(): E = {
    if (inner.isEmpty())
      throw new NoSuchElementException()
    else
      pollLast()
  }

  def pollFirst(): E = {
    if (inner.isEmpty()) null.asInstanceOf[E]
    else {
      val res = inner.remove(0)
      status += 1
      res
    }
  }

  def pollLast(): E = {
    if (inner.isEmpty()) null.asInstanceOf[E]
    else {
      val res = inner.remove(inner.size() - 1)
      status += 1
      res
    }
  }

  def getFirst(): E = {
    if (inner.isEmpty())
      throw new NoSuchElementException()
    else
      peekFirst()
  }

  def getLast(): E = {
    if (inner.isEmpty())
      throw new NoSuchElementException()
    else
      peekLast()
  }

  def peekFirst(): E = {
    if (inner.isEmpty()) null.asInstanceOf[E]
    else inner.get(0)
  }

  def peekLast(): E = {
    if (inner.isEmpty()) null.asInstanceOf[E]
    else inner.get(inner.size() - 1)
  }

  def removeFirstOccurrence(o: Any): Boolean = {
    val index = inner.indexOf(o)
    if (index >= 0) {
      inner.remove(index)
      status += 1
      true
    } else
      false
  }

  def removeLastOccurrence(o: Any): Boolean = {
    val index = inner.lastIndexOf(o)
    if (index >= 0) {
      inner.remove(index)
      status += 1
      true
    } else
      false
  }

  def offer(e: E): Boolean = offerLast(e)

  override def remove(): E = removeFirst()

  def poll(): E = pollFirst()

  def element(): E = getFirst()

  def peek(): E = peekFirst()

  def push(e: E): Unit = addFirst(e)

  def pop(): E = removeFirst()

  def size(): Int = inner.size()

  private def failFastIterator(startIndex: Int, nex: (Int) => Int) = {
    new Iterator[E] {
      private def checkStatus() = {
        if (self.status != actualStatus)
          throw new ConcurrentModificationException()
      }

      private val actualStatus = self.status

      private var index: Int = startIndex

      def hasNext(): Boolean = {
        checkStatus()
        val n = nex(index)
        (n >= 0) && (n < inner.size())
      }

      def next(): E = {
        checkStatus()
        index = nex(index)
        inner.get(index)
      }

      override def remove(): Unit = {
        checkStatus()
        if (index < 0 || index >= inner.size()) {
          throw new IllegalStateException()
        } else {
          inner.remove(index)
        }
      }
    }
  }

  def iterator(): Iterator[E] =
    failFastIterator(-1, x => (x + 1))

  def descendingIterator(): Iterator[E] =
    failFastIterator(inner.size(), x => (x - 1))

  override def contains(o: Any): Boolean = inner.contains(o)

  override def remove(o: Any): Boolean = removeFirstOccurrence(o)

  override def clear(): Unit = {
    if (!inner.isEmpty()) status += 1
    inner.clear()
  }

  override def toArray(): Array[AnyRef] = {
    inner.toArray()
  }

  override def toArray[T](a: Array[T]): Array[T] = {
    inner.toArray(a)
  }
}
