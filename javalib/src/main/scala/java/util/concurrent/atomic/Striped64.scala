// Ported from JSR 166 revision 1.28

package java.util.concurrent.atomic

import java.lang.Double._
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleBinaryOperator;
import java.util.function.LongBinaryOperator;
import scala.scalanative.annotation._
import scala.scalanative.unsafe._
import scala.scalanative.libc.atomic.{CAtomicInt, CAtomicLongLong, memory_order}
import scala.scalanative.runtime.{fromRawPtr, Intrinsics}

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

@SuppressWarnings(Array("serial"))
object Striped64 {

  final private[atomic] class Cell private[atomic] (var value: Long) {
    @volatile private[concurrent] var _value = value

    @alwaysinline def valueAtomic() = new CAtomicLongLong(
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "_value"))
    )

    final private[atomic] def cas(cmp: Long, `val`: Long) =
      valueAtomic().compareExchangeWeak(
        cmp,
        `val`,
        memory_order.memory_order_release
      )

    final private[atomic] def reset(): Unit =
      valueAtomic().store(0L, memory_order.memory_order_seq_cst)

    final private[atomic] def reset(identity: Long): Unit =
      valueAtomic().store(identity, memory_order.memory_order_seq_cst)

    final private[atomic] def getAndSet(`val`: Long) =
      valueAtomic().exchange(`val`).asInstanceOf[Long]
  }

  private[atomic] val NCPU: Int = Runtime.getRuntime().availableProcessors()

  private[atomic] def getProbe(self: Striped64): Int =
    self.threadProbeAtomic().load().asInstanceOf[Int]

  private[atomic] def advanceProbe(self: Striped64, probe: Int) = {
    var _probe = probe
    _probe = _probe ^ (_probe << 13) // xorshift

    _probe = _probe ^ (_probe >>> 17)
    _probe = _probe ^ (_probe << 5)
    self.threadProbeAtomic().store(_probe)
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
abstract class Striped64 private[atomic] () extends Number {

  @volatile private[atomic] var cells: Array[Striped64.Cell] = null

  @volatile private[atomic] var base: Long = 0L

  @volatile private[atomic] var cellsBusy: Int = 0

  @alwaysinline private def baseAtomic() = new CAtomicLongLong(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "base"))
  )

  @alwaysinline private def cellsBusyAtomic() = new CAtomicInt(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "cellsBusy"))
  )

  @alwaysinline private def threadProbeAtomic() = new CAtomicInt(
    fromRawPtr(
      Intrinsics.classFieldRawPtr(
        Thread.currentThread(),
        "threadLocalRandomProbe"
      )
    )
  )

  final private[atomic] def casBase(cmp: Long, `val`: Long) =
    baseAtomic().compareExchangeWeak(
      cmp,
      `val`,
      memory_order.memory_order_release
    )

  final private[atomic] def getAndSetBase(`val`: Long) =
    baseAtomic().exchange(`val`)

  final private[atomic] def casCellsBusy() =
    cellsBusyAtomic().compareExchangeWeak(0, 1)

  final private[atomic] def longAccumulate(
      x: Long,
      fn: LongBinaryOperator,
      wasUncontended: Boolean,
      index: Int
  ): Unit = {
    var _index = index
    var _wasUncontended = wasUncontended

    if (_index == 0) {
      ThreadLocalRandom.current() // force initialization

      _index = Striped64.getProbe(this)
      _wasUncontended = true
    }

    var continue1 = true
    var continue2 = true
    var collide = false
    while (continue1) { // True if last slot nonempty
      continue2 = true
      var cs: Array[Striped64.Cell] = null
      var c: Striped64.Cell = null
      var n = 0
      var v = 0L
      if ({ cs = cells; n = cs.length; cs != null && n > 0 }) {
        if ({ c = cs((n - 1) & _index); c == null }) {
          if (cellsBusy == 0) { // Try to attach new Cell
            val r = new Striped64.Cell(x) // Optimistically create
            if (cellsBusy == 0 && casCellsBusy()) {
              try { // Recheck under lock
                var rs: Array[Striped64.Cell] = null
                var m = 0
                var j = 0
                if ({
                  rs = cells; m = rs.length; j = (m - 1) & _index;
                  rs != null && m > 0 && rs(j) == null
                }) {
                  rs(j) = r
                  continue1 = false
                  continue2 = false

                }
              } finally {
                cellsBusy = 0
              }
              continue2 = false
            }
          }
          if (continue2 == true)
            collide = false
        } else if (!_wasUncontended) { // CAS already known to fail
          _wasUncontended = true // Continue after rehash
        } else if (c.cas(
              { v = c.value; v },
              if (fn == null) v + x
              else fn.applyAsLong(v, x)
            )) {
          continue1 = false
          continue2 = false
        } else if (n >= Striped64.NCPU || (cells != cs))
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
          continue2 = false
          // Retry with expanded table
        }
        if (continue2 == true)
          _index = Striped64.advanceProbe(this, _index)
      } else if (cellsBusy == 0 && (cells == cs) && casCellsBusy())
        try // Initialize table
          if (cells == cs) {
            val rs = new Array[Striped64.Cell](2)
            rs(_index & 1) = new Striped64.Cell(x)
            cells = rs
            continue1 = false
          }
        finally cellsBusy = 0
      else { // Fall back on using base
        if (casBase(
              { v = base; v },
              if (fn == null) v + x
              else fn.applyAsLong(v, x)
            )) {
          continue1 = false
        }
      }
    }
  }

  final private[atomic] def doubleAccumulate(
      x: Double,
      fn: DoubleBinaryOperator,
      wasUncontended: Boolean,
      index: Int
  ): Unit = {
    var _index = index
    var _wasUncontended = wasUncontended
    if (_index == 0) {
      ThreadLocalRandom.current()
      _index = Striped64.getProbe(this)
      _wasUncontended = true
    }
    var continue1 = true
    var continue2 = true
    var collide = false
    while (continue1) {
      var cs: Array[Striped64.Cell] = null
      var c: Striped64.Cell = null
      var n = 0
      var v = 0L
      if ({ cs = cells; n = cs.length; cs != null && n > 0 }) {
        if ({ c = cs((n - 1) & _index); c == null }) {
          if (cellsBusy == 0) {
            val r = new Striped64.Cell(doubleToRawLongBits(x))
            if (cellsBusy == 0 && casCellsBusy()) {
              try {
                var rs: Array[Striped64.Cell] = null
                var m = 0
                var j = 0
                if ({
                  rs = cells; m = rs.length; j = (m - 1) & _index;
                  rs != null && m > 0 && rs(j) == null
                }) {
                  rs(j) = r
                  continue1 = false
                  continue2 = false
                }
              } finally cellsBusy = 0
              continue2 = false
            }
          }
          if (continue2 == true)
            collide = false
        } else if (!_wasUncontended) _wasUncontended = true
        else if (c.cas({ v = c.value; v }, Striped64._apply(fn, v, x))) {
          continue1 = false
          continue2 = false
        } else if (n >= Striped64.NCPU || (cells != cs)) collide = false
        else if (!collide) collide = true
        else if (cellsBusy == 0 && casCellsBusy()) {
          try {
            if (cells == cs)
              cells = Arrays.copyOf(cs, n << 1)
          } finally {
            cellsBusy = 0
          }
          collide = false
          continue2 = false
        }
        if (continue2 == true)
          _index = Striped64.advanceProbe(this, _index)
      } else if (cellsBusy == 0 && cells == cs && casCellsBusy())
        try {
          if (cells == cs) {
            val rs = new Array[Striped64.Cell](2)
            rs(_index & 1) = new Striped64.Cell(doubleToRawLongBits(x))
            cells = rs
            continue1 = false
          }
        } finally {
          cellsBusy = 0
        }
      else if (casBase({ v = base; v }, Striped64._apply(fn, v, x))) {
        continue1 = false
        continue2 = false
      }
    }
  }
}
