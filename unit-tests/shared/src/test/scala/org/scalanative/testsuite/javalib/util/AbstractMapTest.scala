// Ported from Scala.js commit: 2253950 dated: 2022-10-02

/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package org.scalanative.testsuite.javalib.util

import java.util as ju

import scala.reflect.ClassTag

abstract class AbstractMapTest extends MapTest {
  def factory: AbstractMapFactory
}

abstract class AbstractMapFactory extends MapFactory {
  def implementationName: String

  def empty[K: ClassTag, V: ClassTag]: ju.AbstractMap[K, V]
}
