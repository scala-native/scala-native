package scala

import scala.collection.immutable.ArraySeq
import scala.language.implicitConversions

import org.junit.Assert._
import org.junit.Test

// Collection Compat Issues
class Issue2025 {

  // from ArraySeqTest.t6727_and_t6440_and_8627
  @Test def lazyListUnit(): Unit = {
    assertTrue(
      LazyList.continually(()).filter(_ => true).take(2) == Seq((), ())
    )
    assertTrue(
      LazyList.continually(()).filterNot(_ => false).take(2) == Seq((), ())
    )
  }

  // from LazyListTest.slice
  @Test def implicitFailsAtRuntime(): Unit = {
    // fails at runtime
    implicit def array2ArraySeq[T](array: Array[T]): ArraySeq[T] =
      ArraySeq.unsafeWrapArray(array)

    def unit1(): Unit = {}
    def unit2(): Unit = {}
    assertEquals("Units are equal", unit1(), unit2())
    // unitArray is actually an instance of Immutable[BoxedUnit], the check to which is actually checked slice
    // implementation of ofRef
    val unitArray: ArraySeq[Unit] = Array(unit1(), unit2(), unit1(), unit2())
    check(unitArray, Array(unit1(), unit1()), Array(unit1(), unit1()))

  }

  private def check[T](
      array: ArraySeq[T],
      expectedSliceResult1: ArraySeq[T],
      expectedSliceResult2: ArraySeq[T]
  ): Unit = {
    assertEquals(array, array.slice(-1, 4))
    assertEquals(array, array.slice(0, 5))
    assertEquals(array, array.slice(-1, 5))
    assertEquals(expectedSliceResult1, array.slice(0, 2))
    assertEquals(expectedSliceResult2, array.slice(1, 3))
    assertEquals(ArraySeq.empty[Nothing], array.slice(1, 1))
    assertEquals(ArraySeq.empty[Nothing], array.slice(2, 1))
  }

}
