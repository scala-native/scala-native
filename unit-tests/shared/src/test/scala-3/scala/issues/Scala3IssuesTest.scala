package scala.issues

import org.junit.Test
import org.junit.Assert._
import scala.scalanative.unsafe._

class Scala3IssuesTest:

  // Test itself does not have a large value, it does however assert that
  // usage of macros in the code, does not break compiler plugin
  @Test def canUseMacros(): Unit = {
    val result = Macros.test("foo")
    assertEquals(List(1, 2, 3), result)
  }

  @Test def issue2485(): Unit = {
    object testing:
      object types:
        opaque type MyEnum = Int
        object MyEnum:
          given Tag[MyEnum] = Tag.materializeIntTag
      import types.MyEnum

      assertEquals(Tag.materializeIntTag, summon[Tag[MyEnum]])
      // Check if compiles
      CFuncPtr1.fromScalaFunction[MyEnum, MyEnum] { a =>
        a
      }
    end testing
  }
