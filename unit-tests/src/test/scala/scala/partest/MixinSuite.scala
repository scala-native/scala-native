package scala
package partest

object MixinSuite extends tests.Suite {
  var messages: List[String] = Nil
  def log(msg: String)       = messages ::= msg
  def clear(): Unit          = messages = Nil
  def testEffects(name: String)(effects: String*)(body: => Unit): Unit =
    test(name) {
      clear()
      body
      assert(messages.reverse == effects)
    }

  // Test 1: "super" coming from mixins

  object Test1 {
    class A {
      def f = "A::f";
    }

    class B extends A {
      override def f = "B::f";
    }

    trait M1 extends A {
      override def f = "M1::" + super.f;
    }

    class C extends B with M1 {
      override def f = super[M1].f;
    }

    def test(): Unit = {
      val c = new C;
      log(c.f);
    }
  }

  // Test 2: qualified "super" inside of the host class

  object Test2 {
    class M1 {
      def f = "M1::f";
    }

    trait M2 {
      def f = "M2::f";
    }

    trait M3 {
      def f = "M3::f";
    }

    class Host extends M1 with M2 with M3 {
      override def f = super[M1].f + " " + super[M2].f + " " + super[M3].f
    }

    def test(): Unit = {
      val h = new Host;
      log(h.f)
    }
  }

  // Test 3: mixin evaluation order (bug 120)

  object Test3 {

    class A(x: Unit, y: Unit) {
      log("A");
    }

    trait B {
      log("B");
    }

    class C extends A({ log("one"); }, { log("two"); }) with B {
      log("C");
    }

    def test() = {
      val c = new C();
    }
  }

  // Actual tests

  testEffects("1")("M1::B::f") { Test1.test() }
  testEffects("2")("M1::f M2::f M3::f") { Test2.test() }
  testEffects("3")("one", "two", "A", "B", "C") { Test3.test() }
}
