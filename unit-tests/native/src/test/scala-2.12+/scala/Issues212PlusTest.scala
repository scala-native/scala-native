package scala

import org.junit.Test
import org.junit.Assert._

class Issues212PlusTest {
  @Test
  def testIssue2144(): Unit = {
    java.util.Arrays.sort(
      Array.empty[Object],
      (l: Object, r: Object) => 0
    )
  }
}
