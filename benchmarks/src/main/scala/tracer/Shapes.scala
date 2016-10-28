/*                     __                                               *\
**     ________ ___   / /  ___      __ ____  Scala.js Benchmarks        **
**    / __/ __// _ | / /  / _ | __ / // __/  Adam Burmister             **
**  __\ \/ /__/ __ |/ /__/ __ |/_// /_\ \    2012, Google, Inc          **
** /____/\___/_/ |_/____/_/ | |__/ /____/    2013, Jonas Fonseca        **
**                          |/____/                                     **
\*                                                                      */

// The ray tracer code in this file is written by Adam Burmister. It
// is available in its original form from:
//
//   http://labs.flog.co.nz/raytracer/
//
// Ported from the v8 benchmark suite by Google 2012.
// Ported from the Dart benchmark_harness to Scala.js by Jonas Fonseca 2013

package tracer

case class IntersectionInfo(shape: Shape = null,
                            position: Vector = null,
                            normal: Vector = null,
                            color: Color = new Color(0.0, 0.0, 0.0),
                            distance: Double = 0.0,
                            isHit: Boolean = false,
                            hitCount: Int = 0)

abstract class Shape(val position: Vector, val material: Material) {
  def intersect(ray: Ray): IntersectionInfo
  override def toString = "Shape"
}

class Plane(position: Vector, val d: Double, material: Material)
    extends Shape(position, material) {

  def intersect(ray: Ray): IntersectionInfo = {
    val Vd = this.position.dot(ray.direction);
    if (Vd == 0)
      return new IntersectionInfo() // no intersection

    val t = -(this.position.dot(ray.position) + this.d) / Vd;
    if (t <= 0)
      return new IntersectionInfo() // no intersection

    val intersection = ray.position + ray.direction.multiplyScalar(t)
    val color = if (this.material.hasTexture) {
      val vU        = new Vector(this.position.y, this.position.z, -this.position.x)
      val vV        = vU.cross(this.position)
      val u: Double = intersection.dot(vU)
      val v: Double = intersection.dot(vV)
      this.material.getColor(u, v)
    } else {
      this.material.getColor(0, 0)
    }

    new IntersectionInfo(
      shape = this,
      isHit = true,
      position = intersection,
      normal = this.position,
      distance = t,
      color = color
    )
  }

  override def toString = s"Plane [$position, d=$d]"
}

class Sphere(position: Vector, radius: Double, material: Material)
    extends Shape(position, material) {

  def intersect(ray: Ray): IntersectionInfo = {
    val dst = ray.position - this.position

    val B = dst.dot(ray.direction)
    val C = dst.dot(dst) - (this.radius * this.radius)
    val D = (B * B) - C

    if (D <= 0)
      return new IntersectionInfo(null) // no intersection

    val distance = (-B) - math.sqrt(D)
    val pos      = ray.position + ray.direction.multiplyScalar(distance)

    new IntersectionInfo(
      shape = this,
      isHit = true,
      position = pos,
      normal = (pos - this.position).normalize,
      distance = distance,
      color = this.material.getColor(0, 0)
    )
  }

  override def toString = s"Sphere [position=$position, radius=$radius]"
}
