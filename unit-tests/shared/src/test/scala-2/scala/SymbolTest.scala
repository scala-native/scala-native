package scala

import org.junit.Assert._
import org.junit.Test

class SymbolTest {
  def foo1: scala.Symbol = 'foo
  def foo2: scala.Symbol = scala.Symbol("foo")
  def bar: scala.Symbol = 'bar

  @Test def symbolsAreInterned(): Unit = {
    assertTrue(foo1 eq foo2)
  }

  @Test def symbolsHaveNames(): Unit = {
    assertTrue(foo1.name == "foo")
    assertTrue(foo2.name == "foo")
    assertTrue(bar.name == "bar")
  }

  @Test def symbolHashCodeIsNameHashCode(): Unit = {
    assertTrue(foo1.hashCode == "foo".hashCode)
    assertTrue(foo2.hashCode == "foo".hashCode)
    assertTrue(bar.hashCode == "bar".hashCode)
  }

  @Test def symbolEquality(): Unit = {
    assertTrue(foo1 == foo1)
    assertTrue(foo2 == foo2)
    assertTrue(foo1 == foo2)
    assertTrue(foo2 == foo1)
    assertTrue(foo1 != bar)
    assertTrue(foo2 != bar)
    assertTrue(bar != foo1)
    assertTrue(bar != foo2)
    assertTrue(bar == bar)
  }

  @Test def symbolUnapply(): Unit = {
    val scala.Symbol(name) = foo1
    assertTrue(name == "foo")
  }
}
