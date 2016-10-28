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

class Ray(val position: Vector, val direction: Vector) {
  override def toString = s"Ray [$position, $direction]"
}

class Camera(val position: Vector, val lookAt: Vector, val up: Vector) {
  val equator          = lookAt.normalize.cross(this.up)
  val screen           = position + lookAt
  val REVERSE_Y_VECTOR = new Vector(1.0, -1.0, 1.0)

  def getRay(vx: Double, vy: Double): Ray = {
    val pos = (screen - (equator.multiplyScalar(vx) - up.multiplyScalar(vy))) * REVERSE_Y_VECTOR
    val dir = pos - this.position
    new Ray(pos, dir.normalize)
  }

  override def toString = "Camera []"
}

class Background(val color: Color, val ambience: Double)

class Light(val position: Vector,
            val color: Color,
            val intensity: Double = 10.0)

abstract class Scene {
  val shapes: List[Shape]
  val lights: List[Light]
  val background: Background // = new Background(new Color(0.0, 0.0, 0.5), 0.2)
  val camera: Camera /* = new Camera(new Vector(0.0, 0.0, -0.5),
                        new Vector(0.0, 0.0, 1.0),
                        new Vector(0.0, 1.0, 0.0))*/
}
