package org.scalanative.testsuite.javalib.util.jar

// Ported from Apache Harmony

import java.util.jar._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class AttributesNameTest {

  @Test def constructor(): Unit = {
    assertThrows(
      classOf[IllegalArgumentException],
      new Attributes.Name(
        "01234567890123456789012345678901234567890123456789012345678901234567890"
      )
    )
  }
}
