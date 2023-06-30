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

  @SerialVersionUID(7249069246863182397L)
  private class SerializationProxy(val a: LongAdder) extends Serializable {
    final private var value = a.sum

    private def readResolve = {
      val a = new LongAdder
      a.base = value
      a
    }
  }
}

@SerialVersionUID(7249069246863182397L)
class LongAdder() extends Striped64 with Serializable {

  def add(x: Long): Unit = {
    var cs: Array[Striped64.Cell] = null.asInstanceOf[Array[Striped64.Cell]]
    var b = 0L
    var v = 0L
    var m = 0
    var c: Striped64.Cell = null
    if ({ cs = cells; cs != null || !casBase({ b = base; b }, b + x) }) {
      val index = Striped64.getProbe()
      var uncontended = true
      if ({
        m = cs.length;
        c = cs(index & m);
        v = c.value;
        uncontended = c.cas(v, v + x);
        cs == null || m < 0 || c == null || !uncontended
      })
        longAccumulate(x, null, uncontended, index)
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
