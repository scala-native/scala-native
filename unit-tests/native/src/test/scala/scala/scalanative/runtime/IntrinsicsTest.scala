package scala.scalanative.runtime

import org.junit.Test
import org.junit.Assert._
import scala.scalanative.meta.LinktimeInfo.is32BitPlatform
import scala.language.implicitConversions

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

  @Test def allowsToCalculateMemoryLayoutSize(): Unit = {
    case class Entry(actualSize: Int, fieldsSize64: Int, fieldsSize32: Int)
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
    object E extends E(0, 0, "")
    class F(a: String = "") {
      override def toString(): String = s"{$a}"
    }
    // Make sure each type and it's fields are reachable to prevent elimination of unused fields
    Seq(new A(), new B(), new C(), new C2(), new D(), new E(), E, new F())
      .map(_.toString())

    import scala.scalanative.runtime.Intrinsics._
    implicit def rawSizeToInt(size: RawSize): Int = castRawSizeToInt(size)

    assertEquals(4, sizeOf[Int]: Int)
    assertEquals(8, sizeOf[Long]: Int)
    assertNotEquals(
      scala.scalanative.unsafe.sizeof[String].toInt, // sizeOf[Ptr]
      scala.scalanative.runtime.Intrinsics
        .sizeOf[String]: Int // based on fields
    )

    val ClassHeaderSize = if (is32BitPlatform) 4 else 8
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
    } assertEquals(expected, actual)
  }

}
