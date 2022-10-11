package scala

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class Scala3_2_StdLibTest:
  // Usage of methods added in Scala 3.2
  // to make sure scalalib was compiled with correct sources
  @Test def canLinkScalaVersionSpecificMethods(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      scala.runtime.Scala3RunTime.nnFail()
    )

    val mirror = new scala.runtime.TupleMirror(2)
    assertNotNull(mirror.fromProduct((3, 2)))
  }
