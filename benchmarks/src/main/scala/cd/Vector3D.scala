package cd

final class Vector3D(val x: Double, val y: Double, val z: Double) {
  def plus(other: Vector3D) =
    new Vector3D(x + other.x, y + other.y, z + other.z)

  def minus(other: Vector3D) =
    new Vector3D(x - other.x, y - other.y, z - other.z)

  def dot(other: Vector3D): Double =
    x * other.x + y * other.y + z * other.z

  def squaredMagnitude(): Double =
    this.dot(this)

  def magnitude(): Double =
    Math.sqrt(squaredMagnitude())

  def times(amount: Double): Vector3D =
    new Vector3D(x * amount, y * amount, z * amount)
}
