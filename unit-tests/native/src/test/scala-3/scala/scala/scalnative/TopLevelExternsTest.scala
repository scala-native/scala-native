package scala.scalanative

import org.junit.Test
import org.junit.Assert._

import scalanative.unsafe._

var topLevelExternVariable: Int = extern
def topLevelExternFunction: Int = extern
@name("topLevelExternFunction2")
def topLevelExternFunction(n: Int): Int = extern

class TopLevelExternsTest {
  @Test def canAccessExternFunciton(): Unit = {
    assertEquals(42, topLevelExternFunction)
    assertEquals(84, topLevelExternFunction(2))
  }

  @Test def canAccessExternVariable(): Unit = {
    assertEquals(42, topLevelExternVariable)
  }

  @Test def canMutateExternVariable(): Unit = {
    val previousValue = topLevelExternVariable
    try {
      topLevelExternVariable = previousValue * 10
      assertEquals(previousValue * 10, topLevelExternVariable)
    } finally {
      topLevelExternVariable = previousValue
    }
  }
}
