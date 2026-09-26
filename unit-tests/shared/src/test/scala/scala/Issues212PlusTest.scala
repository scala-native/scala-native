package scala

import org.junit.Assert._
import org.junit.Test

class Issues212PlusTest {
  @Test
  def testIssue2144(): Unit = {
    java.util.Arrays.sort(
      Array.empty[Object],
      (l: Object, r: Object) => 0
    )
  }
}
