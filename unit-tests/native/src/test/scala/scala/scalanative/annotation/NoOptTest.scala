package scala.scalanative
package annotation

import org.junit.Test
import org.junit.Assert.*

class NoOptTest {
  class A { def foo: String = "A" }
  class B extends A { override def foo: String = "B" }
  class C extends A { override def foo: String = "C" }

  @noinline def noinl: A = new B
  @nooptimize def noopt: A = new B
  @nospecialize def nospec(x: A) = x

  @Test def noInline(): Unit = {
    assertTrue(noinl.foo == "B")
  }

  @Test def noOptimize(): Unit = {
    assertTrue(noopt.foo == "B")
  }

  @Test def noSpecialize(): Unit = {
    assertTrue(nospec(new B).foo == "B")
  }
}
