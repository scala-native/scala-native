// Ported from Scala.js commit: ad7d82f dated: 2020-10-05
//
// Post Java 8 Static methods on List added for Scala Native

/* Some of the strange coding style, especially of specifying type
 * parameters explicitly and not using lambdas is due to the need to
 * support Scala versions from 2.12.19 through 3.N.
 */

package java.util

import java.util.function.UnaryOperator

trait List[E] extends SequencedCollection[E] {
  def replaceAll(operator: UnaryOperator[E]): Unit = {
    val iter = listIterator()
    while (iter.hasNext())
      iter.set(operator.apply(iter.next()))
  }

  def sort(c: Comparator[_ >: E]): Unit = {
    val arrayBuf = toArray()
    Arrays.sort[AnyRef with E](arrayBuf.asInstanceOf[Array[AnyRef with E]], c)

    val len = arrayBuf.length

    if (this.isInstanceOf[RandomAccess]) {
      var i = 0
      while (i != len) {
        set(i, arrayBuf(i).asInstanceOf[E])
        i += 1
      }
    } else {
      var i = 0
      val iter = listIterator()
      while (i != len) {
        iter.next()
        iter.set(arrayBuf(i).asInstanceOf[E])
        i += 1
      }
    }
  }

  def get(index: Int): E
  def set(index: Int, element: E): E
  def add(index: Int, element: E): Unit
  def remove(index: Int): E
  def indexOf(o: Any): Int
  def lastIndexOf(o: Any): Int
  def listIterator(): ListIterator[E]
  def listIterator(index: Int): ListIterator[E]
  def subList(fromIndex: Int, toIndex: Int): List[E]
  def addAll(index: Int, c: Collection[_ <: E]): Boolean
}

object List {

  private class UnmodifiableList[E](underlying: ArrayList[E])
      extends AbstractList[E]
      with RandomAccess {

    /* Since 'set()' is not defined, various methods which try to
     * mutate the list, such as subList#add or listIterator#remove,
     * should all throw, as desired.
     */

    override def size(): Int =
      underlying.size()

    override def get(index: Int): E = {
      underlying.get(index)
    }
  }

  def copyOf[E](coll: Collection[? <: E]): List[E] = {
    Objects.requireNonNull(coll)

    /*  The JVM List.copyOf() doc _may_ be saying, obtusely, that if the
     *  given collection is already an unmodifiable List then the original
     *  will be returned and a copy will not be made.
     *
     *  Scala Native may differ here in that it always makes a copy because
     *  the author saw no way to check for "already an unmodifiable list".
     *  Scala Native may need to implement and use a private class
     *  UnmodifiableList. A quick study of Collections.scala showed
     *  no suitable candidate.
     *
     *  A future pass may want to correct this difference, if it exists.
     *  Until then this code should provide some benefit in the by far
     *  more likely 'normal' cases.
     */

    val listSize = coll.size()

    val underlying = new ArrayList[E](listSize)

    coll.forEach(e => {
      Objects.requireNonNull(e)
      underlying.add(e)
    })

    new UnmodifiableList[E](underlying)
  }

  def of[E](): List[E] = {
    val listSize = 0

    val underlying = new ArrayList[E](listSize)

    new UnmodifiableList[E](underlying)
  }

  private def appendListOfElement[E](e: E, al: ArrayList[E]): Unit = {
    Objects.requireNonNull(e)
    al.add(e)
  }

  def of[E](e1: E): List[E] = {
    val listSize = 1

    val underlying = new ArrayList[E](listSize)
    appendListOfElement[E](e1, underlying)

    new UnmodifiableList[E](underlying)
  }

  def of[E](elements: Array[Object]): List[E] = {
    /* This overload handles varargs & must not conflict with single argument
     * overload. That is the reason for 'Array[Object]' rather than 'Array[E]'.
     */

    Objects.requireNonNull(elements)

    val listSize = elements.size

    for (j <- 0 until listSize)
      Objects.requireNonNull(elements(j))

    val underlying = Arrays
      .copyOf(elements, listSize)
      .asInstanceOf[Array[E]]

    /* It would be consistent, regular, & nice to call
     * "new UnmodifiableList[E](underlying)" here.
     *
     * That is not readily available here, because 'underlying' is an
     * Array and UnmodifiableList takes an ArrayList argument.
     * A defensive copy of the list has already been done. Open coding
     * the resultant list breaks regularity but avoids a costly
     * element-by-element copy from the defensive Array to an ArrayList
     * to use as an argument.
     *
     * Individual 'List.of()' methods are more likely to be used in the wild.
     * When this varargs overload is used, there will be more than 10 elements
     * so some efficiency will be needed.
     */

    new AbstractList[E] with RandomAccess {
      def size(): Int =
        listSize

      def get(index: Int): E =
        underlying(index)
    }
  }

  def of[E](e1: E, e2: E): List[E] = {
    val listSize = 2

    val underlying = new ArrayList[E](listSize)
    appendListOfElement[E](e1, underlying)
    appendListOfElement[E](e2, underlying)

    new UnmodifiableList[E](underlying)
  }

  def of[E](e1: E, e2: E, e3: E): List[E] = {
    val listSize = 3

    val underlying = new ArrayList[E](listSize)
    appendListOfElement[E](e1, underlying)
    appendListOfElement[E](e2, underlying)
    appendListOfElement[E](e3, underlying)

    new UnmodifiableList[E](underlying)
  }

  def of[E](e1: E, e2: E, e3: E, e4: E): List[E] = {
    val listSize = 4

    val underlying = new ArrayList[E](listSize)
    appendListOfElement[E](e1, underlying)
    appendListOfElement[E](e2, underlying)
    appendListOfElement[E](e3, underlying)
    appendListOfElement[E](e4, underlying)

    new UnmodifiableList[E](underlying)
  }

  def of[E](e1: E, e2: E, e3: E, e4: E, e5: E): List[E] = {
    val listSize = 5

    val underlying = new ArrayList[E](listSize)
    appendListOfElement[E](e1, underlying)
    appendListOfElement[E](e2, underlying)
    appendListOfElement[E](e3, underlying)
    appendListOfElement[E](e4, underlying)
    appendListOfElement[E](e5, underlying)

    new UnmodifiableList[E](underlying)
  }

  def of[E](e1: E, e2: E, e3: E, e4: E, e5: E, e6: E): List[E] = {
    val listSize = 6

    val underlying = new ArrayList[E](listSize)
    appendListOfElement[E](e1, underlying)
    appendListOfElement[E](e2, underlying)
    appendListOfElement[E](e3, underlying)
    appendListOfElement[E](e4, underlying)
    appendListOfElement[E](e5, underlying)
    appendListOfElement[E](e6, underlying)

    new UnmodifiableList[E](underlying)
  }

  def of[E](e1: E, e2: E, e3: E, e4: E, e5: E, e6: E, e7: E): List[E] = {
    val listSize = 7

    val underlying = new ArrayList[E](listSize)
    appendListOfElement[E](e1, underlying)
    appendListOfElement[E](e2, underlying)
    appendListOfElement[E](e3, underlying)
    appendListOfElement[E](e4, underlying)
    appendListOfElement[E](e5, underlying)
    appendListOfElement[E](e6, underlying)
    appendListOfElement[E](e7, underlying)

    new UnmodifiableList[E](underlying)
  }

  def of[E](e1: E, e2: E, e3: E, e4: E, e5: E, e6: E, e7: E, e8: E): List[E] = {
    val listSize = 8

    val underlying = new ArrayList[E](listSize)
    appendListOfElement[E](e1, underlying)
    appendListOfElement[E](e2, underlying)
    appendListOfElement[E](e3, underlying)
    appendListOfElement[E](e4, underlying)
    appendListOfElement[E](e5, underlying)
    appendListOfElement[E](e6, underlying)
    appendListOfElement[E](e7, underlying)
    appendListOfElement[E](e8, underlying)

    new UnmodifiableList[E](underlying)
  }

  def of[E](e1: E, e2: E, e3: E, e4: E, e5: E, e6: E, e7: E, e8: E, e9: E)
      : List[E] = {
    val listSize = 9

    val underlying = new ArrayList[E](listSize)
    appendListOfElement[E](e1, underlying)
    appendListOfElement[E](e2, underlying)
    appendListOfElement[E](e3, underlying)
    appendListOfElement[E](e4, underlying)
    appendListOfElement[E](e5, underlying)
    appendListOfElement[E](e6, underlying)
    appendListOfElement[E](e7, underlying)
    appendListOfElement[E](e8, underlying)
    appendListOfElement[E](e9, underlying)

    new UnmodifiableList[E](underlying)
  }

  def of[E](
      e1: E,
      e2: E,
      e3: E,
      e4: E,
      e5: E,
      e6: E,
      e7: E,
      e8: E,
      e9: E,
      e10: E
  ): List[E] = {
    val listSize = 10

    val underlying = new ArrayList[E](listSize)
    appendListOfElement[E](e1, underlying)
    appendListOfElement[E](e2, underlying)
    appendListOfElement[E](e3, underlying)
    appendListOfElement[E](e4, underlying)
    appendListOfElement[E](e5, underlying)
    appendListOfElement[E](e6, underlying)
    appendListOfElement[E](e7, underlying)
    appendListOfElement[E](e8, underlying)
    appendListOfElement[E](e9, underlying)
    appendListOfElement[E](e10, underlying)

    new UnmodifiableList[E](underlying)
  }
}
