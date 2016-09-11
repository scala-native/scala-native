import scalanative.native._

@struct
class i1(val _1: Int = 0)
@struct
class i2(val _1: Int = 0, val _2: Int = 0)
@struct
class i3(val _1: Int = 0, val _2: Int = 0, val _3: Int = 0)
@struct
class i4(val _1: Int = 0, val _2: Int = 0, val _3: Int = 0, val _4: Int = 0)
@struct
class i5(val _1: Int = 0, val _2: Int = 0, val _3: Int = 0, val _4: Int = 0, val _5: Int = 0)
@struct
class i6(val _1: Int = 0,
         val _2: Int = 0,
         val _3: Int = 0,
         val _4: Int = 0,
         val _5: Int = 0,
         val _6: Int = 0)
@struct
class i7(val _1: Int = 0,
         val _2: Int = 0,
         val _3: Int = 0,
         val _4: Int = 0,
         val _5: Int = 0,
         val _6: Int = 0,
         val _7: Int = 0)
@struct
class i8(val _1: Int = 0,
         val _2: Int = 0,
         val _3: Int = 0,
         val _4: Int = 0,
         val _5: Int = 0,
         val _6: Int = 0,
         val _7: Int = 0,
         val _8: Int = 0)

@extern
object dummy {

  def takei1(i: i1): Unit = extern
  def takei2(i: i2): Unit = extern
  def takei3(i: i3): Unit = extern
  def takei4(i: i4): Unit = extern
  def takei5(i: i5): Unit = extern
  def takei6(i: i6): Unit = extern
  def takei7(i: i7): Unit = extern
  def takei8(i: i8): Unit = extern

  def makei3(i: Int): i3 = extern
  def makei8(i: Int): i8 = extern

}

@struct
class Vec(val x: Double = 0, val y: Double = 0, val z: Double = 0) {
  @inline def +(v: Vec) = new Vec(x + v.x, y + v.y, z + v.z)
  @inline def -(v: Vec) = new Vec(x - v.x, y - v.y, z - v.z)
  @inline def *(v: Double) = new Vec(x * v, y * v, z * v)
  @inline def mult(v: Vec) = new Vec(x * v.x, y * v.y, z * v.z)
  @inline def dot(v: Vec) = x * v.x + y * v.y + z * v.z
  @inline def %(v: Vec) = new Vec(y*v.z - z*v.y, z*v.x - x*v.z, x*v.y - y*v.x)
}

object Test {
  import dummy._
  def main(args: Array[String]): Unit = {
//    val vec = new Vec(1, 2, 3)
    val s = genI8
    stdio.fprintf(stdio.stdout, c"%d %d %d %d %d %d %d %d\n", s._1, s._2, s._3, s._4, s._5, s._6, s._7, s._8)
  }

  @noinline def genI8 = new i8(1, 2, 3, 4, 5, 6, 7, 8)
}
