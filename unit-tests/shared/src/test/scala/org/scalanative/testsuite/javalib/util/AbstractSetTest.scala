// Ported from Scala.js commit: a6c1451 dated: 2021-10-16

package org.scalanative.testsuite.javalib.util

import java.{util => ju}

import scala.reflect.ClassTag

abstract class AbstractSetTest extends SetTest {
  def factory: AbstractSetFactory
}

trait AbstractSetFactory extends SetFactory {
  def empty[E: ClassTag]: ju.AbstractSet[E]
}
