package scala

import org.junit.Test
import org.junit.Assert.*

class PrimitiveTest {
  val char = 1.toChar
  val byte = 1.toByte
  val short = 1.toShort
  val int = 1
  val long = 1L
  val float = 1f
  val double = 1d

  val negint = -1
  val neglong = -1L
  val negfloat = -1f
  val negdouble = -1d

  val notint = -2
  val notlong = -2

  @Test def negativeX(): Unit = {
    assertTrue(-char == negint)
    assertTrue(-byte == negint)
    assertTrue(-short == negint)
    assertTrue(-int == negint)
    assertTrue(-long == neglong)
    assertTrue(-float == negfloat)
    assertTrue(-double == negdouble)
  }

  @Test def complementX(): Unit = {
    assertTrue(~char == notint)
    assertTrue(~byte == notint)
    assertTrue(~short == notint)
    assertTrue(~int == notint)
    assertTrue(~long == notlong)
  }

  @Test def positiveX(): Unit = {
    assertTrue(+char == int)
    assertTrue(+byte == int)
    assertTrue(+short == int)
    assertTrue(+int == int)
    assertTrue(+long == long)
    assertTrue(+float == float)
    assertTrue(+double == double)
  }

  @deprecated @Test def xShiftLeftY(): Unit = {
    val x: Int = 3
    val y: Long = 33
    assertTrue((x << y) == 6)
  }

  @Test def xRemainderYDoesNotOverflowNoinline(): Unit = {
    assertTrue(intRemByNegOne(4) == 0)
    assertTrue(intRemByNegOne(Int.MinValue) == 0)
    assertTrue(longRemByNegOne(4) == 0L)
    assertTrue(longRemByNegOne(Long.MinValue) == 0L)

    // prevent partial evaluation
    @noinline
    def intRemByNegOne(i: Int): Int = {
      i % -1
    }

    // prevent partial evaluation
    @noinline
    def longRemByNegOne(l: Long): Long = {
      l % -1L
    }
  }

  @Test def xRemainderYDoesNotOverflowInline(): Unit = {
    assertTrue(intRemByNegOne(4) == 0)
    assertTrue(intRemByNegOne(Int.MinValue) == 0)
    assertTrue(longRemByNegOne(4) == 0L)
    assertTrue(longRemByNegOne(Long.MinValue) == 0L)

    // facilitate partial evaluation
    @inline
    def intRemByNegOne(i: Int): Int = {
      i % -1
    }

    // facilitate partial evaluation
    @inline
    def longRemByNegOne(l: Long): Long = {
      l % -1L
    }
  }

}
