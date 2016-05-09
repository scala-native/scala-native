package demo

import scalanative.native._
import scalanative.runtime.Math.{
  `llvm.sin.f64` => sin,
  `llvm.cos.f64` => cos,
  `llvm.fabs.f64` => abs,
  `llvm.pow.f64` => pow,
  `llvm.sqrt.f64` => sqrt
}

@extern
object Extern {
  def malloc(size: Word): Ptr[_] = extern
  def erand48(xsubi: Ptr[Short]): Double = extern
  def fopen(filename: CString, mode: CString): Ptr[_] = extern
  def fputs(str: CString, stream: Ptr[_]): Unit = extern
  def puts(str: CString): Unit = extern
  //char *  itoa ( int value, char * str, int base );
  def itoa(value: Int, buf: CString, base: Int): CString = extern
}
import Extern._
@struct class Vec(
  val x: Double,
  val y: Double,
  val z: Double
) {
  @inline def +(v: Vec) = new Vec(x + v.x, y + v.y, z + v.z)
  @inline def -(v: Vec) = new Vec(x - v.x, y - v.y, z - v.z)
  @inline def *(v: Double) = new Vec(x * v, y * v, y * v)
  @inline def mult(v: Vec) = new Vec(x * v.x, y * v.y, z * v.z)
  @inline def norm() = this * (1/sqrt(x*x + y*y + z*z))
  @inline def dot(v: Vec) = x * v.x + y * v.y + z * v.z
  @inline def %(v: Vec) = new Vec(y*v.z - z*v.y, z*v.x - x*v.z, x*v.y - y*v.x)
}

object Vec {
  @inline def apply(x: Double = 0, y: Double = 0, z: Double = 0): Vec =
    new Vec(x, y, z)
}

@struct class Ray(
  val o: Vec,
  val d: Vec
)

object Ray {
  @inline def apply(o: Vec, d: Vec): Ray =
    new Ray(o, d)
}

@struct class Sphere(
  val rad: Double,
  val p: Vec,
  val e: Vec,
  val c: Vec,
  val refl: Main.Refl
) {
  def intersect(r: Ray): Double = {
    val op = p - r.o
    val eps = 1e-4d
    val b = op.dot(r.d)
    var det = b * b - op.dot(op) + rad * rad
    var t = 0.0d
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

object Sphere {
  def apply(rad: Double, p: Vec, e: Vec, c: Vec, refl: Main.Refl): Sphere =
    new Sphere(rad, p, e, c, refl)
}

object Main {
  type Refl = Int
  final val DIFF: Refl = 1
  final val SPEC: Refl = 2
  final val REFR: Refl = 3

  final val PI = 3.141592653589793

  final val SPHERES = 9
  val spheres = malloc(sizeof[Sphere] * SPHERES).cast[Ptr[Sphere]]
  spheres(0) = Sphere(1e5, Vec( 1e5+1,40.8,81.6), Vec(),Vec(.75,.25,.25),DIFF)
  spheres(1) = Sphere(1e5, Vec(-1e5+99,40.8,81.6),Vec(),Vec(.25,.25,.75),DIFF)
  spheres(2) = Sphere(1e5, Vec(50,40.8, 1e5),     Vec(),Vec(.75,.75,.75),DIFF)
  spheres(3) = Sphere(1e5, Vec(50,40.8,-1e5+170), Vec(),Vec(),           DIFF)
  spheres(4) = Sphere(1e5, Vec(50, 1e5, 81.6),    Vec(),Vec(.75,.75,.75),DIFF)
  spheres(5) = Sphere(1e5, Vec(50,-1e5+81.6,81.6),Vec(),Vec(.75,.75,.75),DIFF)
  spheres(6) = Sphere(16.5,Vec(27,16.5,47),       Vec(),Vec(1,1,1)*.999, SPEC)
  spheres(7) = Sphere(16.5,Vec(73,16.5,78),       Vec(),Vec(1,1,1)*.999, REFR)
  spheres(8) = Sphere(600, Vec(50,681.6-.27,81.6),Vec(12,12,12),  Vec(), DIFF)

  // inline double clamp(double x){ return x<0 ? 0 : x>1 ? 1 : x; }
  @inline def clamp(x: Double): Double =
    if (x < 0) 0
    else if (x > 1) 1
    else x

  @inline def toInt(x: Double): Int =
    (pow(clamp(x), 1/2.2) * 255 + .5).toInt
  @inline def toCString(i: Int): CString = {
    val buf = malloc(sizeof[CChar] * 64).cast[Ptr[CChar]]
    itoa(i, buf, 10)
    buf
  }

  final val inf = 1e20
  @inline def intersect(r: Ray, t: Ptr[Double], id: Ptr[Int]): Boolean = {
    !t = inf
    var d = 0.0d
    var i = SPHERES
    while (i != 0) {
      i -= 1
      d = spheres(i).intersect(r)
      if ((d != 0) && d < !t) {
        !t = d
        !id = i
      }
    }
    return !t < inf
  }

  def radiance(r: Ray, _depth: Int, Xi: Ptr[Short]): Vec = {
    var depth = _depth
    val t = stackalloc[Double]
    val id = stackalloc[Int]
    !id = 0
    if (!intersect(r, t, id)) return Vec()
    val obj = spheres(!id)
    val x = r.o + r.d * !t
    val n = (x - obj.p).norm
    val nl = if (n.dot(r.d) < 0) n else n * -1
    var f = obj.c
    val p =
      if (f.x > f.y && f.x > f.z) f.x
      else if (f.y > f.z) f.y
      else f.z

    depth += 1
    if (depth > 5) {
      if (erand48(Xi) < p) f = f * (1/ p)
      else return obj.e
    }

    if (obj.refl == DIFF) {
      val r1 = 2 * PI * erand48(Xi)
      val r2 = erand48(Xi)
      val r2s = sqrt(r2)
      val w = nl
      val u = ((if (abs(w.x) > .1) Vec(0, 1) else Vec(1)) % w).norm()
      val v = w % u
      val d = (u * cos(r1) * r2s + v * sin(r1) * r2s + w * sqrt(1 - r2)).norm()
      return obj.e + f.mult(radiance(Ray(x, r.d - n * 2 * n.dot(r.d)), depth, Xi))
    } else if (obj.refl == SPEC) {
      return obj.e + f.mult(radiance(Ray(x, r.d - n * 2 * n.dot(r.d)), depth, Xi))
    }

    val reflRay = Ray(x, r.d - n * 2 * n.dot(r.d))
    val into = n.dot(nl) > 0
    val nc = 1d
    val nt = 1.5d
    val nnt = if (into) nc/nt else nt/nc
    val ddn = r.d.dot(nl)
    val cos2t = 1 - nnt * nnt * (1 - ddn * ddn)
    if (cos2t < 0)
      return obj.e + f.mult(radiance(reflRay, depth, Xi))
    val tdir = (r.d*nnt - n*((if (into) 1 else -1)*(ddn*nnt+sqrt(cos2t)))).norm();
    val a = nt - nc
    val b = nt + nc
    val R0 = (a * a ) / (b * b)
    val c = 1 - (if (into) -ddn else tdir.dot(n))
    val Re = R0 + (1 - R0) * c * c * c * c * c
    val Tr = 1 - Re
    val P = .25d + .5d * Re
    val RP = Re/P
    val TP = Tr/(1 - P)
    return obj.e + f.mult(
      if (depth > 2)
        (if (erand48(Xi) < P) radiance(reflRay, depth, Xi)*RP
         else radiance(Ray(x, tdir), depth, Xi)*TP)
      else
        radiance(reflRay, depth, Xi) * Re + radiance(Ray(x, tdir), depth, Xi) * Tr
    )
  }

  final val W = 640
  final val H = 480
  final val SAMPLES = 1
  def main(args: Array[String]): Unit = {
    val cam = Ray(Vec(50d, 52d, 295.6),
                  Vec(0d,-0.042612d,-1d).norm())
    val cx = Vec(W * .5135d/H)
    val cy = (cx % cam.d).norm() * .5135d
    var r  = Vec()
    val c  = malloc(sizeof[Vec] * W * H).cast[Ptr[Vec]]
    val Xi = malloc(sizeof[Short] * 3).cast[Ptr[Short]]
    var y = 0
    while (y < H) {
      Xi(0) = 0.toShort
      Xi(1) = 0.toShort
      Xi(2) = (y * y * y).toShort
      var x = 0
      while (x < W) {
        val i = (H - y - 1) * W + x
        var sy = 0
        while (sy < 2) {
          var sx = 0
          while (sx < 2) {
            var s = 0
            while (s < SAMPLES) {
              val r1 = 2 * erand48(Xi)
              val r2 = 2 * erand48(Xi)
              val dx = if (r1 < 1) sqrt(r1) - 1 else 1 - sqrt(2 - r1)
              val dy = if (r2 < 1) sqrt(r2) - 1 else 1 - sqrt(2 - r1)
              val d = cx * (((sx + .5d + dx)/2d + x)/W - .5d) +
                      cy * (((sy + .5d + dy)/2d + y)/H - .5d) + cam.d
              r = r + radiance(Ray(cam.o+d*140, d.norm()), 0, Xi) * (1.0d/SAMPLES)
              s += 1
            }
            c(i) = c(i) + Vec(clamp(r.x), clamp(r.y), clamp(r.z)) * .25d
            r = Vec()
            sx += 1
          }
          sy += 1
        }
        x += 1
      }
      y += 1
    }

    val f = fopen(c"image0.ppm", c"w")
    //fprintf(f, c"P3\n%d %d\n%d\n", W, H, 255)
    fputs(c"P3\n", f)
    fputs(toCString(W), f)
    fputs(c" ", f)
    fputs(toCString(H), f)
    fputs(c"\n", f)
    fputs(toCString(255), f)
    fputs(c"\n", f)
    var i = 0
    while (i < W * H) {
      //fprintf(f, c"%d %d %d ", toInt(c(i).x), toInt(c(i).y), toInt(c(i).z))
      fputs(toCString(toInt(c(i).x)), f)
      fputs(c" ", f)
      fputs(toCString(toInt(c(i).y)), f)
      fputs(c" ", f)
      fputs(toCString(toInt(c(i).z)), f)
      fputs(c" ", f)
      i += 1
    }
  }
}
