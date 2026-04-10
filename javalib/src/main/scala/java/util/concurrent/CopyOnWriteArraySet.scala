/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.io.{ObjectInputStream, ObjectStreamException, StreamCorruptedException}
import java.{util => ju}
import java.util.{AbstractSet, Collection, Iterator, Objects, Set, Spliterator, Spliterators}
import java.util.function.{Consumer, Predicate}

class CopyOnWriteArraySet[E] extends AbstractSet[E] with Serializable {
  import CopyOnWriteArraySet._

  @SerialVersionUID(5457747651344034263L)
  private final val al: CopyOnWriteArrayList[E] = new CopyOnWriteArrayList[E]()

  def this(c: Collection[_ <: E]) = {
    this()
    if (c.getClass() == classOf[CopyOnWriteArraySet[_]]) {
      val cc = c.asInstanceOf[CopyOnWriteArraySet[E]]
      al.addAll(cc.al)
    } else {
      al.addAllAbsent(c)
    }
  }

  def size(): Int = al.size()

  override def isEmpty(): Boolean = al.isEmpty()

  def contains(o: Any): Boolean = al.contains(o)

  override def toArray(): Array[AnyRef] = al.toArray()

  override def toArray[T <: AnyRef](a: Array[T]): Array[T] = al.toArray(a)

  override def clear(): Unit = al.clear()

  def remove(o: Any): Boolean = al.remove(o)

  def add(e: E): Boolean = al.addIfAbsent(e)

  def containsAll(c: Collection[_]): Boolean = {
    c match {
      case s: Set[_] => compareSets(al.getArray(), s) >= 0
      case _         => al.containsAll(c)
    }
  }

  override def addAll(c: Collection[_ <: E]): Boolean = al.addAllAbsent(c) > 0

  override def removeAll(c: Collection[_]): Boolean = al.removeAll(c)

  override def retainAll(c: Collection[_]): Boolean = al.retainAll(c)

  def iterator(): Iterator[E] = al.iterator()

  override def equals(o: Any): Boolean = {
    (o eq this) || {
      o match {
        case s: Set[_] => compareSets(al.getArray(), s) == 0
        case _         => false
      }
    }
  }

  override def removeIf(filter: Predicate[_ >: E]): Boolean = al.removeIf(filter)

  override def forEach(action: Consumer[_ >: E]): Unit = al.forEach(action)

  override def spliterator(): Spliterator[E] = {
    Spliterators.spliterator(
      al.getArray(),
      Spliterator.IMMUTABLE | Spliterator.DISTINCT
    )
  }

  @throws[ObjectStreamException]
  private def readObjectNoData(): Unit = {
    throw new StreamCorruptedException("Deserialized CopyOnWriteArraySet requires data")
  }

  @throws[Exception]
  private def readObject(in: ObjectInputStream): Unit = {
    val fields = in.readFields()
    val inAl = fields.get("al", null).asInstanceOf[CopyOnWriteArrayList[E]]

    if (inAl == null ||
        inAl.getClass() != classOf[CopyOnWriteArrayList[_]] ||
        { val newAl = new CopyOnWriteArrayList[E]()
          newAl.addAllAbsent(inAl) != inAl.size() }) {
      throw new StreamCorruptedException("Content is invalid")
    }

    // Use reflection to set the final field
    val field = classOf[CopyOnWriteArraySet[_]].getDeclaredField("al")
    field.setAccessible(true)
    field.set(this, newAl)
  }
}

private object CopyOnWriteArraySet {
  /**
   * Tells whether the objects in snapshot (regarded as a set) are a
   * superset of the given set.
   *
   * @return -1 if snapshot is not a superset, 0 if the two sets
   * contain precisely the same elements, and 1 if snapshot is a
   * proper superset of the given set
   */
  private def compareSets(snapshot: Array[AnyRef], set: Set[_]): Int = {
    val len = snapshot.length
    val matched = new Array[Boolean](len)
    var j = 0

    val iter = set.iterator()
    while (iter.hasNext()) {
      val x = iter.next()
      var found = false
      var i = j
      while (!found && i < len) {
        if (!matched(i) && Objects.equals(x, snapshot(i))) {
          matched(i) = true
          found = true
          if (i == j) {
            var k = j
            while (k < len && matched(k)) k += 1
            j = k
          }
        }
        i += 1
      }
      if (!found) return -1
    }
    if (j == len) 0 else 1
  }
}
