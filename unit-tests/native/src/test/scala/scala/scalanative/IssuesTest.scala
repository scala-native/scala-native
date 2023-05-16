package scala.scalanative

import org.junit.Test
import org.junit.Assert._
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import scalanative.unsigned._
import scalanative.unsafe._
import scala.annotation.nowarn
import scala.scalanative.annotation.alwaysinline

import scala.language.higherKinds

class IssuesTest {

  def foo(arg: Int): Unit = ()
  def crash(arg: CFuncPtr1[Int, Unit]): Unit = ()
  def lifted208Test(): Unit =
    crash((_: Int) => ())

  @Test def test_Issue208(): Unit = {
    // If we put the test directly, behind the scenes, this will
    // create a nested closure with a pointer to the outer one
    // and the latter is not supported in scala-native
    lifted208Test()
  }

  @Test def test_Issue253(): Unit = {
    class Cell(val value: Int)

    val arr = Array(new Cell(1), new Cell(2))
    val reverse = arr.reverse

    assertTrue(reverse(0).value == 2)
    assertTrue(reverse(1).value == 1)
  }

  @Test def test_Issue260(): Unit = {
    def getStr(): String = {
      val bytes = Array('h'.toByte, 'o'.toByte, 'l'.toByte, 'a'.toByte)
      new String(bytes)
    }

    val sz = getStr()

    assertTrue("hola" == sz)
    assertTrue("hola".equals(sz))
  }

  @Test def test_Issue275(): Unit = {
    val arr = new Array[Int](10)
    assertTrue(arr.getClass.getName == "scala.scalanative.runtime.IntArray")
    assertTrue(arr.toList == List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0))

    val arr2 = arr.map(_ + 1)
    assertTrue(arr2.getClass.getName == "scala.scalanative.runtime.IntArray")
    assertTrue(arr2.toList == List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1))
  }

  @Test def test_Issue314(): Unit = {
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

  @Test def test_Issue326(): Unit = {
    abstract class A
    case class S[T](a: T) extends A

    def check(a: A) = a match {
      case S(d: Double) => "double"
      case S(d: Int)    => "int"
      case _            => "neither"
    }

    def main(args: Array[String]): Unit = {
      val (dbl, int, obj) = (S(2.3), S(2), S(S(2)))
      assertTrue(check(dbl) == "double")
      assertTrue(check(int) == "int")
      assertTrue(check(obj) == "neither")
    }
  }

  @Test def test_Issue327(): Unit = {
    val a = BigInt(1)
    assertTrue(a.toInt == 1)
  }

  @Test def test_Issue337(): Unit = {
    case class TestObj(value: Int)
    val obj = TestObj(10)
    assertTrue(obj.value == 10)
  }

  @Test def test_Issue350(): Unit = {
    val div = java.lang.Long.divideUnsigned(42L, 2L)
    assertTrue(div == 21L)
  }

  @Test def test_Issue374(): Unit = {
    assertTrue("42" == bar(42))
    assertTrue("bar" == bar_i32())
  }

  def bar(i: Int): String = i.toString
  def bar_i32(): String = "bar"

  @Test def test_Issue376(): Unit = {
    val m = scala.collection.mutable.Map.empty[String, String]
    val hello = "hello"
    val world = "world"
    m(hello) = world
    val h = m.getOrElse(hello, "Failed !")
    assertTrue(h equals world)
  }

  @deprecated val fptrBoxed: CFuncPtr0[Integer] = () => new Integer(1)
  val fptr: CFuncPtr0[CInt] = () => 1
  val fptrFloat: CFuncPtr0[CFloat] = () => 1.0f
  val fptrDouble: CFuncPtr0[CDouble] = () => 1.0
  def intIdent(x: Int): Int = x

  @deprecated @Test def test_Issue382(): Unit = {
    /// that gave NPE

    import scala.scalanative.unsafe._
    intIdent(fptr())
    assertTrue(fptr() == 1)

    // Reported issue
    assertTrue(fptr() == 1)
    assertTrue(fptrFloat() == 1.0)
    assertTrue(fptrBoxed() == new Integer(1))

    // Other variations which must work as well
    val x1 = fptr()
    assertTrue(x1 == 1)
    val x2 = fptrFloat()
    assertTrue(x2 == 1.0)
  }

  @Test def test_Issue404(): Unit = {
    // this must not throw an exception
    this.getClass.##
  }

  @Test def test_Issue424(): Unit = {
    // this used not to link
    val cls = classOf[Array[Unit]]
  }

  @Test def test_Issue445(): Unit = {
    val char: Any = 66.toChar
    val byte: Any = 66.toByte
    val short: Any = 66.toShort
    val int: Any = 66.toInt
    val long: Any = 66.toLong
    val float: Any = 66.toFloat
    val double: Any = 66.toDouble
    assertTrue(char == char)
    assertTrue(char == byte)
    assertTrue(char == short)
    assertTrue(char == int)
    assertTrue(char == long)
    assertTrue(char == float)
    assertTrue(char == double)
    assertTrue(byte == char)
    assertTrue(byte == byte)
    assertTrue(byte == short)
    assertTrue(byte == int)
    assertTrue(byte == long)
    assertTrue(byte == float)
    assertTrue(byte == double)
    assertTrue(short == char)
    assertTrue(short == byte)
    assertTrue(short == short)
    assertTrue(short == int)
    assertTrue(short == long)
    assertTrue(short == float)
    assertTrue(short == double)
    assertTrue(int == char)
    assertTrue(int == byte)
    assertTrue(int == short)
    assertTrue(int == int)
    assertTrue(int == long)
    assertTrue(int == float)
    assertTrue(int == double)
    assertTrue(long == char)
    assertTrue(long == byte)
    assertTrue(long == short)
    assertTrue(long == int)
    assertTrue(long == long)
    assertTrue(long == float)
    assertTrue(long == double)
    assertTrue(float == char)
    assertTrue(float == byte)
    assertTrue(float == short)
    assertTrue(float == int)
    assertTrue(float == long)
    assertTrue(float == float)
    assertTrue(float == double)
    assertTrue(double == char)
    assertTrue(double == byte)
    assertTrue(double == short)
    assertTrue(double == int)
    assertTrue(double == long)
    assertTrue(double == float)
    assertTrue(double == double)
  }

  @Test def test_Issue449(): Unit = {
    import scalanative.unsafe.Ptr
    import scala.scalanative.runtime.ByteArray
    val bytes = new Array[Byte](2)
    bytes(0) = 'b'.toByte
    bytes(1) = 'a'.toByte
    val p: Ptr[Byte] = bytes.at(0)
    assertEquals('b'.toByte, !p)
    assertEquals('a'.toByte, !(p + 1))
  }

  @Test def test_Issue349(): Unit = {
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

    assertTrue(events.isEmpty)
    foo()
    assertTrue(events == List("c", "a"))
  }

  @Test def test_Issue482(): Unit = {
    assertTrue('\uD800'.toInt == 55296)
  }

  @Test def test_Issue667(): Unit = {
    val map = new java.util.HashMap[Int, Int]
    map.put(1, 2)
    val ks = map.keySet()
    assertTrue(ks.contains(1))
  }

  @Test def test_Issue679(): Unit = {
    val `"` = 42
    assertTrue(("double-quotes " + `"`) == "double-quotes 42")
    assertTrue(s"double-quotes ${`"`}" == "double-quotes 42")
  }

  @Test def test_Issue695(): Unit = {
    val a = List(1, 2, 3)
    var eff = List.empty[(Int, Int)]

    val result = a.corresponds(a) { (x, y) =>
      eff ::= ((x, y))
      true
    }

    assertTrue(eff == List((3, 3), (2, 2), (1, 1)))
  }

  @Test def test_Issue762(): Unit = {
    val byte = 1.toByte
    val negbyte: Any = -byte
    assertTrue(negbyte.isInstanceOf[Int])
    assertTrue(negbyte.toString == "-1")

    val short = 1.toByte
    val negshort: Any = -short
    assertTrue(negshort.isInstanceOf[Int])
    assertTrue(negshort.toString == "-1")

    val int = 1
    val negint: Any = -int
    assertTrue(negint.isInstanceOf[Int])
    assertTrue(negint.toString == "-1")

    val long = 1L
    val neglong: Any = -long
    assertTrue(neglong.isInstanceOf[Long])
    assertTrue(neglong.toString == "-1")
  }

  @Test def test_Issue780(): Unit = {
    import java.util.{HashMap, Collections}
    val hashmap = new HashMap[String, String]()
    hashmap.put("a", "b")
    val frozen = Collections.unmodifiableMap[String, String](hashmap)
    val iter = frozen.entrySet().iterator()
    val ab = iter.next()
    assertTrue(ab.getKey() == "a")
    assertTrue(ab.getValue() == "b")
    assertFalse(iter.hasNext())
  }

  @Test def test_Issue809(): Unit = {
    assertTrue(null.asInstanceOf[AnyRef].## == 0)
  }

  @Test def test_Issue899(): Unit = {
    def giveNothing: Any = throw new RuntimeException()

    assertThrows(classOf[RuntimeException], giveNothing == true)
  }

  @Test def test_Issue900(): Unit = {
    val c = new issue900.C("any")
    assertTrue(c.init == "foobar")
  }

  @Test def test_Issue1155(): Unit = {
    assertTrue(issue1155.C.CLASS.toString.contains("C$CLASS$@"))
  }

  @Test def test_Issue1090(): Unit = {
    val xs = new Array[issue1090.X](20)
    val ys = new Array[issue1090.Y](20)
    assertTrue(issue1090.A.foo(xs) == "X array")
    assertTrue(issue1090.A.foo(ys) == "Y array")
  }

  @Test def test_Issue1239(): Unit = {
    val ulong = java.lang.Long.parseUnsignedLong("9223372036854775808").toULong
    assertTrue(ulong.toDouble == 9223372036854775808.0d)
  }

  @Test def test_Issue1359(): Unit = {
    issue1359.Main.main(Array())
  }

  @Test def test_Issue1516(): Unit = {
    locally {
      val data = new Array[UByte](6)
      data(0) = 64.toUByte
      assertTrue(data(0).getClass == classOf[UByte])
      assertTrue(data(0).toString == "64")
    }
    locally {
      val data = new Array[UShort](6)
      data(0) = 64.toUShort
      assertTrue(data(0).getClass == classOf[UShort])
      assertTrue(data(0).toString == "64")
    }
    locally {
      val data = new Array[UInt](6)
      data(0) = 64.toUInt
      assertTrue(data(0).getClass == classOf[UInt])
      assertTrue(data(0).toString == "64")
    }
    locally {
      val data = new Array[ULong](6)
      data(0) = 64.toULong
      assertTrue(data(0).getClass == classOf[ULong])
      assertTrue(data(0).toString == "64")
    }
  }

  @Test def test_Issue1909(): Unit = {
    import issue1909._
    assertNotNull(new RandomWrapper().nextInt())
  }

  @Test def test_Issue1950(): Unit = {
    import issue1950._
    List(new ValueClass(1.0f))
      .map(_.value)
      .foreach(assertEquals(1.0f, _, 0.00001))

    List(ValueClass2("hello"))
      .map(_.string)
      .foreach(assertEquals("hello", _))
  }

  @nowarn
  @Test def test_Issue2187(): Unit = {
    val args = List.empty[String]
    // In issue 2187 match with guards would not compile
    val res = "Hello, World!" match {
      case "Hello" if args.isEmpty  => "foo"
      case "Hello" if args.nonEmpty => "foo2"
      // With next line it compiled because it was not referencing default case
      // case "Hello" => "foo3"
      case "World" if args.nonEmpty   => "bar"
      case "World" if args.length > 3 => "bar2"
      case "World" if args.isEmpty    => "bar3"
      case "World"                    => "bar4"
      case _ if args != null          => "bar-baz"
      case _                          => "baz-bar"
    }
    assertNotNull(res)
  }

  @Test def test_Issue2504(): Unit = {
    // In issue 2504 accessing iterator of immutable Java collection, would lead to
    // infinite loop and segmentation fault.
    import java.util.Collections
    import java.util.Map.Entry
    Seq(
      Collections.EMPTY_MAP.entrySet().iterator(),
      Collections.EMPTY_MAP.keySet().iterator(),
      Collections.EMPTY_MAP.values().iterator(),
      Collections.EMPTY_SET.iterator(),
      Collections.EMPTY_LIST.iterator()
    ).zipWithIndex.foreach {
      case (iterator, idx) =>
        assertNotNull(idx.toString, iterator)
        assertFalse(idx.toString(), iterator.hasNext())
    }

    val value = "foo"
    val singletonMap = Collections.singletonMap(value, value)
    val singletonSet = Collections.singleton(value)
    val singletonList = Collections.singletonList(value)
    val unmodifiableMap =
      Collections.unmodifiableMap[String, String](singletonMap)
    val unmodifiableSet = Collections.unmodifiableSet(singletonSet)
    val unmodifiableList = Collections.unmodifiableList(singletonList)
    val unmodifiableCollection =
      Collections.unmodifiableCollection(singletonList)

    Seq(
      singletonMap.entrySet().iterator(),
      singletonMap.keySet().iterator(),
      singletonMap.values().iterator(),
      singletonSet.iterator(),
      singletonList.iterator(),
      unmodifiableMap.entrySet().iterator(),
      unmodifiableMap.keySet().iterator(),
      unmodifiableMap.values().iterator(),
      unmodifiableSet.iterator(),
      unmodifiableList.iterator(),
      unmodifiableCollection.iterator()
    ).zipWithIndex.foreach {
      case (iterator, idx) =>
        assertNotNull(idx.toString, iterator)
        assertTrue(idx.toString(), iterator.hasNext())
        iterator.next() match {
          case entry: Entry[String, String] @unchecked =>
            assertEquals(s"$idx key", value, entry.getKey())
            assertEquals(s"$idx value", value, entry.getValue())
          case v => assertEquals(idx.toString(), value, v)
        }
    }
  }

  @Test def test_Issue2519(): Unit = {
    import scala.scalanative.reflect.Reflect

    def isInstantiatableClass(fqcn: String) =
      Reflect.lookupInstantiatableClass(fqcn).isDefined
    def isLoadableModule(fqcn: String) =
      Reflect.lookupLoadableModuleClass(fqcn).isDefined

    assertTrue(
      "A should be instantiatable",
      isInstantiatableClass("scala.scalanative.issue2519.A")
    )
    assertFalse(
      "B should be not instantiatable",
      isInstantiatableClass("scala.scalanative.issue2519.B")
    )
    assertTrue(
      "C should be instantiatable",
      isInstantiatableClass("scala.scalanative.issue2519.C")
    )
    assertTrue(
      "D should be loadable",
      isLoadableModule("scala.scalanative.issue2519.D$")
    )
    assertTrue(
      "E should be loadable",
      isLoadableModule("scala.scalanative.issue2519.E$")
    )
  }

  @Test def test_Issue2520(): Unit = {
    import issue2520._
    import issue2520.Kleisli.KleisliOpt
    implicit def instance: Strong[Function1] = new Strong[Function1] {
      override def first[A, B, C](f: A => B): Function1[(A, C), (B, C)] = {
        case (a1, a2) => (f(a1), a2)
      }
    }

    assertNotNull {
      val res =
        implicitly[Strong[KleisliOpt]].law.test[Int, Int, Int, Int](x => x)
      println(res)
      res
    }
  }

  @Test def test_Issue2550(): Unit = {
    // The code was failing to link
    // There were 2 reasons:
    // 1. Incorrect number of arguments (tested in all cases)
    // 2. Incorrect return type j.l.Object instead of nir Unit (tested in Func)
    type Func = CFuncPtr2[Ptr[Byte], Int, Unit]
    type Func1 = CFuncPtr2[Ptr[Byte], Int, Ptr[Byte]]
    type Func2 = CFuncPtr2[Ptr[Byte], Int, Int]
    type Func3 = CFuncPtr2[Ptr[Byte], Int, java.lang.Integer]

    val func0: Func = (handle: Ptr[Byte], i: Int) => ()
    val func1: Func1 = (handle: Ptr[Byte], i: Int) => handle
    val func2: Func2 = (handle: Ptr[Byte], i: Int) => i
    val func3: Func3 = (handle: Ptr[Byte], i: Int) =>
      java.lang.Integer.valueOf(i)

    val stub = stackalloc[Byte]()
    val n = 10

    assertEquals((), func0(stub, n))
    assertEquals(stub, func1(stub, n))
    assertEquals(n, func2(stub, n))
    assertEquals(n, func3(stub, n))
  }

  @Test def test_Issue2552() = {
    import issue2552._
    // Test that methods (lambdas) as reachable, issue reported missing symbols
    assertEquals("case 0", 0, issue2552.foo(0))
    assertEquals("case 1", 0, baz())
    assertEquals("case 2", 0, Bar.bar())
  }

  @Test def test_Issue2712() = {
    import issue2712._
    def f[A]: Refined[A] => Refined[A] =
      x => new Refined(x.value)

    def g: Refined[Byte] => Boolean =
      x => (x.value == 126.toByte)

    val x = new Refined[Byte](126.toByte)
    assertTrue(g(f(x)))
  }

  @Test def test_Issue2858() = {
    // In the reported issue symbols for scala.Nothing and scala.Null
    assertEquals("class scala.runtime.Nothing$", classOf[Nothing].toString())
    assertEquals("class scala.runtime.Null$", classOf[Null].toString())
  }

  @nowarn // nowarn does suppress warnings in Scala 2.13
  @Test def test_Issue2866() = {
    // In the issue the calls to malloc and srand would fail
    // becouse null would be passed to extern method taking unboxed type Size/Int
    import scala.scalanative.libc.stdlib.{malloc, free, srand}
    val ptr = malloc(null) // CSize -> RawSize should equal to malloc(0)
    free(ptr) // memory allocated by malloc(0) should always be safe to free
    srand(null) // CUnsignedInt -> Int should equal to srand(0UL)
    free(null)
  }

  @Test def `can initialize lazy vals using linktime if`() = {
    object Foo {
      val fooLiteral = "foo"
      lazy val fooLazy =
        if (scala.scalanative.meta.LinktimeInfo.isWindows) fooLiteral
        else fooLiteral
    }
    assertEquals(Foo.fooLiteral, Foo.fooLazy)
  }
  @Test def i3147(): Unit = {
    // It's not a runtime, but linktime bug related to nir.Show new lines escapes for string literals
    println("$\n")
  }

  @Test def i3195(): Unit = {
    // Make sure that inlined calls are resetting the stack upon returning
    // Otherwise calls to functions allocating on stack in loop might lead to stack overflow
    @alwaysinline def allocatingFunction(): CSize = {
      import scala.scalanative.unsafe.{CArray, Nat}
      import Nat._
      def `64KB` = (64 * 1024).toUSize
      val chunk = stackalloc[Byte](`64KB`)
      assertNotNull("stackalloc was null", chunk)
      `64KB`
    }
    // 32MB, may more then available stack 1MB on Windows, < 8 MB on Unix
    val toAllocate = (32 * 1024 * 1024).toUSize
    var allocated = 0.toUSize
    while (allocated < toAllocate) {
      allocated += allocatingFunction()
    }
  }

  @Test def issue3196(): Unit = {
    object ctx {
      type Foo
    }
    val ptr1 = stackalloc[Ptr[ctx.Foo]]()
    println(!ptr1) // segfault

    val ptr2 = stackalloc[Ptr[_]]()
    println(!ptr2) // segfault
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

package issue1909 {
  import java.util.Random
  class RandomWrapper(impl: Random = new Random()) extends Random {
    def this(seed: Long) = {
      this()
    }
  }
}

package issue1950 {
  final class ValueClass(val value: Float) extends AnyVal
  final case class ValueClass2(string: String) extends AnyVal
}

package issue2519 {
  import scala.scalanative.reflect.Reflect

  @scala.scalanative.reflect.annotation.EnableReflectiveInstantiation
  class A
  abstract class B extends A
  class C extends B
  object D extends A
  object E extends B
}

package issue2520 {
  case class Kleisli[F[_], A, B](run: A => F[B])

  object Kleisli {
    type KleisliOpt[A, B] = Kleisli[Option, A, B]

    implicit def instance: Strong[KleisliOpt] = new Strong[KleisliOpt] {
      override def first[A, B, C](
          f: KleisliOpt[A, B]
      ): KleisliOpt[(A, C), (B, C)] =
        Kleisli[Option, (A, C), (B, C)] {
          case (a1, a2) => f.run(a1).map(_ -> a2)
        }
    }
  }

  trait Strong[F[_, _]] {
    def first[A, B, C](fa: F[A, B]): F[(A, C), (B, C)]

    trait Law {
      def test[A, B, C, D](f: C => D)(implicit PF: Strong[Function1]) = {
        val x: ((C, B)) => (D, B) = PF.first[C, D, B](f)
      }
    }

    def law: Law = new Law {}
  }
}

package object issue2552 {
  def foo(a: Int, b: Int = 0): Int = 0
  val baz = () => foo(1)
  object Bar {
    val bar = () => foo(0)
  }
}

package object issue2712 {
  final class Refined[A](val value: A) extends AnyVal
}
