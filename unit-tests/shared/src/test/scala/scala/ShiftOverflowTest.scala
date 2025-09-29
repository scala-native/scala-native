package scala

import org.junit.Assert._
import org.junit.Test

@deprecated class ShiftOverflowTest {
  @noinline def noinlineByte42: Byte = 42.toByte
  @noinline def noinlineShort42: Short = 42.toShort
  @noinline def noinlineChar42: Char = 42.toChar
  @noinline def noinlineInt42: Int = 42
  @noinline def noinlineLong42: Long = 42L
  @noinline def noinlineInt33: Int = 33
  @noinline def noinlineLong65: Long = 65L
  @noinline def noinlineInt21: Int = 21
  @noinline def noinlineLong21: Long = 21L
  @noinline def noinlineInt84: Int = 84
  @noinline def noinlineLong84: Long = 84L

  @inline def inlineByte42: Byte = 42.toByte
  @inline def inlineShort42: Short = 42.toShort
  @inline def inlineChar42: Char = 42.toChar
  @inline def inlineInt42: Int = 42
  @inline def inlineLong42: Long = 42L
  @inline def inlineInt33: Int = 33
  @inline def inlineLong65: Long = 65L
  @inline def inlineInt21: Int = 21
  @inline def inlineLong21: Long = 21L
  @inline def inlineInt84: Int = 84
  @inline def inlineLong84: Long = 84L

  @Test def testShiftLeft33Noinline(): Unit = {
    assertTrue((noinlineByte42 << noinlineInt33) == noinlineInt84)
    assertTrue((noinlineShort42 << noinlineInt33) == noinlineInt84)
    assertTrue((noinlineChar42 << noinlineInt33) == noinlineInt84)
    assertTrue((noinlineInt42 << noinlineInt33) == noinlineInt84)
  }

  @Test def testShiftLeft33Inline(): Unit = {
    assertTrue((inlineByte42 << inlineInt33) == inlineInt84)
    assertTrue((inlineShort42 << inlineInt33) == inlineInt84)
    assertTrue((inlineChar42 << inlineInt33) == inlineInt84)
    assertTrue((inlineInt42 << inlineInt33) == inlineInt84)
  }

  @Test def testShiftLeft65LNoinline(): Unit = {
    assertTrue((noinlineByte42 << noinlineLong65) == noinlineLong84)
    assertTrue((noinlineShort42 << noinlineLong65) == noinlineLong84)
    assertTrue((noinlineChar42 << noinlineLong65) == noinlineLong84)
    assertTrue((noinlineInt42 << noinlineLong65) == noinlineLong84)
    assertTrue((noinlineLong42 << noinlineLong65) == noinlineLong84)
  }

  @Test def testShiftLeft65LInline(): Unit = {
    assertTrue((inlineByte42 << inlineLong65) == inlineLong84)
    assertTrue((inlineShort42 << inlineLong65) == inlineLong84)
    assertTrue((inlineChar42 << inlineLong65) == inlineLong84)
    assertTrue((inlineInt42 << inlineLong65) == inlineLong84)
    assertTrue((inlineLong42 << inlineLong65) == inlineLong84)
  }

  @Test def testShiftRight33Noinline(): Unit = {
    assertTrue((noinlineByte42 >> noinlineInt33) == noinlineInt21)
    assertTrue((noinlineShort42 >> noinlineInt33) == noinlineInt21)
    assertTrue((noinlineChar42 >> noinlineInt33) == noinlineInt21)
    assertTrue((noinlineInt42 >> noinlineInt33) == noinlineInt21)
  }

  @Test def testShiftRight33Inline(): Unit = {
    assertTrue((inlineByte42 >> inlineInt33) == inlineInt21)
    assertTrue((inlineShort42 >> inlineInt33) == inlineInt21)
    assertTrue((inlineChar42 >> inlineInt33) == inlineInt21)
    assertTrue((inlineInt42 >> inlineInt33) == inlineInt21)
  }

  @Test def testShiftRight65LNoinline(): Unit = {
    assertTrue((noinlineByte42 >> noinlineLong65) == noinlineLong21)
    assertTrue((noinlineShort42 >> noinlineLong65) == noinlineLong21)
    assertTrue((noinlineChar42 >> noinlineLong65) == noinlineLong21)
    assertTrue((noinlineInt42 >> noinlineLong65) == noinlineLong21)
    assertTrue((noinlineLong42 >> noinlineLong65) == noinlineLong21)
  }

  @Test def testShiftRight65LInline(): Unit = {
    assertTrue((inlineByte42 >> inlineLong65) == inlineLong21)
    assertTrue((inlineShort42 >> inlineLong65) == inlineLong21)
    assertTrue((inlineChar42 >> inlineLong65) == inlineLong21)
    assertTrue((inlineInt42 >> inlineLong65) == inlineLong21)
    assertTrue((inlineLong42 >> inlineLong65) == inlineLong21)
  }

  @Test def testShiftRightFill33Noinline(): Unit = {
    assertTrue((noinlineByte42 >>> noinlineInt33) == noinlineInt21)
    assertTrue((noinlineShort42 >>> noinlineInt33) == noinlineInt21)
    assertTrue((noinlineChar42 >>> noinlineInt33) == noinlineInt21)
    assertTrue((noinlineInt42 >>> noinlineInt33) == noinlineInt21)
  }

  @Test def testShiftRightFill33Inline(): Unit = {
    assertTrue((inlineByte42 >>> inlineInt33) == inlineInt21)
    assertTrue((inlineShort42 >>> inlineInt33) == inlineInt21)
    assertTrue((inlineChar42 >>> inlineInt33) == inlineInt21)
    assertTrue((inlineInt42 >>> inlineInt33) == inlineInt21)
  }

  @Test def testShiftRightFill65LNoinline(): Unit = {
    assertTrue((noinlineByte42 >>> noinlineLong65) == noinlineLong21)
    assertTrue((noinlineShort42 >>> noinlineLong65) == noinlineLong21)
    assertTrue((noinlineChar42 >>> noinlineLong65) == noinlineLong21)
    assertTrue((noinlineInt42 >>> noinlineLong65) == noinlineLong21)
    assertTrue((noinlineLong42 >>> noinlineLong65) == noinlineLong21)
  }

  @Test def testShiftRightFill65LInline(): Unit = {
    assertTrue((inlineByte42 >>> inlineLong65) == inlineLong21)
    assertTrue((inlineShort42 >>> inlineLong65) == inlineLong21)
    assertTrue((inlineChar42 >>> inlineLong65) == inlineLong21)
    assertTrue((inlineInt42 >>> inlineLong65) == inlineLong21)
    assertTrue((inlineLong42 >>> inlineLong65) == inlineLong21)
  }
}
