package scala

import org.junit.Test
import org.junit.Assert.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class DivisionByZeroTest {
  @noinline def byte1 = 1.toByte
  @noinline def char1 = 1.toChar
  @noinline def short1 = 1.toShort
  @noinline def int1 = 1
  @noinline def long1 = 1L
  @noinline def byte0 = 0.toByte
  @noinline def char0 = 0.toChar
  @noinline def short0 = 0.toShort
  @noinline def int0 = 0
  @noinline def long0 = 0L

  @Test def byteDivideZero(): Unit = {
    assertThrows(classOf[ArithmeticException], byte1 / byte0)
    assertThrows(classOf[ArithmeticException], byte1 / short0)
    assertThrows(classOf[ArithmeticException], byte1 / char0)
    assertThrows(classOf[ArithmeticException], byte1 / int0)
    assertThrows(classOf[ArithmeticException], byte1 / long0)
  }

  @Test def byteRemainderZero(): Unit = {
    assertThrows(classOf[ArithmeticException], byte1 / byte0)
    assertThrows(classOf[ArithmeticException], byte1 / short0)
    assertThrows(classOf[ArithmeticException], byte1 / char0)
    assertThrows(classOf[ArithmeticException], byte1 / int0)
    assertThrows(classOf[ArithmeticException], byte1 / long0)
  }

  @Test def shortDivideZero(): Unit = {
    assertThrows(classOf[ArithmeticException], short1 / byte0)
    assertThrows(classOf[ArithmeticException], short1 / short0)
    assertThrows(classOf[ArithmeticException], short1 / char0)
    assertThrows(classOf[ArithmeticException], short1 / int0)
    assertThrows(classOf[ArithmeticException], short1 / long0)
  }

  @Test def shortRemainderZero(): Unit = {
    assertThrows(classOf[ArithmeticException], short1 / byte0)
    assertThrows(classOf[ArithmeticException], short1 / short0)
    assertThrows(classOf[ArithmeticException], short1 / char0)
    assertThrows(classOf[ArithmeticException], short1 / int0)
    assertThrows(classOf[ArithmeticException], short1 / long0)
  }

  @Test def charDivideZero(): Unit = {
    assertThrows(classOf[ArithmeticException], char1 / byte0)
    assertThrows(classOf[ArithmeticException], char1 / short0)
    assertThrows(classOf[ArithmeticException], char1 / char0)
    assertThrows(classOf[ArithmeticException], char1 / int0)
    assertThrows(classOf[ArithmeticException], char1 / long0)
  }

  @Test def charRemainderZero(): Unit = {
    assertThrows(classOf[ArithmeticException], char1 / byte0)
    assertThrows(classOf[ArithmeticException], char1 / short0)
    assertThrows(classOf[ArithmeticException], char1 / char0)
    assertThrows(classOf[ArithmeticException], char1 / int0)
    assertThrows(classOf[ArithmeticException], char1 / long0)
  }

  @Test def intDivideZero(): Unit = {
    assertThrows(classOf[ArithmeticException], int1 / byte0)
    assertThrows(classOf[ArithmeticException], int1 / short0)
    assertThrows(classOf[ArithmeticException], int1 / char0)
    assertThrows(classOf[ArithmeticException], int1 / int0)
    assertThrows(classOf[ArithmeticException], int1 / long0)
  }

  @Test def intRemainderZero(): Unit = {
    assertThrows(classOf[ArithmeticException], int1 / byte0)
    assertThrows(classOf[ArithmeticException], int1 / short0)
    assertThrows(classOf[ArithmeticException], int1 / char0)
    assertThrows(classOf[ArithmeticException], int1 / int0)
    assertThrows(classOf[ArithmeticException], int1 / long0)
  }

  @Test def longDivideZero(): Unit = {
    assertThrows(classOf[ArithmeticException], long1 / byte0)
    assertThrows(classOf[ArithmeticException], long1 / short0)
    assertThrows(classOf[ArithmeticException], long1 / char0)
    assertThrows(classOf[ArithmeticException], long1 / int0)
    assertThrows(classOf[ArithmeticException], long1 / long0)
  }

  @Test def longRemainderZero(): Unit = {
    assertThrows(classOf[ArithmeticException], long1 / byte0)
    assertThrows(classOf[ArithmeticException], long1 / short0)
    assertThrows(classOf[ArithmeticException], long1 / char0)
    assertThrows(classOf[ArithmeticException], long1 / int0)
    assertThrows(classOf[ArithmeticException], long1 / long0)
  }
}
