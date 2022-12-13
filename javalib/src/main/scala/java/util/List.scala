// Ported from Scala.js commit: ad7d82f dated: 2020-10-05

package java.util

import java.util.function.UnaryOperator

trait List[E] extends Collection[E] {
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
