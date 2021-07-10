package scala.scalanative.nir

import org.scalatest.funsuite.AnyFunSuite

class TypesSuite extends AnyFunSuite {

  test("Determinate if type boxes pointer for known types") {
    Type.boxesTo.foreach {
      case (boxed: Type.Ref, Type.Ptr) =>
        assert(Type.isPtrBox(boxed), s"$boxed should be Type.Ptr")
      case (boxed: Type.Ref, _) =>
        assert(!Type.isPtrBox(boxed), s"$boxed should be primitive type")
      case (ty, _) =>
        fail(s"Expected reference boxed type, but got ${ty}")
    }
  }

  test("Unknown reference types are not PtrBox") {
    assert(!Type.isPtrBox(Type.Ref(Global.Top("foo.bar"))))
  }

}
