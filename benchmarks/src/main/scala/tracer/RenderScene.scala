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

// 'event' null means scalar we are benchmarking
class RenderScene extends Scene {

  val camera = new Camera(
    new Vector(0.0, 0.0, -15.0),
    new Vector(-0.2, 0.0, 5.0),
    new Vector(0.0, 1.0, 0.0)
  )

  val background = new Background(new Color(0.5, 0.5, 0.5), 0.4)

  val plane = new Plane(
    new Vector(0.1, 0.9, -0.5).normalize,
    1.2,
    new Chessboard(
      new Color(1.0, 1.0, 1.0),
      new Color(0.0, 0.0, 0.0),
      0.2,
      0.0,
      1.0,
      0.7
    )
  )

  val sphere = new Sphere(
    new Vector(-1.5, 1.5, 2.0),
    1.5,
    new Solid(
      new Color(0.0, 0.5, 0.5),
      0.3,
      0.0,
      0.0,
      2.0
    )
  )

  val sphere1 = new Sphere(
    new Vector(1.0, 0.25, 1.0),
    0.5,
    new Solid(
      new Color(0.9, 0.9, 0.9),
      0.1,
      0.0,
      0.0,
      1.5
    )
  )

  val shapes = List(plane, sphere, sphere1)

  var light = new Light(
    new Vector(5.0, 10.0, -1.0),
    new Color(0.8, 0.8, 0.8)
  )

  var light1 = new Light(
    new Vector(-3.0, 5.0, -15.0),
    new Color(0.8, 0.8, 0.8),
    100.0
  )

  val lights = List(light, light1)

  def renderScene(config: EngineConfiguration,
                  canvas: CanvasRenderingContext2D): Unit = {
    new Engine(config).renderScene(this, canvas)
  }
}
