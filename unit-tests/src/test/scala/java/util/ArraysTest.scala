package java.util

import org.junit.Test
import org.junit.Assert._

class ArraysTest {
  @Test def asList(): Unit = {
    val array = Array("a", "c")
    val list  = Arrays.asList(array: _*)
    array.update(1, "b")
    assertTrue(list.size() == 2)
    assertTrue(list.get(0) == "a")
    assertTrue(list.get(1) == "b")

    list.set(0, "1")
    assertTrue(array(0) == "1")
  }
}
