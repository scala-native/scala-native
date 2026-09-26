package org.scalanative.testsuite.javalib.lang

import java.lang._
// Because this test is in the java.lang package, an unqualified Double
// is a java.lang.Double. Prior art used unqualified Double freely,
// with that intent. Scala.js JDouble is introduced to minimize changes
// in ported Scala.js tests. Existing usages of unqualified Double
// are not changed. Joys of blending code bases.
import java.lang.{Double => JDouble}

import org.junit.Assert._
import org.junit.Test

class DoubleTestOnJDK20 {

  /* Scala Native additions for features added after Java 8.
   *
   * The Tests of this file are the same, mutatis mutandis, as those
   * of FloatTestOnJDK.scala. Keep them together in the same 'require-jdk20'
   * directory to aid comparison and maintenance.
   */

  /** Since: Java 12 */
  @Test def testDescribeConstable(): Unit = {
    val expected = JDouble.valueOf(1.618)
    val result = expected
      .describeConstable()
      .orElseGet(() => { fail("describeConstable is empty"); null })

    assertTrue(result.eq(expected)) // Require reference equality
  }

  /** Since: Java 12 */
  /** Requires reflection so can not presently (SN 0.5.12) be implemented on
   *  Scala Native.
   */
  //  @Test def testresolveConstantDesc (): Unit

  /** Since: Java 19 */
  @Test def testPrecision(): Unit = {
    assertEquals("PRECISION", 53, JDouble.PRECISION)
  }
}
