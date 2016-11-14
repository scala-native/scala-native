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

// gloss:        [0...infinity] 0 = matt
// transparency:  0=opaque
// reflection:   [0...infinity] 0 = no reflection
abstract class Material(val reflection: Double,
                        val transparency: Double,
                        val gloss: Double) {
  // var refraction = 0.50;
  def hasTexture = false
  def getColor(u: Double, v: Double): Color
}

class Chessboard(colorEven: Color,
                 colorOdd: Color,
                 reflection: Double,
                 transparency: Double,
                 gloss: Double,
                 density: Double)
    extends Material(reflection, transparency, gloss) {

  override def hasTexture = true

  def getColor(u: Double, v: Double): Color = {
    val t = wrapUp(u * density) * wrapUp(v * density)

    if (t < 0.0) {
      colorEven
    } else {
      colorOdd
    }
  }

  def wrapUp(value: Double): Double = {
    var t = value % 2.0

    if (t < -1.0)
      t = t + 2.0

    if (t >= 1.0)
      t - 2.0
    else
      t
  }
}

class Solid(color: Color,
            reflection: Double,
            refraction: Double,
            transparency: Double,
            gloss: Double)
    extends Material(reflection, transparency, gloss) {

  //def refraction = refraction

  def getColor(u: Double, v: Double): Color =
    color
}
