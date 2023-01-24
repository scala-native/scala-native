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

  @Test def issue3063(): Unit = {
    import scala.scalanative.issue3063.types.*
    val ctx: Ptr[mu_Context] = stackalloc()
    // Check links
    (!ctx).text_width = CFuncPtr2.fromScalaFunction { (_, _) => 0 }
  }
}

object issue2485:
  object types:
    opaque type MyEnum = Int
    object MyEnum:
      given Tag[MyEnum] = Tag.materializeIntTag
end issue2485

object issue3063:
  object types:
    opaque type mu_Font = Ptr[Byte]
    object mu_Font:
      given Tag[mu_Font] = Tag.Ptr(Tag.Byte)

    opaque type mu_Context = Ptr[Byte]
    object mu_Context:
      given Tag[mu_Context] = Tag.Ptr(Tag.Byte)
      extension (struct: mu_Context)
        def text_width: CFuncPtr2[mu_Font, CString, CInt] = ???
        def text_width_=(value: CFuncPtr2[mu_Font, CString, CInt]): Unit = ()
