// Ported from JSR 166 revision 1.28

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent.atomic

import java.lang.Double.*
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleBinaryOperator;
import java.util.function.LongBinaryOperator;
import scala.scalanative.annotation.*
import scala.scalanative.unsafe.*
import scala.scalanative.libc.stdatomic.{
  AtomicInt,
  AtomicLongLong,
  memory_order
}
import scala.scalanative.runtime.{fromRawPtr, Intrinsics}

@SuppressWarnings(Array("serial"))
private[atomic] object Striped64 {
  type Contended = scala.scalanative.annotation.align
  @Contended private[atomic] final class Cell private[atomic] (
      @volatile private[atomic] var value: Long
  ) {

    @alwaysinline def valueAtomic() = new AtomicLongLong(
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "value"))
    )

    private[atomic] final def cas(cmp: Long, `val`: Long) =
      valueAtomic().compareExchangeWeak(
        cmp,
        `val`,
        memory_order.memory_order_release
      )

    private[atomic] final def reset(): Unit =
      valueAtomic().store(0L, memory_order.memory_order_seq_cst)

    private[atomic] final def reset(identity: Long): Unit =
      valueAtomic().store(identity, memory_order.memory_order_seq_cst)

    private[atomic] final def getAndSet(`val`: Long) =
      valueAtomic().exchange(`val`).asInstanceOf[Long]
  }

  private[atomic] val NCPU: Int = Runtime.getRuntime().availableProcessors()

  @alwaysinline private[atomic] def threadProbeAtomic() = new AtomicInt(
    fromRawPtr(
      Intrinsics.classFieldRawPtr(
        Thread.currentThread(),
        "threadLocalRandomProbe"
      )
    )
  )

  private[atomic] def getProbe(): Int =
    threadProbeAtomic().load()

  private[atomic] def advanceProbe(probe: Int) = {
    var _probe = probe
    _probe = _probe ^ (_probe << 13) // xorshift

    _probe = _probe ^ (_probe >>> 17)
    _probe = _probe ^ (_probe << 5)
    threadProbeAtomic().store(_probe)
    _probe
  }

  private def _apply(fn: DoubleBinaryOperator, v: Long, x: Double) = {
    var d = longBitsToDouble(v)
    d =
      if (fn == null) d + x
      else fn.applyAsDouble(d, x)
    doubleToRawLongBits(d)
  }
}

@SuppressWarnings(Array("serial"))
private[atomic] abstract class Striped64 private[atomic] () extends Number {
  import Striped64.*

  @transient @volatile private[atomic] var cells: Array[Striped64.Cell] = _

  @transient @volatile private[atomic] var base: Long = 0L

  @transient @volatile private[atomic] var cellsBusy: Int = 0

  @alwaysinline private def baseAtomic() = new AtomicLongLong(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "base"))
  )

  @alwaysinline private def cellsBusyAtomic() = new AtomicInt(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "cellsBusy"))
  )

  private[atomic] final def casBase(cmp: Long, `val`: Long) =
    baseAtomic().compareExchangeWeak(
      cmp,
      `val`,
      memory_order.memory_order_release
    )

  private[atomic] final def getAndSetBase(`val`: Long) =
    baseAtomic().exchange(`val`)

  private[atomic] final def casCellsBusy() =
    cellsBusyAtomic().compareExchangeWeak(0, 1)

  private[atomic] final def longAccumulate(
      x: Long,
      fn: LongBinaryOperator,
      _wasUncontended: Boolean,
      _index: Int
  ): Unit = {
    var index = _index
    var wasUncontended = _wasUncontended

    if (index == 0) {
      ThreadLocalRandom.current() // force initialization
      index = getProbe()
      wasUncontended = true
    }
    var collide = false
    while (true) {
      var cs: Array[Cell] = null
      var c: Cell = null
      var n: Int = 0
      var v: Long = 0

      if (cells != null && { n = cells.length; n > 0 }) {
        c = cells((n - 1) & index)
        if (c == null) {
          var continue = false
          if (cellsBusy == 0) {
            val r = new Cell(x)
            if (cellsBusy == 0 && casCellsBusy()) {
              try {
                var rs: Array[Cell] = null
                var m: Int = 0
                var j: Int = 0
                rs = cells
                if (rs != null && { m = rs.length; m > 0 } &&
                    rs({ j = (m - 1) & index; j }) == null) {
                  rs(j) = r
                  return
                }
              } finally {
                cellsBusy = 0
              }
              continue = true
            }
          }
          if (!continue) collide = false
        } else if (!wasUncontended) { // CAS already known to fail
          wasUncontended = true // Continue after rehash
        } else if (c.cas(
              { v = c.value; v },
              if (fn == null) v + x else fn.applyAsLong(v, x)
            )) {
          return
        } else if (n >= NCPU || cells != cs)
          collide = false // At max size or stale
        else if (!collide) collide = true
        else if (cellsBusy == 0 && casCellsBusy()) {
          try {
            if (cells == cs) { // Expand table unless stale
              cells = Arrays.copyOf(cs, n << 1)
            }
          } finally {
            cellsBusy = 0
          }
          collide = false
          return
        }
        index = advanceProbe(index)
      } else if (cellsBusy == 0 && cells == cs && casCellsBusy()) {
        try { // Initialize table
          if (cells == cs) {
            val rs = new Array[Cell](2)
            rs(index & 1) = new Cell(x)
            cells = rs
            return
          }
        } finally cellsBusy = 0
      } else if (casBase( // Fall back on using base
            { v = base; v },
            if (fn == null) v + x else fn.applyAsLong(v, x)
          )) return
    }
  }

  private[atomic] final def doubleAccumulate(
      x: Double,
      fn: DoubleBinaryOperator,
      _wasUncontended: Boolean,
      _index: Int
  ): Unit = {
    var index = _index
    var wasUncontended = _wasUncontended

    if (index == 0) {
      ThreadLocalRandom.current() // force initialization
      index = getProbe()
      wasUncontended = true
    }
    var collide = false

    while (true) {
      var cs: Array[Cell] = null
      var c: Cell = null
      var n: Int = 0
      var v: Long = 0

      if (cells != null && { n = cells.length; n > 0 }) {
        c = cells((n - 1) & index)
        if (c == null) {
          var continue = false
          if (cellsBusy == 0) {
            val r = new Cell(doubleToRawLongBits(x))
            if (cellsBusy == 0 && casCellsBusy()) {
              try {
                var rs: Array[Cell] = null
                var m: Int = 0
                var j: Int = 0
                rs = cells
                if (rs != null && { m = rs.length; m > 0 } &&
                    rs({ j = (m - 1) & index; j }) == null) {
                  rs(j) = r
                  return
                }
              } finally {
                cellsBusy = 0
              }
              continue = true
            }
          }
          if (!continue) collide = false
        } else if (!wasUncontended) { // CAS already known to fail
          wasUncontended = true // Continue after rehash
        } else if (c.cas({ v = c.value; v }, Striped64._apply(fn, v, x)))
          return
        else if (n >= NCPU || cells != cs)
          collide = false // At max size or stale
        else if (!collide) collide = true
        else if (cellsBusy == 0 && casCellsBusy()) {
          try {
            if (cells == cs) { // Expand table unless stale
              cells = Arrays.copyOf(cs, n << 1)
            }
          } finally {
            cellsBusy = 0
          }
          collide = false
          return
        }
        index = advanceProbe(index)
      } else if (cellsBusy == 0 && cells == cs && casCellsBusy()) {
        try { // Initialize table
          if (cells == cs) {
            val rs = new Array[Cell](2)
            rs(index & 1) = new Cell(doubleToLongBits(x))
            cells = rs
            return
          }
        } finally cellsBusy = 0
      } else if (casBase( // Fall back on using base
            { v = base; v },
            Striped64._apply(fn, v, x)
          )) return
    }
  }
}
