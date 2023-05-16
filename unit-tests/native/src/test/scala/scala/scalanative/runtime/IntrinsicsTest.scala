package scala.scalanative.runtime

import org.junit.Test
import org.junit.Assert
import org.junit.Assert._
import scala.scalanative.meta.LinktimeInfo._
import scala.language.implicitConversions
import scala.scalanative.unsigned._
import scala.scalanative.unsafe.{Ptr, CArray, Size}
import scala.scalanative.unsafe.{CStruct1, CStruct2, CStruct3, CStruct4}

private object IntrinsicsTest {
  object sizeOfClassTypes {
    def outer = this
    class A()
    class B(a: Int = 0) { override def toString(): String = s"{$a}" }
    class C(a: Long = 0L) { override def toString(): String = s"{$a}" }
    class C2(a: Int = 0, b: Int = 0) {
      override def toString(): String = s"{$a,$b}"
    }
    class D(a: Int = 0, b: Long = 0L) {
      override def toString(): String = s"{$a,$b}"
    }
    class E(a: Int = 0, b: Long = 0, c: String = "") {
      override def toString(): String = s"{$a,$b,$c}"
    }
    object E extends E(0, 0, "") {
      val outerRef = outer
      assert(outerRef != null)
    }
    class F(a: String = "") {
      override def toString(): String = s"{$a}"
    }

    // Make sure each type and it's fields are reachable to prevent elimination of unused fields
    def init() =
      Seq(new A(), new B(), new C(), new C2(), new D(), new E(), E, new F())
        .map(_.toString())
        .foreach(e => assert(e != null))
  }
}

class IntrinsicsTest {

  @Test
  def allowsToAccessFieldPtr(): Unit = {
    class Foo(
        var longField: Long,
        var shortField: Short,
        var intField: Int,
        var byteField: Byte
    )

    val foo = new Foo(1L, 2.toShort, 42, 4.toByte)
    val fieldPtr = fromRawPtr[Int](Intrinsics.classFieldRawPtr(foo, "intField"))
    assertEquals(42, !fieldPtr)
    !fieldPtr = 13
    assertEquals(13, foo.intField)
    // Ensure other fields were not mutated
    assertEquals(1L, foo.longField)
    assertEquals(2.toShort, foo.shortField)
    assertEquals(4.toByte, foo.byteField)
  }

  @Test
  def allowsToAccessInheritedFieldPtr(): Unit = {
    class Bar {
      var bar = 42
    }
    class Foo(
        var longField: Long,
        var shortField: Short,
        var intField: Int,
        var byteField: Byte
    ) extends Bar

    val foo = new Foo(1L, 2.toShort, 3, 4.toByte)
    val fieldPtr = fromRawPtr[Int](Intrinsics.classFieldRawPtr(foo, "bar"))
    assertEquals(42, !fieldPtr)
    !fieldPtr = 13
    assertEquals(13, foo.bar)
    // Ensure other fields were not mutated
    assertEquals(1L, foo.longField)
    assertEquals(2.toShort, foo.shortField)
    assertEquals(3, foo.intField)
    assertEquals(4.toByte, foo.byteField)
  }

  @Test
  def allowsToAccessInheritedFromTraitFieldPtr(): Unit = {
    trait Bar {
      var bar = 42
    }
    class Foo(
        var longField: Long,
        var shortField: Short,
        var intField: Int,
        var byteField: Byte
    ) extends Bar

    val foo = new Foo(1L, 2.toShort, 3, 4.toByte)
    val fieldPtr = fromRawPtr[Int](Intrinsics.classFieldRawPtr(foo, "bar"))
    assertEquals(42, !fieldPtr)
    !fieldPtr = 13
    assertEquals(13, foo.bar)
    // Ensure other fields were not mutated
    assertEquals(1L, foo.longField)
    assertEquals(2.toShort, foo.shortField)
    assertEquals(3, foo.intField)
    assertEquals(4.toByte, foo.byteField)
  }

  @Test def sizeOfTest(): Unit = {
    import Intrinsics.sizeOf
    def assertEquals(msg: => String, expected: Int, actual: RawSize): Unit =
      Assert.assertEquals(
        msg,
        expected,
        Intrinsics.castRawSizeToInt(actual)
      )
    val sizeOfPtr = Intrinsics.castRawSizeToInt(scalanative.runtime.sizeOfPtr)

    assertEquals("byte", 1, sizeOf[Byte])
    assertEquals("short", 2, sizeOf[Short])
    assertEquals("char", 2, sizeOf[Char])
    assertEquals("int", 4, sizeOf[Int])
    assertEquals("long", 8, sizeOf[Long])
    assertEquals("float", 4, sizeOf[Float])
    assertEquals("double", 8, sizeOf[Double])
    assertEquals("ptr", sizeOfPtr, sizeOf[Ptr[_]])
    assertEquals("ubyte", 1, sizeOf[UByte])
    assertEquals("ushort", 2, sizeOf[UShort])
    assertEquals("uint", 4, sizeOf[UInt])
    assertEquals("ulong", 8, sizeOf[ULong])
    assertEquals("size", sizeOfPtr, sizeOf[Size])
    assertEquals("usize", sizeOfPtr, sizeOf[USize])

    type S1 = CStruct1[Short]
    assertEquals("s1", 2, sizeOf[S1])

    type S2 = CStruct2[Byte, Short]
    assertEquals("s2", 4, sizeOf[S2])

    type S3 = CStruct4[Byte, Short, Int, Int]
    assertEquals("s3", 12, sizeOf[S3])

    @struct class SC1(val a: Short)
    assertEquals("sc1", 2, sizeOf[SC1])

    @struct class SC2(val a: Byte, val b: Short)
    assertEquals("sc2", 4, sizeOf[SC2])

    @struct class SC3(val a: Byte, val b: Short, val c: Int, val d: Int)
    assertEquals("sc3", 12, sizeOf[SC3])

    import scala.scalanative.unsafe.Nat._
    type A1 = CArray[Short, _1]
    assertEquals("a1", 2, sizeOf[A1])

    type A2 = CArray[Short, _2]
    assertEquals("a2", 4, sizeOf[A2])

    type A3 = CArray[Short, Digit2[_4, _2]]
    assertEquals("a3", 2 * 42, sizeOf[A3])

    type A4 = CArray[S3, Digit2[_4, _2]]
    assertEquals("a4", 12 * 42, sizeOf[A4])

    type C1 = CStruct3[S3, SC3, A4]
    assertEquals("c1", 12 + 12 + (12 * 42), sizeOf[C1])
  }

  @Test def sizeOfClassTest(): Unit = {
    import scala.scalanative.runtime.Intrinsics._
    import IntrinsicsTest.sizeOfClassTypes._
    init()

    implicit def rawSizeToInt(size: RawSize): Int = castRawSizeToInt(size)

    case class Entry(actualSize: Int, fieldsSize64: Int, fieldsSize32: Int)
    val SizeOfPtr = scalanative.runtime.sizeOfPtr: Int
    val ClassHeaderSize =
      if (isMultithreadingEnabled) 2 * SizeOfPtr
      else SizeOfPtr

    assertEquals(4, sizeOf[Int]: Int)
    assertEquals(8, sizeOf[Long]: Int)
    assertEquals(SizeOfPtr, sizeOf[Ptr[_]]: Int)

    for {
      Entry(actual, fieldsSize64, fieldsSize32) <- Seq(
        Entry(sizeOf[A], 0, 0),
        Entry(sizeOf[B], 8, 4),
        Entry(sizeOf[C], 8, 8),
        Entry(sizeOf[C2], 8, 8),
        Entry(sizeOf[D], 16, 12),
        Entry(sizeOf[E], 24, 16),
        Entry(sizeOf[E.type], 32, 20), // Fields of E + reference to outer
        // Size of each reference field should to sizeOf[Ptr]
        Entry(sizeOf[F], 8, 4)
      )
      fieldsSize = if (is32BitPlatform) fieldsSize32 else fieldsSize64
      expected = ClassHeaderSize + fieldsSize
    } assertEquals(
      s"fieldsSize=${fieldsSize64}/${fieldsSize32}, total=$expected",
      expected,
      actual
    )
  }

  @Test def alignmentOfTest(): Unit = {
    import Intrinsics.alignmentOf
    def assertEquals(msg: => String, expected: Int, actual: RawSize) =
      Assert.assertEquals(
        msg,
        expected,
        Intrinsics.castRawSizeToInt(actual)
      )
    val sizeOfPtr = Intrinsics.castRawSizeToInt(scalanative.runtime.sizeOfPtr)

    assertEquals("byte", 1, alignmentOf[Byte])
    assertEquals("short", 2, alignmentOf[Short])
    assertEquals("char", 2, alignmentOf[Char])
    assertEquals("int", 4, alignmentOf[Int])
    assertEquals("long", sizeOfPtr, alignmentOf[Long])
    assertEquals("float", 4, alignmentOf[Float])
    assertEquals("double", sizeOfPtr, alignmentOf[Double])
    assertEquals("ptr", sizeOfPtr, alignmentOf[Ptr[_]])
    assertEquals("ubyte", 1, alignmentOf[UByte])
    assertEquals("ushort", 2, alignmentOf[UShort])
    assertEquals("uint", 4, alignmentOf[UInt])
    assertEquals("ulong", sizeOfPtr, alignmentOf[ULong])
    assertEquals("size", sizeOfPtr, alignmentOf[Size])
    assertEquals("usize", sizeOfPtr, alignmentOf[USize])
    assertEquals("ref", sizeOfPtr, alignmentOf[java.lang.String])

    type S1 = CStruct1[Short]
    assertEquals("s1", 2, alignmentOf[S1])

    type S2 = CStruct2[Byte, Short]
    assertEquals("s2", 2, alignmentOf[S2])

    type S3 = CStruct4[Byte, Short, Int, Int]
    assertEquals("s3", 4, alignmentOf[S3])

    @struct class SC1(val b: Short)
    assertEquals("sc1", 2, alignmentOf[SC1])

    @struct class SC2(val a: Byte, val b: Short)
    assertEquals("sc2", 2, alignmentOf[SC2])

    @struct class SC3(val a: Byte, val b: Short, val c: Int, val d: Int)
    assertEquals("sc3", 4, alignmentOf[SC3])

    import scala.scalanative.unsafe.Nat._
    type A1 = CArray[Short, _1]
    assertEquals("a1", 2, alignmentOf[A1])

    type A2 = CArray[Short, _2]
    assertEquals("a2", 2, alignmentOf[A2])

    type A3 = CArray[Short, Digit2[_4, _2]]
    assertEquals("a3", 2, alignmentOf[A3])

    type A4 = CArray[S3, Digit2[_4, _2]]
    assertEquals("a4", 4, alignmentOf[A4])

    type C1 = CStruct3[S3, SC3, A4]
    assertEquals("c1", 4, alignmentOf[C1])
  }

}
