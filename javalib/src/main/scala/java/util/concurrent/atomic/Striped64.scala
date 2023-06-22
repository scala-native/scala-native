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

  private[atomic] val NCPU = Runtime.getRuntime().availableProcessors()

  private[atomic] def getProbe(self: Striped64) =
    self.threadProbeAtomic(Thread.currentThread()).load().asInstanceOf[Int]

  private[atomic] def advanceProbe(self: Striped64, probe: Int) = {
    var _probe = probe
    _probe = _probe ^ (_probe << 13) // xorshift

    _probe = _probe ^ (_probe >>> 17)
    _probe = _probe ^ (_probe << 5)
    self.threadProbeAtomic(Thread.currentThread()).store(_probe)
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

  // MEMO: is this ok?
  @alwaysinline private def threadProbeAtomic(t: Thread) = new CAtomicInt(
    fromRawPtr(
      // TODO: check
      Intrinsics.classFieldRawPtr(t, "threadLocalRandomProbe")
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

  // final private[atomic] def longAccumulate(
  //     x: Long,
  //     fn: LongBinaryOperator,
  //     wasUncontended: Boolean,
  //     index: Int
  // ): Unit = {
  //   if (index == 0) {
  //     ThreadLocalRandom.current // force initialization

  //     index = Striped64.getProbe
  //     wasUncontended = true
  //   }
  //   var collide = false
  //   while ({
  //     true
  //   }) { // True if last slot nonempty
  //     var cs = null
  //     var c = null
  //     var n = 0
  //     var v = 0L
  //     if ((cs = cells) != null && (n = cs.length) > 0) {
  //       if ((c = cs((n - 1) & index)) == null) {
  //         if (cellsBusy == 0) { // Try to attach new Cell
  //           val r = new Striped64.Cell(x) // Optimistically create
  //           if (cellsBusy == 0 && casCellsBusy) {
  //             try { // Recheck under lock
  //               var rs = null
  //               var m = 0
  //               var j = 0
  //               if ((rs = cells) != null && (m = rs.length) > 0 && rs(j =
  //                     (m - 1) & index
  //                   ) == null) {
  //                 rs(j) = r
  //                 break // todo: break is not supported

  //               }
  //             } finally cellsBusy = 0
  //             continue // todo: continue is not supported
  //             // Slot is now non-empty

  //           }
  //         }
  //         collide = false
  //       } else if (!wasUncontended) { // CAS already known to fail
  //         wasUncontended = true // Continue after rehash
  //       } else if (c.cas(
  //             v = c.value,
  //             if (fn == null) v + x
  //             else fn.applyAsLong(v, x)
  //           )) break // todo: break is not supported
  //       else if (n >= Striped64.NCPU || (cells ne cs))
  //         collide = false // At max size or stale
  //       else if (!collide) collide = true
  //       else if (cellsBusy == 0 && casCellsBusy) {
  //         try
  //           if (cells eq cs) { // Expand table unless stale
  //             cells = util.Arrays.copyOf(cs, n << 1)
  //           }
  //         finally cellsBusy = 0
  //         collide = false
  //         continue // todo: continue is not supported
  //         // Retry with expanded table

  //       }
  //       index = Striped64.advanceProbe(index)
  //     } else if (cellsBusy == 0 && (cells eq cs) && casCellsBusy)
  //       try // Initialize table
  //         if (cells eq cs) {
  //           val rs = new Array[Striped64.Cell](2)
  //           rs(index & 1) = new Striped64.Cell(x)
  //           cells = rs
  //           break // todo: break is not supported

  //         }
  //       finally cellsBusy = 0
  //     else { // Fall back on using base
  //       if (casBase(
  //             v = base,
  //             if (fn == null) v + x
  //             else fn.applyAsLong(v, x)
  //           )) break // todo: break is not supported
  //     }
  //   }
  // }

  // final private[atomic] def doubleAccumulate(
  //     x: Double,
  //     fn: DoubleBinaryOperator,
  //     wasUncontended: Boolean,
  //     index: Int
  // ): Unit = {
  //   if (index == 0) {
  //     ThreadLocalRandom.current
  //     index = Striped64.getProbe
  //     wasUncontended = true
  //   }
  //   var collide = false
  //   while ({
  //     true
  //   }) {
  //     var cs = null
  //     var c = null
  //     var n = 0
  //     var v = 0L
  //     if ((cs = cells) != null && (n = cs.length) > 0) {
  //       if ((c = cs((n - 1) & index)) == null) {
  //         if (cellsBusy == 0) {
  //           val r = new Striped64.Cell(Double.doubleToRawLongBits(x))
  //           if (cellsBusy == 0 && casCellsBusy) {
  //             try {
  //               var rs = null
  //               var m = 0
  //               var j = 0
  //               if ((rs = cells) != null && (m = rs.length) > 0 && rs(j =
  //                     (m - 1) & index
  //                   ) == null) {
  //                 rs(j) = r
  //                 break // todo: break is not supported

  //               }
  //             } finally cellsBusy = 0
  //             continue // todo: continue is not supported

  //           }
  //         }
  //         collide = false
  //       } else if (!wasUncontended) wasUncontended = true
  //       else if (c.cas(v = c.value, Striped64.apply(fn, v, x)))
  //         break // todo: break is not supported
  //       else if (n >= Striped64.NCPU || (cells ne cs)) collide = false
  //       else if (!collide) collide = true
  //       else if (cellsBusy == 0 && casCellsBusy) {
  //         try if (cells eq cs) cells = util.Arrays.copyOf(cs, n << 1)
  //         finally cellsBusy = 0
  //         collide = false
  //         continue // todo: continue is not supported

  //       }
  //       index = Striped64.advanceProbe(index)
  //     } else if (cellsBusy == 0 && (cells eq cs) && casCellsBusy)
  //       try
  //         if (cells eq cs) {
  //           val rs = new Array[Striped64.Cell](2)
  //           rs(index & 1) = new Striped64.Cell(Double.doubleToRawLongBits(x))
  //           cells = rs
  //           break // todo: break is not supported

  //         }
  //       finally cellsBusy = 0
  //     else if (casBase(v = base, Striped64.apply(fn, v, x)))
  //       break // todo: break is not supported
  //   }
  // }
}
