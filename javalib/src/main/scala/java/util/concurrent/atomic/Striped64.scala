// Ported from JSR 166 revision 1.28

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
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

@SuppressWarnings(Array("serial"))
object Striped64 {

  /** Currently, Contended annotation is not supported. */
  // @jdk.internal.vm.annotation.Contended
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

  @alwaysinline private[atomic] def threadProbeAtomic() = new CAtomicInt(
    fromRawPtr(
      Intrinsics.classFieldRawPtr(
        Thread.currentThread(),
        "threadLocalRandomProbe"
      )
    )
  )

  private[atomic] def getProbe(): Int =
    threadProbeAtomic().load().asInstanceOf[Int]

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
      _wasUncontended: Boolean,
      _index: Int
  ): Unit = {
    var index = _index
    var wasUncontended = _wasUncontended

    if (index == 0) {
      ThreadLocalRandom.current() // force initialization

      index = Striped64.getProbe()
      wasUncontended = true
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
        if ({ c = cs((n - 1) & index); c == null }) {
          if (cellsBusy == 0) { // Try to attach new Cell
            val r = new Striped64.Cell(x) // Optimistically create
            if (cellsBusy == 0 && casCellsBusy()) {
              try { // Recheck under lock
                var rs: Array[Striped64.Cell] = null
                var m = 0
                var j = 0
                if ({
                  rs = cells; m = rs.length; j = (m - 1) & index;
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
        } else if (!wasUncontended) { // CAS already known to fail
          wasUncontended = true // Continue after rehash
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
          index = Striped64.advanceProbe(index)
      } else if (cellsBusy == 0 && (cells == cs) && casCellsBusy())
        try // Initialize table
          if (cells == cs) {
            val rs = new Array[Striped64.Cell](2)
            rs(index & 1) = new Striped64.Cell(x)
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
      _wasUncontended: Boolean,
      _index: Int
  ): Unit = {
    var index = _index
    var wasUncontended = _wasUncontended
    if (index == 0) {
      ThreadLocalRandom.current()
      index = Striped64.getProbe()
      wasUncontended = true
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
        if ({ c = cs((n - 1) & index); c == null }) {
          if (cellsBusy == 0) {
            val r = new Striped64.Cell(doubleToRawLongBits(x))
            if (cellsBusy == 0 && casCellsBusy()) {
              try {
                var rs: Array[Striped64.Cell] = null
                var m = 0
                var j = 0
                if ({
                  rs = cells; m = rs.length; j = (m - 1) & index;
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
        } else if (!wasUncontended) wasUncontended = true
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
          index = Striped64.advanceProbe(index)
      } else if (cellsBusy == 0 && cells == cs && casCellsBusy())
        try {
          if (cells == cs) {
            val rs = new Array[Striped64.Cell](2)
            rs(index & 1) = new Striped64.Cell(doubleToRawLongBits(x))
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
