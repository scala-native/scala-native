package org.scalanative.testsuite.javalib.util

import java.util.{Arrays, Comparator, Objects}
import java.{lang => jl}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/* These Tests are written so that they compile on Scala 2.12.latest,
 * 3.13.latest, and Scala 3.
 *
 * Tests often provide a good example of how to call and use a method in
 * other code.
 *
 * In many cases, code examples intended strictly for Scala 3 can be more
 * idiomatic if the type parameter is omitted; e.g mismatch() not
 * mismatch[T]().
 */

class ArraysOfObjectTestOnJDK9 {

  private class Datum(var field_1: Int, var field_2: Int)
      extends Comparable[Datum] {
    /* field_1 used for ascending, shifted up by 1 to avoid indistinguishable 0
     * field_2 used for descending, shifted down by 1 to avoid 0
     */

    def canEqual(a: Any) = a.isInstanceOf[Datum]

    override def equals(that: Any): scala.Boolean = {
      /* Library code under test would usually return false for null.
       * Detect and report it here as a null means a bug, either in this
       * test code or the code under test.
       */

      that match {
        case d: Datum => {
          Objects.requireNonNull(d)
          if (this.eq(d)) true
          else
            d.canEqual(this) &&
              ((this.field_1 == d.field_1) && (this.field_2 == d.field_2))
        }

        case _ => false
      }
    }

    override def hashCode(): Int =
      Integer.hashCode(field_1)

    def compareTo(that: Datum): Int =
      if (that == null) 1 // nulls always less than a realized instance.
      else {
        Integer.compare(this.field_1, that.field_1)
      }
  }

  private val comparatorOfDatumField_1 =
    new Comparator[AnyRef] {
      def compare(o1: AnyRef, o2: AnyRef) = {
        val d1 = o1.asInstanceOf[Datum] // will throw if not right type
        val d2 = o2.asInstanceOf[Datum]

        if ((d1 == null) && (d2 == null)) 0
        else if (d1 == null) -1
        else if (d2 == null) 1
        else
          d1.field_1 - d2.field_1
      }

      def equals(o1: AnyRef, o2: AnyRef) =
        compare(o1, o2) == 0
    }

  private val comparatorOfDatumField_2 =
    new Comparator[AnyRef] {
      def compare(o1: AnyRef, o2: AnyRef) = {
        val d1 = o1.asInstanceOf[Datum] // will throw if not right type
        val d2 = o2.asInstanceOf[Datum]

        if ((d1 == null) && (d2 == null)) 0
        else if (d1 == null) -1
        else if (d2 == null) 1
        else
          d1.field_2 - d2.field_2
      }

      def equals(o1: AnyRef, o2: AnyRef) =
        compare(o1, o2) == 0
    }

// compare JDK 9

  @Test def compare_Object_2Arg(): Unit = {
    val srcSize = 32

    val arrA = new Array[Datum](srcSize)
    val arrB = new Array[Datum](srcSize)

    for (idx <- 0 until srcSize) {
      arrA(idx) = new Datum(idx + 1, -(idx + 1))
      arrB(idx) = new Datum(idx + 1, -(idx + 2))
    }

    // By construction arrA and arrB are not reference equal.
    assertEquals(
      s"element content equality: lenA == LenB, A == B",
      0,
      Arrays.compare[Datum](arrA, arrB)
    )

    val changeBAt = srcSize - 1
    arrB(changeBAt).field_1 = 1021

    assertTrue(
      s"element content equality: lenA == LenB, A < B",
      Arrays.compare[Datum](arrA, arrB) < 0
    )

    arrB(changeBAt).field_1 = jl.Integer.MIN_VALUE

    assertTrue(
      s"element content equality: lenA == LenB, A > B",
      Arrays.compare[Datum](arrA, arrB) > 0
    )

    assertTrue(
      s"element content equality: lenA < LenB, A == B",
      Arrays.compare[Datum](
        Arrays.copyOfRange[Datum](arrA, 0, changeBAt - 1),
        arrB
      ) < 0
    )

    assertTrue(
      s"element content equality: lenA < LenB, A == B",
      Arrays.compare[Datum](
        arrA,
        Arrays.copyOfRange[Datum](arrA, 0, changeBAt - 2)
      ) > 0
    )
  }

  @Test def compare_Object_3Arg(): Unit = {
    val srcSize = 32

    val arrA = new Array[Datum](srcSize)
    val copyOfA = new Array[Datum](srcSize)
    val arrB = new Array[Datum](srcSize)

    for (idx <- 0 until srcSize) {
      arrA(idx) = new Datum(idx + 1, -(idx + 1))
      copyOfA(idx) = new Datum(idx + 1, -(idx + 1))
      arrB(idx) = new Datum(idx + 1, -(idx + 2))
    }

    assertThrows(
      "null comparator arg",
      classOf[NullPointerException],
      Arrays.compare(arrA, arrB, null)
    )

    // By construction arrA and copyOfA are not reference equal.
    assertEquals(
      s"element content equality: lenA == LenB, A == B",
      0,
      Arrays.compare[Datum](arrA, copyOfA, comparatorOfDatumField_2)
    )

    assertTrue(
      s"element content equality: lenA == LenB, A > B",
      Arrays.compare[Datum](arrA, arrB, comparatorOfDatumField_2) > 0
    )

    val changeBAt = srcSize - 1
    arrB(changeBAt).field_2 = Integer.MAX_VALUE

    assertTrue(
      s"element content equality: lenA == LenB, A < B",
      Arrays.compare[Datum](arrA, arrB, comparatorOfDatumField_2) > 0
    )

    assertTrue(
      s"element content equality: lenA < LenB, A == B",
      Arrays.compare[Datum](
        Arrays.copyOfRange[Datum](copyOfA, 0, changeBAt - 1),
        copyOfA,
        comparatorOfDatumField_2
      ) < 0
    )

    assertTrue(
      s"element content equality: lenA > LenB, A == B",
      Arrays.compare[Datum](
        copyOfA,
        Arrays.copyOfRange[Datum](copyOfA, 0, changeBAt - 2),
        comparatorOfDatumField_2
      ) > 0
    )
  }

  @Test def compare_Object_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[Datum](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = new Datum(idx + 1, -(idx + 1))

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[Datum](srcSize)
    for (idx <- t2FromIdx until srcSize) { // fill extra length to entice bugs
      val offsetIdx = idx - t1Shift
      arrB(idx) = new Datum(offsetIdx + 1, -(offsetIdx + 1))
    }

    val copyOfRangeA = Arrays.copyOfRange[Datum](arrA, t1FromIdx, t1ToIdx)
    val copyOfRangeB = Arrays.copyOfRange[Datum](arrB, t2FromIdx, t2ToIdx)

    assertEquals(
      "copyOfRange lengths #1",
      copyOfRangeA.length,
      copyOfRangeB.length
    )

    for (j <- 0 until copyOfRangeA.length) {
      assertEquals(
        s"copyOfRange contents at j: ${j}",
        0,
        copyOfRangeA(j).compareTo(copyOfRangeB(j))
      )
    }

    assertEquals(
      s"copyOfRange mismatch",
      -1,
      Arrays.mismatch(
        copyOfRangeA.asInstanceOf[Array[Object]],
        copyOfRangeB.asInstanceOf[Array[Object]]
      )
    )

    // same ranges do not match
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t1FromIdx}, ${t1ToIdx})",
      Arrays.compare[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t1FromIdx,
        t1ToIdx
      ) != 0
    )

    // different ranges match
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.compare[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t2FromIdx,
        t2ToIdx
      ) == 0
    )

    val expectedMismatchAtAIdx = 2
    val changeBAt = t2FromIdx + expectedMismatchAtAIdx
    arrB(changeBAt).field_1 = 6

    // ranges which used to  no longer do when field_1 in one has changed.
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx}), field_1",
      Arrays.compare[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t2FromIdx,
        t2ToIdx
      ) > 0
    )

    val expectedShortage = 3
    val t3ToIdx = t1ToIdx - expectedShortage
    val expectedShortMismatchAtIdx = t3ToIdx - t1FromIdx

    assertTrue(
      "common prefix but a.length < b.length",
      Arrays.compare[Datum](
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx
      ) < 0
    )

    assertTrue(
      "common prefix but a.length > b.length",
      Arrays.compare[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx
      ) > 0
    )
  }

  @Test def compare_Object_7Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[Datum](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = new Datum(idx + 1, -(idx + 1))

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[Datum](srcSize)
    for (idx <- t2FromIdx until srcSize) { // fill extra length to entice bugs
      val offsetIdx = idx - t1Shift
      arrB(idx) = new Datum(offsetIdx + 1, -(offsetIdx + 1))
    }

    val copyOfRangeA = Arrays.copyOfRange[Datum](arrA, t1FromIdx, t1ToIdx)
    val copyOfRangeB = Arrays.copyOfRange[Datum](arrB, t2FromIdx, t2ToIdx)

    assertEquals(
      "copyOfRange lengths #1",
      copyOfRangeA.length,
      copyOfRangeB.length
    )

    for (j <- 0 until copyOfRangeA.length) {
      assertEquals(
        s"copyOfRange contents at j: ${j}",
        0,
        copyOfRangeA(j).compareTo(copyOfRangeB(j))
      )
    }

    assertEquals(
      s"copyOfRange mismatch",
      -1,
      Arrays.mismatch(
        copyOfRangeA.asInstanceOf[Array[Object]],
        copyOfRangeB.asInstanceOf[Array[Object]]
      )
    )

    assertThrows(
      "null comparator arg",
      classOf[NullPointerException],
      Arrays.compare[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t1FromIdx,
        t1ToIdx,
        null
      ) == 0
    )

    // same ranges do not match
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t1FromIdx}, ${t1ToIdx})",
      Arrays.compare[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t1FromIdx,
        t1ToIdx,
        comparatorOfDatumField_1
      ) != 0
    )

    // different ranges match
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.compare[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t2FromIdx,
        t2ToIdx,
        comparatorOfDatumField_1
      ) == 0
    )

    val expectedMismatchAtAIdx = 2
    val changeBAt = t2FromIdx + expectedMismatchAtAIdx
    arrB(changeBAt).field_1 = 6

    // ranges which used to  no longer do when field_1 in one has changed.
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx}), field_1",
      Arrays.compare[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t2FromIdx,
        t2ToIdx,
        comparatorOfDatumField_1
      ) > 0
    )

    /* Test that changing Comparators changes the meaning or "sense"
     * of compare().
     */

    // ranges which no longer match on field_1 still match on field_2
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx}, field_2)",
      Arrays.compare[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t2FromIdx,
        t2ToIdx,
        comparatorOfDatumField_2
      ) == 0
    )

    val expectedMismatchAtAIdxF2 = 7
    val changeBAtF2 = t2FromIdx + expectedMismatchAtAIdxF2
    arrB(changeBAtF2).field_2 = 76

    // ranges which used to match on field_2 no longer do after element changed
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx}, field_2)",
      Arrays.compare[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t2FromIdx,
        t2ToIdx,
        comparatorOfDatumField_2
      ) < 0
    )

    val expectedShortage = 3
    val t3ToIdx = t1ToIdx - expectedShortage
    val expectedShortMismatchAtIdx = t3ToIdx - t1FromIdx

    assertTrue(
      "common prefix but a.length < b.length",
      Arrays.compare[Datum](
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx,
        comparatorOfDatumField_2
      ) < 0
    )

    assertTrue(
      "common prefix but a.length > b.length",
      Arrays.compare[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx,
        comparatorOfDatumField_2
      ) > 0
    )
  }

// equals JDK 9

  @Test def equals_Object_3Arg(): Unit = {
    val srcSize = 32

    val arrA = new Array[Datum](srcSize)
    val arrB = new Array[Datum](srcSize)

    for (idx <- 0 until srcSize) {
      arrA(idx) = new Datum(idx + 1, -(idx + 1))
      arrB(idx) = new Datum(idx + 1, -(idx + 2))
    }

    assertThrows(
      "null comparator arg",
      classOf[NullPointerException],
      Arrays.equals[Datum](arrA, arrB, null)
    )

    assertTrue(
      s"null == null",
      Arrays.equals[Datum](null, null, comparatorOfDatumField_2)
    )

    assertFalse(
      s"null == b",
      Arrays.equals[Datum](null, arrB, comparatorOfDatumField_2)
    )

    assertFalse(
      s"a == null",
      Arrays.equals[Datum](arrA, null, comparatorOfDatumField_2)
    )

    // equals on field_1
    assertTrue(
      s"element content equality: lenA == LenB, field_1",
      Arrays.equals[Datum](arrA, arrB, comparatorOfDatumField_1)
    )

    // but not equals on field_2
    assertFalse(
      s"elements content equality: lenA == LenB, field_2",
      Arrays.equals[Datum](arrA, arrB, comparatorOfDatumField_2)
    )
  }

  @Test def equals_Object_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[Datum](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = new Datum(idx + 1, -(idx + 1))

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[Datum](srcSize)
    for (idx <- t2FromIdx until srcSize) { // fill extra length to entice bugs
      val offsetIdx = idx - t1Shift
      arrB(idx) = new Datum(offsetIdx + 1, -(offsetIdx + 1))
    }

    val copyOfRangeA = Arrays.copyOfRange[Datum](arrA, t1FromIdx, t1ToIdx)
    val copyOfRangeB = Arrays.copyOfRange[Datum](arrB, t2FromIdx, t2ToIdx)

    assertEquals(
      "copyOfRange lengths #1",
      copyOfRangeA.length,
      copyOfRangeB.length
    )

    for (j <- 0 until copyOfRangeA.length) {
      assertEquals(
        s"copyOfRange contents at j: ${j}",
        0,
        copyOfRangeA(j).compareTo(copyOfRangeB(j))
      )
    }

    assertEquals(
      s"copyOfRange mismatch",
      -1,
      Arrays.mismatch(
        copyOfRangeA.asInstanceOf[Array[Object]],
        copyOfRangeB.asInstanceOf[Array[Object]]
      )
    )

    // same ranges do not match
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t1FromIdx}, ${t1ToIdx})",
      Arrays.equals(
        arrA.asInstanceOf[Array[Object]],
        t1FromIdx,
        t1ToIdx,
        arrB.asInstanceOf[Array[Object]],
        t1FromIdx,
        t1ToIdx
      )
    )

    // different ranges match
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.equals(
        arrA.asInstanceOf[Array[Object]],
        t1FromIdx,
        t1ToIdx,
        arrB.asInstanceOf[Array[Object]],
        t2FromIdx,
        t2ToIdx
      )
    )

    val expectedMismatchAtAIdx = 2
    val changeBAt = t2FromIdx + expectedMismatchAtAIdx
    arrB(changeBAt).field_1 = 6

    // ranges which used to equal longer do when field_1 in one has changed.
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx}), field_1",
      Arrays.equals(
        arrA.asInstanceOf[Array[Object]],
        t1FromIdx,
        t1ToIdx,
        arrB.asInstanceOf[Array[Object]],
        t2FromIdx,
        t2ToIdx
      )
    )

    val expectedShortage = 3
    val t3ToIdx = t1ToIdx - expectedShortage
    val expectedShortMismatchAtIdx = t3ToIdx - t1FromIdx

    assertFalse(
      "common prefix but a.length < b.length",
      Arrays.equals(
        arrA.asInstanceOf[Array[Object]],
        t1FromIdx,
        t3ToIdx,
        arrA.asInstanceOf[Array[Object]],
        t1FromIdx,
        t1ToIdx
      )
    )

    assertFalse(
      "common prefix but a.length > b.length",
      Arrays.equals(
        arrA.asInstanceOf[Array[Object]],
        t1FromIdx,
        t1ToIdx,
        arrA.asInstanceOf[Array[Object]],
        t1FromIdx,
        t3ToIdx
      )
    )
  }

  @Test def equals_Object_7Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[Datum](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = new Datum(idx + 1, -(idx + 1))

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[Datum](srcSize)
    for (idx <- t2FromIdx until srcSize) { // fill extra length to entice bugs
      val offsetIdx = idx - t1Shift
      arrB(idx) = new Datum(offsetIdx + 1, -(offsetIdx + 1))
    }

    val copyOfRangeA = Arrays.copyOfRange[Datum](arrA, t1FromIdx, t1ToIdx)
    val copyOfRangeB = Arrays.copyOfRange[Datum](arrB, t2FromIdx, t2ToIdx)

    assertEquals(
      "copyOfRange lengths #1",
      copyOfRangeA.length,
      copyOfRangeB.length
    )

    for (j <- 0 until copyOfRangeA.length) {
      assertEquals(
        s"copyOfRange contents at j: ${j}",
        0,
        copyOfRangeA(j).compareTo(copyOfRangeB(j))
      )
    }

    assertEquals(
      s"copyOfRange mismatch",
      -1,
      Arrays.mismatch(
        copyOfRangeA.asInstanceOf[Array[Object]],
        copyOfRangeB.asInstanceOf[Array[Object]]
      )
    )

    assertThrows(
      "null comparator arg",
      classOf[NullPointerException],
      Arrays.equals[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t1FromIdx,
        t1ToIdx,
        null
      )
    )

    // same ranges do not match
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t1FromIdx}, ${t1ToIdx})",
      Arrays.equals[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t1FromIdx,
        t1ToIdx,
        comparatorOfDatumField_1
      )
    )

    // different ranges match
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      Arrays.equals[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t2FromIdx,
        t2ToIdx,
        comparatorOfDatumField_1
      )
    )

    val expectedMismatchAtAIdx = 2
    val changeBAt = t2FromIdx + expectedMismatchAtAIdx
    arrB(changeBAt).field_1 = 6

    // ranges which used to  no longer do when field_1 in one has changed.
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx}), field_1",
      Arrays.equals[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t2FromIdx,
        t2ToIdx,
        comparatorOfDatumField_1
      )
    )

    /* Test that changing Comparators changes the meaning or "sense"
     * of equal().
     */

    // ranges which no longer match on field_1 still match on field_2
    assertTrue(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx}, field_2)",
      Arrays.equals[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t2FromIdx,
        t2ToIdx,
        comparatorOfDatumField_2
      )
    )

    val expectedMismatchAtAIdxF2 = 7
    val changeBAtF2 = t2FromIdx + expectedMismatchAtAIdxF2
    arrB(changeBAtF2).field_2 = 76

    // ranges which used to match on field_2 no longer do after element changed
    assertFalse(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx}, field_2)",
      Arrays.equals[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t2FromIdx,
        t2ToIdx,
        comparatorOfDatumField_2
      )
    )

    val expectedShortage = 3
    val t3ToIdx = t1ToIdx - expectedShortage
    val expectedShortMismatchAtIdx = t3ToIdx - t1FromIdx

    assertFalse(
      "common prefix but a.length < b.length",
      Arrays.equals[Datum](
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx,
        comparatorOfDatumField_2
      )
    )

    assertFalse(
      "common prefix but a.length > b.length",
      Arrays.equals[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx,
        comparatorOfDatumField_2
      )
    )
  }

// mismatch JDK 9

  @Test def mismatch_Object_2Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[Datum](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = new Datum(idx + 1, -(idx + 1))

    val copyOfA = new Array[Datum](srcSize)
    for (idx <- 0 until srcSize)
      copyOfA(idx) = new Datum(arrA(idx).field_1, arrA(idx).field_2)

    // ensure two arrays are content equal but not reference eq.
    val arrB = new Array[Datum](srcSize)
    for (idx <- 0 until srcSize) {
      arrB(idx) = new Datum(arrA(idx).field_1, arrA(idx).field_2)
      assertFalse(s"a(${idx})!eq b(${idx})", arrB.eq(arrA))
    }

    assertEquals(
      s"mismatch(A, copyOfA)",
      -1,
      Arrays.mismatch(
        arrA.asInstanceOf[Array[Object]],
        copyOfA.asInstanceOf[Array[Object]]
      )
    )

    val expectMismatchAtAIdx = 10
    arrB(expectMismatchAtAIdx).field_1 = Integer.MIN_VALUE

    assertEquals(
      s"mismatch(A, B)",
      expectMismatchAtAIdx,
      Arrays.mismatch(
        arrA.asInstanceOf[Array[Object]],
        arrB.asInstanceOf[Array[Object]]
      )
    )

    val truncatedSrcSize = srcSize - 10
    // Possible reference eq is OK here
    val sliceOfA = Arrays.copyOfRange[Datum](arrA, 0, truncatedSrcSize)

    assertEquals(
      s"mismatch length, A < sliceOfA",
      truncatedSrcSize,
      Arrays.mismatch(
        sliceOfA.asInstanceOf[Array[Object]],
        arrA.asInstanceOf[Array[Object]]
      )
    )

    assertEquals(
      s"mismatch length, A > sliceOfA",
      truncatedSrcSize,
      Arrays.mismatch(
        arrA.asInstanceOf[Array[Object]],
        sliceOfA.asInstanceOf[Array[Object]]
      )
    )
  }

  @Test def mismatch_Object_3Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[Datum](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = new Datum(idx + 1, -(idx + 1))

    val copyOfA = new Array[Datum](srcSize)
    for (idx <- 0 until srcSize)
      copyOfA(idx) = new Datum(arrA(idx).field_1, arrA(idx).field_2)

    // ensure two arrays are content equal but not reference eq.
    val arrB = new Array[Datum](srcSize)
    for (idx <- 0 until srcSize) {
      arrB(idx) = new Datum(arrA(idx).field_1, arrA(idx).field_2)
      assertFalse(s"a(${idx})!eq b(${idx})", arrB.eq(arrA))
    }

    assertThrows(
      "null comparator arg",
      classOf[NullPointerException],
      Arrays.mismatch[Datum](
        arrA,
        copyOfA,
        null
      )
    )

    assertEquals(
      s"mismatch(A, copyOfA)",
      -1,
      Arrays.mismatch[Datum](
        arrA,
        copyOfA,
        comparatorOfDatumField_2
      )
    )

    val expectMismatchAtAIdx = 10
    arrB(expectMismatchAtAIdx).field_2 = Integer.MIN_VALUE

    assertEquals(
      s"mismatch(A, B)",
      expectMismatchAtAIdx,
      Arrays.mismatch[Datum](
        arrA,
        arrB,
        comparatorOfDatumField_2
      )
    )

    val truncatedSrcSize = srcSize - 10
    // Possible reference eq is OK here
    val sliceOfA = Arrays.copyOfRange[Datum](arrA, 0, truncatedSrcSize)

    assertEquals(
      s"mismatch length, A < sliceOfA",
      truncatedSrcSize,
      Arrays.mismatch[Datum](
        sliceOfA,
        arrA,
        comparatorOfDatumField_2
      )
    )

    assertEquals(
      s"mismatch length, A > sliceOfA",
      truncatedSrcSize,
      Arrays.mismatch[Datum](
        arrA,
        sliceOfA,
        comparatorOfDatumField_2
      )
    )
  }

  @Test def mismatch_Object_6Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[Datum](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = new Datum(idx + 1, -(idx + 1))

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[Datum](srcSize)
    for (idx <- t2FromIdx until srcSize) { // fill extra length to entice bugs
      val offsetIdx = idx - t1Shift
      arrB(idx) = new Datum(offsetIdx + 1, -(offsetIdx + 1))
    }

    val copyOfRangeA = Arrays.copyOfRange[Datum](arrA, t1FromIdx, t1ToIdx)
    val copyOfRangeB = Arrays.copyOfRange[Datum](arrB, t2FromIdx, t2ToIdx)

    assertEquals(
      "copyOfRange lengths #1",
      copyOfRangeA.length,
      copyOfRangeB.length
    )

    for (j <- 0 until copyOfRangeA.length) {
      assertEquals(
        s"copyOfRange contents at j: ${j}",
        0,
        copyOfRangeA(j).compareTo(copyOfRangeB(j))
      )
    }

    assertEquals(
      s"copyOfRange mismatch",
      -1,
      Arrays.mismatch(
        copyOfRangeA.asInstanceOf[Array[Object]],
        copyOfRangeB.asInstanceOf[Array[Object]]
      )
    )

    // same ranges do not match
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      0,
      Arrays.mismatch(
        arrA.asInstanceOf[Array[Object]],
        t1FromIdx,
        t1ToIdx,
        arrB.asInstanceOf[Array[Object]],
        t1FromIdx,
        t1ToIdx
      )
    )

    // different ranges match
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      -1, // No mismatch found
      Arrays.mismatch(
        arrA.asInstanceOf[Array[Object]],
        t1FromIdx,
        t1ToIdx,
        arrB.asInstanceOf[Array[Object]],
        t2FromIdx,
        t2ToIdx
      )
    )

    val expectedMismatchAtAIdx = 2
    val changeBAt = t2FromIdx + expectedMismatchAtAIdx
    arrB(changeBAt).field_1 = 6

    // ranges which used to match no longer do when field_1 in one has changed.
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx}), field_1",
      expectedMismatchAtAIdx,
      Arrays.mismatch(
        arrA.asInstanceOf[Array[Object]],
        t1FromIdx,
        t1ToIdx,
        arrB.asInstanceOf[Array[Object]],
        t2FromIdx,
        t2ToIdx
      )
    )

    val expectedShortage = 3
    val t3ToIdx = t1ToIdx - expectedShortage
    val expectedShortMismatchAtIdx = t3ToIdx - t1FromIdx

    assertEquals(
      "common prefix but a.length < b.length",
      expectedShortMismatchAtIdx,
      Arrays.mismatch(
        arrA.asInstanceOf[Array[Object]],
        t1FromIdx,
        t3ToIdx,
        arrA.asInstanceOf[Array[Object]],
        t1FromIdx,
        t1ToIdx
      )
    )

    assertEquals(
      "common prefix but a.length > b.length",
      expectedShortMismatchAtIdx,
      Arrays.mismatch(
        arrA.asInstanceOf[Array[Object]],
        t1FromIdx,
        t1ToIdx,
        arrA.asInstanceOf[Array[Object]],
        t1FromIdx,
        t3ToIdx
      )
    )
  }

  @Test def mismatch_Object_7Arg(): Unit = {
    val srcSize = 64

    val arrA = new Array[Datum](srcSize)
    for (idx <- 0 until srcSize)
      arrA(idx) = new Datum(idx + 1, -(idx + 1))

    val t1Shift = 20

    val t1FromIdx = 10
    val t1ToIdx = 20

    val t2FromIdx = t1FromIdx + t1Shift
    val t2ToIdx = t1ToIdx + t1Shift

    val arrB = new Array[Datum](srcSize)
    for (idx <- t2FromIdx until srcSize) { // fill extra length to entice bugs
      val offsetIdx = idx - t1Shift
      arrB(idx) = new Datum(offsetIdx + 1, -(offsetIdx + 1))
    }

    val copyOfRangeA = Arrays.copyOfRange[Datum](arrA, t1FromIdx, t1ToIdx)
    val copyOfRangeB = Arrays.copyOfRange[Datum](arrB, t2FromIdx, t2ToIdx)

    assertEquals(
      "copyOfRange lengths #1",
      copyOfRangeA.length,
      copyOfRangeB.length
    )

    for (j <- 0 until copyOfRangeA.length) {
      assertEquals(
        s"copyOfRange contents at j: ${j}",
        0,
        copyOfRangeA(j).compareTo(copyOfRangeB(j))
      )
    }

    assertEquals(
      s"copyOfRange mismatch",
      -1,
      Arrays.mismatch(
        copyOfRangeA.asInstanceOf[Array[Object]],
        copyOfRangeB.asInstanceOf[Array[Object]]
      )
    )

    assertThrows(
      "null comparator arg",
      classOf[NullPointerException],
      Arrays.mismatch[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t1FromIdx,
        t1ToIdx,
        null
      )
    )

    // same ranges do not match
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t1FromIdx}, ${t1ToIdx})",
      0,
      Arrays.mismatch[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t1FromIdx,
        t1ToIdx,
        comparatorOfDatumField_1
      )
    )

    // different ranges match
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) == b[${t2FromIdx}, ${t2ToIdx})",
      -1, // No mismatch found
      Arrays.mismatch[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t2FromIdx,
        t2ToIdx,
        comparatorOfDatumField_1
      )
    )

    val expectedMismatchAtAIdx = 2
    val changeBAt = t2FromIdx + expectedMismatchAtAIdx
    arrB(changeBAt).field_1 = 6

    // ranges which used to match no longer do when field_1 in one has changed.
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx})",
      expectedMismatchAtAIdx,
      Arrays.mismatch[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t2FromIdx,
        t2ToIdx,
        comparatorOfDatumField_1
      )
    )

    /* Show that changing Comparators changes if & where mismatch is found.
     */

    // ranges which no longer match on field_1 still match on field_2
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx})",
      -1, // No mismatch found
      Arrays.mismatch[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t2FromIdx,
        t2ToIdx,
        comparatorOfDatumField_2
      )
    )

    val expectedMismatchAtAIdxF2 = 7
    val changeBAtF2 = t2FromIdx + expectedMismatchAtAIdxF2
    arrB(changeBAtF2).field_2 = 76

    // ranges which used to match on field_2 no longer do after element changed
    assertEquals(
      s"a[${t1FromIdx}, ${t1ToIdx}) != b[${t2FromIdx}, ${t2ToIdx}, field_2)",
      expectedMismatchAtAIdxF2,
      Arrays.mismatch[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrB,
        t2FromIdx,
        t2ToIdx,
        comparatorOfDatumField_2
      )
    )

    val expectedShortage = 3
    val t3ToIdx = t1ToIdx - expectedShortage
    val expectedShortMismatchAtIdx = t3ToIdx - t1FromIdx

    assertEquals(
      "common prefix but a.length < b.length",
      expectedShortMismatchAtIdx,
      Arrays.mismatch[Datum](
        arrA,
        t1FromIdx,
        t3ToIdx,
        arrA,
        t1FromIdx,
        t1ToIdx,
        comparatorOfDatumField_2
      )
    )

    assertEquals(
      "common prefix but a.length > b.length",
      expectedShortMismatchAtIdx,
      Arrays.mismatch[Datum](
        arrA,
        t1FromIdx,
        t1ToIdx,
        arrA,
        t1FromIdx,
        t3ToIdx,
        comparatorOfDatumField_2
      )
    )
  }

}
