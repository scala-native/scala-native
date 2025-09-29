// Ported from Dotty repo (test 'i4496b')

package scala.reflect

import scala.reflect.Selectable.reflectiveSelectable

import org.junit.Assert._
import org.junit.Test

trait Foo1 { val a: Int }
trait Foo2 { def a: Int }
trait Foo3 { var a: Int }

private class FooBar1 extends Foo1 { val a: Int = 10 }
private class FooBar2 extends Foo2 { def a: Int = 10 }
private class FooBar3 extends Foo3 { var a: Int = 10 }

private class Bar1 { val a: Int = 10 }
private class Bar2 { def a: Int = 10 }
private class Bar3 { var a: Int = 10 }

class StructuralTest() {
  // This test is also an (abstracted) motivating example.

  @Test def testStructuralVal(): Unit = {
    // Consider one module upcasting all these instances to T. These casts are clearly well-typed.
    type T = { def a: Int }
    def upcast1(v: Foo1): T = v
    def upcast2(v: Foo2): T = v
    def upcast3(v: Foo3): T = v

    // These accesses are also clearly well-typed
    def consume(v: T) = v.a
    inline def consumeInl(v: T) = v.a
    def verify(v: T) = {
      assertEquals(consume(v), 10)
      assertEquals(consumeInl(v), 10)
      assertEquals(v.a, 10)
    }

    // These calls are also clearly well-typed, hence can't be rejected.
    verify(upcast1(new Foo1 { val a = 10 }))
    verify(upcast2(new Foo2 { val a = 10 }))
    verify(upcast3(new Foo3 { var a = 10 }))
    // Ditto, so we must override access control to the class.
    verify(upcast1(new FooBar1))
    verify(upcast2(new FooBar2))
    verify(upcast3(new FooBar3))

    // Other testcases
    verify(new { val a = 10 }: T)
    verify(new { var a = 10 }: T)
    verify(new { def a = 10 }: T)

    verify(new Bar1: T)
    verify(new Bar2: T)
    verify(new Bar3: T)
  }

  @Test def testStructuralDef(): Unit = {
    type T = { def a: Int }
    def upcast1(v: Foo1): T = v
    def upcast2(v: Foo2): T = v
    def upcast3(v: Foo3): T = v
    def consume(v: T) = v.a
    inline def consumeInl(v: T) = v.a
    def verify(v: T) = {
      assertEquals(consume(v), 10)
      assertEquals(consumeInl(v), 10)
      assertEquals(v.a, 10)
    }

    verify(upcast1(new Foo1 { val a = 10 }))
    verify(upcast2(new Foo2 { val a = 10 }))
    verify(upcast3(new Foo3 { var a = 10 }))

    verify(upcast1(new FooBar1))
    verify(upcast2(new FooBar2))
    verify(upcast3(new FooBar3))

    verify(new { val a = 10 }: T)
    verify(new { var a = 10 }: T)
    verify(new { def a = 10 }: T)

    verify(new Bar1: T)
    verify(new Bar2: T)
    verify(new Bar3: T)
  }

  @Test def testStructuralVar(): Unit = {
    type T = { def a: Int; def a_=(x: Int): Unit }
    def upcast3(v: Foo3): T = v
    def consume(v: T) = v.a
    inline def consumeInl(v: T) = v.a
    def verify(v: T) = {
      assertEquals(consume(v), 10)
      assertEquals(consumeInl(v), 10)
      assertEquals(v.a, 10)
      // Pending, per https://github.com/lampepfl/dotty/issues/4528.
      // v.a = 11
      // assert(consume(v) == 11)
      // assert(consumeInl(v) == 11)
      // assert(v.a == 11)
    }

    verify(upcast3(new Foo3 { var a = 10 }))
    verify(upcast3(new FooBar3))
    verify(new { var a = 10 }: T)
    verify(new Bar3: T)
  }

}
