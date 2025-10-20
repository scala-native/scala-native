package scala

import org.junit.Test
import org.junit.Assert.*

class EqualityTest {
  case class O(m: Int)

  @Test def caseClassEquality(): Unit = {
    assertTrue(O(5) == O(5))
  }

  @Test def nullEqualsNull(): Unit = {
    assertTrue((null: Object) == (null: Object))
  }

  @Test def nullDoesNotEqualObject(): Unit = {
    val obj = new Object
    assertTrue((null: Object) != obj)
  }

  @Test def objectDoesNotEqualNull(): Unit = {
    val obj = new Object
    assertTrue(obj != (null: Object))
  }

  @Test def notEqNullDoesNotCallEquals(): Unit = {
    var equalsCalled = false
    val obj = new Object {
      override def equals(other: Any) = {
        equalsCalled = true
        other.asInstanceOf[AnyRef] eq this
      }
    }
    assertTrue(obj != null)
    assertFalse(equalsCalled)
    val iamnull: Any = null
    assertTrue(obj != iamnull)
    assertTrue(equalsCalled)
  }
}
