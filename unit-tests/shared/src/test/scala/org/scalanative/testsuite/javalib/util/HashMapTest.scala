package org.scalanative.testsuite.javalib.util

import java.util.*

// Ported from Scala.js

import java.util as ju

import scala.reflect.ClassTag

class HashMapTest extends MapTest {
  def factory: HashMapFactory = new HashMapFactory
}

class HashMapFactory extends AbstractMapFactory {
  override def implementationName: String =
    "java.util.HashMap"

  override def empty[K: ClassTag, V: ClassTag]: ju.HashMap[K, V] =
    new ju.HashMap[K, V]

  def allowsNullKeys: Boolean = true
  def allowsNullValues: Boolean = true
}
