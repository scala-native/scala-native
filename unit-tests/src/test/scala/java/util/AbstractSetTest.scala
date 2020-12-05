// Ported from Scala.js commit: 434b8ce dated: 2019-08-19


package org.scalanative.testsuite.javalib.util

import java.{util => ju}

import scala.reflect.ClassTag

abstract class AbstractSetTest extends SetTest {
  def factory: AbstractSetFactory
}

trait AbstractSetFactory extends SetFactory {
  def empty[E: ClassTag]: ju.AbstractSet[E]
}
