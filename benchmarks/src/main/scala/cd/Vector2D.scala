package cd

final class Vector2D(val x: Double, val y: Double)
    extends Comparable[Vector2D] {
  import Vector2D._

  def plus(other: Vector2D): Vector2D =
    new Vector2D(x + other.x, y + other.y)

  def minus(other: Vector2D): Vector2D =
    return new Vector2D(x - other.x, y - other.y)

  @Override
  def compareTo(other: Vector2D): Int = {
    val result = compareNumbers(this.x, other.x)
    if (result != 0) {
      return result
    }
    return compareNumbers(this.y, other.y)
  }
}

object Vector2D {
  def compareNumbers(a: Double, b: Double): Int = {
    if (a == b) {
      return 0;
    }
    if (a < b) {
      return -1;
    }
    if (a > b) {
      return 1;
    }

    // We say that NaN is smaller than non-NaN.
    if (a == a) {
      return 1;
    }
    return -1;
  }
}
