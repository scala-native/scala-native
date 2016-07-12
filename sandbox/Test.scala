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

object Test {
  import dummy._
  def main(args: Array[String]): Unit = {

  }
}
