// Ported from JSR 166 revision 1.23

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent.atomic

import java.io.Serializable

@SerialVersionUID(7249069246863182397L)
object LongAdder {

  // This SerializationProxy provides sufficient serialization for LongAdder,
  // without unnecessary parent class info.
  @SerialVersionUID(7249069246863182397L)
  private class SerializationProxy(a: LongAdder) extends Serializable {
    private final var value = a.sum

    private def readResolve = {
      val a = new LongAdder
      a.base = value
      a
    }
  }
}

@SerialVersionUID(7249069246863182397L)
class LongAdder() extends Striped64 with Serializable {
  import Striped64.{Cell, getProbe}

  def add(x: Long): Unit = {
    var cs: Array[Cell] = null
    var b: Long = 0
    var v: Long = 0
    var m: Int = 0
    var c: Cell = null

    if ({ cs = cells; cs != null } || !casBase({ b = base; b }, b + x)) {
      val index = getProbe()
      var uncontended = true
      if (cs == null || { m = cells.length - 1; m < 0 } ||
          { c = cells(index & m); c == null } ||
          { uncontended = c.cas({ v = c.value; v }, v + x); !uncontended }) {
        longAccumulate(x, null, uncontended, index)
      }
    }
  }

  def increment(): Unit = {
    add(1L)
  }

  def decrement(): Unit = {
    add(-1L)
  }

  def sum: Long = {
    val cs = cells
    var sum = base
    if (cs != null) for (c <- cs) {
      if (c != null) sum += c.value
    }
    sum
  }

  def reset(): Unit = {
    val cs = cells
    base = 0L
    if (cs != null) for (c <- cs) {
      if (c != null) c.reset()
    }
  }

  def sumThenReset: Long = {
    val cs = cells
    var sum = getAndSetBase(0L)
    if (cs != null) for (c <- cs) {
      if (c != null) sum += c.getAndSet(0L)
    }
    sum
  }

  override def toString: String = sum.toString()

  override def longValue(): Long = sum

  override def intValue(): Int = sum.toInt

  override def floatValue(): Float = sum.toFloat

  override def doubleValue(): Double = sum.toDouble

  private def writeReplace = new LongAdder.SerializationProxy(this)

  // @throws[java.io.InvalidObjectException]
  // private def readObject(s: ObjectInputStream): Unit = {
  //   throw new InvalidObjectException("Proxy required")
  // }
}
