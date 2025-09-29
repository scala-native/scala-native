package scala

import org.junit.Assert._
import org.junit.Test

class ExceptionHandlingTest {
  class A extends Exception
  class B extends Exception
  class C extends Exception

  def throwA() = throw new A
  def throwB() = throw new B
  def throwC() = throw new C

  @Test def test1(): Unit = {
    assertTrue {
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

  @Test def test2(): Unit = {
    assertTrue {
      try throwA()
      catch {
        case a: A => true
        case b: B => false
      }
    }
  }

  @Test def test3(): Unit = {
    assertTrue {
      try throwB()
      catch {
        case a: A => false
        case b: B => true
      }
    }
  }
}
