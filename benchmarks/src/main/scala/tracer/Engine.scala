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

case class EngineConfiguration(imageWidth: Int = 100,
                               imageHeight: Int = 100,
                               pixelWidth: Int = 2,
                               pixelHeight: Int = 2,
                               rayDepth: Int = 2,
                               renderDiffuse: Boolean = false,
                               renderShadows: Boolean = false,
                               renderHighlights: Boolean = false,
                               renderReflections: Boolean = false) {

  val canvasHeight = imageHeight / pixelHeight
  val canvasWidth  = imageWidth / pixelWidth
}

class Engine(val config: EngineConfiguration) {

  // Sum for verifying that the scene was ray traced correctly.
  private var diagonalColorBrightnessCheckSum = 0

  def setPixel(canvasContext: CanvasRenderingContext2D,
               x: Int,
               y: Int,
               color: Color): Unit = {
    val (pxW, pxH) = (config.pixelWidth, config.pixelHeight)

    if (canvasContext != null) {
      canvasContext.fillStyle = color.toString
      canvasContext.fillRect(x * pxW, y * pxH, pxW, pxH)
    } else {
      if (x == y) {
        diagonalColorBrightnessCheckSum += color.brightness
      }
    }
  }

  // 'canvasContext' can be null if raytracer runs as benchmark
  def renderScene(scene: Scene,
                  canvasContext: CanvasRenderingContext2D): Unit = {
    for {
      y <- 0 until config.canvasHeight
      x <- 0 until config.canvasWidth
    } {
      val yp = y * 1.0 / config.canvasHeight * 2 - 1;
      val xp = x * 1.0 / config.canvasWidth * 2 - 1;

      val ray   = scene.camera.getRay(xp, yp);
      val color = getPixelColor(ray, scene)
      setPixel(canvasContext, x, y, color)
    }

    if (canvasContext == null && diagonalColorBrightnessCheckSum != 2321) {
      throw new Error("Scene rendered incorrectly")
    }
  }

  def getPixelColor(ray: Ray, scene: Scene): Color = {
    val info = this.testIntersection(ray, scene, null)
    if (info.isHit)
      rayTrace(info, ray, scene, 0)
    else
      scene.background.color
  }

  def testIntersection(ray: Ray,
                       scene: Scene,
                       exclude: Shape): IntersectionInfo = {
    var hits = 0;
    var best = new IntersectionInfo(distance = 2000)

    for (shape <- scene.shapes) {
      if (shape != exclude) {
        val info = shape.intersect(ray)
        if (info.isHit && info.distance >= 0 && info.distance < best.distance) {
          best = info
          hits = hits + 1
        }
      }
    }

    //best.hitCount = hits;
    best
  }

  def getReflectionRay(P: Vector, N: Vector, V: Vector): Ray = {
    val c1 = -N.dot(V)
    val R1 = N.multiplyScalar(2 * c1) + V

    new Ray(P, R1)
  }

  def rayTrace(info: IntersectionInfo,
               ray: Ray,
               scene: Scene,
               depth: Int): Color = {
    // Calc ambient
    var color     = info.color.multiplyScalar(scene.background.ambience)
    val oldColor  = color
    val shininess = math.pow(10, info.shape.material.gloss + 1)

    for (light <- scene.lights) {
      // Calc diffuse lighting
      val v = (light.position - info.position).normalize

      if (config.renderDiffuse) {
        val L = v.dot(info.normal)
        if (L > 0.0) {
          color = color + info.color * light.color.multiplyScalar(L)
        }
      }

      // The greater the depth the more accurate the colours, but
      // this is exponentially (!) expensive
      if (depth <= config.rayDepth) {
        // calculate reflection ray
        if (config.renderReflections && info.shape.material.reflection > 0) {
          val reflectionRay =
            getReflectionRay(info.position, info.normal, ray.direction)
          val refl = testIntersection(reflectionRay, scene, info.shape)

          val reflColor = if (refl.isHit && refl.distance > 0) {
            rayTrace(refl, reflectionRay, scene, depth + 1)
          } else {
            scene.background.color
          }

          color = color.blend(reflColor, info.shape.material.reflection)
        }
        // Refraction
        /* TODO */
      }
      /* Render shadows and highlights */

      var shadowInfo = new IntersectionInfo()

      if (config.renderShadows) {
        val shadowRay = new Ray(info.position, v)

        shadowInfo = testIntersection(shadowRay, scene, info.shape)
        if (shadowInfo.isHit && shadowInfo.shape != info.shape) {
          val vA = color.multiplyScalar(0.5)
          val dB = (0.5 * math.pow(shadowInfo.shape.material.transparency,
                                   0.5))
          color = vA.addScalar(dB)
        }
      }
      // Phong specular highlights
      if (config.renderHighlights && !shadowInfo.isHit && info.shape.material.gloss > 0) {
        var Lv = (info.shape.position - light.position).normalize

        var E = (scene.camera.position - info.shape.position).normalize

        var H = (E - Lv).normalize

        var glossWeight = math.pow(math.max(info.normal.dot(H), 0), shininess)
        color = light.color.multiplyScalar(glossWeight) + color
      }
    }
    color.limit()
  }

  override def toString() =
    s"Engine [canvasWidth: $config.canvasWidth, canvasHeight: $config.canvasHeight]"
}
