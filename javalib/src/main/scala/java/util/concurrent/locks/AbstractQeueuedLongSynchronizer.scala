package java.util
package concurrent.locks

// ported from Harmony

import java.util
import java.util.concurrent.TimeUnit

import scala.scalanative.runtime.CAtomicsImplicits._
import scala.scalanative.runtime.{CAtomicInt, CAtomicLong, CAtomicRef}
import scala.scalanative.native.{CInt, CLong}

abstract class AbstractQeueuedLongSynchronizer
    extends AbstractOwnableSynchronizer
    with java.io.Serializable {

  import AbstractQeueuedLongSynchronizer._

  //volatile
  private val head: CAtomicRef[Node] = CAtomicRef[Node]()

  //volatile
  private val tail: CAtomicRef[Node] = CAtomicRef[Node]()

  private val state: CAtomicLong = CAtomicLong()

  protected final def getState: Long = state.load()

  protected final def setState(newState: Long): Unit = state.store(newState)

  protected final def compareAndSetState(expect: Long, update: Long): Boolean =
    state.compareAndSwapStrong(expect, update)

  private def enq(node: Node): Node = {
    while (true) {
      val t: Node = tail
      if (t == null) {
        if (compareAndSetHead(new Node()))
          tail.store(head)
      } else {
        node.next.store(t)
        if (compareAndSetTail(t, node)) {
          t.next.store(node)
          t
        }
      }
    }
    // for the compiler
    null.asInstanceOf[Node]
  }

  private def addWaiter(mode: Node): Node = {
    val node: Node = new Node(Thread.currentThread(), mode)

    val pred: Node = tail
    if (pred != null) {
      node.next.store(pred)
      if (compareAndSetTail(pred, node)) {
        pred.next.store(node)
        node
      }
    }
    enq(node)
    node
  }

  private def setHead(node: Node): Unit = {
    head.store(node)
    node.thread.store(null.asInstanceOf[Thread])
    node.next.store(null.asInstanceOf[Node])
  }

  private def unparkSuccessor(node: Node): Unit = {
    val ws: Int = node.waitStatus
    if (ws < 0)
      compareAndSetWaitStatus(node, ws, 0)

    var s: Node = node.next
    if (s == null || s.waitStatus > 0) {
      s = null
      var t: Node = tail
      while (t != null && t != node) {
        if (t.waitStatus <= 0)
          s = t
        t = t.prev
      }
    }
    if (s != null)
      LockSupport.unpark(s.thread)
  }

  private def doReleaseShared(): Unit = {
    var continue: Boolean = false
    var break: Boolean    = false
    while (!break) {
      continue = false
      val h: Node = head
      if (h != null && h != tail.load().asInstanceOf[Node]) {
        val ws: Int = h.waitStatus
        if (ws == Node.SIGNAL) {
          if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
            continue = true
          if (!continue) unparkSuccessor(h)
        } else if (ws == 0 && !compareAndSetWaitStatus(h, 0, Node.PROPAGATE) && !continue)
          continue = true
      }
      if (h == head.load().asInstanceOf[Node] && !continue)
        break = true
    }
  }

  private def setHeadAndPropagate(node: Node, propagate: Long): Unit = {
    val h: Node = head
    setHead(node)

    if (propagate > 0 || h == null || h.waitStatus < 0) {
      val s: Node = node.next
      if (s == null || s.isShared)
        doReleaseShared()
    }
  }

  private def cancelAcquire(node: Node): Unit = {
    if (node == null)
      return
    node.thread.store(null.asInstanceOf[Thread])

    var pred: Node = node.prev
    while (pred.waitStatus > 0) {
      pred = pred.prev
      node.next.store(pred)
    }

    val predNext: Node = pred.next

    node.waitStatus.store(Node.CANCELLED)

    if (node == tail
          .load()
          .asInstanceOf[Node] && compareAndSetTail(node, pred)) {
      compareAndSetNext(pred, predNext, null)
    } else {
      val ws: Int = pred.waitStatus
      if (pred != head.load().asInstanceOf[Node] &&
          (ws == Node.SIGNAL ||
          (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
          pred.thread != null) {
        val next: Node = node.next
        if (next != null && next.waitStatus <= 0)
          compareAndSetNext(pred, pred.next, next)
      } else {
        unparkSuccessor(node)
      }

      node.next.store(node)
    }
  }

  private def parkAndCheckInterrupt(): Boolean = {
    LockSupport.park()
    Thread.interrupted()
  }

  def acquireQueued(node: Node, arg: Long): Boolean = {
    var failed = true
    try {
      var interrupted = false
      while (true) {
        val p: Node = node.predecessor()
        if (p == head.load().asInstanceOf[Node] && tryAcquire(arg)) {
          setHead(node)
          p.next.store(null.asInstanceOf[Node])
          failed = false
          return interrupted
        }
        if (shouldParkAfterFailedAcquire(p, node) &&
            parkAndCheckInterrupt())
          interrupted = true
      }
      // for the compiler
      false
    } finally {
      if (failed)
        cancelAcquire(node)
    }
  }

  private def doAcquireInterruptibly(arg: Long): Unit = {
    val node: Node      = addWaiter(Node.EXCLUSIVE)
    var failed: Boolean = true
    try {
      while (true) {
        val p: Node = node.predecessor()
        if (p == head.load().asInstanceOf[Node] && tryAcquire(arg)) {
          setHead(node)
          p.next.store(null.asInstanceOf[Node])
          failed = false
          return
        }
        if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
          throw new InterruptedException
      }
    } finally {
      if (failed)
        cancelAcquire(node)
    }
  }

  private def doAcquireNanos(arg: Long, nt: Long): Boolean = {
    var nanosTimeout: Long = nt
    var lastTime: Long     = System.nanoTime()
    val node: Node         = addWaiter(Node.EXCLUSIVE)
    var failed             = true
    try {
      while (true) {
        val p: Node = node.predecessor()
        if (p == head.load().asInstanceOf[Node] && tryAcquire(arg)) {
          setHead(node)
          p.next.store(null.asInstanceOf[Node])
          failed = false
          return true
        }
        if (nanosTimeout <= 0)
          return false
        if (shouldParkAfterFailedAcquire(p, node) &&
            nanosTimeout > spinForTimeoutThreshold)
          LockSupport.parkNanos(nanosTimeout)
        val now: Long = System.nanoTime()
        nanosTimeout -= now - lastTime
        lastTime = now
        if (Thread.interrupted())
          throw new InterruptedException()
      }
      // for the compiler
      false
    } finally {
      if (failed)
        cancelAcquire(node)
    }
  }

  private def doAcquireShared(arg: Long): Unit = {
    val node: Node = addWaiter(Node.SHARED)
    var failed     = true
    try {
      var interrupted = true
      while (true) {
        val p: Node = node.predecessor()
        if (p == head.load().asInstanceOf[Node]) {
          val r: Long = tryAcquireShared(arg)
          if (r >= 0) {
            setHeadAndPropagate(node, r)
            p.next.store(null.asInstanceOf[Node])
            if (interrupted)
              selfInterrupt()
            failed = false
            return
          }
        }
        if (shouldParkAfterFailedAcquire(p, node) &&
            parkAndCheckInterrupt())
          interrupted = true
      }
    } finally {
      if (failed)
        cancelAcquire(node)
    }
  }

  private def doAcquireSharedInterruptibly(arg: Long): Unit = {
    val node: Node = addWaiter(Node.SHARED)
    var failed     = true
    try {
      while (true) {
        val p: Node = node.predecessor()
        if (p == head.load().asInstanceOf[Node]) {
          val r: Long = tryAcquireShared(arg)
          if (r >= 0) {
            setHeadAndPropagate(node, r)
            p.next.store(null.asInstanceOf[Node])
            failed = false
            return
          }
        }
        if (shouldParkAfterFailedAcquire(p, node) &&
            parkAndCheckInterrupt())
          throw new InterruptedException()
      }
    } finally {
      if (failed)
        cancelAcquire(node)
    }
  }

  private def doAcquireSharedNanos(arg: Long, nt: Long): Boolean = {
    var nanosTimeout: Long = nt
    var lastTime: Long     = System.nanoTime()
    val node: Node         = addWaiter(Node.SHARED)
    var failed             = true
    try {
      while (true) {
        val p: Node = node.predecessor()
        if (p == head.load().asInstanceOf[Node]) {
          val r: Long = tryAcquireShared(arg)
          if (r >= 0) {
            setHeadAndPropagate(node, r)
            p.next.store(null.asInstanceOf[Node])
            failed = false
            return true
          }
        }
        if (nanosTimeout <= 0)
          return false
        if (shouldParkAfterFailedAcquire(p, node) &&
            nanosTimeout > spinForTimeoutThreshold)
          LockSupport.parkNanos(nanosTimeout)
        val now: Long = System.nanoTime()
        nanosTimeout -= now - lastTime
        lastTime = now
        if (Thread.interrupted())
          throw new InterruptedException()
      }
      // for the compiler
      false
    } finally {
      if (failed)
        cancelAcquire(node)
    }
  }

  protected def tryAcquire(arg: Long): Boolean =
    throw new UnsupportedOperationException()

  protected def tryRelease(arg: Long): Boolean =
    throw new UnsupportedOperationException()

  protected def tryAcquireShared(arg: Long): Long =
    throw new UnsupportedOperationException()

  protected def tryReleaseShared(arg: Long): Boolean =
    throw new UnsupportedOperationException()

  protected def isHeldExclusively(): Boolean =
    throw new UnsupportedOperationException()

  final def acquire(arg: Long): Unit = {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
      selfInterrupt()
  }

  final def acquireInterruptibly(arg: Long): Unit = {
    if (Thread.interrupted())
      throw new InterruptedException()
    if (!tryAcquire(arg))
      doAcquireInterruptibly(arg)
  }

  final def tryAcquireNanos(arg: Long, nanosTimeout: Long): Boolean = {
    if (Thread.interrupted())
      throw new InterruptedException()
    tryAcquire(arg) || doAcquireNanos(arg, nanosTimeout)
  }

  final def release(arg: Long): Boolean = {
    if (tryRelease(arg)) {
      val h: Node = head
      if (h != null && h.waitStatus != 0)
        unparkSuccessor(h)
      return true
    }
    false
  }

  final def acquireShared(arg: Long): Unit = {
    if (tryAcquireShared(arg) < 0)
      doAcquireShared(arg)
  }

  final def acquireSharedInterruptibly(arg: Long): Unit = {
    if (Thread.interrupted())
      throw new InterruptedException()
    if (tryAcquireShared(arg) < 0)
      doAcquireSharedInterruptibly(arg)
  }

  final def tryAcquireSharedNanos(arg: Long, nanosTimeout: Long): Boolean = {
    if (Thread.interrupted())
      throw new InterruptedException()
    tryAcquireShared(arg) >= 0 || doAcquireSharedNanos(arg, nanosTimeout)
  }

  final def releaseShared(arg: Long): Boolean = {
    if (tryReleaseShared(arg)) {
      doReleaseShared()
      return true
    }
    false
  }

  final def hasQueuedThreads(): Boolean = head != tail

  final def hasContented(): Boolean = head != null

  final def getFirstQueuedThread(): Thread =
    if (head == tail) null else fullGetFirstQueuedThread()

  private def fullGetFirstQueuedThread(): Thread = {
    val h: Node    = head
    val s: Node    = h.next
    val st: Thread = s.thread

    if ((h != null && s != null &&
        s.prev == head && st != null) ||
        (h != null && s != null &&
        s.prev == head && st != null))
      return st

    var t: Node             = tail
    var firstThread: Thread = null
    while (t != null && t != head.load().asInstanceOf[Node]) {
      val tt: Thread = t.thread
      if (tt != null)
        firstThread = tt
      t = t.prev
    }
    firstThread
  }

  final def isQueued(thread: Thread): Boolean = {
    if (thread == null)
      throw new NullPointerException()
    var p: Node = tail
    while (p != null) {
      if (p.thread == thread)
        return true
      p = p.prev
    }
    false
  }

  final def apparentlyFirstQueuedIsExclusive(): Boolean = {
    val h: Node = head
    val s: Node = h.next
    h != null && s != null &&
    !s.isShared && s.thread != null
  }

  final def hasQueuedPredecessors: Boolean = {
    val t: Node = tail
    val h: Node = head
    val s: Node = h.next
    h != t && (s == null || s.thread != Thread.currentThread())
  }

  final def getQeueueLength: Long = {
    var n       = 0
    var p: Node = tail
    while (p != null) {
      if (p.thread != null)
        n += 1
      p = p.prev
    }
    n
  }

  final def getQueuedThreads: util.Collection[Thread] = {
    val list: util.ArrayList[Thread] = new util.ArrayList[Thread]()
    var p: Node                      = tail
    while (p != null) {
      val t: Thread = p.thread
      if (t != null)
        list.add(t)
      p = p.prev
    }
    list
  }

  final def getExclusiveQueuedThreads: util.Collection[Thread] = {
    val list: util.ArrayList[Thread] = new util.ArrayList[Thread]()
    var p: Node                      = tail
    while (p != null) {
      if (!p.isShared) {
        val t: Thread = p.thread
        if (t != null)
          list.add(t)
        p = p.prev
      }
    }
    list
  }

  final def getSharedQueuedThreads: util.Collection[Thread] = {
    val list: util.ArrayList[Thread] = new util.ArrayList[Thread]()
    var p: Node                      = tail
    while (p != null) {
      if (p.isShared) {
        val t: Thread = p.thread
        if (t != null)
          list.add(t)
        p = p.prev
      }
    }
    list
  }

  override def toString: String = {
    val s: Long   = getState
    val q: String = if (hasQueuedThreads()) "non" else ""
    super.toString + "[State = " + s + ", " + q + "empty queue]"
  }

  final def isOnSyncQueue(node: Node): Boolean = {
    if (node.waitStatus == Node.CONDITION || node.prev == null)
      return false
    if (node.next != null)
      return true

    findNodeFromTail(node)
  }

  private def findNodeFromTail(node: Node): Boolean = {
    var t: Node = tail
    while (true) {
      if (t == node)
        return true
      if (t == null)
        return false
      t = t.prev
    }
    // for the compiler
    false
  }

  final def transferForSignal(node: Node): Boolean = {
    if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
      return false

    val p: Node = enq(node)
    val ws: Int = p.waitStatus
    if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
      LockSupport.unpark(node.thread)
    true
  }

  final def transferAfterCancelledWait(node: Node): Boolean = {
    if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
      enq(node)
      return true
    }

    while (!isOnSyncQueue(node)) Thread.`yield`()
    false
  }

  final def fullyRelease(node: Node): Long = {
    var failed = true
    try {
      val savedState: Long = getState
      if (release(savedState)) {
        failed = false
        savedState
      } else {
        throw new IllegalMonitorStateException()
      }
    } finally {
      if (failed)
        node.waitStatus.store(Node.CANCELLED)
    }
  }

  final def owns(condition: ConditionObject): Boolean = {
    if (condition == null)
      throw new NullPointerException()
    condition.isOwnedBy(this)
  }

  final def hasWaiters(condition: ConditionObject): Boolean = {
    if (!owns(condition))
      throw new IllegalArgumentException("Not owner")
    condition.hasWaiters()
  }

  final def getWaitQueueLength(condition: ConditionObject): Int = {
    if (!owns(condition))
      throw new IllegalArgumentException()
    condition.getWaitQueueLength()
  }

  final def getWaitingThreads(
      condition: ConditionObject): util.Collection[Thread] = {
    if (!owns(condition))
      throw new IllegalArgumentException("Not owner")
    condition.getWaitingThreads()
  }

  class ConditionObject extends Condition with Serializable {

    import ConditionObject._

    private var firstWaiter: Node = _
    private var lastWaiter: Node  = _

    private def addConditionWaiter(): Node = {
      var t: Node = lastWaiter

      if (t != null && t.waitStatus != Node.CONDITION) {
        unlinkCancelledWaiters()
        t = lastWaiter
      }
      val node: Node = new Node(Thread.currentThread(), Node.CONDITION)
      if (t == null)
        firstWaiter = node
      else
        t.nextWaiter = node
      lastWaiter = node
      node
    }

    private def doSignal(f: Node): Unit = {
      var first: Node = f
      do {
        firstWaiter = first.nextWaiter
        if (firstWaiter == null)
          lastWaiter = null
        first.nextWaiter = null
        first = firstWaiter
      } while (!transferForSignal(first) && first != null)
    }

    private def doSignalAll(f: Node) = {
      var first: Node = f
      firstWaiter = null
      lastWaiter = firstWaiter
      do {
        val next: Node = first.nextWaiter
        first.nextWaiter = null
        transferForSignal(first)
        first = next
      } while (first != null)
    }

    private def unlinkCancelledWaiters(): Unit = {
      var t: Node     = firstWaiter
      var trail: Node = null
      while (t != null) {
        val next: Node = t.nextWaiter
        if (t.waitStatus != Node.CONDITION) {
          t.nextWaiter = null
          if (trail == null)
            firstWaiter = next
          else
            trail.nextWaiter = next
          if (next == null)
            lastWaiter = trail
        } else
          trail = t
        t = next
      }
    }

    final def signal(): Unit = {
      if (!isHeldExclusively())
        throw new IllegalMonitorStateException()
      val first: Node = firstWaiter
      if (first != null)
        doSignal(first)
    }

    final def signalAll(): Unit = {
      if (!isHeldExclusively())
        throw new IllegalMonitorStateException()
      val first: Node = firstWaiter
      if (first != null)
        doSignalAll(first)
    }

    final def awaitUninterruptibly() = {
      val node: Node       = addConditionWaiter()
      val savedState: Long = fullyRelease(node)
      var interrupted      = false
      while (!isOnSyncQueue(node)) {
        LockSupport.park()
        if (Thread.interrupted())
          interrupted = true
      }
      if (acquireQueued(node, savedState) || interrupted)
        selfInterrupt()
    }

    private def checkInterruptWhileWaiting(node: Node): Int = {
      if (Thread.interrupted())
        if (transferAfterCancelledWait(node)) THROW_IE else REINTERRUPT
      else 0
    }

    private def reportInterruptAfterWait(interruptMode: Int): Unit = {
      if (interruptMode == THROW_IE)
        throw new InterruptedException()
      else if (interruptMode == REINTERRUPT)
        selfInterrupt()
    }

    final def await(): Unit = {
      if (Thread.interrupted())
        throw new InterruptedException()
      val node: Node       = addConditionWaiter()
      val savedState: Long = fullyRelease(node)
      var interruptMode    = 0
      var break: Boolean   = false
      while (!isOnSyncQueue(node) && !break) {
        LockSupport.park()
        interruptMode = checkInterruptWhileWaiting(node)
        if (interruptMode != 0)
          break = true
      }
      if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
        interruptMode = REINTERRUPT
      if (node.nextWaiter != null)
        unlinkCancelledWaiters()
      if (interruptMode != 0)
        reportInterruptAfterWait(interruptMode)
    }

    final def awaitNanos(nt: Long): Long = {
      var nanosTimeout: Long = nt
      if (Thread.interrupted())
        throw new InterruptedException()
      val node: Node         = addConditionWaiter()
      val savedState: Long   = fullyRelease(node)
      var lastTime: Long     = System.nanoTime()
      var interruptMode: Int = 0
      var break: Boolean     = false
      while (!isOnSyncQueue(node) && !break) {
        if (nanosTimeout <= 0L) {
          transferAfterCancelledWait(node)
          break = true
        }
        if (!break) {
          LockSupport.parkNanos(nanosTimeout)
          interruptMode = checkInterruptWhileWaiting(node)
          if (interruptMode != 0)
            break = true

          val now: Long = System.nanoTime()
          nanosTimeout -= now - lastTime
          lastTime = now
        }
      }
      if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
        interruptMode = REINTERRUPT
      if (node.nextWaiter != null)
        unlinkCancelledWaiters()
      if (interruptMode != 0)
        reportInterruptAfterWait(interruptMode)
      nanosTimeout - (System.nanoTime() - lastTime)
    }

    final def awaitUntil(deadline: Date): Boolean = {
      if (deadline == null)
        throw new NullPointerException()
      val abstime: Long = deadline.getTime()
      if (Thread.interrupted())
        throw new InterruptedException()
      val node: Node       = addConditionWaiter()
      val savedState: Long = fullyRelease(node)
      var timedout         = false
      var interruptMode    = 0
      var break: Boolean   = false
      while (!isOnSyncQueue(node) && !break) {
        if (System.currentTimeMillis() > abstime) {
          timedout = transferAfterCancelledWait(node)
          break = true
        }
        if (!break) {
          LockSupport.parkUntil(abstime)
          interruptMode = checkInterruptWhileWaiting(node)
          if (interruptMode != 0)
            break = true
        }
      }
      if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
        interruptMode = REINTERRUPT
      if (node.nextWaiter != null)
        unlinkCancelledWaiters()
      if (interruptMode != 0)
        reportInterruptAfterWait(interruptMode)
      !timedout
    }

    final def await(time: Long, unit: TimeUnit): Boolean = {
      if (unit == null)
        throw new NullPointerException()
      var nanosTimeout: Long = unit.toNanos(time)
      if (Thread.interrupted())
        throw new InterruptedException()
      val node: Node         = addConditionWaiter()
      val savedState: Long   = fullyRelease(node)
      var lastTime: Long     = System.nanoTime()
      var timedout           = false
      var interruptMode: Int = 0
      var break: Boolean     = false
      while (!isOnSyncQueue(node) && !break) {
        if (nanosTimeout <= 0L) {
          timedout = transferAfterCancelledWait(node)
          break = true
        }
        if (!break) {
          if (nanosTimeout >= spinForTimeoutThreshold)
            LockSupport.parkNanos(nanosTimeout)
          interruptMode = checkInterruptWhileWaiting(node)
          if (interruptMode != 0)
            break = true
          if (!break) {
            val now: Long = System.nanoTime()
            nanosTimeout -= now - lastTime
            lastTime = now
          }
        }
      }
      if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
        interruptMode = REINTERRUPT
      if (node.nextWaiter != null)
        unlinkCancelledWaiters()
      if (interruptMode != 0)
        reportInterruptAfterWait(interruptMode)
      !timedout
    }

    final def isOwnedBy(sync: AbstractQeueuedLongSynchronizer): Boolean =
      sync == AbstractQeueuedLongSynchronizer.this

    protected[locks] final def hasWaiters(): Boolean = {
      if (!isHeldExclusively())
        throw new IllegalMonitorStateException()
      var w: Node = firstWaiter
      while (w != null) {
        if (w.waitStatus == Node.CONDITION)
          return true
        w = w.nextWaiter
      }
      false
    }

    protected[locks] final def getWaitQueueLength(): Int = {
      if (!isHeldExclusively())
        throw new IllegalMonitorStateException()
      var n       = 0
      var w: Node = firstWaiter
      while (w != null) {
        if (w.waitStatus == Node.CONDITION)
          n += 1
        w = w.nextWaiter
      }
      n
    }

    protected[locks] final def getWaitingThreads(): util.Collection[Thread] = {
      if (!isHeldExclusively())
        throw new IllegalMonitorStateException()
      val list: util.ArrayList[Thread] = new util.ArrayList[Thread]()
      var w: Node                      = firstWaiter
      while (w != null) {
        if (w.waitStatus == Node.CONDITION) {
          val t: Thread = w.thread
          if (t != null)
            list.add(t)
          w = w.nextWaiter
        }
      }
      list
    }

  }

  object ConditionObject {

    private final val serialVersionUID: Long = 1173984872572414699L

    private final val REINTERRUPT: Int = 1

    private final val THROW_IE: Int = -1

  }

  private final def compareAndSetHead(update: Node): Boolean =
    head.compareAndSwapStrong(head, update)

  private final def compareAndSetTail(expect: Node, update: Node): Boolean =
    tail.compareAndSwapStrong(expect, update)
}

object AbstractQeueuedLongSynchronizer {

  private final val serialVersionUID: Long = 7373984972572414692L

  final val spinForTimeoutThreshold: Long = 1000L

  private def shouldParkAfterFailedAcquire(pr: Node, node: Node): Boolean = {
    var pred: Node = pr
    val ws: Int    = pred.waitStatus
    if (ws == Node.SIGNAL) return true
    if (ws > 0) {
      do {
        pred = pred.prev
        node.next.store(pred)
      } while (pred.waitStatus > 0)
      pred.next.store(node)
    } else {
      compareAndSetWaitStatus(pred, ws, Node.SIGNAL)
    }
    false
  }

  private def selfInterrupt(): Unit = Thread.currentThread().interrupt()

  private final def compareAndSetWaitStatus(node: Node,
                                            expect: Int,
                                            update: Int): Boolean =
    node.waitStatus.compareAndSwapStrong(expect, update)

  private final def compareAndSetNext(node: Node, expect: Node, update: Node) =
    node.next.compareAndSwapStrong(expect, update)

  final class Node {

    import Node._

    var waitStatus: CAtomicInt = CAtomicInt()

    //volatile
    var prev: CAtomicRef[Node] = CAtomicRef[Node]

    //volatile
    var next: CAtomicRef[Node] = CAtomicRef[Node]

    //volatile
    var thread: CAtomicRef[Thread] = CAtomicRef[Thread]

    var nextWaiter: Node = _

    def this(thread: Thread, mode: Node) = {
      this()
      this.nextWaiter = mode
      this.thread.store(thread)
    }

    def this(thread: Thread, waitStatus: Int) = {
      this()
      this.waitStatus.store(waitStatus)
      this.thread.store(thread)
    }

    def isShared: Boolean = nextWaiter == SHARED

    def predecessor(): Node = {
      val p = prev
      if (p == null)
        throw new NullPointerException()
      else p
    }

  }

  object Node {

    final val SHARED: Node = new Node()

    final val EXCLUSIVE: Node = null

    final val CANCELLED: Int = 1

    final val SIGNAL: Int = -1

    final val CONDITION: Int = -2

    final val PROPAGATE: Int = -3

  }

}
