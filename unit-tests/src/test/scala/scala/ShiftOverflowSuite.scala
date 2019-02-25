package scala

object ShiftOverflowSuite extends tests.Suite {
  @noinline def noinlineByte42: Byte   = 42.toByte
  @noinline def noinlineShort42: Short = 42.toShort
  @noinline def noinlineChar42: Char   = 42.toChar
  @noinline def noinlineInt42: Int     = 42
  @noinline def noinlineLong42: Long   = 42L
  @noinline def noinlineInt33: Int     = 33
  @noinline def noinlineLong65: Long   = 65L
  @noinline def noinlineInt21: Int     = 21
  @noinline def noinlineLong21: Long   = 21L
  @noinline def noinlineInt84: Int     = 84
  @noinline def noinlineLong84: Long   = 84L

  @inline def inlineByte42: Byte   = 42.toByte
  @inline def inlineShort42: Short = 42.toShort
  @inline def inlineChar42: Char   = 42.toChar
  @inline def inlineInt42: Int     = 42
  @inline def inlineLong42: Long   = 42L
  @inline def inlineInt33: Int     = 33
  @inline def inlineLong65: Long   = 65L
  @inline def inlineInt21: Int     = 21
  @inline def inlineLong21: Long   = 21L
  @inline def inlineInt84: Int     = 84
  @inline def inlineLong84: Long   = 84L

  test("x << 33 (noinline)") {
    assert((noinlineByte42 << noinlineInt33) == noinlineInt84)
    assert((noinlineShort42 << noinlineInt33) == noinlineInt84)
    assert((noinlineChar42 << noinlineInt33) == noinlineInt84)
    assert((noinlineInt42 << noinlineInt33) == noinlineInt84)
  }

  test("x << 33 (inline)") {
    assert((inlineByte42 << inlineInt33) == inlineInt84)
    assert((inlineShort42 << inlineInt33) == inlineInt84)
    assert((inlineChar42 << inlineInt33) == inlineInt84)
    assert((inlineInt42 << inlineInt33) == inlineInt84)
  }

  test("x << 65L (noinline)") {
    assert((noinlineByte42 << noinlineLong65) == noinlineLong84)
    assert((noinlineShort42 << noinlineLong65) == noinlineLong84)
    assert((noinlineChar42 << noinlineLong65) == noinlineLong84)
    assert((noinlineInt42 << noinlineLong65) == noinlineLong84)
    assert((noinlineLong42 << noinlineLong65) == noinlineLong84)
  }

  test("x << 65L (inline)") {
    assert((inlineByte42 << inlineLong65) == inlineLong84)
    assert((inlineShort42 << inlineLong65) == inlineLong84)
    assert((inlineChar42 << inlineLong65) == inlineLong84)
    assert((inlineInt42 << inlineLong65) == inlineLong84)
    assert((inlineLong42 << inlineLong65) == inlineLong84)
  }

  test("x >> 33 (noinline)") {
    assert((noinlineByte42 >> noinlineInt33) == noinlineInt21)
    assert((noinlineShort42 >> noinlineInt33) == noinlineInt21)
    assert((noinlineChar42 >> noinlineInt33) == noinlineInt21)
    assert((noinlineInt42 >> noinlineInt33) == noinlineInt21)
  }

  test("x >> 33 (inline)") {
    assert((inlineByte42 >> inlineInt33) == inlineInt21)
    assert((inlineShort42 >> inlineInt33) == inlineInt21)
    assert((inlineChar42 >> inlineInt33) == inlineInt21)
    assert((inlineInt42 >> inlineInt33) == inlineInt21)
  }

  test("x >> 65L (noinline)") {
    assert((noinlineByte42 >> noinlineLong65) == noinlineLong21)
    assert((noinlineShort42 >> noinlineLong65) == noinlineLong21)
    assert((noinlineChar42 >> noinlineLong65) == noinlineLong21)
    assert((noinlineInt42 >> noinlineLong65) == noinlineLong21)
    assert((noinlineLong42 >> noinlineLong65) == noinlineLong21)
  }

  test("x >> 65L (inline)") {
    assert((inlineByte42 >> inlineLong65) == inlineLong21)
    assert((inlineShort42 >> inlineLong65) == inlineLong21)
    assert((inlineChar42 >> inlineLong65) == inlineLong21)
    assert((inlineInt42 >> inlineLong65) == inlineLong21)
    assert((inlineLong42 >> inlineLong65) == inlineLong21)
  }

  test("x >>> 33 (noinline)") {
    assert((noinlineByte42 >>> noinlineInt33) == noinlineInt21)
    assert((noinlineShort42 >>> noinlineInt33) == noinlineInt21)
    assert((noinlineChar42 >>> noinlineInt33) == noinlineInt21)
    assert((noinlineInt42 >>> noinlineInt33) == noinlineInt21)
  }

  test("x >>> 33 (inline)") {
    assert((inlineByte42 >>> inlineInt33) == inlineInt21)
    assert((inlineShort42 >>> inlineInt33) == inlineInt21)
    assert((inlineChar42 >>> inlineInt33) == inlineInt21)
    assert((inlineInt42 >>> inlineInt33) == inlineInt21)
  }

  test("x >>> 65L (noinline)") {
    assert((noinlineByte42 >>> noinlineLong65) == noinlineLong21)
    assert((noinlineShort42 >>> noinlineLong65) == noinlineLong21)
    assert((noinlineChar42 >>> noinlineLong65) == noinlineLong21)
    assert((noinlineInt42 >>> noinlineLong65) == noinlineLong21)
    assert((noinlineLong42 >>> noinlineLong65) == noinlineLong21)
  }

  test("x >>> 65L (inline)") {
    assert((inlineByte42 >>> inlineLong65) == inlineLong21)
    assert((inlineShort42 >>> inlineLong65) == inlineLong21)
    assert((inlineChar42 >>> inlineLong65) == inlineLong21)
    assert((inlineInt42 >>> inlineLong65) == inlineLong21)
    assert((inlineLong42 >>> inlineLong65) == inlineLong21)
  }
}
