package scala

import org.junit.Test
import org.junit.Assert._

class ObjectCloneTest {

  case class I(var i: Int) extends Cloneable {
    def copy(): I = this.clone().asInstanceOf[I]
  }

  @Test def cloneWithPrimitive(): Unit = {
    val obj = I(123)
    val clone = obj.copy()

    obj.i = 124

    assertTrue(obj.i == 124)
    assertTrue(clone.i == 123)
  }

  case class Arr(val arr: Array[Int]) extends Cloneable {
    def copy(): Arr = this.clone().asInstanceOf[Arr]
  }

  @Test def cloneWithRef(): Unit = {
    val obj = Arr(Array(123))
    val clone = obj.copy()

    obj.arr(0) = 124

    assertTrue(obj.arr(0) == 124)
    assertTrue(clone.arr(0) == 124)
  }

}
