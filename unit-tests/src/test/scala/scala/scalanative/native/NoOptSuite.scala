package scala.scalanative.native

object NoOptSuite extends tests.Suite {
  class A { def foo: String = "A" }
  class B extends A { override def foo: String = "B" }
  class C extends A { override def foo: String = "C" }

  @noinline def noinl: A         = new B
  @nooptimize def noopt: A       = new B
  @nospecialize def nospec(x: A) = x

  test("no inline") {
    assert(noinl.foo == "B")
  }

  test("no optimize") {
    assert(noopt.foo == "B")
  }

  test("no specialize") {
    assert(nospec(new B).foo == "B")
  }
}
