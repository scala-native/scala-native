// Ported from Scala.js commit: a6c1451 dated: 2021-10-16

package org.scalanative.testsuite.javalib.util

import java.{util => ju}

import scala.reflect.ClassTag

class HashSetTest extends AbstractSetTest {
  def factory: HashSetFactory = new HashSetFactory
}

class HashSetFactory extends AbstractSetFactory {
  def implementationName: String =
    "java.util.HashSet"

  def empty[E: ClassTag]: ju.HashSet[E] =
    new ju.HashSet[E]()
}
