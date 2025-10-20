package scala.scalanative.nir

import org.junit.Test
import org.junit.Assert.*

class TypesSuite {
  @Test def pointerBoxTypes(): Unit = {
    Type.boxesTo.foreach {
      case (boxed: Type.Ref, Type.Ptr) =>
        assertTrue(s"$boxed should be Type.Ptr", Type.isPtrBox(boxed))
      case (boxed: Type.Ref, _) =>
        assertTrue(s"$boxed should be primitive type", !Type.isPtrBox(boxed))
      case (ty, _) =>
        fail(s"Expected reference boxed type, but got ${ty}")
    }
  }

  @Test def nonPointerBoxType(): Unit = {
    assertFalse(Type.isPtrBox(Type.Ref(Global.Top("foo.bar"))))
  }

}
