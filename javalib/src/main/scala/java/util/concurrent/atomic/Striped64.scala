// Ported from JSR 166 revision 1.28

package java.util.concurrent.atomic

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleBinaryOperator;
import java.util.function.LongBinaryOperator;
import scala.scalanative.annotation._
import scala.scalanative.unsafe._
import scala.scalanative.libc.atomic.CAtomicLong
import scala.scalanative.runtime.{fromRawPtr, Intrinsics}

import scala.scalanative.annotation._
import scala.scalanative.unsafe._
import scala.scalanative.libc.atomic.{CAtomicInt, CAtomicLongLong, CAtomicRef}
import scala.scalanative.runtime.{fromRawPtr, Intrinsics, ObjectArray}

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
      valueAtomic().compareExchangeWeak(cmp, `val`)

    final private[atomic] def reset(): Unit = {
      valueAtomic().store(0L)
      // Cell.VALUE.setVolatile(this, 0L)
    }

    final private[atomic] def reset(identity: Long): Unit = {
      valueAtomic().store(identity)
      // Cell.VALUE.setVolatile(this, identity)
    }

    final private[atomic] def getAndSet(`val`: Long) =
      valueAtomic().exchange(`val`).asInstanceOf[Long]
    // Cell.VALUE.getAndSet(this, `val`).asInstanceOf[Long]
  }

  // private[atomic] val NCPU = Runtime.getRuntime.availableProcessors

  // private[atomic] def getProbe =
  //   THREAD_PROBE.get(Thread.currentThread).asInstanceOf[Int]

  // private[atomic] def advanceProbe(probe: Int) = {
  //   probe ^= probe << 13 // xorshift

  //   probe ^= probe >>> 17
  //   probe ^= probe << 5
  //   THREAD_PROBE.set(Thread.currentThread, probe)
  //   probe
  // }

  // private def apply(fn: DoubleBinaryOperator, v: Long, x: Double) = {
  //   var d = Double.longBitsToDouble(v)
  //   d =
  //     if (fn == null) d + x
  //     else fn.applyAsDouble(d, x)
  //   Double.doubleToRawLongBits(d)
  // }

  // private var BASE = null
  // private var CELLSBUSY = null
  // private var THREAD_PROBE = null

  // try
  //   try {
  //     val l = MethodHandles.lookup
  //     BASE = l.findVarHandle(classOf[Striped64], "base", classOf[Long])
  //     CELLSBUSY = l.findVarHandle(classOf[Striped64], "cellsBusy", classOf[Int])
  //     @SuppressWarnings(Array("removal")) val l2 =
  //       java.security.AccessController.doPrivileged(
  //         new PrivilegedAction[MethodHandles.Lookup]() {
  //           override def run: MethodHandles.Lookup = try
  //             MethodHandles
  //               .privateLookupIn(classOf[Thread], MethodHandles.lookup)
  //           catch {
  //             case e: ReflectiveOperationException =>
  //               throw new ExceptionInInitializerError(e)
  //           }
  //         }
  //       )
  //     THREAD_PROBE = l2.findVarHandle(
  //       classOf[Thread],
  //       "threadLocalRandomProbe",
  //       classOf[Int]
  //     )
  //   } catch {
  //     case e: ReflectiveOperationException =>
  //       throw new ExceptionInInitializerError(e)
  //   }

}

@SuppressWarnings(Array("serial"))
abstract class Striped64 private[atomic] () extends Number {

  // private[atomic] var cells = null

  // private[atomic] val base = 0L

  // private[atomic] var cellsBusy = 0

  // final private[atomic] def casBase(cmp: Long, `val`: Long) =
  //   Striped64.BASE.weakCompareAndSetRelease(this, cmp, `val`)

  // final private[atomic] def getAndSetBase(`val`: Long) =
  //   Striped64.BASE.getAndSet(this, `val`).asInstanceOf[Long]

  // final private[atomic] def casCellsBusy =
  //   Striped64.CELLSBUSY.compareAndSet(this, 0, 1)

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
