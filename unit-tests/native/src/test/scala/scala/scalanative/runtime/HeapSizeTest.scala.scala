package scala.scalanative.runtime
import org.junit.Test
import org.junit.Before
import org.junit.Assert._
import scala.scalanative.unsafe.CSize
import scalanative.unsigned.{ULong, UnsignedRichInt}

class HeapSizeTest {

  @Before
  val conversionFactor = (1024 * 1024 * 1024).toULong
  val lowerBound: ULong = 0.toULong
  val higherBound: ULong = 32.toULong * conversionFactor

  @Test def checkInitHeapSize(): Unit = {
    val initHeapSz = GC.getInitHeapSize()
    assertTrue(
      s"0 <= ${initHeapSz / conversionFactor}GB < 32GB",
      initHeapSz >= lowerBound && initHeapSz < higherBound
    )
  }

  @Test def checkMaxHeapSize(): Unit = {
    val maxHeapSize = GC.getMaxHeapSize()
    assertTrue(
      s"0 < ${maxHeapSize / conversionFactor}GB <= 32GB",
      maxHeapSize > lowerBound && maxHeapSize <= higherBound
    )
  }

}
