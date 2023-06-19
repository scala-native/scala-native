package java.util.concurrent.atomic

import java.io.Serializable

@SerialVersionUID(7249069246863182397L)
object LongAdder {

  @SerialVersionUID(7249069246863182397L)
  private class SerializationProxy private[atomic] (val a: LongAdder)
      extends Serializable {
    value = a.sum

    final private var value = 0L

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
    var cs = null
    var b = 0L
    var v = 0L
    var m = 0
    var c = null
    if ((cs = cells) != null || !casBase(b = base, b + x)) {
      val index = getProbe
      var uncontended = true
      if (cs == null || (m = cs.length - 1) < 0 || (c =
            cs(index & m)) == null || !(uncontended =
            c.cas(v = c.value, v + x)))
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

  override def toString: String = Long.toString(sum)

  override def longValue: Long = sum

  override def intValue: Int = sum.toInt

  override def floatValue: Float = sum.toFloat

  override def doubleValue: Double = sum.toDouble

  private def writeReplace = new LongAdder.SerializationProxy(this)

  @throws[java.io.InvalidObjectException]
  private def readObject(s: ObjectInputStream): Unit = {
    throw new InvalidObjectException("Proxy required")
  }
}
