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
}
import Extern._

@struct class Vec(
  val x: Double,
  val y: Double,
  val z: Double
) {
  @inline def +(v: Vec) = Vec(x + v.x, y + v.y, z + v.z)
  @inline def -(v: Vec) = Vec(x - v.x, y - v.y, z - v.z)
  @inline def *(v: Double) = Vec(x * v, y * v, y * v)
  @inline def mult(v: Vec) = Vec(x * v.x, y * v.y, z * v.z)
  @inline def norm() = this * (1/sqrt(x*x + y*y + z*z))
  @inline def dot(v: Vec) = x * v.x + y * v.y + z * v.z
  @inline def %(v: Vec) = Vec(y*v.z - z*v.y, z*v.x - x*v.z, x*v.y - y*v.x)
}

object Vec {
  def apply(x: Double = 0, y: Double = 0, z: Double = 0): Vec =
    new Vec(x, y, z)
}

@struct class Ray(
  val o: Vec,
  val d: Vec
)

object Ray {
  def apply(o: Vec, d: Vec): Ray =
    new Ray(o, d)
}

@struct class Sphere(
  val rad: Double,
  val p: Vec,
  val e: Vec,
  val c: Vec,
  val refl: Main.Refl
) {
  // double intersect(const Ray &r) const { // returns distance, 0 if nohit
  //   Vec op = p-r.o; // Solve t^2*d.d + 2*t*(o-p).d + (o-p).(o-p)-R^2 = 0
  //   double t, eps=1e-4, b=op.dot(r.d), det=b*b-op.dot(op)+rad*rad;
  //   if (det<0) return 0; else det=sqrt(det);
  //   return (t=b-det)>eps ? t : ((t=b+det)>eps ? t : 0);
  // }
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

  // inline int toInt(double x){ return int(pow(clamp(x),1/2.2)*255+.5); }
  @inline def toInt(x: Double): Int =
    (pow(clamp(x), 1/2.2) * 255 + .5).toInt

  // inline bool intersect(const Ray &r, double &t, int &id){
  //   double n=sizeof(spheres)/sizeof(Sphere), d, inf=t=1e20;
  //   for(int i=int(n);i--;) if((d=spheres[i].intersect(r))&&d<t){t=d;id=i;}
  //   return t<inf;
  // }
  final val inf = 1e20
  @inline def intersect(r: Ray, t: Double, id: Ptr[Int]): Boolean = {
    var d = 1.0d
    var t = 1e20
    var i = SPHERES
    while (i != 0) {
      d = spheres(i).intersect(r)
      if (d < t) {
        t = d
        !id = i
      }
      i -= 1
    }
    return t < inf
  }

  // Vec radiance(const Ray &r, int depth, unsigned short *Xi){
  //   double t;                               // distance to intersection
  //   int id=0;                               // id of intersected object
  //   if (!intersect(r, t, id)) return Vec(); // if miss, return black
  //   const Sphere &obj = spheres[id];        // the hit object
  //   Vec x=r.o+r.d*t, n=(x-obj.p).norm(), nl=n.dot(r.d)<0?n:n*-1, f=obj.c;
  //   double p = f.x>f.y && f.x>f.z ? f.x : f.y>f.z ? f.y : f.z; // max refl
  //   if (++depth>5) if (erand48(Xi)<p) f=f*(1/p); else return obj.e; //R.R.
  //   if (obj.refl == DIFF){                  // Ideal DIFFUSE reflection
  //     double r1=2*M_PI*erand48(Xi), r2=erand48(Xi), r2s=sqrt(r2);
  //     Vec w=nl, u=((fabs(w.x)>.1?Vec(0,1):Vec(1))%w).norm(), v=w%u;
  //     Vec d = (u*cos(r1)*r2s + v*sin(r1)*r2s + w*sqrt(1-r2)).norm();
  //     return obj.e + f.mult(radiance(Ray(x,d),depth,Xi));
  //   } else if (obj.refl == SPEC)            // Ideal SPECULAR reflection
  //     return obj.e + f.mult(radiance(Ray(x,r.d-n*2*n.dot(r.d)),depth,Xi));
  //   Ray reflRay(x, r.d-n*2*n.dot(r.d));     // Ideal dielectric REFRACTION
  //   bool into = n.dot(nl)>0;                // Ray from outside going in?
  //   double nc=1, nt=1.5, nnt=into?nc/nt:nt/nc, ddn=r.d.dot(nl), cos2t;
  //   if ((cos2t=1-nnt*nnt*(1-ddn*ddn))<0)    // Total internal reflection
  //     return obj.e + f.mult(radiance(reflRay,depth,Xi));
  //   Vec tdir = (r.d*nnt - n*((into?1:-1)*(ddn*nnt+sqrt(cos2t)))).norm();
  //   double a=nt-nc, b=nt+nc, R0=a*a/(b*b), c = 1-(into?-ddn:tdir.dot(n));
  //   double Re=R0+(1-R0)*c*c*c*c*c,Tr=1-Re,P=.25+.5*Re,RP=Re/P,TP=Tr/(1-P);
  //   return obj.e + f.mult(depth>2 ? (erand48(Xi)<P ?   // Russian roulette
  //     radiance(reflRay,depth,Xi)*RP:radiance(Ray(x,tdir),depth,Xi)*TP) :
  //     radiance(reflRay,depth,Xi)*Re+radiance(Ray(x,tdir),depth,Xi)*Tr);
  // }
  def radiance(r: Ray, _depth: Int): Vec = {
    var depth = _depth
    val t = 0.0d
    val id = stackalloc[Int]
    if (!intersect(r, t, id)) return Vec()
    val obj = spheres(!id)
    val x = r.o + r.d * t
    val n = (x - obj.p).norm
    val nl = if (n.dot(r.d) < 0) n else n * -1
    var f = obj.c
    val p =
      if (f.x > f.y && f.x > f.z) f.x
      else if (f.y > f.z) f.y
      else f.z

    depth += 1
    if (depth > t) {
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
      return obj.e + f.mult(radiance(Ray(x, r.d - n * 2 * n.dot(r.d)), depth))
    } else if (obj.refl == SPEC) {
      return obj.e + f.mult(radiance(Ray(x, r.d - n * 2 * n.dot(r.d)), depth))
    }

    val reflRay = Ray(x, r.d - n * 2 * n.dot(r.d))
    val into = n.dot(nl) > 0
    val nc = 1d
    val nt = 1.5d
    val nnt = if (into) nc/nt else nt/nc
    val ddn = r.d.dot(nl)
    val cos2t = 1 - nnt * nnt * (1 - ddn * ddn)
    if (cos2t < 0)
      return obj.e + f.mult(radiance(reflRay, depth))
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
        (if (erand48(Xi)<P) radiance(reflRay,depth)*RP
         else radiance(Ray(x,tdir),depth)*TP)
      else
        radiance(reflRay,depth)*Re+radiance(Ray(x,tdir),depth)*Tr
    )
  }

  // int main(int argc, char *argv[]){
  //   int w=1024, h=768, samps = argc==2 ? atoi(argv[1])/4 : 1; // # samples
  //   Ray cam(Vec(50,52,295.6), Vec(0,-0.042612,-1).norm()); // cam pos, dir
  //   Vec cx=Vec(w*.5135/h), cy=(cx%cam.d).norm()*.5135, r, *c=new Vec[w*h];
  // #pragma omp parallel for schedule(dynamic, 1) private(r)       // OpenMP
  //   for (int y=0; y<h; y++){                       // Loop over image rows
  //     fprintf(stderr,"\rRendering (%d spp) %5.2f%%",samps*4,100.*y/(h-1));
  //     for (unsigned short x=0, Xi[3]={0,0,y*y*y}; x<w; x++)   // Loop cols
  //       for (int sy=0, i=(h-y-1)*w+x; sy<2; sy++)     // 2x2 subpixel rows
  //         for (int sx=0; sx<2; sx++, r=Vec()){        // 2x2 subpixel cols
  //           for (int s=0; s<samps; s++){
  //             double r1=2*erand48(Xi), dx=r1<1 ? sqrt(r1)-1: 1-sqrt(2-r1);
  //             double r2=2*erand48(Xi), dy=r2<1 ? sqrt(r2)-1: 1-sqrt(2-r2);
  //             Vec d = cx*( ( (sx+.5 + dx)/2 + x)/w - .5) +
  //                     cy*( ( (sy+.5 + dy)/2 + y)/h - .5) + cam.d;
  //             r = r + radiance(Ray(cam.o+d*140,d.norm()),0,Xi)*(1./samps);
  //           } // Camera rays are pushed ^^^^^ forward to start in interior
  //           c[i] = c[i] + Vec(clamp(r.x),clamp(r.y),clamp(r.z))*.25;
  //         }
  //   }
  //   FILE *f = fopen("image.ppm", "w");         // Write image to PPM file.
  //   fprintf(f, "P3\n%d %d\n%d\n", w, h, 255);
  //   for (int i=0; i<w*h; i++)
  //     fprintf(f,"%d %d %d ", toInt(c[i].x), toInt(c[i].y), toInt(c[i].z));
  // }
  final val W = 1024
  final val H = 768
  final val SAMPLES = 1
  var Xi: Ptr[Short] = _
  def main(args: Array[String]): Unit = {
    Xi = malloc(sizeof[Short] * 3).cast[Ptr[Short]]
    if (Xi.cast[Word] == 0L) puts(c"null") else puts(c"not-null")
    val cam = Ray(Vec(50d, 52d, 295.6),
                  Vec(0d,-0.042612d,-1d).norm())
    val cx = Vec(W * .5135d/H)
    val cy = (cx % cam.d).norm() * .5135d
    var r  = Vec()
    val c  = malloc(sizeof[Vec] * W * H).cast[Ptr[Vec]]
    var y  = 0
    while (y < H) {
      var x = 0
      Xi(0) = 0.toShort
      Xi(1) = 0.toShort
      Xi(2) = (y * y * y).toShort
      while (x < W) {
        var sy = 0
        var i = (H - y - 1) * W + x
        while (sy < 2) {
          var sx = 0
          while (sx < 2) {
            var s = 0
            while (s < SAMPLES) {
              var r1 = 2 * erand48(Xi)
              val r2 = 2 * erand48(Xi)
              val dx = if (r1 < 1) sqrt(r1) - 1 else 1 - sqrt(2 - r1)
              val dy = if (r2 < 1) sqrt(r2) - 1 else 1 - sqrt(2 - r1)
              val d = cx * (((sx + .5d + dx)/2d + x)/W - .5d) +
                      cy * (((sy + .5d + dy)/2d + y)/H - .5d) + cam.d
              r = r + radiance(Ray(cam.o+d*140,d.norm()),0) * (1.0d/SAMPLES)
              s += 1
            }
            c(i) = c(i) + Vec(clamp(r.x), clamp(r.y), clamp(r.z)) * .25d
            sx += 1
            r = Vec()
          }
          sy += 1
        }
        x += 1.toShort
      }
      y += 1
    }

    val f = fopen(c"image.ppm", c"w")
    //fprintf(f, c"P3\n%d %d\n%d\n", W, H, 255)
    var i = 0
    while (i < W * H) {
      //fprintf(f, c"%d %d %d ", toInt(c(i).x), toInt(c(i).y), toInt(c(i).z))
      i += 1
    }
  }
}
