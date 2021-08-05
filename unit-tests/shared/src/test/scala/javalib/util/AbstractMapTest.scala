package javalib.util

import java.util._

// Ported from Scala.js

import java.{util => ju}

import scala.reflect.ClassTag

abstract class AbstractMapTest extends MapTest {
  def factory: AbstractMapFactory
}

abstract class AbstractMapFactory extends MapFactory {
  def implementationName: String

  def empty[K: ClassTag, V: ClassTag]: ju.AbstractMap[K, V]
}
