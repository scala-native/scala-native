// Ported from Scala.js, commit: dff0db4, dated: 2022-04-01

package org.scalanative.testsuite.javalib.util

import java.util.UUID

import org.junit.Assert._
import org.junit.Test

/** Additional tests for `java.util.UUID` that require
 *  `java.security.SecureRandom`.
 */
class UUIDTestEx {

  @Test def randomUUID(): Unit = {
    val uuid1 = UUID.randomUUID()
    assertEquals(2, uuid1.variant())
    assertEquals(4, uuid1.version())

    val uuid2 = UUID.randomUUID()
    assertEquals(2, uuid2.variant())
    assertEquals(4, uuid2.version())

    assertNotEquals(uuid1, uuid2)
  }

}
