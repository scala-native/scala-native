/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
// revision 1.72
package java.util.concurrent

import scala.scalanative.runtime.{Intrinsics, fromRawPtr}
import scala.scalanative.libc.atomic.CAtomicInt
import scala.annotation.tailrec

abstract class CountedCompleter[T] protected (
    final private[concurrent] val completer: CountedCompleter[_],
    initialPendingCount: Int
) extends ForkJoinTask[T] {

  @volatile private var pending = initialPendingCount
  private val atomicPending = new CAtomicInt(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "pending"))
  )

  protected def this(completer: CountedCompleter[_]) = this(completer, 0)

  protected def this() = this(null, 0)

  def compute(): Unit

  def onCompletion(caller: CountedCompleter[_]): Unit = {}

  def onExceptionalCompletion(
      ex: Throwable,
      caller: CountedCompleter[_]
  ) = true

  final def getCompleter(): CountedCompleter[_] = completer

  final def getPendingCount(): Int = pending

  final def setPendingCount(count: Int): Unit = pending = count

  final def addToPendingCount(delta: Int): Unit = atomicPending.fetchAdd(delta)

  final def compareAndSetPendingCount(expected: Int, count: Int): Boolean =
    atomicPending.compareExchangeStrong(expected, count)

  // internal-only weak version
  final private[concurrent] def weakCompareAndSetPendingCount(
      expected: Int,
      count: Int
  ) = atomicPending.compareExchangeWeak(expected, count)

  final def decrementPendingCountUnlessZero: Int = {
    var c = 0
    while ({
      c = pending
      pending != 0 && !weakCompareAndSetPendingCount(c, c - 1)
    }) ()
    c
  }

  final def getRoot(): CountedCompleter[_] = {
    @tailrec def loop(a: CountedCompleter[_]): CountedCompleter[_] =
      a.completer match {
        case null => a
        case p    => loop(p)
      }
    loop(this)
  }

  final def tryComplete(): Unit = {
    var a: CountedCompleter[_] = this
    var s = a
    var c = 0
    while (true) {
      c = a.pending
      if (c == 0) {
        a.onCompletion(s)
        s = a
        a = a.completer
        if (a == null) {
          s.quietlyComplete()
          return
        }
      } else if (a.weakCompareAndSetPendingCount(c, c - 1)) return
    }
  }

  final def propagateCompletion(): Unit = {
    var a: CountedCompleter[_] = this
    var s = null: CountedCompleter[_]
    var c = 0
    while (true) {
      c = a.pending
      if (c == 0) {
        s = a
        a = a.completer
        if (a == null) {
          s.quietlyComplete()
          return
        }
      } else if (a.weakCompareAndSetPendingCount(c, c - 1)) return
    }
  }

  override def complete(rawResult: T): Unit = {
    setRawResult(rawResult)
    onCompletion(this)
    quietlyComplete()
    val p = completer
    if (p != null) p.tryComplete()
  }

  final def firstComplete(): CountedCompleter[_] = {
    var c = 0
    while (true) {
      c = pending
      if (c == 0) return this
      else if (weakCompareAndSetPendingCount(c, c - 1)) return null
    }
    null // unreachable
  }

  final def nextComplete(): CountedCompleter[_] =
    completer match {
      case null => quietlyComplete(); null
      case p    => p.firstComplete()
    }

  final def quietlyCompleteRoot(): Unit = {
    var a: CountedCompleter[_] = this
    while (true) {
      a.completer match {
        case null => a.quietlyComplete(); return
        case p    => a = p
      }
    }
  }

  final def helpComplete(maxTasks: Int): Unit = {
    val t = Thread.currentThread()
    val owned = t.isInstanceOf[ForkJoinWorkerThread]
    val q =
      if (owned) t.asInstanceOf[ForkJoinWorkerThread].workQueue
      else ForkJoinPool.commonQueue()

    if (q != null && maxTasks > 0) q.helpComplete(this, owned, maxTasks)
  }

  override final private[concurrent] def trySetException(ex: Throwable): Int = {
    var a: CountedCompleter[_] = this
    var p = a
    while ({
      ForkJoinTask.isExceptionalStatus(a.trySetThrown(ex)) &&
      a.onExceptionalCompletion(ex, p) && {
        p = a; a = a.completer; a != null
      } &&
      a.status >= 0
    }) ()
    status
  }

  override final protected def exec(): Boolean = {
    compute()
    false
  }

  override def getRawResult(): T = null.asInstanceOf[T]

  override protected def setRawResult(t: T): Unit = {}
}
