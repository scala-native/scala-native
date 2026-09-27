/*
 * Written by Doug Lea and Martin Buchholz with assistance from
 * members of JCP JSR-166 Expert Group and released to the public
 * domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.scalanative.testsuite.javalib.util.concurrent

/** Allows tests to work with different Map implementations. */
trait MapImplementation {
  def klazz(): Class[_]
  def emptyMap(): java.util.Map[_, _]

  def makeKey(i: Int): AnyRef = Integer.valueOf(i)
  def makeValue(i: Int): AnyRef = Integer.valueOf(i)
  def keyToInt(key: AnyRef): Int = key.asInstanceOf[Integer].intValue()
  def valueToInt(value: AnyRef): Int = value.asInstanceOf[Integer].intValue()

  def isConcurrent(): Boolean
  def remappingFunctionCalledAtMostOnce(): Boolean = true
  def permitsNullKeys(): Boolean
  def permitsNullValues(): Boolean
  def supportsSetValue(): Boolean
}
