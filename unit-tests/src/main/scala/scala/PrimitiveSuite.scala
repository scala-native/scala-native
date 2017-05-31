package scala

object PrimitiveSuite extends tests.Suite {
  val char   = 1.toChar
  val byte   = 1.toByte
  val short  = 1.toShort
  val int    = 1
  val long   = 1L
  val float  = 1F
  val double = 1D

  val negint    = -1
  val neglong   = -1L
  val negfloat  = -1F
  val negdouble = -1D

  val notint  = -2
  val notlong = -2

  test("-x") {
    assert(-char == negint)
    assert(-byte == negint)
    assert(-short == negint)
    assert(-int == negint)
    assert(-long == neglong)
    assert(-float == negfloat)
    assert(-double == negdouble)
  }

  test("~x") {
    assert(~char == notint)
    assert(~byte == notint)
    assert(~short == notint)
    assert(~int == notint)
    assert(~long == notlong)
  }

  test("+x") {
    assert(+char == int)
    assert(+byte == int)
    assert(+short == int)
    assert(+int == int)
    assert(+long == long)
    assert(+float == float)
    assert(+double == double)
  }
}
