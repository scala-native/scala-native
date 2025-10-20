package org.scalanatvie.testsuite.scala.scalanative

import org.junit.Test
import org.junit.Assert.*

class IssuesTestScala3_4 {
  @Test def i4082(): Unit = {
    // Ensure links
    def test[I[_], O[_]](f: [C] => (I[C], I[C] => O[C]) => O[C]): Unit = ()
    assertEquals((), test[List, List]([C] => (input, cont) => List.empty))
  }
}
