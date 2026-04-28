/*
 * Ported from JSR-166 TCK tests and released to the public domain, as
 * explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * Modified for Scala Native.
 */

package org.scalanative.testsuite.javalib.util

import java.util.{AbstractMap, Map}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.javalib.util.concurrent.JSR166Test
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class EntryTest extends JSR166Test {
  private final val k1 = "1"
  private final val v1 = "a"
  private final val k2 = "2"
  private final val v2 = "b"

  @Test def testConstructor1(): Unit = {
    val e: Map.Entry[String, String] = new AbstractMap.SimpleEntry(k1, v1)
    assertEquals(k1, e.getKey())
    assertEquals(v1, e.getValue())
  }

  @Test def testConstructor2(): Unit = {
    val s: Map.Entry[String, String] =
      new AbstractMap.SimpleImmutableEntry(k1, v1)
    assertEquals(k1, s.getKey())
    assertEquals(v1, s.getValue())
  }

  @Test def testConstructor3(): Unit = {
    val e2: Map.Entry[String, String] = new AbstractMap.SimpleEntry(k1, v1)
    val e: Map.Entry[String, String] = new AbstractMap.SimpleEntry(e2)
    assertEquals(k1, e.getKey())
    assertEquals(v1, e.getValue())
  }

  @Test def testConstructor4(): Unit = {
    val s2: Map.Entry[String, String] =
      new AbstractMap.SimpleImmutableEntry(k1, v1)
    val s: Map.Entry[String, String] =
      new AbstractMap.SimpleImmutableEntry(s2)
    assertEquals(k1, s.getKey())
    assertEquals(v1, s.getValue())
  }

  @Test def testEquals(): Unit = {
    val e2: Map.Entry[String, String] = new AbstractMap.SimpleEntry(k1, v1)
    val e: Map.Entry[String, String] = new AbstractMap.SimpleEntry(e2)
    val s2: Map.Entry[String, String] =
      new AbstractMap.SimpleImmutableEntry(k1, v1)
    val s: Map.Entry[String, String] =
      new AbstractMap.SimpleImmutableEntry(s2)
    assertEquals(e2, e)
    assertEquals(e2.hashCode(), e.hashCode())
    assertEquals(s2, s)
    assertEquals(s2.hashCode(), s.hashCode())
    assertEquals(e2, s2)
    assertEquals(e2.hashCode(), s2.hashCode())
    assertEquals(e, s)
    assertEquals(e.hashCode(), s.hashCode())
  }

  @Test def testNotEquals(): Unit = {
    val e2: Map.Entry[String, String] = new AbstractMap.SimpleEntry(k1, v1)
    var e: Map.Entry[String, String] = new AbstractMap.SimpleEntry(k2, v1)
    assertFalse(e2.equals(e))
    e = new AbstractMap.SimpleEntry(k1, v2)
    assertFalse(e2.equals(e))
    e = new AbstractMap.SimpleEntry(k2, v2)
    assertFalse(e2.equals(e))

    val s2: Map.Entry[String, String] =
      new AbstractMap.SimpleImmutableEntry(k1, v1)
    var s: Map.Entry[String, String] =
      new AbstractMap.SimpleImmutableEntry(k2, v1)
    assertFalse(s2.equals(s))
    s = new AbstractMap.SimpleImmutableEntry(k1, v2)
    assertFalse(s2.equals(s))
    s = new AbstractMap.SimpleImmutableEntry(k2, v2)
    assertFalse(s2.equals(s))
  }

  @Test def testSetValue1(): Unit = {
    val e2: Map.Entry[String, String] = new AbstractMap.SimpleEntry(k1, v1)
    val e: Map.Entry[String, String] = new AbstractMap.SimpleEntry(e2)
    assertEquals(k1, e.getKey())
    assertEquals(v1, e.getValue())
    e.setValue(k2)
    assertEquals(k2, e.getValue())
    assertFalse(e2.equals(e))
  }

  @Test def testSetValue2(): Unit = {
    val s2: Map.Entry[String, String] =
      new AbstractMap.SimpleImmutableEntry(k1, v1)
    val s: Map.Entry[String, String] =
      new AbstractMap.SimpleImmutableEntry(s2)
    assertEquals(k1, s.getKey())
    assertEquals(v1, s.getValue())
    assertThrows(classOf[UnsupportedOperationException], s.setValue(k2))
  }
}
