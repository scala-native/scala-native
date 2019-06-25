package scala.scalanative

import scalanative.unsigned._
import scalanative.unsafe._

object IssuesSuite extends tests.Suite {

  def foo(arg: Int): Unit                    = ()
  def crash(arg: CFuncPtr1[Int, Unit]): Unit = ()
  def lifted208Test(): Unit =
    crash(new CFuncPtr1[Int, Unit] {
      def apply(value: Int): Unit = ()
    })

  test("#208") {
    // If we put the test directly, behind the scenes, this will
    // create a nested closure with a pointer to the outer one
    // and the latter is not supported in scala-native
    lifted208Test()
  }

  test("#253") {
    class Cell(val value: Int)

    val arr     = Array(new Cell(1), new Cell(2))
    val reverse = arr.reverse

    assert(reverse(0).value == 2)
    assert(reverse(1).value == 1)
  }

  test("#260") {
    def getStr(): String = {
      val bytes = Array('h'.toByte, 'o'.toByte, 'l'.toByte, 'a'.toByte)
      new String(bytes)
    }

    val sz = getStr()

    assert("hola" == sz)
    assert("hola".equals(sz))
  }

  test("#275") {
    val arr = new Array[Int](10)
    assert(arr.getClass.getName == "scala.scalanative.runtime.IntArray")
    assert(arr.toList == List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0))

    val arr2 = arr.map(_ + 1)
    assert(arr2.getClass.getName == "scala.scalanative.runtime.IntArray")
    assert(arr2.toList == List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1))
  }

  test("#314") {
    // Division by zero is defined behavior.
    assert {
      try {
        5 / 0
        false
      } catch {
        case _: ArithmeticException =>
          true
      }
    }
  }

  test("#326") {
    abstract class A
    case class S[T](a: T) extends A

    def check(a: A) = a match {
      case S(d: Double) => "double"
      case S(d: Int)    => "int"
      case _            => "neither"
    }

    def main(args: Array[String]): Unit = {
      val (dbl, int, obj) = (S(2.3), S(2), S(S(2)))
      assert(check(dbl) == "double")
      assert(check(int) == "int")
      assert(check(obj) == "neither")
    }
  }

  test("#327") {
    val a = BigInt(1)
    assert(a.toInt == 1)
  }

  test("#337") {
    case class TestObj(value: Int)
    val obj = TestObj(10)
    assert(obj.value == 10)
  }

  test("#350") {
    val div = java.lang.Long.divideUnsigned(42L, 2L)
    assert(div == 21L)
  }

  test("#374") {
    assert("42" == bar(42))
    assert("bar" == bar_i32())
  }

  def bar(i: Int): String = i.toString
  def bar_i32(): String   = "bar"

  test("#376") {
    val m     = scala.collection.mutable.Map.empty[String, String]
    val hello = "hello"
    val world = "world"
    m(hello) = world
    val h = m.getOrElse(hello, "Failed !")
    assert(h equals world)
  }

  val fptrBoxed: CFuncPtr0[Integer] = new CFuncPtr0[Integer] {
    def apply() = new Integer(1)
  }
  val fptr: CFuncPtr0[CInt] = new CFuncPtr0[CInt] { def apply() = 1 }
  val fptrFloat: CFuncPtr0[CFloat] = new CFuncPtr0[CFloat] {
    def apply() = 1.0.toFloat
  }
  val fptrDouble: CFuncPtr0[CDouble] = new CFuncPtr0[CDouble] {
    def apply() = 1.0
  }
  def intIdent(x: Int): Int = x
  test("#382") {
    /// that gave NPE

    import scala.scalanative.unsafe._
    intIdent(fptr())
    assert(fptr() == 1)

    // Reported issue
    assert(fptr() == 1)
    assert(fptrFloat() == 1.0)
    assert(fptrBoxed() == new Integer(1))

    // Other variations which must work as well
    val x1 = fptr()
    assert(x1 == 1)
    val x2 = fptrFloat()
    assert(x2 == 1.0)
  }

  test("#404") {
    // this must not throw an exception
    this.getClass.##
  }

  test("#424") {
    // this used not to link
    val cls = classOf[Array[Unit]]
  }

  test("#445") {
    val char: Any   = 66.toChar
    val byte: Any   = 66.toByte
    val short: Any  = 66.toShort
    val int: Any    = 66.toInt
    val long: Any   = 66.toLong
    val float: Any  = 66.toFloat
    val double: Any = 66.toDouble
    assert(char == char)
    assert(char == byte)
    assert(char == short)
    assert(char == int)
    assert(char == long)
    assert(char == float)
    assert(char == double)
    assert(byte == char)
    assert(byte == byte)
    assert(byte == short)
    assert(byte == int)
    assert(byte == long)
    assert(byte == float)
    assert(byte == double)
    assert(short == char)
    assert(short == byte)
    assert(short == short)
    assert(short == int)
    assert(short == long)
    assert(short == float)
    assert(short == double)
    assert(int == char)
    assert(int == byte)
    assert(int == short)
    assert(int == int)
    assert(int == long)
    assert(int == float)
    assert(int == double)
    assert(long == char)
    assert(long == byte)
    assert(long == short)
    assert(long == int)
    assert(long == long)
    assert(long == float)
    assert(long == double)
    assert(float == char)
    assert(float == byte)
    assert(float == short)
    assert(float == int)
    assert(float == long)
    assert(float == float)
    assert(float == double)
    assert(double == char)
    assert(double == byte)
    assert(double == short)
    assert(double == int)
    assert(double == long)
    assert(double == float)
    assert(double == double)
  }

  test("#449") {
    import scalanative.unsafe.Ptr
    import scala.scalanative.runtime.ByteArray
    val bytes = new Array[Byte](2)
    bytes(0) = 'b'.toByte
    bytes(1) = 'a'.toByte
    val p: Ptr[Byte] = bytes.asInstanceOf[ByteArray].at(0)
    assert(!p == 'b'.toByte)
    assert(!(p + 1) == 'a'.toByte)
  }

  test("#349") {
    var events = List.empty[String]

    def log(s: String): Unit = events ::= s

    def throwExc(): Unit =
      throw new Exception

    def foo(): Unit = {
      try {
        try {
          throwExc()
        } catch {
          case e: Throwable =>
            log("a")
        }

        try {
          throw new Exception
        } catch {
          case e: IllegalArgumentException =>
            log("b")
        }

      } catch {
        case e: Throwable =>
          log("c")
      }
    }

    assert(events.isEmpty)
    foo()
    assert(events == List("c", "a"))
  }

  test("#482") {
    assert('\uD800'.toInt == 55296)
  }

  test("#667") {
    val map = new java.util.HashMap[Int, Int]
    map.put(1, 2)
    val ks = map.keySet()
    assert(ks.contains(1))
  }

  test("#679") {
    val `"` = 42
    assert(("double-quotes " + `"`) == "double-quotes 42")
    assert(s"double-quotes ${`"`}" == "double-quotes 42")
  }

  test("#695") {
    val a   = List(1, 2, 3)
    var eff = List.empty[(Int, Int)]

    val result = a.corresponds(a) { (x, y) =>
      eff ::= ((x, y))
      true
    }

    assert(eff == List((3, 3), (2, 2), (1, 1)))
  }

  test("#762") {
    val byte         = 1.toByte
    val negbyte: Any = -byte
    assert(negbyte.isInstanceOf[Int])
    assert(negbyte.toString == "-1")

    val short         = 1.toByte
    val negshort: Any = -short
    assert(negshort.isInstanceOf[Int])
    assert(negshort.toString == "-1")

    val int         = 1
    val negint: Any = -int
    assert(negint.isInstanceOf[Int])
    assert(negint.toString == "-1")

    val long         = 1L
    val neglong: Any = -long
    assert(neglong.isInstanceOf[Long])
    assert(neglong.toString == "-1")
  }

  test("#780") {
    import java.util.{HashMap, Collections}
    val hashmap = new HashMap[String, String]()
    hashmap.put("a", "b")
    val frozen = Collections.unmodifiableMap[String, String](hashmap)
    val iter   = frozen.entrySet().iterator()
    val ab     = iter.next()
    assert(ab.getKey() == "a")
    assert(ab.getValue() == "b")
    assert(!iter.hasNext())
  }

  test("#803") {
    val x1: String = null
    var x2: String = "right"
    assert(x1 + x2 == "nullright")

    val x3: String = "left"
    val x4: String = null
    assert(x3 + x4 == "leftnull")

    val x5: AnyRef = new { override def toString = "custom" }
    val x6: String = null
    assert(x5 + x6 == "customnull")

    val x7: String = null
    val x8: AnyRef = new { override def toString = "custom" }
    assert(x7 + x8 == "nullcustom")

    val x9: String  = null
    val x10: String = null
    assert(x9 + x10 == "nullnull")

    val x11: AnyRef = null
    val x12: String = null
    assert(x11 + x12 == "nullnull")

    val x13: String = null
    val x14: AnyRef = null
    assert(x13 + x14 == "nullnull")
  }

  test("#809") {
    assert(null.asInstanceOf[AnyRef].## == 0)
  }

  test("#900") {
    val c = new issue900.C("any")
    assert(c.init == "foobar")
  }

  test("#1155") {
    assert(issue1155.C.CLASS.toString.contains("C$CLASS$@"))
  }

  test("#1090") {
    val xs = new Array[issue1090.X](20)
    val ys = new Array[issue1090.Y](20)
    assert(issue1090.A.foo(xs) == "X array")
    assert(issue1090.A.foo(ys) == "Y array")
  }

  test("#1239") {
    val ulong = java.lang.Long.parseUnsignedLong("9223372036854775808").toULong
    assert(ulong.toDouble == 9223372036854775808.0D)
  }

  test("#1359") {
    issue1359.Main.main(Array())
  }

  test("#1516") {
    locally {
      val data = new Array[UByte](6)
      data(0) = 64.toUByte
      assert(data(0).getClass == classOf[UByte])
      assert(data(0).toString == "64")
    }
    locally {
      val data = new Array[UShort](6)
      data(0) = 64.toUShort
      assert(data(0).getClass == classOf[UShort])
      assert(data(0).toString == "64")
    }
    locally {
      val data = new Array[UInt](6)
      data(0) = 64.toUInt
      assert(data(0).getClass == classOf[UInt])
      assert(data(0).toString == "64")
    }
    locally {
      val data = new Array[ULong](6)
      data(0) = 64.toULong
      assert(data(0).getClass == classOf[ULong])
      assert(data(0).toString == "64")
    }
  }
}

package issue1090 {
  class X
  class Y
  object A {
    def foo(a: Array[X]) = "X array"
    def foo(a: Array[Y]) = "Y array"
  }
}

package issue1155 {
  trait C {
    def foo = "bar"
  }

  object C {
    object CLASS extends C
  }
}

package issue900 {
  class C(any: Any) {
    def init: Any = "foobar"
  }
}

package issue1359 {
  object Main {
    def f[T]: T = throw new Exception

    def f2[T](a: => T) = ()

    def main(args: Array[String]): Unit = f2(f)
  }
}
