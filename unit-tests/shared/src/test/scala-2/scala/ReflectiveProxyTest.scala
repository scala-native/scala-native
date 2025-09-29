package scala

// Ported from ScalaJS

import scala.language.reflectiveCalls

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class AnyValWithAnyRefPrimitiveMethods(val x: Int) extends AnyVal {
  def eq(that: AnyRef): Boolean = (x + 1) == that
  def ne(that: AnyRef): Boolean = (x + 1) != that
  def synchronized[T](f: T): Any = f + "there"
}

class ReflectiveProxyTest {

  @Test def shouldAllowSubtypingInReturnTypes(): Unit = {
    class A { def x: Int = 1 }
    class B extends A { override def x: Int = 2 }

    object Generator {
      def generate(): B = new B
    }

    def f(x: { def generate(): A }): A = x.generate

    assertTrue(f(Generator).x == 2)
  }

  @Test def shouldAllowThisDotTypeInReturnTypes(): Unit = {
    type ValueType = { def value: this.type }
    def f(x: ValueType): ValueType = x.value

    class StringValue(x: String) {
      def value: this.type = this
      override def toString(): String = s"StringValue($x)"
    }

    assertTrue(f(new StringValue("foo")).toString == "StringValue(foo)")
  }

  @Test def shouldAllowGenericReturnTypes(): Unit = {
    case class Tata(name: String)

    object Rec {
      def e(x: Tata): Tata = new Tata("iei")
    }

    def m[T](r: Object { def e(x: Tata): T }): T =
      r.e(new Tata("foo"))

    assertTrue(m[Tata](Rec).toString == "Tata(iei)")
  }

  @Test def shouldWorkWithUnaryMethodsOnPrimitiveTypes(): Unit = {
    def fInt(x: Any { def unary_- : Int }): Int = -x
    assertTrue(fInt(1.toByte) == -1)
    assertTrue(fInt(1.toShort) == -1)
    assertTrue(fInt(1.toChar) == -1)
    assertTrue(fInt(1) == -1)

    def fLong(x: Any { def unary_- : Long }): Long = -x
    assertTrue(fLong(1L) == -1L)

    def fFloat(x: Any { def unary_- : Float }): Float = -x
    assertTrue(fFloat(1.5f) == -1.5f)

    def fDouble(x: Any { def unary_- : Double }): Double = -x
    assertTrue(fDouble(1.5) == -1.5)

    def fBoolean(x: Any { def unary_! : Boolean }): Boolean = !x
    assertTrue(fBoolean(false))
    assertFalse(fBoolean(true))
  }

  @Test def shouldWorkWithBinaryOperatorsOnPrimitiveTypes(): Unit = {
    def fLong(x: Any { def +(x: Long): Long }): Long = x + 5L
    assertTrue(fLong(5.toByte) == 10L)
    assertTrue(fLong(10.toShort) == 15L)
    assertTrue(fLong(10.toChar) == 15L)
    assertTrue(fLong(-1) == 4L)
    assertTrue(fLong(17L) == 22L)

    def fInt(x: Any { def /(x: Int): Int }): Int = x / 7
    assertTrue(fInt(65.toByte) == 9)
    assertTrue(fInt(15.toShort) == 2)
    assertTrue(fInt(25.toChar) == 3)
    assertTrue(fInt(-40) == -5)

    def fShort(x: Any { def +(x: Short): Int }): Int = x + 6.toShort
    assertTrue(fShort(65.toByte) == 71)
    assertTrue(fShort(15.toShort) == 21)
    assertTrue(fShort(25.toChar) == 31)
    assertTrue(fShort(-40) == -34)

    def fFloat(x: Any { def %(x: Float): Float }): Float = x % 3.4f
    assertTrue(fFloat(5.5f) == 2.1f)

    def fDouble(x: Any { def /(x: Double): Double }): Double = x / 1.4
    assertTrue(fDouble(-1.5) == -1.0714285714285714)

    def fBoolean(x: Any { def &&(x: Boolean): Boolean }): Boolean =
      x && true
    assertFalse(fBoolean(false))
    assertTrue(fBoolean(true))
  }

  @Test def shouldWorkWithEqualityOperatorsOnPrimitiveTypes(): Unit = {
    def fNum(obj: Any { def ==(x: Int): Boolean }): Boolean = obj == 5
    assertTrue(fNum(5.toByte))
    assertFalse(fNum(6.toByte))
    assertTrue(fNum(5.toShort))
    assertFalse(fNum(7.toShort))
    assertTrue(fNum(5.toChar))
    assertFalse(fNum('r'))
    assertTrue(fNum(5))
    assertFalse(fNum(-4))
    assertTrue(fNum(5L))
    assertFalse(fNum(400L))
    assertTrue(fNum(5.0f))
    assertFalse(fNum(5.6f))
    assertTrue(fNum(5.0))
    assertFalse(fNum(7.9))
    def fBool(obj: Any { def ==(x: Boolean): Boolean }): Boolean = obj == false
    assertFalse(fBool(true))
    assertTrue(fBool(false))

    def fNumN(obj: Any { def !=(x: Int): Boolean }): Boolean = obj != 5
    assertFalse(fNumN(5.toByte))
    assertTrue(fNumN(6.toByte))
    assertFalse(fNumN(5.toShort))
    assertTrue(fNumN(7.toShort))
    assertFalse(fNumN(5.toChar))
    assertTrue(fNumN('r'))
    assertFalse(fNumN(5))
    assertTrue(fNumN(-4))
    assertFalse(fNumN(5L))
    assertTrue(fNumN(400L))
    assertFalse(fNumN(5.0f))
    assertTrue(fNumN(5.6f))
    assertFalse(fNumN(5.0))
    assertTrue(fNumN(7.9))
    def fBoolN(obj: Any { def !=(x: Boolean): Boolean }): Boolean =
      obj != false
    assertTrue(fBoolN(true))
    assertFalse(fBoolN(false))
  }

  @Test def shouldWorkWithArrays(): Unit = {
    type UPD = { def update(i: Int, x: String): Unit }
    type APL = { def apply(i: Int): String }
    type LEN = { def length: Int }
    type CLONE = Any { def clone(): Object }

    def upd(obj: UPD, i: Int, x: String): Unit = obj.update(i, x)
    def apl(obj: APL, i: Int): String = obj.apply(i)
    def len(obj: LEN): Int = obj.length
    def clone(obj: CLONE): Object = obj.clone

    val x = Array("asdf", "foo", "bar")
    val y = clone(x).asInstanceOf[Array[String]]

    assertTrue(len(x) == 3)
    assertTrue(apl(x, 0) == "asdf")
    upd(x, 1, "2foo")
    assertTrue(x(1) == "2foo")
    assertTrue(y(1) == "foo")
  }

  @Test def shouldWorkWithArraysOfPrimitiveValues(): Unit = {
    type UPD = { def update(i: Int, x: Int): Unit }
    type APL = { def apply(i: Int): Int }
    type LEN = { def length: Int }
    type CLONE = Any { def clone(): Object }

    def upd(obj: UPD, i: Int, x: Int): Unit = obj.update(i, x)
    def apl(obj: APL, i: Int): Int = obj.apply(i)
    def len(obj: LEN): Int = obj.length
    def clone(obj: CLONE): Object = obj.clone

    val x = Array(5, 2, 8)
    val y = clone(x).asInstanceOf[Array[Int]]

    assertTrue(len(x) == 3)
    assertTrue(apl(x, 0) == 5)
    upd(x, 1, 1000)
    assertTrue(x(1) == 1000)
    assertTrue(y(1) == 2)
  }

  @Test def shouldWorkWithStrings(): Unit = {
    def get(obj: { def codePointAt(str: Int): Int }): Int =
      obj.codePointAt(1)
    assertTrue(get("Hi") == 'i'.toInt)

    def sub(x: { def substring(x: Int): AnyRef }): AnyRef = x.substring(5)
    assertTrue(sub("asdfasdfasdf") == "sdfasdf")

    type LEN_A = { def length: Any }
    def lenA(x: LEN_A): Any = x.length
    assertTrue(lenA("asdf") == 4)
  }

  @Test def shouldProperlyGenerateForwardersForInheritedMethods(): Unit = {
    trait A {
      def foo: Int
    }

    abstract class B extends A

    class C extends B {
      def foo: Int = 1
    }

    def call(x: { def foo: Int }): Int = x.foo

    assertTrue(call(new C) == 1)
  }

  @Test def shouldBeBugCompatibleWithScalaJVMForInheritedOverloads(): Unit = {
    class Base {
      def foo(x: Option[Int]): String = "a"
    }

    class Sub extends Base {
      def foo(x: Option[String]): Int = 1
    }

    val sub = new Sub

    val x: { def foo(x: Option[Int]): Any } = sub
    assertTrue(x.foo(Some(1)).asInstanceOf[Any] == 1) // here is the "bug"

    val y: { def foo(x: Option[String]): Any } = sub
    assertTrue(y.foo(Some("hello")).asInstanceOf[Any] == 1)
  }

  @Test def shouldWorkOnJavaLangObjectNotifyNotifyAll(): Unit = {
    type ObjNotifyLike = Any {
      def notify(): Unit
      def notifyAll(): Unit
    }
    def objNotifyTest(obj: ObjNotifyLike): Int = {
      obj.notify()
      obj.notifyAll()
      1
    }

    class A

    assertTrue(objNotifyTest(new A()) == 1)
  }

  @Test def shouldWorkOnJavaLangObjectClone(): Unit = {
    type ObjCloneLike = Any { def clone(): AnyRef }
    def objCloneTest(obj: ObjCloneLike): AnyRef = obj.clone()

    class B(val x: Int) extends Cloneable {
      override def clone(): AnyRef = super.clone()
    }

    val b = new B(1)
    val bClone = objCloneTest(b).asInstanceOf[B]

    assertFalse((b eq bClone))
    assertTrue(bClone.x == 1)
  }

  @Test def shouldNotWorkOnScalaAnyRefEqNeSynchronized(): Unit = {
    type ObjWithAnyRefPrimitives = Any {
      def eq(that: AnyRef): Boolean
      def ne(that: AnyRef): Boolean
      def synchronized[T](f: T): Any
    }

    def objEqTest(obj: ObjWithAnyRefPrimitives, that: AnyRef): Boolean =
      obj eq that
    def objNeTest(obj: ObjWithAnyRefPrimitives, that: AnyRef): Boolean =
      obj ne that
    def objSynchronizedTest(obj: ObjWithAnyRefPrimitives, f: String): Any =
      obj.synchronized(f)

    class A

    val a1 = new A
    val a2 = new A

    assertThrows(classOf[java.lang.NoSuchMethodException], objEqTest(a1, a1))
    assertThrows(classOf[java.lang.NoSuchMethodException], objNeTest(a1, a2))
    assertThrows(
      classOf[java.lang.NoSuchMethodException],
      objSynchronizedTest(a1, "hello")
    )
  }

  @Test def shouldWorkWithEqNeSynchronizedOnAnyVal(): Unit = {
    type ObjWithAnyRefPrimitives = Any {
      def eq(that: AnyRef): Boolean
      def ne(that: AnyRef): Boolean
      def synchronized[T](f: T): Any
    }

    def objEqTest(obj: ObjWithAnyRefPrimitives, that: AnyRef): Boolean =
      obj eq that
    def objNeTest(obj: ObjWithAnyRefPrimitives, that: AnyRef): Boolean =
      obj ne that
    def objSynchronizedTest(obj: ObjWithAnyRefPrimitives, f: String): Any =
      obj.synchronized(f)

    val a = new AnyValWithAnyRefPrimitiveMethods(5)

    assertTrue(objEqTest(a, 6: Integer))
    assertFalse(objEqTest(a, 5: Integer))

    assertFalse(objNeTest(a, 6: Integer))
    assertTrue(objNeTest(a, 5: Integer))

    assertTrue("hellothere" == objSynchronizedTest(a, "hello"))
  }

  @Test def shouldWorkWithDefaultArguments(): Unit = {
    def pimpIt(a: Int) = new {
      def foo(b: Int, c: Int = 1): Int = a + b + c
    }

    assertTrue(pimpIt(1).foo(2) == 4)
    assertTrue(pimpIt(2).foo(2, 4) == 8)
  }

  @Test def shouldUnboxAllTypesOfArguments(): Unit = {
    class Foo {
      def makeInt: Int = 5
      def testInt(x: Int): Unit = assertTrue(x == 5)

      def makeRef: Option[String] = Some("hi")
      def testRef(x: Option[String]): Unit = assertTrue(x == Some("hi"))
    }

    /* Note: we should also test with value classes, except that Scala itself
     * does not support value classes as parameters or result type of
     * methods in structural types.
     */

    def test(foo: {
      def makeInt: Int
      def testInt(x: Int): Unit
      def makeRef: Option[String]
      def testRef(x: Option[String]): Unit
    }): Unit = {
      foo.testInt(foo.makeInt)
      foo.testRef(foo.makeRef)
    }

    test(new Foo)
  }

  @Test def throwsNoSuchMethodExceptionWithNoDynMethod(): Unit = {
    class A
    def callFoo(obj: { def foo(): Int }) = obj.foo()
    assertThrows(
      classOf[java.lang.NoSuchMethodException],
      callFoo((new A).asInstanceOf[{ def foo(): Int }])
    )
  }

  @Test def throwsNoSuchMethodExceptionWithOneDynMethod(): Unit = {
    class A {
      def bar(): Int = 42
    }
    def callBar(obj: { def bar(): Int }) = obj.bar()
    def callFoo(obj: { def foo(): Int }) = obj.foo()

    assertTrue(callBar(new A()) == 42)

    assertThrows(
      classOf[java.lang.NoSuchMethodException],
      callFoo((new A).asInstanceOf[{ def foo(): Int }])
    )
  }

  @Test def throwsNoSuchMethodExceptionWithMethodDefinedInOtherClass(): Unit = {
    class A {
      def bar(): Int = 42
    }

    class B {
      def foo(): Int = 43
    }

    def callBar(obj: { def bar(): Int }) = obj.bar()
    def callFoo(obj: { def foo(): Int }) = obj.foo()

    assertTrue(callBar(new A()) == 42)

    assertThrows(
      classOf[java.lang.NoSuchMethodException],
      callFoo((new A).asInstanceOf[{ def foo(): Int }])
    )
  }

  @Test def returnNothingIssue643(): Unit = {
    val foo: { def get: Int } = Some(42)
    assertTrue(foo.get == 42)
  }
}
