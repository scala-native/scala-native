package scala.scalanative.runtime

import org.junit.Test
import org.junit.Assert._

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

}
