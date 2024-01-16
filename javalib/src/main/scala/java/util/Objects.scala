/*
 * Ported from Scala.js
 *   commit SHA1: 558e8a0
 *   dated: 2020-10-20
 */

package java.util

import java.util.function.Supplier

object Objects {

  @inline
  def equals(a: Any, b: Any): Boolean =
    if (a == null) b == null
    else a.asInstanceOf[AnyRef].equals(b)

  @inline
  def deepEquals(a: Any, b: Any): Boolean = {
    if (a.asInstanceOf[AnyRef] eq b.asInstanceOf[AnyRef]) true
    else if (a == null || b == null) false
    else {
      (a, b) match {
        case (a1: Array[AnyRef], a2: Array[AnyRef]) => Arrays.deepEquals(a1, a2)
        case (a1: Array[Long], a2: Array[Long])     => Arrays.equals(a1, a2)
        case (a1: Array[Int], a2: Array[Int])       => Arrays.equals(a1, a2)
        case (a1: Array[Short], a2: Array[Short])   => Arrays.equals(a1, a2)
        case (a1: Array[Byte], a2: Array[Byte])     => Arrays.equals(a1, a2)
        case (a1: Array[Char], a2: Array[Char])     => Arrays.equals(a1, a2)
        case (a1: Array[Boolean], a2: Array[Boolean]) => Arrays.equals(a1, a2)
        case (a1: Array[Float], a2: Array[Float])     => Arrays.equals(a1, a2)
        case (a1: Array[Double], a2: Array[Double])   => Arrays.equals(a1, a2)
        case _                                        => Objects.equals(a, b)
      }
    }
  }

  @inline
  def hashCode(o: Any): Int =
    if (o == null) 0
    else o.hashCode()

  @inline
  def hash(values: Array[AnyRef]): Int =
    Arrays.hashCode(values)

  @inline
  def toString(o: Any): String =
    String.valueOf(o)

  @inline
  def toString(o: Any, nullDefault: String): String =
    if (o == null) nullDefault
    else o.toString

  @inline
  def compare[T](a: T, b: T, c: Comparator[_ >: T]): Int =
    if (a.asInstanceOf[AnyRef] eq b.asInstanceOf[AnyRef]) 0
    else c.compare(a, b)

  @inline
  def requireNonNull[T](obj: T): T =
    if (obj == null) throw new NullPointerException
    else obj

  @inline
  def requireNonNull[T](obj: T, message: String): T =
    if (obj == null) throw new NullPointerException(message)
    else obj

  @inline
  def isNull(obj: Any): Boolean =
    obj == null

  @inline
  def nonNull(obj: Any): Boolean =
    obj != null

  @inline
  def requireNonNull[T](obj: T, messageSupplier: Supplier[String]): T =
    if (obj == null) throw new NullPointerException(messageSupplier.get())
    else obj

  // since JDK9
  /** Checks if subrange <fromIndex, {fromIndex+size}) is withing the bounds of
   *  range <0, length)
   *
   *  @return
   *    fromIndex argument
   *  @throws java.lang.IndexOutOfBoundsException
   *    if not in subrange
   */
  def checkFromIndexSize(fromIndex: Int, size: Int, length: Int): Int = {
    if ((length | fromIndex | size) < 0 || size > length - fromIndex) {
      throw new IndexOutOfBoundsException(
        s"Range [$fromIndex, $fromIndex + $size] out of bounds for length $length"
      )
    }
    fromIndex
  }

  // since JDK16
  def checkFromIndexSize(fromIndex: Long, size: Long, length: Long): Long = {
    if ((length | fromIndex | size) < 0L || size > length - fromIndex) {
      throw new IndexOutOfBoundsException(
        s"Range [$fromIndex, $fromIndex + $size] out of bounds for length $length"
      )
    }
    fromIndex
  }
}
