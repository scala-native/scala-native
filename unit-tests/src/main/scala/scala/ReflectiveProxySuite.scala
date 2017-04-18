package scala

// Ported from ScalaJS

object ReflectiveProxySuite extends tests.Suite {

  test("should allow subtyping in return types") {
    class A { def x: Int = 1 }
    class B extends A { override def x: Int = 2 }

    object Generator {
      def generate(): B = new B
    }

    def f(x: { def generate(): A }): A = x.generate

    assert(f(Generator).x == 2)
  }

  test("should allow this.type in return types") {
    type ValueType = { def value: this.type }
    def f(x: ValueType): ValueType = x.value

    class StringValue(x: String) {
      def value: this.type            = this
      override def toString(): String = s"StringValue($x)"
    }

    assert(f(new StringValue("foo")).toString == "StringValue(foo)")
  }

  test("should allow generic return types") {
    case class Tata(name: String)

    object Rec {
      def e(x: Tata): Tata = new Tata("iei")
    }

    def m[T](r: Object { def e(x: Tata): T }): T =
      r.e(new Tata("foo"))

    assert(m[Tata](Rec).toString == "Tata(iei)")
  }

  test("should work with unary methods on primitive types") {
    // scalastyle:off disallow.space.before.token
    def fInt(x: Any { def unary_- : Int }): Int = -x
    assert(fInt(1.toByte) == -1)
    assert(fInt(1.toShort) == -1)
    assert(fInt(1.toChar) == -1)
    assert(fInt(1) == -1)

    def fLong(x: Any { def unary_- : Long }): Long = -x
    assert(fLong(1L) == -1L)

    def fFloat(x: Any { def unary_- : Float }): Float = -x
    assert(fFloat(1.5f) == -1.5f)

    def fDouble(x: Any { def unary_- : Double }): Double = -x
    assert(fDouble(1.5) == -1.5)

    def fBoolean(x: Any { def unary_! : Boolean }): Boolean = !x
    assert(fBoolean(false))
    assert(!fBoolean(true))
    // scalastyle:on disallow.space.before.token
  }

  test("should work with binary operators on primitive types") {
    def fLong(x: Any { def +(x: Long): Long }): Long = x + 5L
    assert(fLong(5.toByte) == 10L)
    assert(fLong(10.toShort) == 15L)
    assert(fLong(10.toChar) == 15L)
    assert(fLong(-1) == 4L)
    assert(fLong(17L) == 22L)

    def fInt(x: Any { def /(x: Int): Int }): Int = x / 7
    assert(fInt(65.toByte) == 9)
    assert(fInt(15.toShort) == 2)
    assert(fInt(25.toChar) == 3)
    assert(fInt(-40) == -5)

    def fShort(x: Any { def +(x: Short): Int }): Int = x + 6.toShort
    assert(fShort(65.toByte) == 71)
    assert(fShort(15.toShort) == 21)
    assert(fShort(25.toChar) == 31)
    assert(fShort(-40) == -34)

    def fFloat(x: Any { def %(x: Float): Float }): Float = x % 3.4f
    assert(fFloat(5.5f) == 2.1f)

    def fDouble(x: Any { def /(x: Double): Double }): Double = x / 1.4
    assert(fDouble(-1.5) == -1.0714285714285714)

    def fBoolean(x: Any { def &&(x: Boolean): Boolean }): Boolean =
      x && true
    assert(!fBoolean(false))
    assert(fBoolean(true))
  }

  test("should work with equality operators on primitive types") {
    def fNum(obj: Any { def ==(x: Int): Boolean }): Boolean = obj == 5
    assert(fNum(5.toByte))
    assert(!fNum(6.toByte))
    assert(fNum(5.toShort))
    assert(!fNum(7.toShort))
    assert(fNum(5.toChar))
    assert(!fNum('r'))
    assert(fNum(5))
    assert(!fNum(-4))
    assert(fNum(5L))
    assert(!fNum(400L))
    assert(fNum(5.0f))
    assert(!fNum(5.6f))
    assert(fNum(5.0))
    assert(!fNum(7.9))
    def fBool(obj: Any { def ==(x: Boolean): Boolean }): Boolean = obj == false
    assert(!fBool(true))
    assert(fBool(false))

    def fNumN(obj: Any { def !=(x: Int): Boolean }): Boolean = obj != 5
    assert(!fNumN(5.toByte))
    assert(fNumN(6.toByte))
    assert(!fNumN(5.toShort))
    assert(fNumN(7.toShort))
    assert(!fNumN(5.toChar))
    assert(fNumN('r'))
    assert(!fNumN(5))
    assert(fNumN(-4))
    assert(!fNumN(5L))
    assert(fNumN(400L))
    assert(!fNumN(5.0f))
    assert(fNumN(5.6f))
    assert(!fNumN(5.0))
    assert(fNumN(7.9))
    def fBoolN(obj: Any { def !=(x: Boolean): Boolean }): Boolean =
      obj != false
    assert(fBoolN(true))
    assert(!fBoolN(false))
  }

  test("should work with Arrays") {
    type UPD   = { def update(i: Int, x: String): Unit }
    type APL   = { def apply(i: Int): String }
    type LEN   = { def length: Int }
    type CLONE = Any { def clone(): Object }

    def upd(obj: UPD, i: Int, x: String): Unit = obj.update(i, x)
    def apl(obj: APL, i: Int): String          = obj.apply(i)
    def len(obj: LEN): Int                     = obj.length
    def clone(obj: CLONE): Object              = obj.clone

    val x = Array("asdf", "foo", "bar")
    val y = clone(x).asInstanceOf[Array[String]]

    assert(len(x) == 3)
    assert(apl(x, 0) == "asdf")
    upd(x, 1, "2foo")
    assert(x(1) == "2foo")
    assert(y(1) == "foo")
  }

  test("should work with Arrays of primitive values") {
    type UPD   = { def update(i: Int, x: Int): Unit }
    type APL   = { def apply(i: Int): Int }
    type LEN   = { def length: Int }
    type CLONE = Any { def clone(): Object }

    def upd(obj: UPD, i: Int, x: Int): Unit = obj.update(i, x)
    def apl(obj: APL, i: Int): Int          = obj.apply(i)
    def len(obj: LEN): Int                  = obj.length
    def clone(obj: CLONE): Object           = obj.clone

    val x = Array(5, 2, 8)
    val y = clone(x).asInstanceOf[Array[Int]]

    assert(len(x) == 3)
    assert(apl(x, 0) == 5)
    upd(x, 1, 1000)
    assert(x(1) == 1000)
    assert(y(1) == 2)
  }

  test("should work with Strings") {
    def get(obj: { def codePointAt(str: Int): Int }): Int =
      obj.codePointAt(1)
    assert(get("Hi") == 'i'.toInt)

    def sub(x: { def substring(x: Int): AnyRef }): AnyRef = x.substring(5)
    assert(sub("asdfasdfasdf") == "sdfasdf")

    type LEN_A = { def length: Any }
    def lenA(x: LEN_A): Any = x.length
    assert(lenA("asdf") == 4)
  }

  test("should properly generate forwarders for inherited methods") {
    trait A {
      def foo: Int
    }

    abstract class B extends A

    class C extends B {
      def foo: Int = 1
    }

    def call(x: { def foo: Int }): Int = x.foo

    assert(call(new C) == 1)
  }

  test("should be bug-compatible with Scala/JVM for inherited overloads") {
    class Base {
      def foo(x: Option[Int]): String = "a"
    }

    class Sub extends Base {
      def foo(x: Option[String]): Int = 1
    }

    val sub = new Sub

    val x: { def foo(x: Option[Int]): Any } = sub
    assert(x.foo(Some(1)).asInstanceOf[Any] == 1) // here is the "bug"

    val y: { def foo(x: Option[String]): Any } = sub
    assert(y.foo(Some("hello")).asInstanceOf[Any] == 1)
  }

  test("should work on java.lang.Object.{ notify, notifyAll }") {
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

    assert(objNotifyTest(new A()) == 1)
  }

  test("should work on java.lang.Object.clone") {
    type ObjCloneLike = Any { def clone(): AnyRef }
    def objCloneTest(obj: ObjCloneLike): AnyRef = obj.clone()

    class B(val x: Int) extends Cloneable {
      override def clone(): AnyRef = super.clone()
    }

    val b      = new B(1)
    val bClone = objCloneTest(b).asInstanceOf[B]

    assert(!(b eq bClone))
    assert(bClone.x == 1)
  }

  test("should not work on scala.AnyRef.{ eq, ne, synchronized }") {
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

    assertThrows[java.lang.NoSuchMethodException](objEqTest(a1, a1))
    assertThrows[java.lang.NoSuchMethodException](objNeTest(a1, a2))
    assertThrows[java.lang.NoSuchMethodException](
      objSynchronizedTest(a1, "hello"))
  }

  class AnyValWithAnyRefPrimitiveMethods(val x: Int) extends AnyVal {
    def eq(that: AnyRef): Boolean  = (x + 1) == that
    def ne(that: AnyRef): Boolean  = (x + 1) != that
    def synchronized[T](f: T): Any = f + "there"
  }

  test("should work with { eq, ne, synchronized } on AnyVal") {
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

    assert(objEqTest(a, 6: Integer))
    assert(!objEqTest(a, 5: Integer))

    assert(!objNeTest(a, 6: Integer))
    assert(objNeTest(a, 5: Integer))

    assert("hellothere" == objSynchronizedTest(a, "hello"))
  }

  test("should work with default arguments") {
    def pimpIt(a: Int) = new {
      def foo(b: Int, c: Int = 1): Int = a + b + c
    }

    assert(pimpIt(1).foo(2) == 4)
    assert(pimpIt(2).foo(2, 4) == 8)
  }

  test("should unbox all types of arguments") {
    class Foo {
      def makeInt: Int          = 5
      def testInt(x: Int): Unit = assert(x == 5)

      def makeRef: Option[String]          = Some("hi")
      def testRef(x: Option[String]): Unit = assert(x == Some("hi"))
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

  test("NoSuchMethodException with no dyn method") {
    class A
    def callFoo(obj: { def foo(): Int }) = obj.foo()
    assertThrows[java.lang.NoSuchMethodException] {
      callFoo((new A).asInstanceOf[{ def foo(): Int }])
    }
  }

  test("NoSuchMethodException with one dyn method") {
    class A {
      def bar(): Int = 42
    }
    def callBar(obj: { def bar(): Int }) = obj.bar()
    def callFoo(obj: { def foo(): Int }) = obj.foo()

    assert(callBar(new A()) == 42)

    assertThrows[java.lang.NoSuchMethodException] {
      callFoo((new A).asInstanceOf[{ def foo(): Int }])
    }
  }

  test("NoSuchMethodException with method defined in other class") {
    class A {
      def bar(): Int = 42
    }

    class B {
      def foo(): Int = 43
    }

    def callBar(obj: { def bar(): Int }) = obj.bar()
    def callFoo(obj: { def foo(): Int }) = obj.foo()

    assert(callBar(new A()) == 42)

    assertThrows[java.lang.NoSuchMethodException] {
      callFoo((new A).asInstanceOf[{ def foo(): Int }])
    }
  }

  test("issue #643 - return Nothing") {
    val foo: { def get: Int } = Some(42)
    assert(foo.get == 42)
  }
}
