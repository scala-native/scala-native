// Ported from Scala.js commit: ba618ed dated: 2020-10-05
//
// Contains Scala Native specific updates subsequent to original port.
// See Scala Native git repository history.

package java.util

import scala.scalanative.annotation.alwaysinline
import scala.annotation.tailrec

import scala.reflect.ClassTag

import scala.scalanative.libc

import scala.scalanative.unsafe._
import scala.scalanative.unsafe.Size // Required by Scala 2.12 for toSize()

import scala.scalanative.unsigned._

import scala.scalanative.libc.string

import java.{lang => jl}

import java.{util => ju}
import java.util.function._
import java.util.stream.StreamSupport

object Arrays {
  @inline
  private final implicit def naturalOrdering[T <: AnyRef]: Ordering[T] = {
    new Ordering[T] {
      def compare(x: T, y: T): Int = x.asInstanceOf[_Comparable[T]].compareTo(y)
    }
  }

  // Impose the total ordering of java.lang.Float.compare in Arrays
  private implicit object FloatTotalOrdering extends Ordering[Float] {
    def compare(x: Float, y: Float): Int = java.lang.Float.compare(x, y)
  }

  // Impose the total ordering of java.lang.Double.compare in Arrays
  private implicit object DoubleTotalOrdering extends Ordering[Double] {
    def compare(x: Double, y: Double): Int = java.lang.Double.compare(x, y)
  }

  @noinline def sort(a: Array[Int]): Unit =
    sortImpl(a)

  @noinline def sort(a: Array[Int], fromIndex: Int, toIndex: Int): Unit =
    sortRangeImpl[Int](a, fromIndex, toIndex)

  @noinline def sort(a: Array[Long]): Unit =
    sortImpl(a)

  @noinline def sort(a: Array[Long], fromIndex: Int, toIndex: Int): Unit =
    sortRangeImpl[Long](a, fromIndex, toIndex)

  @noinline def sort(a: Array[Short]): Unit =
    sortImpl(a)

  @noinline def sort(a: Array[Short], fromIndex: Int, toIndex: Int): Unit =
    sortRangeImpl[Short](a, fromIndex, toIndex)

  @noinline def sort(a: Array[Char]): Unit =
    sortImpl(a)

  @noinline def sort(a: Array[Char], fromIndex: Int, toIndex: Int): Unit =
    sortRangeImpl[Char](a, fromIndex, toIndex)

  @noinline def sort(a: Array[Byte]): Unit =
    sortImpl(a)

  @noinline def sort(a: Array[Byte], fromIndex: Int, toIndex: Int): Unit =
    sortRangeImpl[Byte](a, fromIndex, toIndex)

  @noinline def sort(a: Array[Float]): Unit =
    sortImpl(a)

  @noinline def sort(a: Array[Float], fromIndex: Int, toIndex: Int): Unit =
    sortRangeImpl[Float](a, fromIndex, toIndex)

  @noinline def sort(a: Array[Double]): Unit =
    sortImpl(a)

  @noinline def sort(a: Array[Double], fromIndex: Int, toIndex: Int): Unit =
    sortRangeImpl[Double](a, fromIndex, toIndex)

  @noinline def sort(a: Array[AnyRef]): Unit =
    sortAnyRefImpl(a)

  @noinline def sort(a: Array[AnyRef], fromIndex: Int, toIndex: Int): Unit =
    sortRangeAnyRefImpl(a, fromIndex, toIndex)

  @noinline
  def sort[T <: AnyRef](
      array: Array[T],
      comparator: Comparator[_ >: T]
  ): Unit = {
    implicit val ord = toOrdering(comparator).asInstanceOf[Ordering[AnyRef]]
    sortAnyRefImpl(array.asInstanceOf[Array[AnyRef]])
  }

  @noinline
  def sort[T <: AnyRef](
      array: Array[T],
      fromIndex: Int,
      toIndex: Int,
      comparator: Comparator[_ >: T]
  ): Unit = {
    implicit val ord = toOrdering(comparator).asInstanceOf[Ordering[AnyRef]]
    sortRangeAnyRefImpl(array.asInstanceOf[Array[AnyRef]], fromIndex, toIndex)
  }

  @inline
  private def sortRangeImpl[@specialized T: ClassTag](
      a: Array[T],
      fromIndex: Int,
      toIndex: Int
  )(implicit ord: Ordering[T]): Unit = {
    checkRangeIndices(a, fromIndex, toIndex)
    stableMergeSort[T](a, fromIndex, toIndex)
  }

  @inline
  private def sortRangeAnyRefImpl(
      a: Array[AnyRef],
      fromIndex: Int,
      toIndex: Int
  )(implicit ord: Ordering[AnyRef]): Unit = {
    checkRangeIndices(a, fromIndex, toIndex)
    stableMergeSortAnyRef(a, fromIndex, toIndex)
  }

  @inline
  private def sortImpl[@specialized T: ClassTag: Ordering](a: Array[T]): Unit =
    stableMergeSort[T](a, 0, a.length)

  @inline
  private def sortAnyRefImpl(a: Array[AnyRef])(implicit
      ord: Ordering[AnyRef]
  ): Unit =
    stableMergeSortAnyRef(a, 0, a.length)

  private final val inPlaceSortThreshold = 16

  /** Sort array `a` with merge sort and insertion sort, using the Ordering on
   *  its elements.
   */
  @inline
  private def stableMergeSort[@specialized K: ClassTag](
      a: Array[K],
      start: Int,
      end: Int
  )(implicit ord: Ordering[K]): Unit = {
    if (end - start > inPlaceSortThreshold)
      stableSplitMerge(a, new Array[K](a.length), start, end)
    else
      insertionSort(a, start, end)
  }

  @noinline
  private def stableSplitMerge[@specialized K](
      a: Array[K],
      temp: Array[K],
      start: Int,
      end: Int
  )(implicit ord: Ordering[K]): Unit = {
    val length = end - start
    if (length > inPlaceSortThreshold) {
      val middle = start + (length / 2)
      stableSplitMerge(a, temp, start, middle)
      stableSplitMerge(a, temp, middle, end)
      stableMerge(a, temp, start, middle, end)
      System.arraycopy(temp, start, a, start, length)
    } else {
      insertionSort(a, start, end)
    }
  }

  @inline
  private def stableMerge[@specialized K](
      a: Array[K],
      temp: Array[K],
      start: Int,
      middle: Int,
      end: Int
  )(implicit ord: Ordering[K]): Unit = {
    var outIndex = start
    var leftInIndex = start
    var rightInIndex = middle
    while (outIndex < end) {
      if (leftInIndex < middle &&
          (rightInIndex >= end || ord.lteq(a(leftInIndex), a(rightInIndex)))) {
        temp(outIndex) = a(leftInIndex)
        leftInIndex += 1
      } else {
        temp(outIndex) = a(rightInIndex)
        rightInIndex += 1
      }
      outIndex += 1
    }
  }

  // Ordering[T] might be slow especially for boxed primitives, so use binary
  // search variant of insertion sort
  // Caller must pass end >= start or math will fail.  Also, start >= 0.
  @noinline
  private final def insertionSort[@specialized T](
      a: Array[T],
      start: Int,
      end: Int
  )(implicit ord: Ordering[T]): Unit = {
    val n = end - start
    if (n >= 2) {
      if (ord.compare(a(start), a(start + 1)) > 0) {
        val temp = a(start)
        a(start) = a(start + 1)
        a(start + 1) = temp
      }
      var m = 2
      while (m < n) {
        // Speed up already-sorted case by checking last element first
        val next = a(start + m)
        if (ord.compare(next, a(start + m - 1)) < 0) {
          var iA = start
          var iB = start + m - 1
          while (iB - iA > 1) {
            val ix = (iA + iB) >>> 1 // Use bit shift to get unsigned div by 2
            if (ord.compare(next, a(ix)) < 0)
              iB = ix
            else
              iA = ix
          }
          val ix = iA + (if (ord.compare(next, a(iA)) < 0) 0 else 1)
          var i = start + m
          while (i > ix) {
            a(i) = a(i - 1)
            i -= 1
          }
          a(ix) = next
        }
        m += 1
      }
    }
  }

  /** Sort array `a` with merge sort and insertion sort, using the Ordering on
   *  its elements.
   */
  @inline
  private def stableMergeSortAnyRef(a: Array[AnyRef], start: Int, end: Int)(
      implicit ord: Ordering[AnyRef]
  ): Unit = {
    if (end - start > inPlaceSortThreshold)
      stableSplitMergeAnyRef(a, new Array(a.length), start, end)
    else
      insertionSortAnyRef(a, start, end)
  }

  @noinline
  private def stableSplitMergeAnyRef(
      a: Array[AnyRef],
      temp: Array[AnyRef],
      start: Int,
      end: Int
  )(implicit ord: Ordering[AnyRef]): Unit = {
    val length = end - start
    if (length > inPlaceSortThreshold) {
      val middle = start + (length / 2)
      stableSplitMergeAnyRef(a, temp, start, middle)
      stableSplitMergeAnyRef(a, temp, middle, end)
      stableMergeAnyRef(a, temp, start, middle, end)
      System.arraycopy(temp, start, a, start, length)
    } else {
      insertionSortAnyRef(a, start, end)
    }
  }

  @inline
  private def stableMergeAnyRef(
      a: Array[AnyRef],
      temp: Array[AnyRef],
      start: Int,
      middle: Int,
      end: Int
  )(implicit ord: Ordering[AnyRef]): Unit = {
    var outIndex = start
    var leftInIndex = start
    var rightInIndex = middle
    while (outIndex < end) {
      if (leftInIndex < middle &&
          (rightInIndex >= end || ord.lteq(a(leftInIndex), a(rightInIndex)))) {
        temp(outIndex) = a(leftInIndex)
        leftInIndex += 1
      } else {
        temp(outIndex) = a(rightInIndex)
        rightInIndex += 1
      }
      outIndex += 1
    }
  }

  @noinline
  private final def insertionSortAnyRef(a: Array[AnyRef], start: Int, end: Int)(
      implicit ord: Ordering[AnyRef]
  ): Unit = {
    val n = end - start
    if (n >= 2) {
      if (ord.compare(a(start), a(start + 1)) > 0) {
        val temp = a(start)
        a(start) = a(start + 1)
        a(start + 1) = temp
      }
      var m = 2
      while (m < n) {
        // Speed up already-sorted case by checking last element first
        val next = a(start + m)
        if (ord.compare(next, a(start + m - 1)) < 0) {
          var iA = start
          var iB = start + m - 1
          while (iB - iA > 1) {
            val ix = (iA + iB) >>> 1 // Use bit shift to get unsigned div by 2
            if (ord.compare(next, a(ix)) < 0)
              iB = ix
            else
              iA = ix
          }
          val ix = iA + (if (ord.compare(next, a(iA)) < 0) 0 else 1)
          var i = start + m
          while (i > ix) {
            a(i) = a(i - 1)
            i -= 1
          }
          a(ix) = next
        }
        m += 1
      }
    }
  }

  @noinline def binarySearch(a: Array[Long], key: Long): Int =
    binarySearchImpl[Long](a, 0, a.length, key, _ < _)

  @noinline
  def binarySearch(
      a: Array[Long],
      startIndex: Int,
      endIndex: Int,
      key: Long
  ): Int = {
    checkRangeIndices(a, startIndex, endIndex)
    binarySearchImpl[Long](a, startIndex, endIndex, key, _ < _)
  }

  @noinline def binarySearch(a: Array[Int], key: Int): Int =
    binarySearchImpl[Int](a, 0, a.length, key, _ < _)

  @noinline
  def binarySearch(
      a: Array[Int],
      startIndex: Int,
      endIndex: Int,
      key: Int
  ): Int = {
    checkRangeIndices(a, startIndex, endIndex)
    binarySearchImpl[Int](a, startIndex, endIndex, key, _ < _)
  }

  @noinline def binarySearch(a: Array[Short], key: Short): Int =
    binarySearchImpl[Short](a, 0, a.length, key, _ < _)

  @noinline
  def binarySearch(
      a: Array[Short],
      startIndex: Int,
      endIndex: Int,
      key: Short
  ): Int = {
    checkRangeIndices(a, startIndex, endIndex)
    binarySearchImpl[Short](a, startIndex, endIndex, key, _ < _)
  }

  @noinline def binarySearch(a: Array[Char], key: Char): Int =
    binarySearchImpl[Char](a, 0, a.length, key, _ < _)

  @noinline
  def binarySearch(
      a: Array[Char],
      startIndex: Int,
      endIndex: Int,
      key: Char
  ): Int = {
    checkRangeIndices(a, startIndex, endIndex)
    binarySearchImpl[Char](a, startIndex, endIndex, key, _ < _)
  }

  @noinline def binarySearch(a: Array[Byte], key: Byte): Int =
    binarySearchImpl[Byte](a, 0, a.length, key, _ < _)

  @noinline
  def binarySearch(
      a: Array[Byte],
      startIndex: Int,
      endIndex: Int,
      key: Byte
  ): Int = {
    checkRangeIndices(a, startIndex, endIndex)
    binarySearchImpl[Byte](a, startIndex, endIndex, key, _ < _)
  }

  @noinline def binarySearch(a: Array[Double], key: Double): Int =
    binarySearchImpl[Double](a, 0, a.length, key, _ < _)

  @noinline
  def binarySearch(
      a: Array[Double],
      startIndex: Int,
      endIndex: Int,
      key: Double
  ): Int = {
    checkRangeIndices(a, startIndex, endIndex)
    binarySearchImpl[Double](a, startIndex, endIndex, key, _ < _)
  }

  @noinline def binarySearch(a: Array[Float], key: Float): Int =
    binarySearchImpl[Float](a, 0, a.length, key, _ < _)

  @noinline
  def binarySearch(
      a: Array[Float],
      startIndex: Int,
      endIndex: Int,
      key: Float
  ): Int = {
    checkRangeIndices(a, startIndex, endIndex)
    binarySearchImpl[Float](a, startIndex, endIndex, key, _ < _)
  }

  @noinline def binarySearch(a: Array[AnyRef], key: AnyRef): Int =
    binarySearchImplRef(a, 0, a.length, key)

  @noinline
  def binarySearch(
      a: Array[AnyRef],
      startIndex: Int,
      endIndex: Int,
      key: AnyRef
  ): Int = {
    checkRangeIndices(a, startIndex, endIndex)
    binarySearchImplRef(a, startIndex, endIndex, key)
  }

  @noinline
  def binarySearch[T](a: Array[T], key: T, c: Comparator[_ >: T]): Int =
    binarySearchImpl[T](a, 0, a.length, key, (a, b) => c.compare(a, b) < 0)

  @noinline
  def binarySearch[T](
      a: Array[T],
      startIndex: Int,
      endIndex: Int,
      key: T,
      c: Comparator[_ >: T]
  ): Int = {
    checkRangeIndices(a, startIndex, endIndex)
    binarySearchImpl[T](
      a,
      startIndex,
      endIndex,
      key,
      (a, b) => c.compare(a, b) < 0
    )
  }

  @inline
  @tailrec
  private def binarySearchImpl[T](
      a: Array[T],
      startIndex: Int,
      endIndex: Int,
      key: T,
      lt: (T, T) => Boolean
  ): Int = {
    if (startIndex == endIndex) {
      // Not found
      -startIndex - 1
    } else {
      // Indices are unsigned 31-bit integer, so this does not overflow
      val mid = (startIndex + endIndex) >>> 1
      val elem = a(mid)
      if (lt(key, elem)) {
        binarySearchImpl(a, startIndex, mid, key, lt)
      } else if (key == elem) {
        // Found
        mid
      } else {
        binarySearchImpl(a, mid + 1, endIndex, key, lt)
      }
    }
  }

  @inline
  @tailrec
  def binarySearchImplRef(
      a: Array[AnyRef],
      startIndex: Int,
      endIndex: Int,
      key: AnyRef
  ): Int = {
    if (startIndex == endIndex) {
      // Not found
      -startIndex - 1
    } else {
      // Indices are unsigned 31-bit integer, so this does not overflow
      val mid = (startIndex + endIndex) >>> 1
      val cmp = key.asInstanceOf[_Comparable[AnyRef]].compareTo(a(mid))
      if (cmp < 0) {
        binarySearchImplRef(a, startIndex, mid, key)
      } else if (cmp == 0) {
        // Found
        mid
      } else {
        binarySearchImplRef(a, mid + 1, endIndex, key)
      }
    }
  }

  @noinline def equals(a: Array[Long], b: Array[Long]): Boolean =
    equalsImpl(a, b)

  @noinline def equals(a: Array[Int], b: Array[Int]): Boolean =
    equalsImpl(a, b)

  @noinline def equals(a: Array[Short], b: Array[Short]): Boolean =
    equalsImpl(a, b)

  @noinline def equals(a: Array[Char], b: Array[Char]): Boolean =
    equalsImpl(a, b)

  @noinline def equals(a: Array[Byte], b: Array[Byte]): Boolean =
    equalsImpl(a, b)

  @noinline def equals(a: Array[Boolean], b: Array[Boolean]): Boolean =
    equalsImpl(a, b)

  @noinline def equals(a: Array[Double], b: Array[Double]): Boolean =
    equalsImpl(a, b)

  @noinline def equals(a: Array[Float], b: Array[Float]): Boolean =
    equalsImpl(a, b)

  @noinline def equals(a: Array[AnyRef], b: Array[AnyRef]): Boolean =
    equalsImpl(a, b)

  @inline private def equalsImpl[T](a: Array[T], b: Array[T]): Boolean = {
    // scalastyle:off return
    if (a eq b)
      return true
    if (a == null || b == null)
      return false
    val len = a.length
    if (b.length != len)
      return false
    var i = 0
    while (i != len) {
      if (a(i) != b(i))
        return false
      i += 1
    }
    true
    // scalastyle:on return
  }

  @noinline def fill(a: Array[Long], value: Long): Unit =
    fillImpl(a, 0, a.length, value, checkIndices = false)

  @noinline
  def fill(a: Array[Long], fromIndex: Int, toIndex: Int, value: Long): Unit =
    fillImpl(a, fromIndex, toIndex, value)

  @noinline def fill(a: Array[Int], value: Int): Unit =
    fillImpl(a, 0, a.length, value, checkIndices = false)

  @noinline
  def fill(a: Array[Int], fromIndex: Int, toIndex: Int, value: Int): Unit =
    fillImpl(a, fromIndex, toIndex, value)

  @noinline def fill(a: Array[Short], value: Short): Unit =
    fillImpl(a, 0, a.length, value, checkIndices = false)

  @noinline
  def fill(a: Array[Short], fromIndex: Int, toIndex: Int, value: Short): Unit =
    fillImpl(a, fromIndex, toIndex, value)

  @noinline def fill(a: Array[Char], value: Char): Unit =
    fillImpl(a, 0, a.length, value, checkIndices = false)

  @noinline
  def fill(a: Array[Char], fromIndex: Int, toIndex: Int, value: Char): Unit =
    fillImpl(a, fromIndex, toIndex, value)

  @noinline def fill(a: Array[Byte], value: Byte): Unit =
    fillImpl(a, 0, a.length, value, checkIndices = false)

  @noinline
  def fill(a: Array[Byte], fromIndex: Int, toIndex: Int, value: Byte): Unit =
    fillImpl(a, fromIndex, toIndex, value)

  @noinline def fill(a: Array[Boolean], value: Boolean): Unit =
    fillImpl(a, 0, a.length, value, checkIndices = false)

  @noinline
  def fill(
      a: Array[Boolean],
      fromIndex: Int,
      toIndex: Int,
      value: Boolean
  ): Unit =
    fillImpl(a, fromIndex, toIndex, value)

  @noinline def fill(a: Array[Double], value: Double): Unit =
    fillImpl(a, 0, a.length, value, checkIndices = false)

  @noinline
  def fill(
      a: Array[Double],
      fromIndex: Int,
      toIndex: Int,
      value: Double
  ): Unit =
    fillImpl(a, fromIndex, toIndex, value)

  @noinline def fill(a: Array[Float], value: Float): Unit =
    fillImpl(a, 0, a.length, value, checkIndices = false)

  @noinline
  def fill(a: Array[Float], fromIndex: Int, toIndex: Int, value: Float): Unit =
    fillImpl(a, fromIndex, toIndex, value)

  @noinline def fill(a: Array[AnyRef], value: AnyRef): Unit =
    fillImpl(a, 0, a.length, value, checkIndices = false)

  @noinline
  def fill(
      a: Array[AnyRef],
      fromIndex: Int,
      toIndex: Int,
      value: AnyRef
  ): Unit =
    fillImpl(a, fromIndex, toIndex, value)

  @inline
  private def fillImpl[T](
      a: Array[T],
      fromIndex: Int,
      toIndex: Int,
      value: T,
      checkIndices: Boolean = true
  ): Unit = {
    if (checkIndices)
      checkRangeIndices(a, fromIndex, toIndex)
    var i = fromIndex
    while (i != toIndex) {
      a(i) = value
      i += 1
    }
  }

  @noinline
  def copyOf[T <: AnyRef](original: Array[T], newLength: Int): Array[T] = {
    implicit val tagT = ClassTag[T](original.getClass.getComponentType)
    copyOfImpl(original, newLength)
  }

  @noinline
  def copyOf[T <: AnyRef, U <: AnyRef](
      original: Array[U],
      newLength: Int,
      newType: Class[_ <: Array[T]]
  ): Array[T] = {
    implicit val tag = ClassTag[T](newType.getComponentType)
    copyOfImpl(original, newLength)
  }

  @noinline def copyOf(original: Array[Byte], newLength: Int): Array[Byte] =
    copyOfImpl(original, newLength)

  @noinline def copyOf(original: Array[Short], newLength: Int): Array[Short] =
    copyOfImpl(original, newLength)

  @noinline def copyOf(original: Array[Int], newLength: Int): Array[Int] =
    copyOfImpl(original, newLength)

  @noinline def copyOf(original: Array[Long], newLength: Int): Array[Long] =
    copyOfImpl(original, newLength)

  @noinline def copyOf(original: Array[Char], newLength: Int): Array[Char] =
    copyOfImpl(original, newLength)

  @noinline def copyOf(original: Array[Float], newLength: Int): Array[Float] =
    copyOfImpl(original, newLength)

  @noinline
  def copyOf(original: Array[Double], newLength: Int): Array[Double] =
    copyOfImpl(original, newLength)

  @noinline
  def copyOf(original: Array[Boolean], newLength: Int): Array[Boolean] =
    copyOfImpl(original, newLength)

  @inline
  private def copyOfImpl[U, T: ClassTag](
      original: Array[U],
      newLength: Int
  ): Array[T] = {
    checkArrayLength(newLength)
    val copyLength = Math.min(newLength, original.length)
    val ret = new Array[T](newLength)
    System.arraycopy(original, 0, ret, 0, copyLength)
    ret
  }

  @noinline
  def copyOfRange[T <: AnyRef](
      original: Array[T],
      from: Int,
      to: Int
  ): Array[T] = {
    implicit def tag: ClassTag[T] = ClassTag(original.getClass.getComponentType)
    copyOfRangeImpl[T](original, from, to)
      .asInstanceOf[Array[T]]
  }

  @noinline
  def copyOfRange[T <: AnyRef, U <: AnyRef](
      original: Array[U],
      from: Int,
      to: Int,
      newType: Class[_ <: Array[T]]
  ): Array[T] = {
    implicit def tag: ClassTag[T] = ClassTag(original.getClass.getComponentType)
    copyOfRangeImpl[AnyRef](original.asInstanceOf[Array[AnyRef]], from, to)
      .asInstanceOf[Array[T]]
  }

  @noinline
  def copyOfRange(original: Array[Byte], start: Int, end: Int): Array[Byte] =
    copyOfRangeImpl[Byte](original, start, end)

  @noinline
  def copyOfRange(original: Array[Short], start: Int, end: Int): Array[Short] =
    copyOfRangeImpl(original, start, end)

  @noinline
  def copyOfRange(original: Array[Int], start: Int, end: Int): Array[Int] =
    copyOfRangeImpl(original, start, end)

  @noinline
  def copyOfRange(original: Array[Long], start: Int, end: Int): Array[Long] =
    copyOfRangeImpl(original, start, end)

  @noinline
  def copyOfRange(original: Array[Char], start: Int, end: Int): Array[Char] =
    copyOfRangeImpl(original, start, end)

  @noinline
  def copyOfRange(original: Array[Float], start: Int, end: Int): Array[Float] =
    copyOfRangeImpl(original, start, end)

  @noinline
  def copyOfRange(
      original: Array[Double],
      start: Int,
      end: Int
  ): Array[Double] =
    copyOfRangeImpl(original, start, end)

  @noinline
  def copyOfRange(
      original: Array[Boolean],
      start: Int,
      end: Int
  ): Array[Boolean] =
    copyOfRangeImpl(original, start, end)

  @inline
  private def copyOfRangeImpl[T: ClassTag](
      original: Array[T],
      start: Int,
      end: Int
  ): Array[T] = {
    if (start > end)
      throw new IllegalArgumentException("" + start + " > " + end)

    val retLength = end - start
    val copyLength = Math.min(retLength, original.length - start)
    val ret = new Array[T](retLength)
    System.arraycopy(original, start, ret, 0, copyLength)
    ret
  }

  @inline private def checkArrayLength(len: Int): Unit = {
    if (len < 0)
      throw new NegativeArraySizeException
  }

  @noinline def asList[T <: AnyRef](a: Array[T]): List[T] = {
    new AbstractList[T] with RandomAccess {
      def size(): Int =
        a.length

      def get(index: Int): T =
        a(index)

      override def set(index: Int, element: T): T = {
        val ret = a(index)
        a(index) = element
        ret
      }
    }
  }

  @noinline def hashCode(a: Array[Long]): Int =
    hashCodeImpl[Long](a, _.hashCode())

  @noinline def hashCode(a: Array[Int]): Int =
    hashCodeImpl[Int](a, _.hashCode())

  @noinline def hashCode(a: Array[Short]): Int =
    hashCodeImpl[Short](a, _.hashCode())

  @noinline def hashCode(a: Array[Char]): Int =
    hashCodeImpl[Char](a, _.hashCode())

  @noinline def hashCode(a: Array[Byte]): Int =
    hashCodeImpl[Byte](a, _.hashCode())

  @noinline def hashCode(a: Array[Boolean]): Int =
    hashCodeImpl[Boolean](a, _.hashCode())

  @noinline def hashCode(a: Array[Float]): Int =
    hashCodeImpl[Float](a, _.hashCode())

  @noinline def hashCode(a: Array[Double]): Int =
    hashCodeImpl[Double](a, _.hashCode())

  @noinline def hashCode(a: Array[AnyRef]): Int =
    hashCodeImpl[AnyRef](a, Objects.hashCode(_))

  @inline
  private def hashCodeImpl[T](a: Array[T], elementHashCode: T => Int): Int = {
    if (a == null) {
      0
    } else {
      var acc = 1
      for (i <- 0 until a.length)
        acc = 31 * acc + elementHashCode(a(i))
      acc
    }
  }

  @noinline def deepHashCode(a: Array[AnyRef]): Int = {
    @inline
    def getHash(elem: AnyRef): Int = {
      elem match {
        case elem: Array[AnyRef]  => deepHashCode(elem)
        case elem: Array[Long]    => hashCode(elem)
        case elem: Array[Int]     => hashCode(elem)
        case elem: Array[Short]   => hashCode(elem)
        case elem: Array[Char]    => hashCode(elem)
        case elem: Array[Byte]    => hashCode(elem)
        case elem: Array[Boolean] => hashCode(elem)
        case elem: Array[Float]   => hashCode(elem)
        case elem: Array[Double]  => hashCode(elem)
        case _                    => Objects.hashCode(elem)
      }
    }
    hashCodeImpl(a, getHash)
  }

  @noinline def deepEquals(a1: Array[AnyRef], a2: Array[AnyRef]): Boolean = {
    // scalastyle:off return
    if (a1 eq a2)
      return true
    if (a1 == null || a2 == null)
      return false
    val len = a1.length
    if (a2.length != len)
      return false
    var i = 0
    while (i != len) {
      if (!Objects.deepEquals(a1(i), a2(i)))
        return false
      i += 1
    }
    true
    // scalastyle:on return
  }

  @noinline def toString(a: Array[Long]): String =
    toStringImpl[Long](a)

  @noinline def toString(a: Array[Int]): String =
    toStringImpl[Int](a)

  @noinline def toString(a: Array[Short]): String =
    toStringImpl[Short](a)

  @noinline def toString(a: Array[Char]): String =
    toStringImpl[Char](a)

  @noinline def toString(a: Array[Byte]): String =
    toStringImpl[Byte](a)

  @noinline def toString(a: Array[Boolean]): String =
    toStringImpl[Boolean](a)

  @noinline def toString(a: Array[Float]): String =
    toStringImpl[Float](a)

  @noinline def toString(a: Array[Double]): String =
    toStringImpl[Double](a)

  @noinline def toString(a: Array[AnyRef]): String =
    toStringImpl[AnyRef](a)

  @inline
  private def toStringImpl[T](a: Array[T]): String = {
    if (a == null) {
      "null"
    } else {
      var result = "["
      val len = a.length
      var i = 0
      while (i != len) {
        if (i != 0)
          result += ", "
        result += a(i)
        i += 1
      }
      result + "]"
    }
  }

  @noinline def deepToString(a: Array[AnyRef]): String =
    deepToStringImpl(a, new java.util.HashSet[AsRef])

  private def deepToStringImpl(
      a: Array[AnyRef],
      branch: java.util.Set[AsRef]
  ): String = {
    @inline def valueToString(e: AnyRef): String = {
      if (e == null) "null"
      else {
        e match {
          case e: Array[AnyRef] =>
            branch.add(new AsRef(a))
            deepToStringImpl(e, branch)
          case e: Array[Long]    => toString(e)
          case e: Array[Int]     => toString(e)
          case e: Array[Short]   => toString(e)
          case e: Array[Byte]    => toString(e)
          case e: Array[Char]    => toString(e)
          case e: Array[Boolean] => toString(e)
          case e: Array[Float]   => toString(e)
          case e: Array[Double]  => toString(e)
          case _                 => String.valueOf(e)
        }
      }
    }
    if (a == null) "null"
    else if (branch.contains(new AsRef(a))) "[...]"
    else a.iterator.map(valueToString).mkString("[", ", ", "]")
  }

  @inline
  private def checkRangeIndices[@specialized T](
      a: Array[T],
      start: Int,
      end: Int
  ): Unit = {
    if (start > end)
      throw new IllegalArgumentException(
        "fromIndex(" + start + ") > toIndex(" + end + ")"
      )

    // bounds checks
    if (start < 0)
      a(start)

    if (end > 0)
      a(end - 1)
  }

  @inline
  private def toOrdering[T <: AnyRef](cmp: Comparator[_ >: T]): Ordering[T] = {
    if (cmp == null) {
      naturalOrdering[T]
    } else {
      new Ordering[T] {
        def compare(x: T, y: T): Int = cmp.compare(x, y)
      }
    }
  }

  private final class AsRef(val inner: AnyRef) {
    override def hashCode(): Int =
      System.identityHashCode(inner)

    override def equals(obj: Any): Boolean = {
      obj match {
        case obj: AsRef => obj.inner eq inner
        case _          => false
      }
    }
  }

// Scala Native additions --------------------------------------------------

  /* Note:
   *   For now all of parallelPrefix(), parallelSetAll() and parallelSort()
   *   methods are restricted to a parallelism of 1, i.e. sequential.
   *
   *   Later evolutions could/should increase the parallelism when
   *   multithreading has been enabled.
   */

  def parallelPrefix(array: Array[Double], op: DoubleBinaryOperator): Unit = {
    parallelPrefix(array, 0, array.length, op)
  }

  def parallelPrefix(
      array: Array[Double],
      fromIndex: Int,
      toIndex: Int,
      op: DoubleBinaryOperator
  ): Unit = {
    checkRangeIndices(array, fromIndex, toIndex)
    val rangeSize = toIndex - fromIndex

    if (rangeSize >= 2) { // rangeSize == 0 or 1 leaves array unmodified.
      for (j <- (fromIndex + 1) until toIndex) {
        array(j) = op.applyAsDouble(array(j - 1), array(j))
      }
    }
  }

  def parallelPrefix(array: Array[Int], op: IntBinaryOperator): Unit = {
    parallelPrefix(array, 0, array.length, op)
  }

  def parallelPrefix(
      array: Array[Int],
      fromIndex: Int,
      toIndex: Int,
      op: IntBinaryOperator
  ): Unit = {
    checkRangeIndices(array, fromIndex, toIndex)
    val rangeSize = toIndex - fromIndex

    if (rangeSize >= 2) { // rangeSize == 0 or 1 leaves array unmodified.
      for (j <- (fromIndex + 1) until toIndex) {
        array(j) = op.applyAsInt(array(j - 1), array(j))
      }
    }
  }

  def parallelPrefix(array: Array[Long], op: LongBinaryOperator): Unit = {
    parallelPrefix(array, 0, array.length, op)
  }

  def parallelPrefix(
      array: Array[Long],
      fromIndex: Int,
      toIndex: Int,
      op: LongBinaryOperator
  ): Unit = {
    checkRangeIndices(array, fromIndex, toIndex)
    val rangeSize = toIndex - fromIndex

    if (rangeSize >= 2) { // rangeSize == 0 or 1 leaves array unmodified.
      for (j <- (fromIndex + 1) until toIndex) {
        array(j) = op.applyAsLong(array(j - 1), array(j))
      }
    }
  }

  def parallelPrefix[T <: AnyRef](
      array: Array[T],
      op: BinaryOperator[T]
  ): Unit = {
    parallelPrefix[T](array, 0, array.length, op)
  }

  def parallelPrefix[T <: AnyRef](
      array: Array[T],
      fromIndex: Int,
      toIndex: Int,
      op: BinaryOperator[T]
  ): Unit = {
    checkRangeIndices(array, fromIndex, toIndex)
    val rangeSize = toIndex - fromIndex

    if (rangeSize >= 2) { // rangeSize == 0 or 1 leaves array unmodified.
      for (j <- (fromIndex + 1) until toIndex) {
        array(j) = op.apply(array(j - 1), array(j))
      }
    }
  }

  def parallelSetAll(
      array: Array[Double],
      generator: IntToDoubleFunction
  ): Unit = {
    setAll(array, generator)
  }

  def parallelSetAll(array: Array[Int], generator: IntUnaryOperator): Unit = {
    setAll(array, generator)
  }

  def parallelSetAll(array: Array[Long], generator: IntToLongFunction): Unit = {
    setAll(array, generator)
  }

  def parallelSetAll[T <: AnyRef](
      array: Array[T],
      generator: IntFunction[_ <: T]
  ): Unit = {
    setAll(array, generator)
  }

  def parallelSort(a: Array[Byte]): Unit =
    sort(a)

  def parallelSort(
      a: Array[Byte],
      fromIndex: Int,
      toIndex: Int
  ): Unit =
    sort(a, fromIndex, toIndex)

  def parallelSort(a: Array[Char]): Unit =
    sort(a)

  def parallelSort(
      a: Array[Char],
      fromIndex: Int,
      toIndex: Int
  ): Unit =
    sort(a, fromIndex, toIndex)

  def parallelSort(array: Array[Double]): Unit =
    sort(array)

  def parallelSort(
      array: Array[Double],
      fromIndex: Int,
      toIndex: Int
  ): Unit =
    sort(array, fromIndex, toIndex)

  def parallelSort(a: Array[Float]): Unit =
    sort(a)

  def parallelSort(
      a: Array[Float],
      fromIndex: Int,
      toIndex: Int
  ): Unit =
    sort(a, fromIndex, toIndex)

  def parallelSort(a: Array[Int]): Unit =
    sort(a)

  def parallelSort(a: Array[Int], fromIndex: Int, toIndex: Int): Unit =
    sort(a, fromIndex, toIndex)

  def parallelSort(a: Array[Long]): Unit =
    sort(a)

  def parallelSort(
      a: Array[Long],
      fromIndex: Int,
      toIndex: Int
  ): Unit =
    sort(a, fromIndex, toIndex)

  def parallelSort(a: Array[Short]): Unit =
    sort(a)

  def parallelSort(
      a: Array[Short],
      fromIndex: Int,
      toIndex: Int
  ): Unit =
    sort(a, fromIndex, toIndex)

  def parallelSort(a: Array[AnyRef]): Unit = sort(a)

  def parallelSort[T <: _Comparable[_ <: AnyRef]](
      array: Array[T]
  ): Unit = {
    sort(array.asInstanceOf[Array[AnyRef]])
  }

  def parallelSort[T <: AnyRef](
      array: Array[T],
      comparator: Comparator[_ >: T]
  ): Unit = {
    sort[T](array, comparator)
  }

  def parallelSort[T <: _Comparable[_ <: AnyRef]](
      array: Array[T],
      fromIndex: Int,
      toIndex: Int
  ): Unit =
    sort(array.asInstanceOf[Array[AnyRef]], fromIndex, toIndex)

  def parallelSort[T <: AnyRef](
      array: Array[T],
      fromIndex: Int,
      toIndex: Int,
      comparator: Comparator[_ >: T]
  ): Unit = {
    sort[T](array, fromIndex, toIndex, comparator)
  }

  def setAll(array: Array[Double], generator: IntToDoubleFunction): Unit = {
    for (j <- 0 until array.size)
      array(j) = generator.applyAsDouble(j)
  }

  def setAll(array: Array[Int], generator: IntUnaryOperator): Unit = {
    for (j <- 0 until array.size)
      array(j) = generator.applyAsInt(j)
  }

  def setAll(array: Array[Long], generator: IntToLongFunction): Unit = {
    for (j <- 0 until array.size)
      array(j) = generator.applyAsLong(j)
  }

  def setAll[T <: AnyRef](
      array: Array[T],
      generator: IntFunction[_ <: T]
  ): Unit = {
    for (j <- 0 until array.size)
      array(j) = generator.apply(j)
  }

  private final val standardArraySpliteratorCharacteristics =
    Spliterator.SIZED |
      Spliterator.SUBSIZED |
      Spliterator.ORDERED |
      Spliterator.IMMUTABLE

  def spliterator(array: Array[Double]): Spliterator.OfDouble = {
    Objects.requireNonNull(array)
    Spliterators.spliterator(
      array,
      0,
      array.size,
      standardArraySpliteratorCharacteristics
    )
  }

  def spliterator(
      array: Array[Double],
      startInclusive: Int,
      endExclusive: Int
  ): Spliterator.OfDouble = {
    Objects.requireNonNull(array)
    Spliterators.spliterator(
      array,
      startInclusive,
      endExclusive,
      standardArraySpliteratorCharacteristics
    )
  }

  def spliterator(array: Array[Int]): Spliterator.OfInt = {
    Objects.requireNonNull(array)
    Spliterators.spliterator(
      array,
      0,
      array.size,
      standardArraySpliteratorCharacteristics
    )
  }

  def spliterator(
      array: Array[Int],
      startInclusive: Int,
      endExclusive: Int
  ): Spliterator.OfInt = {
    Objects.requireNonNull(array)
    Spliterators.spliterator(
      array,
      startInclusive,
      endExclusive,
      standardArraySpliteratorCharacteristics
    )
  }

  def spliterator(array: Array[Long]): Spliterator.OfLong = {
    Objects.requireNonNull(array)
    Spliterators.spliterator(
      array,
      0,
      array.size,
      standardArraySpliteratorCharacteristics
    )
  }

  def spliterator(
      array: Array[Long],
      startInclusive: Int,
      endExclusive: Int
  ): Spliterator.OfLong = {
    Objects.requireNonNull(array)
    Spliterators.spliterator(
      array,
      startInclusive,
      endExclusive,
      standardArraySpliteratorCharacteristics
    )
  }

  def spliterator[T](array: Array[AnyRef]): Spliterator[T] = {
    Objects.requireNonNull(array)
    Spliterators.spliterator(
      array,
      0,
      array.size,
      standardArraySpliteratorCharacteristics
    )
  }

  def spliterator[T](
      array: Array[AnyRef],
      startInclusive: Int,
      endExclusive: Int
  ): Spliterator[T] = {
    Objects.requireNonNull(array)
    Spliterators.spliterator(
      array,
      startInclusive,
      endExclusive,
      standardArraySpliteratorCharacteristics
    )
  }

  def stream(array: Array[Double]): ju.stream.DoubleStream = {
    Objects.requireNonNull(array)

    val spliter = Arrays.spliterator(array)
    StreamSupport.doubleStream(spliter, parallel = false)
  }

  def stream(
      array: Array[Double],
      startInclusive: Int,
      endExclusive: Int
  ): ju.stream.DoubleStream = {
    Objects.requireNonNull(array)

    val spliter = Arrays.spliterator(array, startInclusive, endExclusive)
    StreamSupport.doubleStream(spliter, parallel = false)
  }

  def stream(array: Array[Int]): ju.stream.IntStream = {
    Objects.requireNonNull(array)

    val spliter = Arrays.spliterator(array)
    StreamSupport.intStream(spliter, parallel = false)
  }

  def stream(
      array: Array[Int],
      startInclusive: Int,
      endExclusive: Int
  ): ju.stream.IntStream = {
    Objects.requireNonNull(array)

    val spliter = Arrays.spliterator(array, startInclusive, endExclusive)
    StreamSupport.intStream(spliter, parallel = false)
  }

  def stream(array: Array[Long]): ju.stream.LongStream = {
    Objects.requireNonNull(array)

    val spliter = Arrays.spliterator(array)
    StreamSupport.longStream(spliter, parallel = false)
  }

  def stream(
      array: Array[Long],
      startInclusive: Int,
      endExclusive: Int
  ): ju.stream.LongStream = {
    Objects.requireNonNull(array)

    val spliter = Arrays.spliterator(array, startInclusive, endExclusive)
    StreamSupport.longStream(spliter, parallel = false)
  }

  def stream[T <: AnyRef](array: Array[T]): ju.stream.Stream[T] = {
    Objects.requireNonNull(array)

    val spliter = Arrays.spliterator[T](array.asInstanceOf[Array[AnyRef]])
    StreamSupport.stream(spliter, parallel = false)
  }

  def stream[T <: AnyRef](
      array: Array[T],
      startInclusive: Int,
      endExclusive: Int
  ): ju.stream.Stream[T] = {
    Objects.requireNonNull(array)

    val spliter = Arrays.spliterator[T](
      array.asInstanceOf[Array[AnyRef]],
      startInclusive,
      endExclusive
    )

    StreamSupport.stream(spliter, parallel = false)
  }

  // JDK 9 ----------------------------------------------------------------

  /* Portions of this code are manually generated from
   * ArraysOfAnyValOnJDK9.scala.gyb and then manually merged here.
   * Any edits here and not in the .gyb may be lost when snippets are
   * generated again.
   */

  /* Stray slightly from the strict JVM compatability here.
   * The content of the message is not documented, but in practice, on
   * JVM 23, it is a très helpful empty message "".
   * A useful message helps debug who is actually throwing NPEs & why.
   */

  private final val cmpIsNullMsg = "cmp must not be null"

  // Soft attempt at matching JVM message. Java 24 uses this.
  private def noArrayLengthMsg(stem: String): String = {
    // Many Scala 2.1[2-3].n version do not understand or allow "\"".
    s""""Cannot read the array length because \"${stem}\" is null"""
  }

  // Similar in concept to Objects.checkFromIndex() but different Exceptions.
  private def validateFromToIndex(
      fromIndex: Int,
      toIndex: Int,
      length: Int
  ): Int = {

    if (fromIndex > toIndex)
      throw new IllegalArgumentException(
        s"fromIndex($fromIndex) > toIndex($toIndex)"
      )

    if ((fromIndex < 0) || (fromIndex > toIndex) || (toIndex > length)) {
      throw new ArrayIndexOutOfBoundsException(
        s"Range [$fromIndex, $toIndex) out of bounds for length $length"
      )
    }

    fromIndex
  }

  private def compareNullsFirst[T <: Comparable[T]](o1: T, o2: T): Int = {
    // Scala 3 def compareNullsFirst[T <: Comparable[_ >: T]](o1: T, o2: T): Int
    /* The JDK 23 documentation for JDK 9 methods which do not directly
     * take a Comparator argment, such as compare(a, b) and
     * compare(a, Int, Int, b, Int, Int), describe the comparison as
     * """
     * |as if by:
     * |
     * |  Comparator.nullsFirst(Comparator.<T>naturalOrder()).
     * |        compare(a[i], b[i])
     * """
     *
     * Scala Native javalib provides each of those static methods
     * but getting the composition to work with the types used by
     * the compare() methods above proved more difficult than time &
     * skill allowed.
     *
     * There is probably at least one Scala idiomatic way to implement this
     * method. For now, this implementation suits the need and has the
     * benefit of existing.
     *
     * NB: The naturalOrder() at top of this file does not handle nulls, first,
     *     last, or anywhere.
     */
    if (o1 == null && o2 == null) 0
    else if (o1 == null) -1
    else if (o2 == null) 1
    else
      o1.compareTo(o2)
  }

  /* Objects.compare(a, b, cmp) requires a lot of complexity to provide
   * the specified or implied JDK 9 behavior where this method is used.
   */
  private def objectsCompareZeroOrMinus1[T <: AnyRef](a: T, b: T): Int =
    if (Objects.equals(a, b)) 0 else -1

  /* Validate args here, in one place, rather than usual practice of in caller.
   * Pass cmp by name so that Integer.compare() & such get inlined.
   * The specific required validation steps vary by number of arguments.
   */

  private def compareImpl[T](
      a: Array[T],
      b: Array[T],
      cmp: => ju.function.BiFunction[T, T, Int]
  ): Int = {
    // JVM checks cmp first, before checking for null Array args.
    Objects.requireNonNull(cmp, cmpIsNullMsg)

    if (a == null) {
      if (b == null) 0 else -1
    } else if (b == null) {
      1
    } else {
      compareImplCore[T](a, 0, a.length, b, 0, b.length, cmp)
    }
  }

  private def compareImpl[T](
      a: Array[T],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[T],
      bFromIndex: Int,
      bToIndex: Int,
      cmp: => ju.function.BiFunction[T, T, Int]
  ): Int = {
    // JVM checks cmp first, before checking for null Array args.
    Objects.requireNonNull(cmp, cmpIsNullMsg)

    Objects.requireNonNull(a, noArrayLengthMsg("a"))
    Arrays.validateFromToIndex(aFromIndex, aToIndex, a.length)

    Objects.requireNonNull(a, noArrayLengthMsg("b"))
    Arrays.validateFromToIndex(bFromIndex, bToIndex, b.length)

    compareImplCore[T](
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      cmp
    )
  }

  // By construction & contract, caller has validated arguments
  private def compareImplCore[T](
      a: Array[T],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[T],
      bFromIndex: Int,
      bToIndex: Int,
      cmp: => ju.function.BiFunction[T, T, Int]
  ): Int = {
    val i =
      Arrays.mismatchImpl(a, aFromIndex, aToIndex, b, bFromIndex, bToIndex, cmp)

    if ((i >= 0) && (i < Math.min(
          aToIndex - aFromIndex,
          bToIndex - bFromIndex
        ))) {
      cmp(a(aFromIndex + i), b(bFromIndex + i))
    } else {
      (aToIndex - aFromIndex) - (bToIndex - bFromIndex)
    }
  }

  private def equalsImpl[T](
      a: Array[T],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[T],
      bFromIndex: Int,
      bToIndex: Int,
      stride: Int
  ): Boolean = {
    Objects.requireNonNull(a)
    Arrays.validateFromToIndex(aFromIndex, aToIndex, a.length)

    Objects.requireNonNull(b)
    Arrays.validateFromToIndex(bFromIndex, bToIndex, b.length)

    val aCount = aToIndex - aFromIndex
    val bCount = bToIndex - bFromIndex

    if (aCount != bCount) false
    else if (aCount == 0) true
    else {
      val memcmpCount = aCount * stride // in Bytes
      libc.string.memcmp(
        a.at(aFromIndex),
        b.at(bFromIndex),
        memcmpCount.toCSize
      ) == 0
    }
  }

  @alwaysinline
  private def mismatchImpl[T](
      a: Array[T],
      b: Array[T],
      cmp: => ju.function.BiFunction[T, T, Int]
  ): Int = {
    Objects.requireNonNull(a, noArrayLengthMsg("a"))
    Objects.requireNonNull(a, noArrayLengthMsg("b"))

    mismatchImplCore(a, 0, a.length, b, 0, b.length, cmp)
  }

  @alwaysinline
  private def mismatchImpl[T](
      a: Array[T],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[T],
      bFromIndex: Int,
      bToIndex: Int,
      cmp: => ju.function.BiFunction[T, T, Int]
  ): Int = {
    Objects.requireNonNull(a, noArrayLengthMsg("a"))
    Arrays.validateFromToIndex(aFromIndex, aToIndex, a.length)

    Objects.requireNonNull(a, noArrayLengthMsg("b"))
    Arrays.validateFromToIndex(bFromIndex, bToIndex, b.length)

    mismatchImplCore(a, aFromIndex, aToIndex, b, bFromIndex, bToIndex, cmp)
  }

  @alwaysinline
  private def mismatchImplCore[T](
      a: Array[T],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[T],
      bFromIndex: Int,
      bToIndex: Int,
      cmp: => ju.function.BiFunction[T, T, Int]
  ): Int = {
    val aRangeLen = aToIndex - aFromIndex
    val bRangeLen = bToIndex - bFromIndex
    val matchLen = Math.min(aRangeLen, bRangeLen)

    val abOffset = bFromIndex - aFromIndex

    var j = aFromIndex // relative to first Array argument
    var mismatchedAt = -1

    while ((j < (aFromIndex + matchLen)) && (mismatchedAt < 0)) {
      if (cmp(a(j), b(j + abOffset)) == 0) j += 1
      else mismatchedAt = j - aFromIndex
    }

    if (mismatchedAt > -1) mismatchedAt
    else if (aRangeLen == bRangeLen) -1
    else Math.min(aRangeLen, bRangeLen)
  }

  /** @since JDK 9 */
  def compare(a: Array[scala.Boolean], b: Array[scala.Boolean]): Int =
    compareImpl(a, b, jl.Boolean.compare)

  /** @since JDK 9 */
  def compare(
      a: Array[scala.Boolean],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Boolean],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    compareImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Boolean.compare
    )

  def equals(
      a: Array[scala.Boolean],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Boolean],
      bFromIndex: Int,
      bToIndex: Int
  ): scala.Boolean = {
    Objects.requireNonNull(a)
    Arrays.validateFromToIndex(aFromIndex, aToIndex, a.length)

    Objects.requireNonNull(b)
    Arrays.validateFromToIndex(bFromIndex, bToIndex, b.length)

    val aCount = aToIndex - aFromIndex
    val bCount = bToIndex - bFromIndex

    if (aCount != bCount) false
    else if (aCount == 0) true
    else {
      /* Devos: Performance trade-off here.
       * Call mismatch() rather than inline mismatchImplCore() to reduce
       * code size. The latter, which is not small, would get inlined
       * three time; once each for Boolean, Double, & Float.
       *
       * Take the hit of arguments being checked twice.
       *
       * See if experience & measurement show that this is a critical
       * section and should be inlined.
       */
      Arrays.mismatch(
        a,
        aFromIndex,
        aToIndex,
        b,
        bFromIndex,
        bToIndex
      ) == -1
    }
  }

  /** @since JDK 9 */
  @noinline
  def mismatch(a: Array[scala.Boolean], b: Array[scala.Boolean]): Int =
    mismatchImpl(a, b, jl.Boolean.compare)

  /** @since JDK 9 */
  @noinline
  def mismatch(
      a: Array[scala.Boolean],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Boolean],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    mismatchImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Boolean.compare
    )

  /** @since JDK 9 */
  def compare(a: Array[scala.Byte], b: Array[scala.Byte]): Int =
    compareImpl(a, b, jl.Byte.compare)

  /** @since JDK 9 */
  def compare(
      a: Array[scala.Byte],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Byte],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    compareImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Byte.compare
    )

  /** @since JDK 9 */
  def compareUnsigned(a: Array[scala.Byte], b: Array[scala.Byte]): Int =
    compareImpl(a, b, jl.Byte.compareUnsigned)

  /** @since JDK 9 */
  def compareUnsigned(
      a: Array[scala.Byte],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Byte],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    compareImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Byte.compareUnsigned
    )

  def equals(
      a: Array[scala.Byte],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Byte],
      bFromIndex: Int,
      bToIndex: Int
  ): scala.Boolean = {
    equalsImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Byte.BYTES
    )
  }

  /** @since JDK 9 */
  @noinline
  def mismatch(a: Array[scala.Byte], b: Array[scala.Byte]): Int =
    mismatchImpl(a, b, jl.Byte.compare)

  /** @since JDK 9 */
  @noinline
  def mismatch(
      a: Array[scala.Byte],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Byte],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    mismatchImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Byte.compare
    )

  /** @since JDK 9 */
  def compare(a: Array[scala.Char], b: Array[scala.Char]): Int =
    compareImpl(a, b, jl.Character.compare)

  /** @since JDK 9 */
  def compare(
      a: Array[scala.Char],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Char],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    compareImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Character.compare
    )

  def equals(
      a: Array[scala.Char],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Char],
      bFromIndex: Int,
      bToIndex: Int
  ): scala.Boolean = {
    equalsImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Character.BYTES
    )
  }

  /** @since JDK 9 */
  @noinline
  def mismatch(a: Array[scala.Char], b: Array[scala.Char]): Int =
    mismatchImpl(a, b, jl.Character.compare)

  /** @since JDK 9 */
  @noinline
  def mismatch(
      a: Array[scala.Char],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Char],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    mismatchImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Character.compare
    )

  /** @since JDK 9 */
  def compare(a: Array[scala.Double], b: Array[scala.Double]): Int =
    compareImpl(a, b, jl.Double.compare)

  /** @since JDK 9 */
  def compare(
      a: Array[scala.Double],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Double],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    compareImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Double.compare
    )

  def equals(
      a: Array[scala.Double],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Double],
      bFromIndex: Int,
      bToIndex: Int
  ): scala.Boolean = {
    Objects.requireNonNull(a)
    Arrays.validateFromToIndex(aFromIndex, aToIndex, a.length)

    Objects.requireNonNull(b)
    Arrays.validateFromToIndex(bFromIndex, bToIndex, b.length)

    val aCount = aToIndex - aFromIndex
    val bCount = bToIndex - bFromIndex

    if (aCount != bCount) false
    else if (aCount == 0) true
    else {
      /* Devos: Performance trade-off here.
       * Call mismatch() rather than inline mismatchImplCore() to reduce
       * code size. The latter, which is not small, would get inlined
       * three time; once each for Boolean, Double, & Float.
       *
       * Take the hit of arguments being checked twice.
       *
       * See if experience & measurement show that this is a critical
       * section and should be inlined.
       */
      Arrays.mismatch(
        a,
        aFromIndex,
        aToIndex,
        b,
        bFromIndex,
        bToIndex
      ) == -1
    }
  }

  /** @since JDK 9 */
  @noinline
  def mismatch(a: Array[scala.Double], b: Array[scala.Double]): Int =
    mismatchImpl(a, b, jl.Double.compare)

  /** @since JDK 9 */
  @noinline
  def mismatch(
      a: Array[scala.Double],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Double],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    mismatchImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Double.compare
    )

  /** @since JDK 9 */
  def compare(a: Array[scala.Float], b: Array[scala.Float]): Int =
    compareImpl(a, b, jl.Float.compare)

  /** @since JDK 9 */
  def compare(
      a: Array[scala.Float],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Float],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    compareImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Float.compare
    )

  def equals(
      a: Array[scala.Float],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Float],
      bFromIndex: Int,
      bToIndex: Int
  ): scala.Boolean = {
    Objects.requireNonNull(a)
    Arrays.validateFromToIndex(aFromIndex, aToIndex, a.length)

    Objects.requireNonNull(b)
    Arrays.validateFromToIndex(bFromIndex, bToIndex, b.length)

    val aCount = aToIndex - aFromIndex
    val bCount = bToIndex - bFromIndex

    if (aCount != bCount) false
    else if (aCount == 0) true
    else {
      /* Devos: Performance trade-off here.
       * Call mismatch() rather than inline mismatchImplCore() to reduce
       * code size. The latter, which is not small, would get inlined
       * three time; once each for Boolean, Double, & Float.
       *
       * Take the hit of arguments being checked twice.
       *
       * See if experience & measurement show that this is a critical
       * section and should be inlined.
       */
      Arrays.mismatch(
        a,
        aFromIndex,
        aToIndex,
        b,
        bFromIndex,
        bToIndex
      ) == -1
    }
  }

  /** @since JDK 9 */
  @noinline
  def mismatch(a: Array[scala.Float], b: Array[scala.Float]): Int =
    mismatchImpl(a, b, jl.Float.compare)

  /** @since JDK 9 */
  @noinline
  def mismatch(
      a: Array[scala.Float],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Float],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    mismatchImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Float.compare
    )

  /** @since JDK 9 */
  def compare(a: Array[scala.Int], b: Array[scala.Int]): Int =
    compareImpl(a, b, jl.Integer.compare)

  /** @since JDK 9 */
  def compare(
      a: Array[scala.Int],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Int],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    compareImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Integer.compare
    )

  /** @since JDK 9 */
  def compareUnsigned(a: Array[scala.Int], b: Array[scala.Int]): Int =
    compareImpl(a, b, jl.Integer.compareUnsigned)

  /** @since JDK 9 */
  def compareUnsigned(
      a: Array[scala.Int],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Int],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    compareImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Integer.compareUnsigned
    )

  def equals(
      a: Array[scala.Int],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Int],
      bFromIndex: Int,
      bToIndex: Int
  ): scala.Boolean = {
    equalsImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Integer.BYTES
    )
  }

  /** @since JDK 9 */
  @noinline
  def mismatch(a: Array[scala.Int], b: Array[scala.Int]): Int =
    mismatchImpl(a, b, jl.Integer.compare)

  /** @since JDK 9 */
  @noinline
  def mismatch(
      a: Array[scala.Int],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Int],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    mismatchImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Integer.compare
    )

  /** @since JDK 9 */
  def compare(a: Array[scala.Long], b: Array[scala.Long]): Int =
    compareImpl(a, b, jl.Long.compare)

  /** @since JDK 9 */
  def compare(
      a: Array[scala.Long],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Long],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    compareImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Long.compare
    )

  /** @since JDK 9 */
  def compareUnsigned(a: Array[scala.Long], b: Array[scala.Long]): Int =
    compareImpl(a, b, jl.Long.compareUnsigned)

  /** @since JDK 9 */
  def compareUnsigned(
      a: Array[scala.Long],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Long],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    compareImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Long.compareUnsigned
    )

  def equals(
      a: Array[scala.Long],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Long],
      bFromIndex: Int,
      bToIndex: Int
  ): scala.Boolean = {
    equalsImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Long.BYTES
    )
  }

  /** @since JDK 9 */
  @noinline
  def mismatch(a: Array[scala.Long], b: Array[scala.Long]): Int =
    mismatchImpl(a, b, jl.Long.compare)

  /** @since JDK 9 */
  @noinline
  def mismatch(
      a: Array[scala.Long],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Long],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    mismatchImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Long.compare
    )

  /** @since JDK 9 */
  def compare(a: Array[scala.Short], b: Array[scala.Short]): Int =
    compareImpl(a, b, jl.Short.compare)

  /** @since JDK 9 */
  def compare(
      a: Array[scala.Short],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Short],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    compareImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Short.compare
    )

  /** @since JDK 9 */
  def compareUnsigned(a: Array[scala.Short], b: Array[scala.Short]): Int =
    compareImpl(a, b, jl.Short.compareUnsigned)

  /** @since JDK 9 */
  def compareUnsigned(
      a: Array[scala.Short],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Short],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    compareImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Short.compareUnsigned
    )

  def equals(
      a: Array[scala.Short],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Short],
      bFromIndex: Int,
      bToIndex: Int
  ): scala.Boolean = {
    equalsImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Short.BYTES
    )
  }

  /** @since JDK 9 */
  @noinline
  def mismatch(a: Array[scala.Short], b: Array[scala.Short]): Int =
    mismatchImpl(a, b, jl.Short.compare)

  /** @since JDK 9 */
  @noinline
  def mismatch(
      a: Array[scala.Short],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[scala.Short],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    mismatchImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      jl.Short.compare
    )

  /** @since JDK 9 */
  def compare[T <: Comparable[T]]( // Scala 2.12, 2.13
      a: Array[Comparable[_]], // Scala 3 Array[T]
      b: Array[Comparable[_]] // Scala 3 Array[T]
  ): Int = {
    val cmp = Arrays.compareNullsFirst[T] _ // see comments in method
    compareImpl(
      a,
      b,
      (a: Comparable[_], b: Comparable[_]) =>
        cmp(a.asInstanceOf[T], b.asInstanceOf[T])
    )
  }

  /** @since JDK 9 */
  def compare[T <: AnyRef](
      a: Array[T],
      b: Array[T],
      cmp: Comparator[_ >: T]
  ): Int = {
    Objects.requireNonNull(cmp, "cmp")

    compareImpl[AnyRef](
      a.asInstanceOf[Array[AnyRef]],
      b.asInstanceOf[Array[AnyRef]],
      (a: AnyRef, b: AnyRef) =>
        cmp.compare(a.asInstanceOf[T], b.asInstanceOf[T])
    )
  }

  /** @since JDK 9 */
  def compare[T <: Comparable[T]](
      a: Array[Comparable[_]],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[Comparable[_]],
      bFromIndex: Int,
      bToIndex: Int
  ): Int = {
    val cmp = Arrays.compareNullsFirst[T] _ // see comments in method

    compareImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      (a: Comparable[_], b: Comparable[_]) =>
        cmp(a.asInstanceOf[T], b.asInstanceOf[T])
    )
  }

  /** @since JDK 9 */
  def compare[T <: AnyRef](
      a: Array[T],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[T],
      bFromIndex: Int,
      bToIndex: Int,
      cmp: Comparator[_ >: T]
  ): Int = {
    Objects.requireNonNull(cmp, "cmp")

    compareImpl(
      a.asInstanceOf[Array[Any]],
      aFromIndex,
      aToIndex,
      b.asInstanceOf[Array[Any]],
      bFromIndex,
      bToIndex,
      (a: Any, b: Any) => cmp.compare(a.asInstanceOf[T], b.asInstanceOf[T])
    )
  }

  /** @since JDK 9 */
  def equals[T <: AnyRef](
      a: Array[T],
      b: Array[T],
      cmp: Comparator[_ >: T]
  ): scala.Boolean = {
    // compareImpl(a, b, cmp.compare) == 0 // Scala 3
    compareImpl[AnyRef](
      a.asInstanceOf[Array[AnyRef]],
      b.asInstanceOf[Array[AnyRef]],
      (a: AnyRef, b: AnyRef) =>
        cmp.compare(a.asInstanceOf[T], b.asInstanceOf[T])
    ) == 0
  }

  /** @since JDK 9 */
  def equals[T <: AnyRef](
      a: Array[T],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[T],
      bFromIndex: Int,
      bToIndex: Int
  ): scala.Boolean = {
    compareImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      // objectsCompareZeroOrMinus1 // Scala 3
      (a: T, b: T) => objectsCompareZeroOrMinus1(a, b)
    ) == 0
  }

  /** @since JDK 9 */
  def equals[T <: AnyRef](
      a: Array[T],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[T],
      bFromIndex: Int,
      bToIndex: Int,
      cmp: Comparator[_ >: T]
  ): scala.Boolean = {
    compareImpl(
      a.asInstanceOf[Array[Any]],
      aFromIndex,
      aToIndex,
      b.asInstanceOf[Array[Any]],
      bFromIndex,
      bToIndex,
      (a: Any, b: Any) => cmp.compare(a.asInstanceOf[T], b.asInstanceOf[T])
    ) == 0
  }

  /** @since JDK 9 */
  def mismatch(a: Array[Object], b: Array[Object]): Int =
    mismatchImpl(
      a,
      0,
      a.length,
      b,
      0,
      b.length,
      // objectsCompareZeroOrMinus1 // Scala 3
      (a: Object, b: Object) => objectsCompareZeroOrMinus1(a, b)
    )

  /** @since JDK 9 */
  def mismatch[T <: Object](
      a: Array[T],
      b: Array[T],
      cmp: Comparator[_ >: T]
  ): Int =
    mismatchImpl(
      a.asInstanceOf[Array[Any]],
      b.asInstanceOf[Array[Any]],
      (a: Any, b: Any) => cmp.compare(a.asInstanceOf[T], b.asInstanceOf[T])
    )

  /** @since JDK 9 */
  def mismatch[T <: Object](
      a: Array[T],
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[T],
      bFromIndex: Int,
      bToIndex: Int
  ): Int =
    mismatchImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      // objectsCompareZeroOrMinus1 // Scala 3
      (a: T, b: T) => objectsCompareZeroOrMinus1(a, b)
    )

  /** @since JDK 9 */
  def mismatch[T <: Object](
      a: Array[AnyRef], // Scala 3: a: Array[T]
      aFromIndex: Int,
      aToIndex: Int,
      b: Array[AnyRef], // Scala 3: b: Array[T]
      bFromIndex: Int,
      bToIndex: Int,
      cmp: Comparator[_ >: T]
  ): Int = {
    Objects.requireNonNull(cmp, "cmp")

    mismatchImpl(
      a,
      aFromIndex,
      aToIndex,
      b,
      bFromIndex,
      bToIndex,
      (a: AnyRef, b: AnyRef) =>
        cmp.compare(a.asInstanceOf[T], b.asInstanceOf[T])
    )
  }

}
