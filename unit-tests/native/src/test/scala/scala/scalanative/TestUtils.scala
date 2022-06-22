package scala.scalanative

private[scalanative] object TestUtils {
  def isAlmostEqual(act: Float, exp: Float): Boolean = {
    val diff = Math.abs(act - exp)
    diff <= eps(act, exp)
  }
  def isAlmostEqual(act: Double, exp: Double): Boolean = {
    val diff = Math.abs(act - exp)
    diff <= eps(act, exp)
  }

  /* The mathematically inclined and informed will notice that 'eps' here
   * is a misnomer & misdirection (a.k.a lie).
   *
   * These methods have the proper math meaning only when nUlp == 1.
   */

  def eps(actual: Double, expected: Double, nUlp: Int): Double =
    nUlp * Math.max(Math.ulp(actual), Math.ulp(expected))

  def eps(actual: Double, expected: Double): Double =
    2 * Math.max(Math.ulp(actual), Math.ulp(expected))

  def eps(actual: Float, expected: Float): Float =
    2 * Math.max(Math.ulp(actual), Math.ulp(expected))
}
