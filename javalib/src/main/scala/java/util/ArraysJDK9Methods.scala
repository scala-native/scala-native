package java.util

/* This code is generated from ArraysJDK9Methods.scala.gyb.
 * Any edits here and not in the .gyb will be lost when this file is next
 * generated.
 */

// format: off

import scala.scalanative.annotation.alwaysinline

import scala.scalanative.libc

import scala.scalanative.unsafe.UnsafeRichArray
import scala.scalanative.unsafe.Size.intToSize

import java.{lang => jl}
import java.{util => ju}

private[util] trait ArraysJDK9Methods { self: Arrays.type =>

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
    validateFromToIndex(aFromIndex, aToIndex, a.length)

    Objects.requireNonNull(a, noArrayLengthMsg("b"))
    validateFromToIndex(bFromIndex, bToIndex, b.length)

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
    val i = mismatchImpl(a, aFromIndex, aToIndex, b, bFromIndex, bToIndex, cmp)

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
    validateFromToIndex(aFromIndex, aToIndex, a.length)

    Objects.requireNonNull(b)
    validateFromToIndex(bFromIndex, bToIndex, b.length)

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
    validateFromToIndex(aFromIndex, aToIndex, a.length)

    Objects.requireNonNull(a, noArrayLengthMsg("b"))
    validateFromToIndex(bFromIndex, bToIndex, b.length)

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
    validateFromToIndex(aFromIndex, aToIndex, a.length)

    Objects.requireNonNull(b)
    validateFromToIndex(bFromIndex, bToIndex, b.length)

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
    validateFromToIndex(aFromIndex, aToIndex, a.length)

    Objects.requireNonNull(b)
    validateFromToIndex(bFromIndex, bToIndex, b.length)

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
    validateFromToIndex(aFromIndex, aToIndex, a.length)

    Objects.requireNonNull(b)
    validateFromToIndex(bFromIndex, bToIndex, b.length)

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
    val cmp = compareNullsFirst[T] _ // see comments in method
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
    val cmp = compareNullsFirst[T] _ // see comments in method

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
