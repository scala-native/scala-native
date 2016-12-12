package scala

object SymbolSuite extends tests.Suite {
  def foo1: scala.Symbol = 'foo
  def foo2: scala.Symbol = scala.Symbol("foo")
  def bar: scala.Symbol  = 'bar

  test("symbols are interned") {
    assert(foo1 eq foo2)
  }

  test("symbols have names") {
    assert(foo1.name == "foo")
    assert(foo2.name == "foo")
    assert(bar.name == "bar")
  }

  test("symbol hash code is name hash code") {
    assert(foo1.hashCode == "foo".hashCode)
    assert(foo2.hashCode == "foo".hashCode)
    assert(bar.hashCode == "bar".hashCode)
  }

  test("symbol equality") {
    assert(foo1 == foo1)
    assert(foo2 == foo2)
    assert(foo1 == foo2)
    assert(foo2 == foo1)
    assert(foo1 != bar)
    assert(foo2 != bar)
    assert(bar != foo1)
    assert(bar != foo2)
    assert(bar == bar)
  }

  test("symbol unapply") {
    val scala.Symbol(name) = foo1
    assert(name == "foo")
  }
}
