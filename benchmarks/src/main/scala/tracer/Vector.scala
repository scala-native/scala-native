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

@inline
class Vector(val x: Double, val y: Double, val z: Double) {

  @inline def normalize: Vector = {
    val m = this.magnitude
    new Vector(x / m, y / m, z / m)
  }

  @inline def magnitude: Double =
    math.sqrt((x * x) + (y * y) + (z * z))

  @inline def cross(that: Vector): Vector = {
    new Vector(-this.z * that.y + this.y * that.z,
               this.z * that.x - this.x * that.z,
               -this.y * that.x + this.x * that.y)
  }

  @inline def dot(that: Vector): Double =
    this.x * that.x + this.y * that.y + this.z * that.z

  @inline def +(that: Vector): Vector =
    new Vector(that.x + x, that.y + y, that.z + z)

  @inline def -(that: Vector): Vector =
    new Vector(x - that.x, y - that.y, z - that.z)

  @inline def *(that: Vector): Vector =
    new Vector(x * that.x, y * that.y, z * that.z)

  @inline def multiplyScalar(w: Double): Vector =
    new Vector(x * w, y * w, z * w)

  override def toString =
    s"Vector [$x, $y, $z]"
}
