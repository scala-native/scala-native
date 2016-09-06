object ExceptionHandling {
  class A extends Exception
  class B extends Exception
  class C extends Exception

  def throwA() = throw new A
  def throwB() = throw new B
  def throwC() = throw new C

  def main(args: Array[String]): Unit = {
    test1()
    test2()
    test3()
  }

  def test1() = assert {
    try {
      try throwB()
      catch {
        case a: A => false
      }
    } catch {
      case b: B => true
    }
  }

  def test2() = assert {
    try throwA()
    catch {
      case a: A => true
      case b: B => false
    }
  }

  def test3() = assert {
    try throwB()
    catch {
      case a: A => false
      case b: B => true
    }
  }
}
