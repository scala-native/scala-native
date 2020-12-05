// Ported from Scala.js commit: 222e14c dated: 2019-09-11

package org.scalanative.testsuite.javalib.util

import scala.language.implicitConversions

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
