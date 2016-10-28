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

class Color(val red: Double, val green: Double, val blue: Double) {

  def trim(light: Double): Double =
    if (light <= 0.0) 0.0
    else if (light > 1.0) 1.0
    else light

  def limit(): Color =
    new Color(trim(red), trim(green), trim(blue))

  def +(that: Color): Color =
    new Color(red + that.red, green + that.green, blue + that.blue)

  def addScalar(scalar: Double): Color =
    new Color(trim(red + scalar), trim(green + scalar), trim(blue + scalar))

  def *(that: Color): Color =
    new Color(red * that.red, green * that.green, blue * that.blue)

  def multiplyScalar(f: Double): Color =
    new Color(red * f, green * f, blue * f)

  def blend(that: Color, w: Double): Color =
    this.multiplyScalar(1.0 - w) + that.multiplyScalar(w)

  def brightness(): Int = {
    val r = (this.red * 255).toInt
    val g = (this.green * 255).toInt
    val b = (this.blue * 255).toInt

    (r * 77 + g * 150 + b * 29) >> 8;
  }

  override def toString(): String = {
    val r = (this.red * 255).toInt
    val g = (this.green * 255).toInt
    val b = (this.blue * 255).toInt

    s"rgb($r,$g,$b)"
  }
}
