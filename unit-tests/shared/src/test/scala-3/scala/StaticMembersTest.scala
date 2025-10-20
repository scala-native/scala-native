package scala

import scala.annotation.static
import org.junit.Test
import org.junit.Assert.*

private class Foo
private object Foo {
  @static def foo = "foo"
  @static def bar() = "bar"
  @static val fooBar = foo + bar()
  @static var fooBarBaz = ""
}

class StaticMembersTest:

  @Test def canCallScalaStaticMethods(): Unit = {
    assertEquals("foo", Foo.foo)
    assertEquals("bar", Foo.bar())
  }

  @Test def canAccessScalaStaticFields(): Unit = {
    assertEquals("foobar", Foo.fooBar)
    assertEquals("", Foo.fooBarBaz)
    Foo.fooBarBaz = "fooBarBaz"
    assertEquals("fooBarBaz", Foo.fooBarBaz)
  }
