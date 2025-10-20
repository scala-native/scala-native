// Ported from Scala.js commit: bbf0314 dated: Mon, 13 Jun 2022

package org.scalanative.testsuite.javalib.util.concurrent

import java.util as ju

import org.scalanative.testsuite.javalib.util.MapFactory

import scala.reflect.ClassTag

trait ConcurrentMapFactory extends MapFactory {
  def empty[K: ClassTag, V: ClassTag]: ju.concurrent.ConcurrentMap[K, V]

  override def allowsNullValuesQueries: Boolean = false

  override def allowsNullKeysQueries: Boolean = false
}
