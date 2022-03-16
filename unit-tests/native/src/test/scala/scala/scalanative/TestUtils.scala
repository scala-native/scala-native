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
  def eps(actual: Double, expected: Double): Double =
    2 * Math.max(Math.ulp(actual), Math.ulp(expected))
  def eps(actual: Float, expected: Float): Float =
    2 * Math.max(Math.ulp(actual), Math.ulp(expected))
}
