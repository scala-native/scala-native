// Ported from JSR-166, revision: 1.411

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent

import java.lang.Thread.UncaughtExceptionHandler
import java.lang.invoke.VarHandle
import java.util.concurrent.ForkJoinPool.WorkQueue.getAndClearSlot
import java.util.{ArrayList, Collection, Collections, List, concurrent}
import java.util.function.Predicate
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.Condition
import scala.scalanative.annotation._
import scala.scalanative.unsafe._
import scala.scalanative.libc.atomic.{CAtomicInt, CAtomicLongLong, CAtomicRef}
import scala.scalanative.runtime.{fromRawPtr, Intrinsics, ObjectArray}

import scala.scalanative.libc.atomic.memory_order._
import ForkJoinPool._

class ForkJoinPool private (
    factory: ForkJoinPool.ForkJoinWorkerThreadFactory,
    val ueh: UncaughtExceptionHandler,
    saturate: Predicate[ForkJoinPool],
    keepAlive: Long,
    workerNamePrefix: String,
    bounds: Long,
    config: Int
) extends AbstractExecutorService {
  import WorkQueue._

  @volatile var runState: Int = 0 // SHUTDOWN, STOP, TERMINATED bits
  @volatile var stealCount: Long = 0
  @volatile var threadIds: Long = 0
  @volatile var ctl: Long = _ // main pool control

  final var parallelism: Int = _ // target number of workers
  final val registrationLock = new ReentrantLock()

  private[concurrent] var queues: Array[WorkQueue] = _ // main registry
  private[concurrent] var termination: Condition = _

  // Support for atomic operations

  @alwaysinline private def ctlAtomic = new CAtomicLongLong(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "ctl"))
  )
  @alwaysinline private def runStateAtomic = new CAtomicInt(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "runState"))
  )
  @alwaysinline private def threadIdsAtomic = new CAtomicLongLong(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "threadIds"))
  )
  @alwaysinline private def parallelismAtomic = new CAtomicInt(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "parallelism"))
  )

  @alwaysinline
  private def compareAndSetCtl(c: Long, v: Long): Boolean =
    ctlAtomic.compareExchangeStrong(c, v)
  @alwaysinline
  private def compareAndExchangeCtl(c: Long, v: Long): Long = {
    val expected = stackalloc[Long]()
    !expected = c
    ctlAtomic.compareExchangeStrong(expected, v)
    !expected
  }
  @alwaysinline
  private def getAndAddCtl(v: Long): Long = ctlAtomic.fetchAdd(v)
  @alwaysinline
  private def getAndBitwiseOrRunState(v: Int): Int = runStateAtomic.fetchOr(v)
  @alwaysinline
  private def incrementThreadIds(): Long = threadIdsAtomic.fetchAdd(1L)
  @alwaysinline
  private def getAndSetParallelism(v: Int): Int = parallelismAtomic.exchange(v)
  @alwaysinline
  private def getParallelismOpaque(): Int =
    parallelismAtomic.load(memory_order_relaxed)

  // Creating, registering, and deregistering workers

  private def createWorker(): Boolean = {
    val fac = factory
    var wt: ForkJoinWorkerThread = null
    var ex: Throwable = null
    try
      if (runState >= 0 && // avoid construction if terminating
          fac != null && { wt = fac.newThread(this); wt != null }) {
        wt.start()
        return true
      }
    catch { case rex: Throwable => ex = rex }
    deregisterWorker(wt, ex)
    false
  }

  final private[concurrent] def nextWorkerThreadName(): String = {
    val tid = incrementThreadIds() + 1
    val prefix = workerNamePrefix match {
      case null   => "ForkJoinPool.commonPool-worker-"
      case prefix => prefix
    }
    prefix.concat(java.lang.Long.toString(tid))
  }

  final private[concurrent] def registerWorker(w: WorkQueue): Unit = {
    ThreadLocalRandom.localInit()
    val seed = ThreadLocalRandom.getProbe()
    val lock = registrationLock
    var cfg = config & FIFO
    if (w != null && lock != null) {
      w.array = new Array[ForkJoinTask[_]](INITIAL_QUEUE_CAPACITY)
      cfg |= w.config | SRC
      w.stackPred = seed // stash for runWorker

      var id: Int = (seed << 1) | 1 // initial index guess
      lock.lock()
      try {
        val qs = queues
        var n: Int = if (qs != null) qs.length else 0 // find queue index
        if (n > 0) {
          var k: Int = n
          val m: Int = n - 1

          while ({ id &= m; qs(id) != null && k > 0 }) {
            id -= 2
            k -= 2
          }
          if (k == 0) id = n | 1 // resize below
          w.config = id | cfg // now publishable
          w.phase = w.config

          if (id < n) qs(id) = w
          else { // expand array
            val an: Int = n << 1
            val am: Int = an - 1
            val as: Array[WorkQueue] = new Array[WorkQueue](an)
            as(id & am) = w
            for (j <- 1 until n by 2) as(j) = qs(j)
            for (j <- 0 until n by 2) {
              val q: WorkQueue = qs(j)
              if (q != null) { // shared queues may move
                as(q.config & am) = q
              }
            }
            VarHandle.releaseFence() // fill before publish
            queues = as
          }
        }
      } finally lock.unlock()
    }
  }

  final private[concurrent] def deregisterWorker(
      wt: ForkJoinWorkerThread,
      ex: Throwable
  ): Unit = {
    val w = if (wt == null) null else wt.workQueue
    var cfg = if (w == null) 0 else w.config
    var c = ctl
    if ((cfg & TRIMMED) == 0) {
      while ({
        val newC = (RC_MASK & (c - RC_UNIT)) |
          (TC_MASK & (c - TC_UNIT)) |
          (SP_MASK & c)
        c != { c = compareAndExchangeCtl(c, newC); c }
      }) ()
    } else if (c.toInt == 0) // was dropped on timeout
      cfg &= ~SRC // suppress signal if last

    if (!tryTerminate(false, false) && w != null) {
      val ns = w.nsteals & 0xffffffffL
      val lock = registrationLock
      if (lock != null) {
        lock.lock() // remove index unless terminating
        val qs = queues
        val n = if (qs != null) qs.length else 0
        val i = cfg & (n - 1)
        if (n > 0 && (qs(i) eq w))
          qs(i) == null
        stealCount += ns // accumulate steals
        lock.unlock()
      }
      if ((cfg & SRC) != 0)
        signalWork() // possibly replace worker
    }
    if (ex != null) {
      if (w != null) {
        w.access = STOP
        while ({
          val t = w.nextLocalTask(0)
          ForkJoinTask.cancelIgnoringExceptions(t)
          t != null
        }) ()
      }
      ForkJoinTask.rethrow(ex)
    }
  }

  /*
   * Tries to create or release a worker if too few are running.
   */
  final private[concurrent] def signalWork(): Unit = {
    var c: Long = ctl
    val pc = parallelism
    val qs = queues
    val n = if (qs != null) qs.length else 0
    if ((c >>> RC_SHIFT).toShort < pc && n > 0) {
      var break = false
      while (!break) {
        var create = false
        val sp = c.toInt & ~INACTIVE
        val v = qs(sp & (n - 1))
        val deficit: Int = pc - (c >>> TC_SHIFT).toShort
        val ac: Long = (c + RC_UNIT) & RC_MASK
        var nc = 0L
        if (sp != 0 && v != null)
          nc = (v.stackPred & SP_MASK) | (c & TC_MASK)
        else if (deficit <= 0)
          break = true
        else {
          create = true
          nc = ((c + TC_UNIT) & TC_MASK)
        }
        if (!break &&
            c == { c = compareAndExchangeCtl(c, nc | ac); c }) {
          if (create)
            createWorker()
          else {
            val owner = v.owner
            v.phase = sp
            if (v.access == PARKED)
              LockSupport.unpark(owner)
          }
          break = true
        }
      }
    }
  }

  private def reactivate(): WorkQueue = {
    var c = ctl
    val qs = queues
    val n = if (qs != null) qs.length else 0
    if (n > 0) {
      while (true) {
        val sp = c.toInt & ~INACTIVE
        val v = qs(sp & (n - 1))
        val ac = UC_MASK & (c + RC_UNIT)
        if (sp == 0 || v == null)
          return null
        if (c == {
              c = compareAndExchangeCtl(c, (v.stackPred & SP_MASK) | ac); c
            }) {
          val owner = v.owner
          v.phase = sp
          if (v.access == PARKED)
            LockSupport.unpark(owner)
          return v
        }
      }
    }
    null
  }

  private def tryTrim(w: WorkQueue): Boolean = {
    if (w != null) {
      val pred = w.stackPred
      val cfg = w.config | TRIMMED
      val c = ctl
      val sp = c.toInt & ~INACTIVE
      if ((sp & SMASK) == (cfg & SMASK) &&
          compareAndSetCtl(c, (pred & SP_MASK) | (UC_MASK & (c - TC_UNIT)))) {
        w.config = cfg // add sentinel for deregisterWorker
        w.phase = sp
        return true
      }
    }
    false
  }

  private def hasTasks(submissionsOnly: Boolean): Boolean = {
    val step = if (submissionsOnly) 2 else 1
    var checkSum = 0
    while (true) { // repeat until stable (normally twice)
      VarHandle.acquireFence()
      val qs = queues
      val n = if (qs == null) 0 else qs.length
      var sum = 0
      var i = 0
      while (i < n) {
        val q = qs(i)
        if (q != null) {
          val s = q.top
          if (q.access > 0 || s != q.base)
            return true
          sum += (s << 16) + i + 1
        }
        i += step
      }
      if (checkSum == sum) return false
      else checkSum = sum
    }
    false // unreachable
  }

  final private[concurrent] def runWorker(w: WorkQueue): Unit = {
    if (w != null) { // skip on failed init

      var r: Int = w.stackPred
      var src: Int = 0 // use seed from registerWorker

      @inline def tryScan(): Boolean = {
        src = scan(w, src, r)
        src >= 0
      }

      @inline def tryAwaitWork(): Boolean = {
        src = awaitWork(w)
        src == 0
      }

      while ({
        r ^= r << 13
        r ^= r >>> 17
        r ^= r << 5 // xorshift
        tryScan() || tryAwaitWork()
      }) ()
      w.access = STOP; // record normal termination
    }
  }

  private def scan(w: WorkQueue, prevSrc: Int, r0: Int): Int = {
    val qs: Array[WorkQueue] = queues
    val n: Int = if (w == null || qs == null) 0 else qs.length
    var r = r0
    val step: Int = (r >>> 16) | 1
    var i: Int = n
    while (i > 0) {
      val j = r & (n - 1)
      val q = qs(j) // poll at qs[j].array[k]
      val a = if (q != null) q.array else null
      val cap = if (a != null) a.length else 0
      if (cap > 0) {
        val src: Int = j | SRC
        val b = q.base
        val k = (cap - 1) & b
        val nb = b + 1
        val nk = (cap - 1) & nb
        val t: ForkJoinTask[_] = a(k)
        VarHandle.acquireFence()
        if (q.base != b) { // inconsistent
          return prevSrc
        } else if (t != null && WorkQueue.casSlotToNull(a, k, t)) {
          q.base = nb
          w.source = src
          if (prevSrc == 0 && q.base == nb && a(nk) != null)
            signalWork() // propagate
          w.topLevelExec(t, q)
          return src
        } else if ((q.array ne a) || a(k) != null || a(nk) != null) {
          return prevSrc // revisit
        }
      }
      i -= 1
      r += step
    }
    -1
  }

  private def awaitWork(w: WorkQueue): Int = {
    if (w == null)
      return -1 // already terminated
    var p: Int = (w.phase + SS_SEQ) & ~INACTIVE
    var idle = false // true if possibly quiescent
    if (runState < 0)
      return -1 // terminating
    val sp: Long = p & SP_MASK
    var pc: Long = ctl
    var qc: Long = 0
    w.phase = p | INACTIVE
    while ({
      w.stackPred = pc.toInt
      qc = ((pc - RC_UNIT) & UC_MASK) | sp
      pc != { pc = compareAndExchangeCtl(pc, qc); pc }
    }) ()

    if ((qc & RC_MASK) <= 0L) {
      if (hasTasks(true) && (w.phase >= 0 || (reactivate() eq w)))
        return 0 // check for stragglers
      if (runState != 0 && tryTerminate(false, false))
        return -1 // quiescent termination
      idle = true
    }

    val qs = queues // spin for expected #accesses in scan+signal
    var spins = if (qs == null) 0 else ((qs.length & SMASK) << 1) | 0xf
    while ({ p = w.phase; p < 0 } && { spins -= 1; spins > 0 })
      Thread.onSpinWait()

    if (p < 0) {
      var deadline = if (idle) keepAlive + System.currentTimeMillis() else 0L
      LockSupport.setCurrentBlocker(this)

      var break = false
      while (!break) { // await signal or termination
        if (runState < 0)
          return -1
        w.access = PARKED
        if (w.phase < 0) {
          if (idle)
            LockSupport.parkUntil(deadline)
          else
            LockSupport.park()
        }
        w.access = 0
        if (w.phase >= 0) {
          LockSupport.setCurrentBlocker(null)
          break = true
        } else {
          Thread.interrupted() // clear status for next park
          if (idle) { // check for idle timeout
            if (deadline - System.currentTimeMillis() < TIMEOUT_SLOP) {
              if (tryTrim(w))
                return -1
              else
                deadline += keepAlive
            }
          }
        }
      }
    }
    0
  }

  /** Non-overridable version of isQuiescent. Returns true if quiescent or
   *  already terminating.
   */
  private def canStop(): Boolean = {
    var c = ctl
    while ({
      if (runState < 0)
        return true
      if ((c & RC_MASK) > 0L || hasTasks(false))
        return false
      c != { c = ctl; c } // validate
    }) ()
    true
  }

  private def pollScan(submissionsOnly: Boolean): ForkJoinTask[_] = {
    var r = ThreadLocalRandom.nextSecondarySeed()
    if (submissionsOnly)
      r &= ~1
    val step = if (submissionsOnly) 2 else 1
    val qs = queues
    val n = if (qs != null) qs.length else 0
    if (runState >= 0 && n > 0) {
      var i = n
      while (i > 0) {
        val q = qs(r & (n - 1))
        if (q != null) {
          val t = q.poll(this)
          if (t != null) return t
        }
        i -= step
        r += step
      }
    }
    null
  }

  private def tryCompensate(c: Long, canSaturate: Boolean): Int = {
    val b = bounds // unpack fields
    val pc = parallelism
    // counts are signed centered at parallelism level == 0
    val minActive: Int = (b & SMASK).toShort
    val maxTotal: Int = (b >>> SWIDTH).toShort + pc
    val active: Int = (c >>> RC_SHIFT).toShort
    val total: Int = (c >>> TC_SHIFT).toShort
    val sp: Int = c.toInt & ~INACTIVE

    if (sp != 0 && active <= pc) {
      val qs = queues
      val i = sp & SMASK
      val v = if (qs != null && qs.length > i) qs(i) else null
      if (ctl == c && v != null) {
        val nc = (v.stackPred & SP_MASK) | (UC_MASK & c)
        if (compareAndSetCtl(c, nc)) {
          v.phase = sp
          LockSupport.unpark(v.owner)
          return UNCOMPENSATE
        }
      }
      -1 // retry
    } else if (active > minActive && total >= pc) { // reduce active workers
      val nc = ((RC_MASK & (c - RC_UNIT)) | (~RC_MASK & c))
      if (compareAndSetCtl(c, nc)) UNCOMPENSATE else -1
    } else if (total < maxTotal && total < MAX_CAP) { // expand pool
      val nc = ((c + TC_UNIT) & TC_MASK) | (c & ~TC_MASK)
      if (!compareAndSetCtl(c, nc)) -1
      else if (!createWorker()) 0
      else UNCOMPENSATE
    } else if (!compareAndSetCtl(c, c)) // validate
      -1
    else if (canSaturate || (saturate != null && saturate.test(this)))
      0
    else
      throw new RejectedExecutionException(
        "Thread limit exceeded replacing blocked worker"
      )
  }

  final private[concurrent] def uncompensate(): Unit = {
    getAndAddCtl(RC_UNIT)
  }

  final private[concurrent] def helpJoin(
      task: ForkJoinTask[_],
      w: WorkQueue,
      timed: Boolean
  ): Int = {
    if (w == null || task == null)
      return 0

    val wsrc: Int = w.source
    val wid: Int = (w.config & SMASK) | SRC
    var r: Int = wid + 2
    var sctl = 0L // track stability
    var rescan: Boolean = true
    while (true) {
      var s = task.status
      if (s < 0)
        return s
      if (!rescan && sctl == { sctl = ctl; sctl }) {
        if (runState < 0)
          return 0
        s = tryCompensate(sctl, timed)
        if (s >= 0)
          return s // block
      }
      rescan = false
      val qs = queues
      val n = if (qs != null) qs.length else 0
      val m = n - 1
      // scan
      var breakScan = false
      var i = n >>> 1
      while (!breakScan && i > 0) {
        val j = r & m
        val q = qs(j)
        val a = if (q != null) q.array else null
        val cap = if (a != null) a.length else 0
        if (cap > 0) {
          var src = j | SRC
          var break = false
          while (!breakScan && !break) {
            val sq = q.source
            val b = q.base
            val k = (cap - 1) & b
            val nb = b + 1
            val t = a(k)
            VarHandle.acquireFence() // for re-reads
            var eligible = true // check steal chain
            var breakInner = false
            var d = n
            var v = sq
            while (!breakInner) { // may be cyclic; bound
              lazy val p = qs(v & m)
              if (v == wid)
                breakInner = true
              else if (v == 0 || { d -= 1; d == 0 } || p == null) {
                eligible = false
                breakInner = true
              } else {
                v = p.source
              }
            }
            if (q.source != sq || q.base != b) () // stale
            else if ({ s = task.status; s < 0 })
              return s // recheck before taking
            else if (t == null) {
              if (a(k) == null) {
                if (!rescan && eligible &&
                    ((q.array ne a) || q.top != b))
                  rescan = true // resized or stalled
                break = true
              }
            } else if ((t eq task) && !eligible)
              break = true
            else if (WorkQueue.casSlotToNull(a, k, t)) {
              q.base = nb
              w.source = src
              t.doExec()
              w.source = wsrc
              rescan = true
              break = true
              breakScan = true
            }
          }
        }
        i -= 1
        r += 2
      }
    }
    -1 // unreachable
  }

  final private[concurrent] def helpComplete(
      task: ForkJoinTask[_],
      w: WorkQueue,
      owned: Boolean,
      timed: Boolean
  ): Int = {
    if (w == null || task == null)
      return 0
    val wsrc = w.source
    var r = w.config
    var sctl = 0L
    var rescan = true
    while (true) {
      var s = w.helpComplete(task, owned, 0)
      if (s < 0)
        return s
      if (!rescan && sctl == { sctl = ctl; sctl }) {
        if (!owned || runState < 0)
          return 0
        s = tryCompensate(sctl, timed)
        if (s >= 0)
          return s
      }
      rescan = false
      val qs = queues
      val n = if (qs != null) qs.length else 0
      val m = n - 1
      // scan:
      var breakScan = false
      var i = n
      while (!breakScan && i > 0) {
        val j = r & m
        val q = qs(j)
        val a = if (q != null) q.array else null
        val cap = if (a != null) a.length else 0
        if (cap > 0) {
          // poll:
          var breakPoll = false
          val src = j | SRC
          var b = q.base
          while (!breakPoll) {
            val k = (cap - 1) & b
            val nb = b + 1
            val t = a(k)
            VarHandle.acquireFence() // for re-reads
            if (b != { b = q.base; b }) () // stale
            else if ({ s = task.status; s < 0 })
              return s // recheck before taking
            else if (t == null) {
              if (a(k) == null) {
                if (!rescan && // resized or stalled
                    ((q.array ne a) || q.top != b))
                  rescan = true
                breakPoll = true
              }
            } else
              t match {
                case t: CountedCompleter[_] =>
                  var f: CountedCompleter[_] = t
                  var break = false
                  while (!break) {
                    if (f eq task) break = true
                    else if ({ f = f.completer; f == null }) {
                      break = true
                      breakPoll = true
                    }
                  }
                  if (!breakPoll && WorkQueue.casSlotToNull(a, k, t)) {
                    q.base = nb
                    w.source = src
                    t.doExec()
                    w.source = wsrc
                    rescan = true
                    breakPoll = true
                    breakScan = true
                  }
                case _ => breakPoll = true
              }
          }
        }
        i -= 1
        r += 1
      }
    }
    -1 // unreachable
  }

  private def helpQuiesce(
      w: WorkQueue,
      _nanos: Long,
      interruptible: Boolean
  ): Int = {
    val startTime = System.nanoTime()
    var parkTime = 0L
    var nanos = _nanos
    var phase = if (w != null) w.phase else -1
    if (phase < 0) // w.phase set negative when temporarily quiescent
      return 0
    val activePhase = phase
    val inactivePhase = phase | INACTIVE
    var wsrc = w.source
    var r = 0
    var locals = true
    while (true) {
      if (runState < 0) {
        w.phase = activePhase
        return 1
      }
      if (locals) {
        var u = null: ForkJoinTask[_]
        while ({
          u = w.nextLocalTask()
          u != null
        }) u.doExec()
      }

      var rescan, busy = false
      locals = false
      val qs = queues
      val n = if (qs == null) 0 else qs.length
      val m = n - 1
      // scan:
      var breakScan = false
      var i = n
      while (!breakScan && i > 0) {
        val j = m & r
        val q = qs(j)
        if (q != null && (q ne w)) {
          val src = j | SRC
          var break = false
          while (!break) {
            val a = q.array
            val b = q.base
            val cap = if (a != null) a.length else 0
            if (cap <= 0)
              break = true
            else {
              val k = (cap - 1) & b
              val nb = b + 1
              val nk = (cap - 1) & nb
              val t = a(k)
              VarHandle.acquireFence() // for re-reads
              if (q.base != b || (q.array ne a) || (a(k) ne t)) ()
              else if (t == null) {
                if (!rescan) {
                  if (a(nk) != null || q.top - b > 0)
                    rescan = true
                  else if (!busy && q.owner != null && q.phase >= 0)
                    busy = true
                }
                break = true
              } else if (phase < 0) { // reactivate before taking
                phase = activePhase
                w.phase = activePhase
              } else if (WorkQueue.casSlotToNull(a, k, t)) {
                q.base = nb
                w.source = src
                t.doExec()
                w.source = wsrc
                rescan = true
                locals = true
                break = true
                breakScan = true
              }
            }
          }
        }
        i -= 1
        r += 1
      }
      if (rescan) () // retry
      else if (phase >= 0) {
        parkTime = 0L
        phase = inactivePhase
        w.phase = inactivePhase
      } else if (!busy) {
        w.phase = activePhase
        return 1
      } else if (parkTime == 0L) {
        parkTime = 1L << 10 // initially about 1 usec
        Thread.`yield`()
      } else {
        val interrupted = interruptible && Thread.interrupted()
        if (interrupted || System.nanoTime() - startTime > nanos) {
          w.phase = activePhase
          return if (interrupted) -1 else 0
        } else {
          LockSupport.parkNanos(this, parkTime)
          if (parkTime < (nanos >>> 8) && parkTime < (1L << 20))
            parkTime <<= 1 // max sleep approx 1sec or 1% nanos
        }
      }
    }
    -1 // unreachable
  }

  private def externalHelpQuiesce(nanos: Long, interruptible: Boolean): Int = {
    val startTime = System.nanoTime()
    var parkTime = 0L
    while (true) {
      val t = pollScan(false)
      if (t != null) {
        t.doExec()
        parkTime = 0L
      } else if (canStop()) return 1
      else if (parkTime == 0L) {
        parkTime = 1L << 10
        Thread.`yield`()
      } else if ((System.nanoTime() - startTime) > nanos) return 0
      else if (interruptible && Thread.interrupted()) return -1
      else {
        LockSupport.parkNanos(this, parkTime)
        if (parkTime < (nanos >>> 8) && parkTime < (1L << 20))
          parkTime <<= 1
      }
    }
    -1 // unreachable
  }

  final private[concurrent] def nextTaskFor(w: WorkQueue): ForkJoinTask[_] = {
    var t: ForkJoinTask[_] = null.asInstanceOf[ForkJoinTask[_]]
    if (w == null || { t = w.nextLocalTask(); t == null })
      t = pollScan(false)
    t
  }

  // External operations

  final private[concurrent] def submissionQueue(
      isSubmit: Boolean
  ): WorkQueue = {
    val lock = registrationLock
    var r = ThreadLocalRandom.getProbe() match {
      case 0 =>
        ThreadLocalRandom.localInit() // initialize caller's probe
        ThreadLocalRandom.getProbe()
      case n => n
    }
    if (lock != null) { // else init error
      var id = r << 1
      var break = false
      while (!break) {
        val qs = queues
        val n = if (qs != null) qs.length else 0
        if (n <= 0)
          break = true
        else {
          val i = (n - 1) & id
          val q = qs(i)
          if (q == null) {
            val w = new WorkQueue(null, id | SRC)
            w.array = new Array[ForkJoinTask[_]](INITIAL_QUEUE_CAPACITY)
            lock.lock()
            if ((queues eq qs) && qs(i) == null)
              qs(i) = w // else lost race; discard
            lock.unlock()
          } else if (q.getAndSetAccess(1) != 0) { // move and restart
            r = ThreadLocalRandom.advanceProbe(r)
            id = r << 1
          } else if (isSubmit && runState != 0) {
            q.access = 0
            break = true
          } else
            return q
        }
      }
    }
    throw new RejectedExecutionException()
  }

  private def poolSubmit[T](
      signalIfEmpty: Boolean,
      task: ForkJoinTask[T]
  ): ForkJoinTask[T] = {
    VarHandle.storeStoreFence()
    if (task == null) throw new NullPointerException()

    val q = Thread.currentThread() match {
      case wt: ForkJoinWorkerThread if wt.pool eq this =>
        wt.workQueue
      case _ =>
        task.markPoolSubmission()
        submissionQueue(true)
    }
    q.push(task, this, signalIfEmpty)
    task
  }

  final def externalQueue(): WorkQueue = ForkJoinPool.externalQueue(this)

  // Termination

  private def tryTerminate(now: Boolean, enable: Boolean): Boolean = {
    val rs = runState
    if (rs >= 0) { // set SHUTDOWN and/or STOP
      if ((config & ISCOMMON) != 0)
        return false // cannot shutdown
      if (!now) {
        if ((rs & SHUTDOWN) == 0) {
          if (!enable)
            return false
          getAndBitwiseOrRunState(SHUTDOWN)
        }
        if (!canStop())
          return false
      }
      getAndBitwiseOrRunState(SHUTDOWN | STOP)
    }
    val released = reactivate() // try signalling waiter
    val tc: Int = (ctl >>> TC_SHIFT).toShort
    if (released == null && tc > 0) {
      val current = Thread.currentThread()
      val w = current match {
        case wt: ForkJoinWorkerThread => wt.workQueue
        case _                        => null
      }
      val r = if (w == null) 0 else w.config + 1 // stagger traversals
      val qs = queues
      val n = if (qs != null) qs.length else 0
      for (i <- 0 until n) {
        qs((r + i) & (n - 1)) match {
          case null => ()
          case q =>
            val thread = q.owner
            if ((thread ne current) && q.access != STOP) {
              while (q.poll(null) match {
                    case null => false
                    case t =>
                      ForkJoinTask.cancelIgnoringExceptions(t)
                      true
                  }) ()
              if (thread != null && !thread.isInterrupted()) {
                q.forcePhaseActive() // for awaitWork
                try thread.interrupt()
                catch { case ignore: Throwable => () }
              }
            }
        }
      }
    }
    val lock = registrationLock
    if ((tc <= 0 || (ctl >>> TC_SHIFT).toShort <= 0) &&
        (getAndBitwiseOrRunState(TERMINATED) & TERMINATED) == 0 &&
        lock != null) {
      lock.lock()
      termination match {
        case null => ()
        case cond => cond.signalAll()
      }
      lock.unlock()
    }
    true
  }

  // Exported methods
  // Constructors

  def this(
      parallelism: Int,
      factory: ForkJoinPool.ForkJoinWorkerThreadFactory,
      handler: UncaughtExceptionHandler,
      asyncMode: Boolean,
      corePoolSize: Int,
      maximumPoolSize: Int,
      minimumRunnable: Int,
      saturate: Predicate[ForkJoinPool],
      keepAliveTime: Long,
      unit: TimeUnit
  ) = {
    this(
      factory = factory,
      ueh = handler,
      saturate = saturate,
      keepAlive = unit.toMillis(keepAliveTime).max(TIMEOUT_SLOP),
      workerNamePrefix = {
        val pid: String = Integer.toString(getAndAddPoolIds(1) + 1)
        s"ForkJoinPool-$pid-worker-"
      },
      bounds = {
        val p = parallelism
        if (p <= 0 || p > MAX_CAP || p > maximumPoolSize || keepAliveTime <= 0L)
          throw new IllegalArgumentException
        val maxSpares = maximumPoolSize.min(MAX_CAP) - p
        val minAvail = minimumRunnable.max(0).min(MAX_CAP)
        val corep = corePoolSize.max(p).min(MAX_CAP)
        (minAvail & SMASK).toLong |
          (maxSpares << SWIDTH).toLong |
          (corep.toLong << 32)
      },
      config = if (asyncMode) FIFO else 0
    )
    if (factory == null || unit == null) throw new NullPointerException
    val p = parallelism
    val size: Int = 1 << (33 - Integer.numberOfLeadingZeros(p - 1))
    this.parallelism = p
    this.queues = new Array[WorkQueue](size)
  }

  def this(parallelism: Int) = {
    this(
      parallelism,
      ForkJoinPool.defaultForkJoinWorkerThreadFactory,
      null,
      false,
      0,
      ForkJoinPool.MAX_CAP,
      1,
      null,
      ForkJoinPool.DEFAULT_KEEPALIVE,
      TimeUnit.MILLISECONDS
    )
  }

  def this() = this(
    parallelism =
      Math.min(ForkJoinPool.MAX_CAP, Runtime.getRuntime().availableProcessors())
  )

  def this(
      parallelism: Int,
      factory: ForkJoinPool.ForkJoinWorkerThreadFactory,
      handler: UncaughtExceptionHandler,
      asyncMode: Boolean
  ) = {
    this(
      parallelism,
      factory,
      handler,
      asyncMode,
      0,
      ForkJoinPool.MAX_CAP,
      1,
      null,
      ForkJoinPool.DEFAULT_KEEPALIVE,
      TimeUnit.MILLISECONDS
    )
  }

  def invoke[T](task: ForkJoinTask[T]): T = {
    poolSubmit(true, task)
    task.join()
  }

  def execute(task: ForkJoinTask[_]): Unit = {
    poolSubmit(true, task)
  }

  // AbstractExecutorService methods

  override def execute(task: Runnable): Unit = {
    // Scala3 compiler has problems with type intererenfe when passed to externalSubmit directlly
    val taskToUse: ForkJoinTask[_] = task match {
      case task: ForkJoinTask[_] => task // avoid re-wrap
      case _                     => new ForkJoinTask.RunnableExecuteAction(task)
    }
    poolSubmit(true, taskToUse)
  }

  def submit[T](task: ForkJoinTask[T]): ForkJoinTask[T] = {
    poolSubmit(true, task)
  }

  override def submit[T](task: Callable[T]): ForkJoinTask[T] = {
    poolSubmit(true, new ForkJoinTask.AdaptedCallable[T](task))
  }

  override def submit[T](task: Runnable, result: T): ForkJoinTask[T] = {
    poolSubmit(true, new ForkJoinTask.AdaptedRunnable[T](task, result))
  }

  override def submit(task: Runnable): ForkJoinTask[_] = {
    val taskToUse = task match {
      case task: ForkJoinTask[_] => task // avoid re-wrap
      case _ => new ForkJoinTask.AdaptedRunnableAction(task): ForkJoinTask[_]
    }
    poolSubmit(true, taskToUse)
  }

  // Since JDK 19
  def lazySubmit[T](task: ForkJoinTask[T]): ForkJoinTask[T] =
    poolSubmit(false, task)

  // Since JDK 19
  def setParallelism(size: Int): Int = {
    require(size >= 1 && size <= MAX_CAP)
    if ((config & PRESET_SIZE) != 0)
      throw new UnsupportedOperationException("Cannot override System property")
    getAndSetParallelism(size)
  }

  override def invokeAll[T](
      tasks: Collection[_ <: Callable[T]]
  ): List[Future[T]] = {
    val futures = new ArrayList[Future[T]](tasks.size())
    try {
      val it = tasks.iterator()
      while (it.hasNext()) {
        val f = new ForkJoinTask.AdaptedInterruptibleCallable[T](it.next())
        futures.add(f)
        poolSubmit(true, f)
      }
      for (i <- futures.size() - 1 to 0 by -1) {
        futures.get(i).asInstanceOf[ForkJoinTask[_]].quietlyJoin()
      }
      futures
    } catch {
      case t: Throwable =>
        val it = futures.iterator()
        while (it.hasNext()) ForkJoinTask.cancelIgnoringExceptions(it.next())
        throw t
    }
  }

  @throws[InterruptedException]
  override def invokeAll[T](
      tasks: Collection[_ <: Callable[T]],
      timeout: Long,
      unit: TimeUnit
  ): List[Future[T]] = {
    val nanos = unit.toNanos(timeout)
    val futures = new ArrayList[Future[T]](tasks.size())
    try {
      val it = tasks.iterator()
      while (it.hasNext()) {
        val f = new ForkJoinTask.AdaptedInterruptibleCallable[T](it.next())
        futures.add(f)
        poolSubmit(true, f)
      }
      val startTime = System.nanoTime()
      var ns = nanos
      var timedOut = ns < 0L
      for (i <- futures.size() - 1 to 0 by -1) {
        val f = futures.get(i).asInstanceOf[ForkJoinTask[T]]
        if (!f.isDone()) {
          if (!timedOut)
            timedOut = !f.quietlyJoin(ns, TimeUnit.NANOSECONDS)
          if (timedOut)
            ForkJoinTask.cancelIgnoringExceptions(f)
          else
            ns = nanos - (System.nanoTime() - startTime)
        }
      }
      futures
    } catch {
      case t: Throwable =>
        futures.forEach(ForkJoinTask.cancelIgnoringExceptions(_))
        throw t
    }
  }

  @throws[InterruptedException]
  @throws[ExecutionException]
  override def invokeAny[T](tasks: Collection[_ <: Callable[T]]): T = {
    if (tasks.isEmpty()) throw new IllegalArgumentException()
    val n = tasks.size()
    val root = new InvokeAnyRoot[T](n, this)
    val fs = new ArrayList[InvokeAnyTask[T]](n)
    var break = false
    val it = tasks.iterator()
    while (!break && it.hasNext()) {
      it.next() match {
        case null => throw new NullPointerException()
        case c =>
          val f = new InvokeAnyTask[T](root, c)
          fs.add(f)
          poolSubmit(true, f)
          if (root.isDone()) break = true
      }
    }
    try root.get()
    finally {
      fs.forEach(ForkJoinTask.cancelIgnoringExceptions(_))
    }
  }

  @throws[InterruptedException]
  @throws[ExecutionException]
  @throws[TimeoutException]
  override def invokeAny[T](
      tasks: Collection[_ <: Callable[T]],
      timeout: Long,
      unit: TimeUnit
  ): T = {
    val nanos = unit.toNanos(timeout)
    val n = tasks.size()
    if (n <= 0) throw new IllegalArgumentException()
    val root = new InvokeAnyRoot[T](n, this)
    val fs = new ArrayList[InvokeAnyTask[T]](n)
    var break = false
    val it = tasks.iterator()
    while (it.hasNext()) {
      it.next() match {
        case null => throw new NullPointerException()
        case c =>
          val f = new InvokeAnyTask(root, c)
          fs.add(f)
          poolSubmit(true, f)
          if (root.isDone()) break = true
      }
    }
    try root.get(nanos, TimeUnit.NANOSECONDS)
    finally {
      fs.forEach(ForkJoinTask.cancelIgnoringExceptions(_))
    }
  }

  def getFactory(): ForkJoinWorkerThreadFactory = factory

  def getUncaughtExceptionHandler(): UncaughtExceptionHandler = ueh

  def getParallelism(): Int = getParallelismOpaque().max(1)

  def getPoolSize(): Int = (ctl >>> TC_SHIFT).toShort

  def getAsyncMode(): Boolean = (config & FIFO) != 0

  def getRunningThreadCount: Int = {
    val qs = queues
    var rc = 0
    if ((runState & TERMINATED) == 0 && qs != null) {
      for (i <- 1 until qs.length by 2) {
        val q = qs(i)
        if (q != null && q.isApparentlyUnblocked()) rc += 1
      }
    }
    rc
  }

  def getActiveThreadCount(): Int = (ctl >>> RC_SHIFT).toShort.max(0)

  def isQuiescent(): Boolean = canStop()

  def getStealCount(): Long = {
    var count = stealCount
    val qs = queues
    if (queues != null) {
      for {
        i <- 1 until qs.length by 2
        q = qs(i) if q != null
      } count += q.nsteals.toLong & 0xffffffffL
    }
    count
  }

  def getQueuedTaskCount(): Long = {
    var count = 0
    val qs = queues
    if ((runState & TERMINATED) == 0 && qs != null) {
      for {
        i <- 1 until qs.length by 2
        q = qs(i) if q != null
      } count += q.queueSize()
    }
    count
  }

  def getQueuedSubmissionCount(): Int = {
    var count = 0
    val qs = queues
    if ((runState & TERMINATED) == 0 && qs != null) {
      for {
        i <- 0 until qs.length by 2
        q = qs(i) if q != null
      } count += q.queueSize()
    }
    count
  }

  def hasQueuedSubmissions(): Boolean = hasTasks(true)

  protected[concurrent] def pollSubmission(): ForkJoinTask[_] = pollScan(true)

  protected def drainTasksTo(c: Collection[_ >: ForkJoinTask[_]]): Int = {
    var count = 0
    while ({
      val t = pollScan(false)
      t match {
        case null => false
        case t =>
          c.add(t)
          true
      }
    }) {
      count += 1
    }
    count
  }

  override def toString(): String = {
    // Use a single pass through queues to collect counts
    var st: Long = stealCount
    var ss, qt: Long = 0L
    var rc = 0
    if (queues != null) {
      queues.indices.foreach { i =>
        val q = queues(i)
        if (q != null) {
          val size = q.queueSize()
          if ((i & 1) == 0)
            ss += size
          else {
            qt += size
            st += q.nsteals.toLong & 0xffffffffL
            if (q.isApparentlyUnblocked()) { rc += 1 }
          }
        }
      }
    }

    val pc = parallelism
    val c = ctl
    val tc: Int = (c >>> TC_SHIFT).toShort
    val ac: Int = (c >>> RC_SHIFT).toShort match {
      case n if n < 0 => 0 // ignore transient negative
      case n          => n
    }
    val rs = runState

    @alwaysinline
    def stateIs(mode: Int): Boolean = (rs & mode) != 0
    val level =
      if (stateIs(TERMINATED)) "Terminated"
      else if (stateIs(STOP)) "Terminating"
      else if (stateIs(SHUTDOWN)) "Shutting down"
      else "Running"

    return super.toString() +
      "[" + level +
      ", parallelism = " + pc +
      ", size = " + tc +
      ", active = " + ac +
      ", running = " + rc +
      ", steals = " + st +
      ", tasks = " + qt +
      ", submissions = " + ss +
      "]"
  }

  override def shutdown(): Unit = tryTerminate(false, true)

  override def shutdownNow(): List[Runnable] = {
    tryTerminate(true, true)
    Collections.emptyList()
  }

  def isTerminated(): Boolean = (runState & TERMINATED) != 0
  def isTerminating(): Boolean = (runState & (STOP | TERMINATED)) == STOP
  def isShutdown(): Boolean = runState != 0

  @throws[InterruptedException]
  override def awaitTermination(timeout: Long, unit: TimeUnit): Boolean = {
    var nanos = unit.toNanos(timeout)
    var terminated = false
    if ((config & ISCOMMON) != 0) {
      if (helpQuiescePool(this, nanos, true) < 0)
        throw new InterruptedException()
    } else if ({ terminated = (runState & TERMINATED) != 0; !terminated }) {
      tryTerminate(false, false) // reduce transient blocking
      val lock = registrationLock
      if (lock != null && {
            terminated = (runState & TERMINATED) != 0; !terminated
          }) {
        lock.lock()
        try {
          val cond = termination match {
            case null =>
              val cond = lock.newCondition()
              termination = cond
              cond
            case cond => cond
          }
          while ({
            terminated = (runState & TERMINATED) != 0; !terminated
          } && nanos > 0L) {
            nanos = cond.awaitNanos(nanos)
          }
        } finally lock.unlock()
      }
    }
    terminated
  }

  def awaitQuiescence(timeout: Long, unit: TimeUnit): Boolean =
    helpQuiescePool(this, unit.toNanos(timeout), false) > 0

  // Since JDK 19
  override def close(): Unit = {
    if ((config & ISCOMMON) == 0) {
      var terminated = tryTerminate(false, false)
      if (!terminated) {
        shutdown()
        var interrupted = false
        while (!terminated) {
          try {
            terminated = awaitTermination(1L, TimeUnit.DAYS)
          } catch {
            case _: InterruptedException =>
              if (!interrupted) {
                shutdownNow()
                interrupted = true
              }
          }
        }
        if (interrupted) {
          Thread.currentThread().interrupt()
        }
      }
    }
  }

  @throws[InterruptedException]
  private def compensatedBlock(blocker: ManagedBlocker): Unit = {
    if (blocker == null) throw new NullPointerException()
    while (true) {
      val c = ctl
      if (blocker.isReleasable())
        return
      val comp = tryCompensate(c, false)
      if (comp >= 0) {
        val post: Long = if (comp == 0) 0L else RC_UNIT
        val done =
          try blocker.block()
          finally getAndAddCtl(post)
        if (done)
          return
      }
    }
  }

  // AbstractExecutorService.newTaskFor overrides rely on
  // undocumented fact that ForkJoinTask.adapt returns ForkJoinTasks
  // that also implement RunnableFuture.

  override protected[concurrent] def newTaskFor[T](
      runnable: Runnable,
      value: T
  ): RunnableFuture[T] =
    new ForkJoinTask.AdaptedRunnable[T](runnable, value)

  override protected[concurrent] def newTaskFor[T](
      callable: Callable[T]
  ): RunnableFuture[T] =
    new ForkJoinTask.AdaptedCallable[T](callable)
}

object ForkJoinPool {

  trait ForkJoinWorkerThreadFactory {

    def newThread(pool: ForkJoinPool): ForkJoinWorkerThread
  }

  final class DefaultForkJoinWorkerThreadFactory
      extends ForkJoinWorkerThreadFactory {
    override final def newThread(pool: ForkJoinPool): ForkJoinWorkerThread =
      new ForkJoinWorkerThread(null, pool, true, false)
  }

  final private[concurrent] class DefaultCommonPoolForkJoinWorkerThreadFactory
      extends ForkJoinWorkerThreadFactory {

    override final def newThread(pool: ForkJoinPool): ForkJoinWorkerThread = {
      // if (System.getSecurityManager() == null)
      new ForkJoinWorkerThread(null, pool, true, true)
      // else
      // new ForkJoinWorkerThread.InnocuousForkJoinWorkerThread(pool)
    }
  }

  // Constants shared across ForkJoinPool and WorkQueue
  final val DEFAULT_KEEPALIVE = 60000L
  final val TIMEOUT_SLOP = 20L
  final val DEFAULT_COMMON_MAX_SPARES = 256
  final val INITIAL_QUEUE_CAPACITY: Int = 1 << 6
  // Bounds
  final val SWIDTH = 16 // width of short
  final val SMASK = 0xffff // short bits == max index
  final val MAX_CAP = 0x7fff // max #workers - 1
  // pool.runState and workQueue.access bits and sentinels
  final val STOP = 1 << 31
  final val SHUTDOWN = 1
  final val TERMINATED = 2
  final val PARKED = -1
  // {pool, workQueue}.config bits
  final val FIFO = 1 << 16 // fifo queue or access mode
  final val SRC = 1 << 17 // set for valid queue ids
  final val CLEAR_TLS = 1 << 18 // set for Innocuous workers
  final val TRIMMED = 1 << 19 // timed out while idle
  final val ISCOMMON = 1 << 20 // set for common pool
  final val PRESET_SIZE = 1 << 21 // size was set by property

  final val UNCOMPENSATE = 1 << 16 // tryCompensate return
  // Lower and upper word masks
  private val SP_MASK: Long = 0xffffffffL
  private val UC_MASK: Long = ~SP_MASK
  // Release counts
  private val RC_SHIFT: Int = 48
  private val RC_UNIT: Long = 0x0001L << RC_SHIFT
  private val RC_MASK: Long = 0xffffL << RC_SHIFT
  // Total counts
  private val TC_SHIFT: Int = 32
  private val TC_UNIT: Long = 0x0001L << TC_SHIFT
  private val TC_MASK: Long = 0xffffL << TC_SHIFT
  // sp bits
  private final val SS_SEQ = 1 << 16; // version count
  private final val INACTIVE = 1 << 31; // phase bit when idle

  private[concurrent] object WorkQueue {
    // Support for atomic operations
    import scala.scalanative.libc.atomic.memory_order._
    @alwaysinline
    private def arraySlotAtomicAccess[T <: AnyRef](
        a: Array[T],
        i: Int
    ): CAtomicRef[T] = {
      val nativeArray = a.asInstanceOf[ObjectArray]
      val elemRef =
        nativeArray
          .at(i)
          .asInstanceOf[Ptr[T]]
      new CAtomicRef[T](elemRef)
    }

    @alwaysinline
    private[concurrent] def getAndClearSlot(
        a: Array[ForkJoinTask[_]],
        i: Int
    ): ForkJoinTask[_] =
      arraySlotAtomicAccess(a, i)
        .exchange(null: ForkJoinTask[_])

    @alwaysinline
    private[concurrent] def casSlotToNull(
        a: Array[ForkJoinTask[_]],
        i: Int,
        c: ForkJoinTask[_]
    ): Boolean =
      arraySlotAtomicAccess(a, i)
        .compareExchangeWeak(c, null: ForkJoinTask[_])
  }

  final class WorkQueue private (
      val owner: ForkJoinWorkerThread
  ) {
    var config: Int = _ // index, mode, ORed with SRC after init
    var array: Array[ForkJoinTask[_]] = _ // the queued tasks power of 2 size
    var stackPred: Int = 0 // pool stack (ctl) predecessor link
    var base: Int = _ // index of next slot for poll
    var top: Int = _ // index of next slot for push
    @volatile var access: Int = 0 // values 0, 1 (locked), PARKED, STOP
    @volatile var phase: Int = 0 // versioned, negative if inactive
    @volatile var source: Int = 0 // source queue id, lock, or sentinel
    var nsteals: Int = 0 // steals from other queues

    private[concurrent] def this(owner: ForkJoinWorkerThread, config: Int) = {
      this(owner)
      this.config = config
      this.base = 1
      this.top = 1
    }

    @alwaysinline def baseAtomic = new CAtomicInt(
      fromRawPtr[Int](Intrinsics.classFieldRawPtr(this, "base"))
    )
    @alwaysinline def phaseAtomic = new CAtomicInt(
      fromRawPtr[Int](Intrinsics.classFieldRawPtr(this, "phase"))
    )
    @alwaysinline def accessAtomic = new CAtomicInt(
      fromRawPtr[Int](Intrinsics.classFieldRawPtr(this, "access"))
    )

    @alwaysinline final def forcePhaseActive(): Unit =
      phaseAtomic.fetchAnd(0x7fffffff)
    @alwaysinline final def getAndSetAccess(v: Int): Int =
      accessAtomic.exchange(v)
    @alwaysinline final def releaseAccess(): Unit =
      accessAtomic.store(0)

    final def getPoolIndex(): Int =
      (config & 0xffff) >>> 1 // ignore odd/even tag bit

    final def queueSize(): Int = {
      VarHandle.acquireFence()
      0.max(top - base) // ignore transient negative
    }

    final def push(
        _task: ForkJoinTask[_],
        pool: ForkJoinPool,
        signalIfEmpty: Boolean
    ): Unit = {
      var task = _task
      var resize = false
      val s = top
      top += 1
      val b = base
      val a = array
      val cap = if (a != null) a.length else 0
      if (cap > 0) {
        val m = (cap - 1)
        if (m == s - b) {
          resize = true // rapidly grow until large
          val newCap = if (cap < (1 << 24)) cap << 2 else cap << 1
          val newArray =
            try new Array[ForkJoinTask[_]](newCap)
            catch {
              case ex: Throwable =>
                top = s
                access = 0
                throw new RejectedExecutionException("Queue capacity exceeded")
            }
          if (newCap > 0) {
            val newMask = newCap - 1
            var k = s
            while ({
              newArray(k & newMask) = task
              k -= 1
              task = getAndClearSlot(a, k & m)
              task != null
            }) ()
          }
          VarHandle.releaseFence()
          array = newArray
        } else a(m & s) = task
        getAndSetAccess(0)
        if ((resize || (a(m & (s - 1)) == null && signalIfEmpty)) &&
            pool != null)
          pool.signalWork()
      }
    }

    final def nextLocalTask(fifo: Int): ForkJoinTask[_] = {
      var t: ForkJoinTask[_] = null.asInstanceOf[ForkJoinTask[_]]
      val a = array
      val p = top
      val s = p - 1
      var b = base
      val cap = if (a != null) a.length else 0
      if (p - b > 0 && cap > 0) {
        while ({
          val break = {
            val nb = b + 1
            if (fifo == 0 || nb == p) {
              if ({ t = getAndClearSlot(a, (cap - 1) & s); t } != null) top = s
              true // break
            } else if ({ t = getAndClearSlot(a, (cap - 1) & b); t } != null) {
              base = nb
              true // break
            } else {
              while (b == { b = base; b }) {
                VarHandle.acquireFence()
                Thread.onSpinWait() // spin to reduce memory traffic
              }
              false // no-break
            }
          }
          !break && (p - b > 0)
        }) ()
        VarHandle.storeStoreFence() // for timely index updates
      }
      t
    }

    final def nextLocalTask(): ForkJoinTask[_] = nextLocalTask(config & FIFO)

    final def tryUnpush(task: ForkJoinTask[_], owned: Boolean): Boolean = {
      val a = array
      val p = top
      val cap = if (a != null) a.length else 0
      val s = p - 1
      val k = (cap - 1) & s
      if (task != null && base != p && cap > 0 && (a(k) eq task)) {
        if (owned || getAndSetAccess(1) == 0) {
          if (top != p || a(k) != task || getAndClearSlot(a, k) == null)
            access = 0
          else {
            top = s
            access = 0
            return true
          }
        }
      }
      false
    }

    final def peek(): ForkJoinTask[_] = {
      val a = array
      val cfg = config
      val p = top
      var b = base
      val cap = if (a != null) a.length else 0
      if (p != b && cap > 0) {
        if ((cfg & FIFO) == 0)
          return a((cap - 1) & (p - 1))
        else { // skip  over in-progress removal
          while (p - b > 0) {
            a((cap - 1) & b) match {
              case null => b += 1
              case t    => return t
            }
          }

        }
      }
      null
    }

    final def poll(pool: ForkJoinPool): ForkJoinTask[_] = {
      var b = base
      var break = false
      while (!break) {
        val a = array
        val cap = if (a != null) a.length else 0
        if (cap <= 0) break = true // currently impossible
        else {
          val k = (cap - 1) & b
          val nb = b + 1
          val nk = (cap - 1) & nb
          val t = a(k)
          VarHandle.acquireFence() // for re-reads
          if (b != { b = base; b }) () // incosistent
          else if (t != null && WorkQueue.casSlotToNull(a, k, t)) {
            base = nb
            VarHandle.releaseFence()
            if (pool != null && a(nk) != null)
              pool.signalWork() // propagate
            return t
          } else if (array != a || a(k) != null) () // stale
          else if (a(nk) == null && top - b <= 0)
            break = true // empty
        }
      }
      null
    }

    final def tryPool(): ForkJoinTask[_] = {
      var b = base
      val a = array
      val cap = if (a != null) a.length else 0
      if (cap > 0) {
        var break = false
        while (!break) {
          val k = (cap - 1) & b
          val nb = b + 1
          val t = a(k)
          VarHandle.acquireFence() // for re-reads
          if (b != { b = base; b }) () // inconsistent
          else if (t != null) {
            if (WorkQueue.casSlotToNull(a, k, t)) {
              base = nb
              VarHandle.storeStoreFence()
              return t
            }
            break = true // contended
          } else if (a(k) == null)
            break = true // empty or stalled
        }
      }
      null
    }

    // specialized execution methods

    final def topLevelExec(_task: ForkJoinTask[_], src: WorkQueue): Unit = {
      var task = _task
      val cfg = config
      val fifo = cfg & FIFO
      var nstolen = 1
      while (task != null) {
        task.doExec()
        task = nextLocalTask(fifo)
        if (task == null && src != null && {
              task = src.tryPool(); task != null
            })
          nstolen += 1
      }
      nsteals += nstolen
      source = 0
      if ((cfg & CLEAR_TLS) != 0) {
        ThreadLocalRandom.eraseThreadLocals(Thread.currentThread())
      }
    }

    final def tryRemoveAndExec(task: ForkJoinTask[_], owned: Boolean): Int = {
      val a = array
      val p = top
      val s = p - 1
      var d = p - base
      val cap = if (a != null) a.length else 0
      if (task != null && d > 0 && cap > 0) {
        val m = cap - 1
        var i = s
        var break = false
        while (!break) {
          val k = i & m
          val t = a(k)
          if (t eq task) {
            if (!owned && getAndSetAccess(1) != 0)
              break = true // fail if locked
            else if (top != p || (a(k) ne task) ||
                getAndClearSlot(a, k) == null) {
              access = 0
              break = true // missed
            } else {
              if (i != s && i == base)
                base = i + 1 // avoid shift
              else {
                var j = i
                while (j != s) // shift down
                  a(j & m) = getAndClearSlot(a, { j += 1; j & m })
                top = s
              }
              releaseAccess()
              return task.doExec()
            }
          } else if (t == null || { d -= 1; d == 0 })
            break = true
          i -= 1
        }
      }
      0
    }

    final private[concurrent] def helpComplete(
        task: ForkJoinTask[_],
        owned: Boolean,
        _limit: Int
    ): Int = {
      var limit = _limit
      var status = 0
      if (task != null) {
        var breakOuter = false
        while (!breakOuter) {
          status = task.status
          if (status < 0)
            return status
          val a = array
          val cap = if (a != null) a.length else 0
          val p = top
          val s = p - 1
          val k = (cap - 1) & s
          val t = if (cap > 0) a(k) else null
          t match {
            case t: CountedCompleter[_] =>
              var f: CountedCompleter[_] = t
              var break = false
              while (!break) {
                if (f eq task)
                  break = true
                else if ({ f = f.completer; f == null }) {
                  break = true
                  breakOuter = true // ineligible
                }
              }
              if (!breakOuter) {
                if (!owned && getAndSetAccess(1) != 0)
                  breakOuter = true // fail if locked
                else if (top != p || (a(k) ne t) ||
                    getAndClearSlot(a, k) == null) {
                  access = 0
                  breakOuter = true // missed
                }
              }
              if (!breakOuter) {
                top = s
                releaseAccess()
                t.doExec()
                if (limit != 0 && { limit -= 1; limit == 0 })
                  breakOuter = true
              }
            case _ => breakOuter = true
          }
        }
        status = task.status
      }
      status
    }

    final def helpAsyncBlocker(blocker: ManagedBlocker): Unit = {
      if (blocker != null) {
        var break = false
        while (!break) {
          val a = array
          val b = base
          val cap = if (a != null) a.length else 0
          if (cap <= 0 || b == top)
            break = true
          else {
            val k = (cap - 1) & b
            val nb = b + 1
            val nk = (cap - 1) & nb
            val t = a(k)
            VarHandle.acquireFence() // for re-reads
            if (base != b) ()
            else if (blocker.isReleasable())
              break = true
            else if (a(k) ne t) ()
            else if (t != null) {
              if (!t.isInstanceOf[CompletableFuture.AsynchronousCompletionTask])
                break = true
              else if (WorkQueue.casSlotToNull(a, k, t)) {
                base = nb
                VarHandle.storeStoreFence()
                t.doExec()
              }
            } else if (a(nk) == null)
              break = true
          }
        }
      }
    }

    // misc

    final def isApparentlyUnblocked(): Boolean = {
      access != STOP && {
        val wt = owner
        owner != null && {
          val s = wt.getState()
          s != Thread.State.BLOCKED &&
            s != Thread.State.WAITING &&
            s != Thread.State.TIMED_WAITING
        }
      }
    }

    final def setClearThreadLocals(): Unit = config |= CLEAR_TLS
  }

  // TODO should be final, but it leads to problems with static forwarders
  final val defaultForkJoinWorkerThreadFactory: ForkJoinWorkerThreadFactory =
    new DefaultForkJoinWorkerThreadFactory()

  private object commonPoolConfig {
    def prop(sysProp: String) = scala.sys.Prop.IntProp(sysProp)

    private val parallelismOpt = prop(
      "java.util.concurrent.ForkJoinPool.common.parallelism"
    )
    val parallelism = parallelismOpt.option
      .getOrElse(
        1.max(Runtime.getRuntime().availableProcessors() - 1)
      )
      .min(MAX_CAP)
    val presetParallelism = if (parallelismOpt.isSet) PRESET_SIZE else 0

    val maximumSpares =
      prop("java.util.concurrent.ForkJoinPool.common.maximumSpares").option
        .map(_.min(MAX_CAP).max(0))
        .getOrElse(DEFAULT_COMMON_MAX_SPARES)
  }

  private[concurrent] object common
      extends ForkJoinPool(
        factory = defaultForkJoinWorkerThreadFactory,
        ueh = null,
        saturate = null,
        keepAlive = DEFAULT_KEEPALIVE,
        workerNamePrefix = null,
        bounds = (1 | (commonPoolConfig.maximumSpares << SWIDTH)).toLong,
        config = ISCOMMON | commonPoolConfig.presetParallelism
      ) {
    val p = commonPoolConfig.parallelism
    val size =
      if (p == 0) 1
      else 1 << (33 - Integer.numberOfLeadingZeros((p - 1)))
    this.parallelism = p
    this.queues = new Array[WorkQueue](size)
  }

  private val poolIds: AtomicInteger = new AtomicInteger(0)
  @alwaysinline private def getAndAddPoolIds(x: Int): Int =
    poolIds.getAndAdd(x)

  final private[concurrent] def helpQuiescePool(
      pool: ForkJoinPool,
      nanos: Long,
      interruptible: Boolean
  ): Int = {
    @alwaysinline
    def useWorkerthread(wt: ForkJoinWorkerThread): Boolean = {
      val p = wt.pool
      p != null && ((p eq pool) || pool == null)
    }
    Thread.currentThread() match {
      case wt: ForkJoinWorkerThread if (useWorkerthread(wt)) =>
        wt.pool.helpQuiesce(wt.workQueue, nanos, interruptible)
      case _ =>
        val p = if (pool != null) pool else common
        if (p != null)
          p.externalHelpQuiesce(nanos, interruptible)
        else
          0
    }
  }

  private def externalQueue(p: ForkJoinPool): WorkQueue = {
    val r: Int = ThreadLocalRandom.getProbe()
    val qs = if (p != null) p.queues else null
    val n = if (qs != null) qs.length else 0
    if (n > 0 && r != 0) qs((n - 1) & (r << 1))
    else null
  }

  private[concurrent] def commonQueue(): WorkQueue = externalQueue(common)

  private[concurrent] def helpAsyncBlocker(
      e: Executor,
      blocker: ManagedBlocker
  ): Unit = {
    Thread.currentThread() match {
      case wt: ForkJoinWorkerThread =>
        val w =
          if (wt.pool eq e) wt.workQueue
          else if (e eq common) commonQueue()
          else null
        if (w != null) w.helpAsyncBlocker(blocker)
    }
  }

  private[concurrent] def getSurplusQueuedTaskCount(): Int = {
    Thread.currentThread() match {
      case wt: ForkJoinWorkerThread
          if wt.pool != null && wt.workQueue != null =>
        val pool = wt.pool
        val q = wt.workQueue
        val n: Int = q.top - q.base
        var p: Int = pool.parallelism
        val a: Int = (pool.ctl >>> RC_SHIFT).toShort
        n - (if (a > { p >>>= 1; p }) 0
             else if (a > { p >>>= 1; p }) 1
             else if (a > { p >>>= 1; p }) 2
             else if (a > { p >>>= 1; p }) 4
             else 8)
      case _ => 0
    }
  }

  def commonPool(): ForkJoinPool = common
  // assert common != null : "static init error";

  // Task to hold results from InvokeAnyTasks
  @SerialVersionUID(2838392045355241008L)
  final private[concurrent] class InvokeAnyRoot[E](
      n: Int,
      val pool: ForkJoinPool
  ) extends ForkJoinTask[E] {
    @volatile private[concurrent] var result: E = _
    final private[concurrent] val count: AtomicInteger = new AtomicInteger(n)
    final private[concurrent] def tryComplete(c: Callable[E]): Unit = { // called by InvokeAnyTasks
      var ex: Throwable = null
      var failed: Boolean = false
      if (c == null || Thread.interrupted() ||
          (pool != null && pool.runState < 0))
        failed = true
      else if (isDone()) ()
      else {
        try complete(c.call())
        catch {
          case tx: Throwable =>
            ex = tx
            failed = true
        }
      }
      if ((pool != null && pool.runState < 0) ||
          (failed && count.getAndDecrement() <= 1)) {
        trySetThrown(
          if (ex != null) ex
          else new CancellationException()
        )
      }
    }
    override final def exec(): Boolean = false // never forked
    override final def getRawResult(): E = result
    override final def setRawResult(v: E): Unit = result = v
  }

// Variant of AdaptedInterruptibleCallable with results in InvokeAnyRoot
  @SerialVersionUID(2838392045355241008L)
  final private[concurrent] class InvokeAnyTask[E](
      root: InvokeAnyRoot[E],
      callable: Callable[E]
  ) extends ForkJoinTask[E] {
    @volatile var runner: Thread = _

    override final def exec(): Boolean = {
      Thread.interrupted()
      runner = Thread.currentThread()
      root.tryComplete(callable)
      runner = null
      Thread.interrupted()
      true
    }

    override final def cancel(mayInterruptIfRunning: Boolean): Boolean = {
      val stat = super.cancel(false)
      if (mayInterruptIfRunning) runner match {
        case null => ()
        case t =>
          try t.interrupt()
          catch { case ignore: Throwable => () }
      }
      stat
    }
    override final def setRawResult(v: E): Unit = () // unused
    override final def getRawResult(): E = null.asInstanceOf[E]
  }

  def getCommonPoolParallelism(): Int = common.getParallelism()

  trait ManagedBlocker {

    @throws[InterruptedException]
    def block(): Boolean

    def isReleasable(): Boolean
  }

  @throws[InterruptedException]
  def managedBlock(blocker: ManagedBlocker): Unit = {
    Thread.currentThread() match {
      case thread: ForkJoinWorkerThread if thread.pool != null =>
        thread.pool.compensatedBlock(blocker)
      case _ => unmanagedBlock(blocker)
    }
  }

  @throws[InterruptedException]
  private def unmanagedBlock(blocker: ManagedBlocker): Unit = {
    if (blocker == null) throw new NullPointerException()

    while (!blocker.isReleasable() && !blocker.block()) {
      ()
    }
  }

}
