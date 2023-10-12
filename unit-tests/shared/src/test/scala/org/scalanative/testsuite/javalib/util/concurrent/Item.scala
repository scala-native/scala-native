/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.scalanative.testsuite.javalib.util.concurrent

import java.util.Comparator
import java.io.Serializable

/** A simple element class for collections etc
 */
final class Item extends Number with Comparable[Item] with Serializable {
  final var value = 0

  def this(v: Int) = {
    this()
    value = v
  }

  def this(i: Item) = {
    this()
    value = i.value
  }

  def this(i: Integer) = {
    this()
    value = i.intValue
  }

  override def intValue: Int = value

  override def longValue: Long = value.toLong

  override def floatValue: Float = value.toFloat

  override def doubleValue: Double = value.toDouble

  override def equals(x: Any): Boolean =
    x.isInstanceOf[Item] && x.asInstanceOf[Item].value == value

  def equals(b: Int): Boolean = value == b

  override def compareTo(x: Item): Int = Integer.compare(this.value, x.value)

  def compareTo(b: Int): Int = Integer.compare(this.value, b)

  override def hashCode: Int = value

  override def toString: String = Integer.toString(value)
}

object Item {
  import scala.language.implicitConversions
  implicit def fromInt(v: Int): Item = valueOf(v)
  implicit def fromInteger(v: Integer): Item = valueOf(v.intValue())

  def valueOf(i: Int) = new Item(i)

  def compare(x: Item, y: Item): Int = Integer.compare(x.value, y.value)

  def compare(x: Item, b: Int): Int = Integer.compare(x.value, b)

  def comparator = new Item.Cpr

  class Cpr extends Comparator[Item] {
    override def compare(x: Item, y: Item): Int =
      Integer.compare(x.value, y.value)
  }
}
