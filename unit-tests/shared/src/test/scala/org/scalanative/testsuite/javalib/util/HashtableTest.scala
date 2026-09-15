package org.scalanative.testsuite.javalib.util

import java.util._
import java.{util => ju}

import scala.reflect.ClassTag

import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class HashtableTest extends MapTest {
  // Runs the public-domain JSR166 generic Map TCK for Hashtable.
  def factory: HashtableFactory = new HashtableFactory

  @Test def putOnNullKeyOrValue(): Unit = {
    val t = new Hashtable[AnyRef, AnyRef]()
    assertThrows(classOf[NullPointerException], t.put(null, "value"))
    assertThrows(classOf[NullPointerException], t.put("key", null))
  }
}

class HashtableFactory extends MapFactory {
  override def implementationName: String =
    "java.util.Hashtable"

  override def empty[K: ClassTag, V: ClassTag]: ju.Hashtable[K, V] =
    new ju.Hashtable[K, V]

  override def allowsNullKeys: Boolean = false
  override def allowsNullValues: Boolean = false

  override def allowsNullKeysQueries: Boolean = false
  override def allowsNullValuesQueries: Boolean = false

  override def isConcurrent: Boolean = true

  override def removeWithNullValueThrows: Boolean = true

  override def mergeWithNullValueThrows: Boolean = false
}
