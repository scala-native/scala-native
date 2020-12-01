// Ported from Scala.js commit: 3786783 dated: 2019-10-11

package org.scalanative.testsuite.javalib.util

import java.{util => ju}

import scala.reflect.ClassTag

abstract class AbstractMapTest extends MapTest {
  def factory: AbstractMapFactory
}

abstract class AbstractMapFactory extends MapFactory {
  def implementationName: String

  def empty[K: ClassTag, V: ClassTag]: ju.AbstractMap[K, V]
}
