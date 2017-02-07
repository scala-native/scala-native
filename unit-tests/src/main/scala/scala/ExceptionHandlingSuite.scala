package scala

object ExceptionHandlingSuite extends tests.Suite {
  class A extends Exception
  class B extends Exception
  class C extends Exception

  def throwA() = throw new A
  def throwB() = throw new B
  def throwC() = throw new C

  test("1") {
    assert {
      try {
        try throwB()
        catch {
          case a: A => false
        }
      } catch {
        case b: B => true
      }
    }
  }

  test("2") {
    assert {
      try throwA()
      catch {
        case a: A => true
        case b: B => false
      }
    }
  }

  test("3") {
    assert {
      try throwB()
      catch {
        case a: A => false
        case b: B => true
      }
    }
  }
}
