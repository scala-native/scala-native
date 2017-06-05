package demo

import java.lang.Math.{PI, sin, cos, abs, pow, sqrt, random}

class Vec(val x: Double = 0, val y: Double = 0, val z: Double = 0) {
  @inline def +(v: Vec)    = new Vec(x + v.x, y + v.y, z + v.z)
  @inline def -(v: Vec)    = new Vec(x - v.x, y - v.y, z - v.z)
  @inline def *(v: Double) = new Vec(x * v, y * v, z * v)
  @inline def mult(v: Vec) = new Vec(x * v.x, y * v.y, z * v.z)
  @inline def norm()       = this * (1d / sqrt(x * x + y * y + z * z))
  @inline def dot(v: Vec)  = x * v.x + y * v.y + z * v.z
  @inline def %(v: Vec) =
    new Vec(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x)
}

class Ray(val o: Vec, val d: Vec)

object Refl {
  type T = Int
  final val DIFF: Refl.T = 1
  final val SPEC: Refl.T = 2
  final val REFR: Refl.T = 3
}

class Sphere(val rad: Double,
             val p: Vec,
             val e: Vec,
             val c: Vec,
             val refl: Refl.T) {
  def intersect(r: Ray): Double = {
    val op  = p - r.o
    var t   = 0.0d
    val eps = 1e-4d
    val b   = op.dot(r.d)
    var det = b * b - op.dot(op) + rad * rad
    if (det < 0) return 0
    else det = sqrt(det)
    t = b - det
    if (t > eps) t
    else {
      t = b + det
      if (t > eps) t
      else 0
    }
  }
}

object Main {
  val spheres =
    Array[Sphere](
      new Sphere(1e5,
                 new Vec(1e5 + 1, 40.8, 81.6),
                 new Vec(),
                 new Vec(.75, .25, .25),
                 Refl.DIFF),
      new Sphere(1e5,
                 new Vec(-1e5 + 99, 40.8, 81.6),
                 new Vec(),
                 new Vec(.25, .25, .75),
                 Refl.DIFF),
      new Sphere(1e5,
                 new Vec(50, 40.8, 1e5),
                 new Vec(),
                 new Vec(.75, .75, .75),
                 Refl.DIFF),
      new Sphere(1e5,
                 new Vec(50, 40.8, -1e5 + 170),
                 new Vec(),
                 new Vec(),
                 Refl.DIFF),
      new Sphere(1e5,
                 new Vec(50, 1e5, 81.6),
                 new Vec(),
                 new Vec(.75, .75, .75),
                 Refl.DIFF),
      new Sphere(1e5,
                 new Vec(50, -1e5 + 81.6, 81.6),
                 new Vec(),
                 new Vec(.75, .75, .75),
                 Refl.DIFF),
      new Sphere(16.5,
                 new Vec(27, 16.5, 47),
                 new Vec(),
                 new Vec(1, 1, 1) * .999,
                 Refl.SPEC),
      new Sphere(16.5,
                 new Vec(73, 16.5, 78),
                 new Vec(),
                 new Vec(1, 1, 1) * .999,
                 Refl.REFR),
      new Sphere(600,
                 new Vec(50, 681.6 - .27, 81.6),
                 new Vec(12, 12, 12),
                 new Vec(),
                 Refl.DIFF)
    )

  @inline def clamp(x: Double): Double =
    if (x < 0) 0
    else if (x > 1) 1
    else x

  @inline def toInt(x: Double): Int =
    (pow(clamp(x), 1 / 2.2) * 255 + .5).toInt

  final val inf = 1e20
  @inline def intersect(r: Ray, t: Array[Double], id: Array[Int]): Boolean = {
    t(0) = inf
    var d = 0.0d
    var i = spheres.length
    while (i != 0) {
      i -= 1
      d = spheres(i).intersect(r)
      if ((d != 0) && d < t(0)) {
        t(0) = d
        id(0) = i
      }
    }
    return t(0) < inf
  }

  def radiance(r: Ray, _depth: Int): Vec = {
    var depth = _depth
    val t     = new Array[Double](1)
    val id    = new Array[Int](1)
    id(0) = 0
    if (!intersect(r, t, id)) return new Vec()
    val obj = spheres(id(0))
    val x   = r.o + r.d * t(0)
    val n   = (x - obj.p).norm
    val nl  = if (n.dot(r.d) < 0) n else n * -1
    var f   = obj.c
    val p =
      if (f.x > f.y && f.x > f.z) f.x
      else if (f.y > f.z) f.y
      else f.z

    depth += 1
    if (depth > 5) {
      if (depth < 100 && random() < p) f = f * (1 / p)
      else return obj.e
    }

    if (obj.refl == Refl.DIFF) {
      val r1  = 2 * PI * random()
      val r2  = random()
      val r2s = sqrt(r2)
      val w   = nl
      val u   = ((if (abs(w.x) > .1) new Vec(0, 1) else new Vec(1)) % w).norm()
      val v   = w % u
      val d   = (u * cos(r1) * r2s + v * sin(r1) * r2s + w * sqrt(1 - r2)).norm()
      return obj.e + f.mult(radiance(new Ray(x, d), depth))
    } else if (obj.refl == Refl.SPEC) {
      return obj.e + f.mult(
        radiance(new Ray(x, r.d - n * 2 * n.dot(r.d)), depth))
    }

    val reflRay = new Ray(x, r.d - n * 2 * n.dot(r.d))
    val into    = n.dot(nl) > 0
    val nc      = 1d
    val nt      = 1.5d
    val nnt     = if (into) nc / nt else nt / nc
    val ddn     = r.d.dot(nl)
    val cos2t   = 1 - nnt * nnt * (1 - ddn * ddn)
    if (cos2t < 0)
      return obj.e + f.mult(radiance(reflRay, depth))
    val tdir =
      (r.d * nnt - n * ((if (into) 1 else -1) * (ddn * nnt + sqrt(cos2t))))
        .norm();
    val a  = nt - nc
    val b  = nt + nc
    val R0 = (a * a) / (b * b)
    val c  = 1 - (if (into) -ddn else tdir.dot(n))
    val Re = R0 + (1 - R0) * c * c * c * c * c
    val Tr = 1 - Re
    val P  = .25d + .5d * Re
    val RP = Re / P
    val TP = Tr / (1 - P)
    return obj.e + f.mult(
      if (depth > 2)
        (if (random() < P) radiance(reflRay, depth) * RP
         else radiance(new Ray(x, tdir), depth) * TP)
      else
        radiance(reflRay, depth) * Re + radiance(new Ray(x, tdir), depth) * Tr
    )
  }

  final val W       = 800
  final val H       = 600
  final val SAMPLES = 2
  def main(args: Array[String]): Unit = {
    val cam =
      new Ray(new Vec(50d, 52d, 295.6), new Vec(0d, -0.042612d, -1d).norm())
    val cx = new Vec(W * .5135d / H)
    val cy = (cx % cam.d).norm() * .5135d
    var r  = new Vec()
    val c  = Array.fill[Vec](W * H)(new Vec())
    var y  = 0

    def printOverPreviousLine(str: String) = {
      print(f"\u001b[1A"); print(str)
    }

    while (y < H) {
      printOverPreviousLine(
        f"\rRendering (${SAMPLES * 4}%d spp) ${100.0 * y / (H - 1)}%5.2f%%")
      var x = 0
      while (x < W) {
        val i  = (H - y - 1) * W + x
        var sy = 0
        while (sy < 2) {
          var sx = 0
          while (sx < 2) {
            var s = 0
            while (s < SAMPLES) {
              val r1 = 2 * random()
              val r2 = 2 * random()
              val dx = if (r1 < 1d) sqrt(r1) - 1d else 1d - sqrt(2d - r1)
              val dy = if (r2 < 1d) sqrt(r2) - 1d else 1d - sqrt(2d - r2)
              val d = cx * (((sx + .5d + dx) / 2d + x) / W - .5d) +
                cy * (((sy + .5d + dy) / 2d + y) / H - .5d) + cam.d
              r = r + radiance(new Ray(cam.o + d * 140, d.norm()), 0) * (1.0d / SAMPLES)
              s += 1
            }
            c(i) = c(i) + new Vec(clamp(r.x), clamp(r.y), clamp(r.z)) * .25d
            r = new Vec()
            sx += 1
          }
          sy += 1
        }
        x += 1
      }
      y += 1
    }

    val writer = new java.io.PrintWriter(new java.io.File("image1.ppm"))
    writer.print(s"P3\n" + W + " " + H + "\n" + 255 + "\n")
    var i = 0
    while (i < W * H) {
      writer.print(
        toInt(c(i).x) + " " + toInt(c(i).y) + " " + toInt(c(i).z) + " ")
      i += 1
    }
  }
}
