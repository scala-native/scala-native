package scala.scalanative

import org.junit.Test
import org.junit.Assert._
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import scalanative.unsigned._
import scalanative.unsafe._

class IssuesTestScala3 {
  @Test def issue2485(): Unit = {
    import scala.scalanative.issue2485.*
    import types.MyEnum

    assertEquals(Tag.materializeIntTag, summon[Tag[MyEnum]])
    // Check if compiles
    CFuncPtr1.fromScalaFunction[MyEnum, MyEnum] { a => a }
  }
}

object issue2485:
  object types:
    opaque type MyEnum = Int
    object MyEnum:
      given Tag[MyEnum] = Tag.materializeIntTag
end issue2485
