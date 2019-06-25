package scala

object LongFloatPrimitiveSuite extends tests.Suite {
  @inline def inlineFloat(): Float =
    java.lang.Float.intBitsToFloat(1079290514)
  @noinline def noinlineFloat(): Float =
    java.lang.Float.intBitsToFloat(1079290514)
  @inline def inlineLong(): Long =
    1412906027847L
  @noinline def noinlineLong(): Long =
    1412906027847L

  test("scala/bug/issues/11253") {
    assert(noinlineLong % noinlineFloat == 2.3242621F)
    assert(inlineLong   % inlineFloat == 2.3242621F)
  }
}
