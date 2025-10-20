/* scalafmt: {
     binPack.defnSite = always
     binPack.callSite = always
     newlines.configStyle.fallBack.prefer = false
   }
 */

package java.util

trait Set[E] extends Collection[E] {

  def add(obj: E): scala.Boolean
  def addAll(coll: Collection[? <: E]): scala.Boolean
  def clear(): Unit
  def contains(obj: Any): scala.Boolean
  def containsAll(c: Collection[?]): Boolean
  def equals(obj: Any): scala.Boolean
  def hashCode(): scala.Int
  def isEmpty(): scala.Boolean
  def iterator(): Iterator[E]
  def remove(obj: Any): scala.Boolean
  def removeAll(c: Collection[?]): Boolean
  def retainAll(c: Collection[?]): Boolean
  def size(): scala.Int

  override def spliterator(): Spliterator[E] = {
    Spliterators.spliterator[E](
      this,
      Spliterator.SIZED |
        Spliterator.SUBSIZED |
        Spliterator.DISTINCT
    )
  }

  def toArray(): Array[AnyRef]
  def toArray[T <: AnyRef](a: Array[T]): Array[T]

}

object Set {

  // Since: Java 10
  def copyOf[E](coll: Collection[? <: E]): Set[E] = {
    Objects.requireNonNull(coll)

    val setSize = coll.size()

    val underlying = HashSet.newHashSet[E](setSize)

    coll.forEach((e: E) => {
      Objects.requireNonNull(e)
      underlying.add(e) // if duplicate, 1 arbitrary instance will be retained.
    })

    Collections.unmodifiableSet(underlying)
  }

  // Since: Java 9
  def of[E](): Set[E] = {
    val setSize = 0

    val underlying = HashSet.newHashSet[E](setSize)

    Collections.unmodifiableSet[E](underlying)
  }

  private def appendSetOfElement[E](e: E, hs: HashSet[E]): Unit = {
    Objects.requireNonNull(e)
    if (!hs.add(e)) {
      // This is what JVM 22 says. Maybe the "pe" makes sense to you.
      throw new IllegalArgumentException(
        "Cannot invoke \"Object.hashCode()\" because \"pe\" is null"
      )
    }
  }

  // Since: Java 9
  def of[E](e1: E): Set[E] = {
    val setSize = 1

    val underlying = HashSet.newHashSet[E](setSize)

    appendSetOfElement[E](e1, underlying)

    Collections.unmodifiableSet[E](underlying)
  }

  // Since: Java 9
  def of[E](elements: Array[Object]): Set[E] = {
    /* This overload handles varargs & must not conflict with single argument
     * overload. That is the reason for 'Array[Object]' rather than 'Array[E]'.
     */

    Objects.requireNonNull(elements)

    val setSize = elements.size

    val underlying = HashSet.newHashSet[E](setSize)

    for (j <- 0 until setSize)
      appendSetOfElement[E](elements(j).asInstanceOf[E], underlying)

    Collections.unmodifiableSet[E](underlying)
  }

  // Since: Java 9
  def of[E](e1: E, e2: E): Set[E] = {
    val setSize = 2

    val underlying = HashSet.newHashSet[E](setSize)
    appendSetOfElement[E](e1, underlying)
    appendSetOfElement[E](e2, underlying)

    Collections.unmodifiableSet[E](underlying)
  }

  // Since: Java 9
  def of[E](e1: E, e2: E, e3: E): Set[E] = {
    val setSize = 3

    val underlying = HashSet.newHashSet[E](setSize)
    appendSetOfElement[E](e1, underlying)
    appendSetOfElement[E](e2, underlying)
    appendSetOfElement[E](e3, underlying)

    Collections.unmodifiableSet[E](underlying)
  }

  // Since: Java 9
  def of[E](e1: E, e2: E, e3: E, e4: E): Set[E] = {
    val setSize = 4

    val underlying = HashSet.newHashSet[E](setSize)
    appendSetOfElement[E](e1, underlying)
    appendSetOfElement[E](e2, underlying)
    appendSetOfElement[E](e3, underlying)
    appendSetOfElement[E](e4, underlying)

    Collections.unmodifiableSet[E](underlying)
  }

  // Since: Java 9
  def of[E](e1: E, e2: E, e3: E, e4: E, e5: E): Set[E] = {
    val setSize = 5

    val underlying = HashSet.newHashSet[E](setSize)
    appendSetOfElement[E](e1, underlying)
    appendSetOfElement[E](e2, underlying)
    appendSetOfElement[E](e3, underlying)
    appendSetOfElement[E](e4, underlying)
    appendSetOfElement[E](e5, underlying)

    Collections.unmodifiableSet[E](underlying)
  }

  // Since: Java 9
  def of[E](e1: E, e2: E, e3: E, e4: E, e5: E, e6: E): Set[E] = {
    val setSize = 6

    val underlying = HashSet.newHashSet[E](setSize)
    appendSetOfElement[E](e1, underlying)
    appendSetOfElement[E](e2, underlying)
    appendSetOfElement[E](e3, underlying)
    appendSetOfElement[E](e4, underlying)
    appendSetOfElement[E](e5, underlying)
    appendSetOfElement[E](e6, underlying)

    Collections.unmodifiableSet[E](underlying)
  }

  // Since: Java 9
  def of[E](e1: E, e2: E, e3: E, e4: E, e5: E, e6: E, e7: E): Set[E] = {
    val setSize = 7

    val underlying = HashSet.newHashSet[E](setSize)
    appendSetOfElement[E](e1, underlying)
    appendSetOfElement[E](e2, underlying)
    appendSetOfElement[E](e3, underlying)
    appendSetOfElement[E](e4, underlying)
    appendSetOfElement[E](e5, underlying)
    appendSetOfElement[E](e6, underlying)
    appendSetOfElement[E](e7, underlying)

    Collections.unmodifiableSet[E](underlying)
  }

  // Since: Java 9
  def of[E](e1: E, e2: E, e3: E, e4: E, e5: E, e6: E, e7: E, e8: E): Set[E] = {
    val setSize = 8

    val underlying = HashSet.newHashSet[E](setSize)
    appendSetOfElement[E](e1, underlying)
    appendSetOfElement[E](e2, underlying)
    appendSetOfElement[E](e3, underlying)
    appendSetOfElement[E](e4, underlying)
    appendSetOfElement[E](e5, underlying)
    appendSetOfElement[E](e6, underlying)
    appendSetOfElement[E](e7, underlying)
    appendSetOfElement[E](e8, underlying)

    Collections.unmodifiableSet[E](underlying)
  }

  // Since: Java 9
  def of[E](
      e1: E, e2: E, e3: E, e4: E, e5: E, e6: E, e7: E, e8: E, e9: E
  ): Set[E] = {
    val setSize = 9

    val underlying = HashSet.newHashSet[E](setSize)
    appendSetOfElement[E](e1, underlying)
    appendSetOfElement[E](e2, underlying)
    appendSetOfElement[E](e3, underlying)
    appendSetOfElement[E](e4, underlying)
    appendSetOfElement[E](e5, underlying)
    appendSetOfElement[E](e6, underlying)
    appendSetOfElement[E](e7, underlying)
    appendSetOfElement[E](e8, underlying)
    appendSetOfElement[E](e9, underlying)

    Collections.unmodifiableSet[E](underlying)
  }

  // Since: Java 9
  def of[E](
      e1: E, e2: E, e3: E, e4: E, e5: E, e6: E, e7: E, e8: E, e9: E, e10: E
  ): Set[E] = {
    val setSize = 10

    val underlying = HashSet.newHashSet[E](setSize)
    appendSetOfElement[E](e1, underlying)
    appendSetOfElement[E](e2, underlying)
    appendSetOfElement[E](e3, underlying)
    appendSetOfElement[E](e4, underlying)
    appendSetOfElement[E](e5, underlying)
    appendSetOfElement[E](e6, underlying)
    appendSetOfElement[E](e7, underlying)
    appendSetOfElement[E](e8, underlying)
    appendSetOfElement[E](e9, underlying)
    appendSetOfElement[E](e10, underlying)

    Collections.unmodifiableSet[E](underlying)
  }
}
