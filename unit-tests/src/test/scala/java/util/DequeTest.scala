// Ported from Scala.js commit: 434b8ce dated: 2019-08-19

package org.scalanative.testsuite.javalib.util

import java.{util => ju}

import scala.reflect.ClassTag

trait DequeTest extends CollectionTest {
  def factory: DequeFactory
}

trait DequeFactory extends CollectionFactory {
  def empty[E: ClassTag]: ju.Deque[E]
}
