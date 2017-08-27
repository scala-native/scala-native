package scala.concurrent.forkjoin

import java.util
import java.util.Collections
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.RunnableFuture
import java.util.concurrent.TimeUnit

import scalanative.runtime.{CAtomicInt, CAtomicLong, CAtomicRef}
import scalanative.runtime.CAtomicsImplicits._
import scalanative.native.CLong

abstract class CountedCompleter[T] protected extends ForkJoinTask[T] {

  /** This task's completer, or null if none */
  final var completer: CountedCompleter[_] = null
  /** The number of pending tasks until completion */
  //volatile
  var pending = CAtomicInt()

  protected def this(completer: CountedCompleter[_], initialPendingCount: Int) = {
    this()
    this.completer = completer
    pending.store(initialPendingCount)
  }

  protected def this(completer: CountedCompleter[_]) = {
    this()
    this.completer = completer
  }

  def compute(): Unit

  def onCompletion(caller: CountedCompleter[_]): Unit = {}

  def onExceptionalCompletion(ex: Throwable, caller: CountedCompleter[_]): Boolean = true

  final def getCompleter: CountedCompleter[_] = completer

  final def getPendingCount: Int = pending

  final def setPendingCount(count: Int): Unit = pending.store(count)

  final def addToPendingCount(delta: Int): Unit = {
    var c: Int = 0
    do {c = pending} while (!pending.compareAndSwapStrong(c, c + delta))
  }

  final def compareAndSetPendingCount(expected: Int, count: Int): Boolean = {
    pending.compareAndSwapStrong(expected, count)
  }

  final def decrementPendingCountUnlessZero: Int = {
    var c: Int = 0
    do {c = pending} while (c != 0 && !pending.compareAndSwapStrong(c, c - 1))
    c
  }

  final def getRoot: CountedCompleter[_] = {
    var a: CountedCompleter[_] = this
    var p: CountedCompleter[_] = a.completer
    while(p != null) {
      a = p
      p = a.completer
    }
    a
  }

  final def tryComplete(): Unit = {
    var a: CountedCompleter[_] = this
    var s: CountedCompleter[_] = a
    var c: Int = 0
    while(true) {
      c = a.pending
      if(c == 0) {
        a.onCompletion(s)
        s = a
        a = s.completer
        if(a == null) {
          s.quietlyComplete()
          return
        }
      }
      else if(pending.compareAndSwapStrong(c, c - 1))
        return
    }
  }

  final def propagateCompletion(): Unit = {
    var a: CountedCompleter[_] = this
    var s: CountedCompleter[_] = a
    var c: Int = 0
    while(true) {
      c = a.pending
      if(c == 0) {
        s = a
        a = s.completer
        if(a == null) {
          s.quietlyComplete()
          return
        }
      }
      else if(pending.compareAndSwapStrong(c, c - 1))
        return
    }
  }

  override def complete(rawResult: T): Unit = {
    var p: CountedCompleter[_] = null
    setRawResult(rawResult)
    onCompletion(this)
    quietlyComplete()
    p = completer
    if(p != null)
      p.tryComplete()
  }

  final def firstComplete: CountedCompleter[_] = {
    var c: Int = 0
    while(true) {
      c = pending
      if(c == 0)
        this
      else if(pending.compareAndSwapStrong(c, c - 1))
        null
    }
    null
  }

  final def nextComplete: CountedCompleter[_] = {
    val p: CountedCompleter[_] = completer
    if(p != null)
      p.firstComplete
    else {
      quietlyComplete()
      null
    }
  }

  final def quietlyCompleteRoot(): Unit = {
    var a: CountedCompleter[_] = this
    var p: CountedCompleter[_] = null
    while(true) {
      p = a.completer
      if(p == null) {
        a.quietlyComplete()
        return
      }
      a = p
    }
  }

  override def internalPropagateException(ex: Throwable): Unit = {
    var a: CountedCompleter[_] = this
    val s: CountedCompleter[_] = a
    while(a.onExceptionalCompletion(ex, s) && {a = s.completer; a} != null
      && a.status >= 0)
      a.recordExceptionalCompletion(ex)
  }

  override protected final def exec(): Boolean = {
    compute()
    false
  }

  override def getRawResult: T = null.asInstanceOf[T]

  override def setRawResult(v: T): Unit = {}

}

object CountedCompleter {

  private final val serialVersionUID: Long = 5232453752276485070L

  // TODO
  private final var PENDING: Long = 0

}

class ForkJoinPool(parallelism: Int, val factory: ForkJoinPool.ForkJoinWorkerThreadFactory, val ueh: Thread.UncaughtExceptionHandler, asyncMode: Boolean)
  extends AbstractExecutorService {

  import ForkJoinPool._

  def this() = this(Math.min(ForkJoinPool.MAX_CAP, Runtime.getRuntime.availableProcessors()), ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, false)

  def this(p: Int) = this(p, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, false)

  def this(p: Int, c: Long, fac: ForkJoinPool.ForkJoinWorkerThreadFactory, h: Thread.UncaughtExceptionHandler) = {
    this(p, fac, h, false)
    config = p
    ctl.store(c)
    workerNamePrefix = "ForkJoinPool.commonPool-worker-"
  }

  checkPermission()
  if(factory == null)
    throw new NullPointerException()
  if(parallelism <= 0 || parallelism > MAX_CAP)
    throw new IllegalArgumentException()

  val np: Long = (-parallelism).toLong // offset ctl counts
  val pn: Int = nextPoolId
  val sb: StringBuilder = new StringBuilder("ForkJoinPool-")
  sb.append(Integer.toString(pn))
  sb.append("-worker-")

  //volatile
  var pad00, pad01, pad02, pad03, pad04, pad05, pad06: Long = 0
  var pad10, pad11, pad12, pad13, pad14, pad15, pad16, pad17: Object = _
  var pad18, pad19, pad1a, pad1b: Object = _

  /*volatile*/
  val stealCount = CAtomicLong() // collects worker counts
  val ctl = CAtomicLong(((np << AC_SHIFT) & AC_MASK) | ((np << TC_SHIFT) & TC_MASK)) // main pool control
  val plock = CAtomicInt() // shutdown status and seqLock
  val indexSeed = CAtomicInt() // worker/submitter index seed
  /*no longer volatile*/
  var config: Int = parallelism | (if(asyncMode) (FIFO_QUEUE << 16) else 0) // mode and parallelism level
  var workQueues: Array[WorkQueue] = null // main registry
  final var workerNamePrefix: String = sb.toString // to create worker name string

  private def acquirePlock: Int = {
    var spins: Int = PL_SPINS
    var r: Int = 0
    var ps: Int = 0
    var nps: Int = 0

    while(true) {
      ps = plock
      nps = ps + PL_LOCK
      if(((ps & PL_LOCK) == 0) && plock.compareAndSwapStrong(ps, nps))
        return nps
      else if (r == 0) { // randomize spins if possible
        val t: Thread = Thread.currentThread
        var w: WorkQueue = null
        var z: Submitter = null
        w = t.asInstanceOf[ForkJoinWorkerThread].workQueue
        if (t.isInstanceOf[ForkJoinWorkerThread] && w != null) r = w.seed
        else {
          z = submitters.get
          if (z != null) r = z.seed
          else r = 1
        }
      }
      else if (spins >= 0) {
        r ^= r << 1
        r ^= r >>> 3
        r ^= r << 10 // xorshift

        if (r >= 0) {
          spins -= 1; spins
        }
      }
      else if (plock.compareAndSwapStrong(ps, ps | PL_SIGNAL)) {
        this.synchronized {
          if ((plock & PL_SIGNAL) != 0) {
            try {
              wait()
            } catch {
              case ie: InterruptedException =>
                try
                  Thread.currentThread.interrupt()
                catch {
                  case ignore: SecurityException =>
                }
            }
          }
          else notifyAll()
        }
      }
    }
    0 // for the compiler
  }


  private def releasePlock(ps: Int): Unit = {
    plock.store(ps)
    this.synchronized(notifyAll())
  }

  private def tryAddWorker(): Unit = {
    var c: Long = ctl
    var u: Int = (c >>> 32).toInt
    var break: Boolean = false
    while(u < 0 && (u & SHORT_SIGN) != 0 && c.toInt == 0 && !break) {
      val nc: Long = (((u + UTC_UNIT) & UTC_MASK) | ((u + UAC_UNIT) & UAC_MASK)).asInstanceOf[Long] << 32
      if (ctl.compareAndSwapStrong(c, nc)) {
        val fac: ForkJoinWorkerThreadFactory = factory
        var ex: Throwable = null
        val wt: ForkJoinWorkerThread = fac.newThread(this)
        try {
          if (fac != null && wt != null) {
            wt.start()
            break = true
          }
        } catch {
          case e: Throwable =>
            ex = e
        }
        if(!break) deregisterWorker(wt, ex)
        break = true
      }
      c = ctl
      u = (c >>> 32).toInt
    }
  }

  //  Registering and deregistering workers

  def registerWorker(wt: ForkJoinWorkerThread): WorkQueue = {
    val handler: Thread.UncaughtExceptionHandler = ueh
    var ws: Array[WorkQueue] = workQueues
    var s: Int = 0
    var s1: Int = 0
    var ps: Int = plock
    wt.setDaemon(true)
    if (handler != null) wt.setUncaughtExceptionHandler(handler)
    do {s = indexSeed; s1 = s + indexSeed.load()} while (!indexSeed.compareAndSwapStrong(s, s1)) // skip 0

    val w: WorkQueue = new WorkQueue(this, wt, config >>> 16, s)
    if (((ps & PL_LOCK) != 0) || !plock.compareAndSwapStrong(ps, ps + PL_LOCK)) ps = acquirePlock
    val nps: Int = (ps & SHUTDOWN) | ((ps + PL_LOCK) & ~SHUTDOWN)
    try {
      if (ws != null) { // skip if shutting down
        var n: Int = ws.length
        var m: Int = n - 1
        var r: Int = (s << 1) | 1 // use odd-numbered indices
        r &= m
        if (ws(r) != null) { // collision
          var probes: Int = 0
          // step by approx half size
          val step: Int = if (n <= 4) 2 else ((n >>> 1) & EVENMASK) + 2
          r = r + step & m
          while (ws(r) != null) {
            probes = probes + 1
            if (probes >= n) {
              n <<= 1
              ws = util.Arrays.copyOf(ws, n)
              workQueues = ws
              m = n - 1
              probes = 0
            }
            r = r + step & m
          }
        }
        w.poolIndex = r // volatile write orders
        w.eventCount = w.poolIndex

        ws(r) = w
      }
    } finally
      if (!plock.compareAndSwapStrong(ps, nps)) releasePlock(nps)
    wt.setName(workerNamePrefix.concat(Integer.toString(w.poolIndex)))
    w
  }

  def deregisterWorker(wt: ForkJoinWorkerThread, ex: Throwable): Unit = {
    val w: WorkQueue = wt.workQueue
    if(wt != null && w != null) {
      var ps: Int = 0
      w.qlock.store(-1) // ensure set
      val ns: Long = w.nsteals // collect steal count
      var sc: Long = stealCount
      do {sc = stealCount} while (!stealCount.compareAndSwapStrong(sc, sc + ns))
      ps = plock
      if((ps & PL_LOCK) != 0 || !plock.compareAndSwapStrong(ps, {ps = ps + PL_LOCK; ps}))
        ps = acquirePlock
      val nps: Int = (ps & SHUTDOWN) | ((ps + PL_LOCK) & ~SHUTDOWN)
      try {
        val idx: Int = w.poolIndex
        val ws: Array[WorkQueue] = workQueues
        if(ws != null && idx >= 0 && idx < ws.length && ws(idx) == w)
          ws(idx) = null
      } finally {
        if(!plock.compareAndSwapStrong(ps, nps))
          releasePlock(nps)
      }
    }

    var c: Long = ctl //adjust ctl counts
    do {c = ctl} while(!ctl.compareAndSwapStrong(c, ((c - AC_UNIT) & AC_MASK) | ((c - TC_UNIT) & TC_MASK) | (c & ~(AC_MASK | TC_MASK))))
    if(!tryTerminate(false, false) && w != null && w.array != null) {
      w.cancelAll()
      c = ctl
      var ws: Array[WorkQueue] = workQueues
      var p: Thread = null
      var u: Int = (c >>> 32).toInt
      var e: Int = c.toInt
      var i: Int = e & SMASK
      var v: WorkQueue = ws(i)

      var break: Boolean = false
      while(u < 0 && e >= 0 && !break) {
        if(e > 0) { // activate or create replacement
          if(ws == null || i >= ws.length || v == null)
            break = true
          val nc: Long = (v.nextWait & E_MASK).toLong | ((u + UAC_UNIT) << 32).toLong
          if(v.eventCount != (e | INT_SIGN) && !break)
            break = true
          if(ctl.compareAndSwapStrong(c, nc) && !break) {
            v.eventCount = (e + E_SEQ) & E_MASK
            p = v.parker
            if(p != null)
              //U.unpark(p)
            break = true
          }
        } else {
          if(u.toShort < 0 && ! break)
            tryAddWorker()
          break = true
        }

        if(!break) {
          u = (c >>> 32).toInt
          e = c.toInt
          i = e & SMASK
          v = ws(i)
          ws = workQueues
        }
      }
    }
    if(ex == null) // help clean refs on way out
      ForkJoinTask.helpExpungeStaleExceptions()
    else // rethrow
      ForkJoinTask.rethrow(ex)
  }

  // Submissions

  def externalPush(task: ForkJoinTask[_]): Unit = {
    val ws: Array[WorkQueue] = workQueues
    val z: Submitter = submitters.get
    val m: Int = ws.length - 1
    val q: WorkQueue = ws(m & z.seed & SQMASK)
    val a: Array[CAtomicRef[ForkJoinTask[_]]] = q.array
    if (z != null && plock > 0 && ws != null && m >= 0 && q != null && q.qlock.compareAndSwapStrong(0, 1)) { // lock
      val b: Int = q.base
      val s: Int = q.top
      val n: Int = s + 1 - b
      val an: Int = a.length
      if (a != null && an > n) {
        val j = (an - 1) & s
        a(j).store(task)
        q.top = s + 1 // push on to deque

        q.qlock.store(0)
        if (n <= 2) signalWork(q)
        return
      }
      q.qlock.store(0)
    }
    fullExternalPush(task)
  }

  private def fullExternalPush(task: ForkJoinTask[_]): Unit = {
    var r: Int = 0 // random index seed
    var z: Submitter = submitters.get()
    while(true) {
      var ws: Array[WorkQueue] = workQueues
      var q: WorkQueue = null
      var ps: Int = plock
      val m: Int = 0
      var k: Int = 0
      if(z == null) {
        val r1 = indexSeed
        r = r1 + SEED_INCREMENT
        if(indexSeed.compareAndSwapStrong(r1, r) && r != 0) {
          z = Submitter(r)
          submitters.set(z)
        }
      } else if(r == 0) {
        r = z.seed
        r ^= r << 13
        r ^= r >>> 17
        z.seed = r ^ (r << 5)
      } else if(ps < 0)
        throw new RejectedExecutionException()
      else if(ps == 0 || ws == null || m < 0) {
        val p: Int = config & SMASK // find power of two table size
        var n: Int = if(p > 1) p -1 else 1 // ensure at least 2 slots
        n |= n >>> 1
        n |= n >>> 2
        n |= n >>> 4
        n |= n >>> 8
        n |= n >>> 16
        n = (n + 1) << 1
        ws = workQueues
        val nws: Array[WorkQueue] = if(ws == null || ws.length == 0) new Array[WorkQueue](n) else null
        val ps1 = ps
        ps = ps + PL_LOCK
        if((ps1 & PL_LOCK) != 0 || !plock.compareAndSwapStrong(ps1, ps))
          ps = acquirePlock
        ws = workQueues
        if((ws == null || ws.length == 0) && nws != null)
          workQueues = nws
        val nps: Int = (ps & SHUTDOWN) | (ps + PL_LOCK) & ~SHUTDOWN
        if(!plock.compareAndSwapStrong(ps, nps))
          releasePlock(nps)
      } else {
        k = r & m & SQMASK
        q = ws(k)
        if(q != null) {
          if(q.qlock == 0 && q.qlock.compareAndSwapStrong(0, 1)) {
            var a: Array[CAtomicRef[ForkJoinTask[_]]] = q.array
            val s: Int = q.top
            var submitted: Boolean = false
            try { // locked version of push
              if((a != null && a.length > s + 1 - q.base) ||
                (q.growArray != null)) { // must presize
                a = q.growArray
                val j: Int = (a.length - 1) & s
                a(j).store(task)
                q.top = s + 1
                submitted = true
              }
            } finally {
              q.qlock.store(0 )// unlock
            }
            if(submitted) {
              signalWork(q)
              return
            }
          }
          r = 0 // move on failure
        } else {
          ps = plock
          if((ps & PL_LOCK) == 0) { //create new queue
            q = new WorkQueue(this, null, SHARED_QUEUE, r)
            val ps1 = plock
            ps = ps1 + PL_LOCK
            if(((ps & PL_LOCK) != 0) || !plock.compareAndSwapStrong(ps1, ps))
              ps = acquirePlock
            ws = workQueues
            if(ws != null && k < ws.length && ws(k) == null)
              ws(k) = q
            val nps: Int = (ps & SHUTDOWN) | ((ps + PL_LOCK) & ~SHUTDOWN)
            if(!plock.compareAndSwapStrong(ps, nps))
              releasePlock(nps)
          }
          else r = 0 // try elsewhere while lock held
        }
      }
      z = submitters.get()
    }
  }

  // Maintaining ctl counts

  def incrementActiveCount(): Unit = {
    var c: Long = ctl
    while(!ctl.compareAndSwapStrong(c, c + AC_UNIT)) {
      c = ctl
    }
  }

  def signalWork(q: WorkQueue): Unit = {
    val hint: Int = q.poolIndex
    var c: Long = ctl
    var e: Int = 0
    var u: Int = (c >>> 32).toInt
    var i: Int = 0
    var n: Int = 0
    var ws: Array[WorkQueue] = null
    var w: WorkQueue = null
    var p: Thread = null

    var break: Boolean = false
    while(u < 0 && !break) {
      e = c.toInt
      if(e > 0) {
        ws = workQueues
        i = e & SMASK
        w = ws(i)
        if(ws != null && ws.length > i && w != null && w.eventCount == (e | INT_SIGN)) {
          val nc: Long = (w.nextWait & E_MASK).toLong | ((u + UAC_UNIT) << 32).toLong
          if(ctl.compareAndSwapStrong(c, nc)) {
            w.hint = hint
            w.eventCount = (e + E_SEQ) & E_MASK
            p = w.parker
            if(p != null)
              //U.unpark(p)
            break = true
          }
          if(q.top - q.base <= 0 && !break)
            break = true
        } else break = true
      } else {
        if(u.toShort < 0 && ! break)
          tryAddWorker()
        break = true
      }
      if(!break) {
        c = ctl
        u = (c >>> 32).toInt
      }
    }
  }

  // Scanning for tasks

  def runWorker(w: WorkQueue): Unit = {
    w.growArray // allocate queue
    do { w.runTask(scan(w)) } while(w.qlock >= 0)
  }

  private def scan(w: WorkQueue): ForkJoinTask[_] = {
    val ps: Int = plock // read plock before ws
    val ws: Array[WorkQueue] = workQueues
    val m: Int = ws.length - 1
    if(w != null && ws != null && m >= 0) {
      val ec: Int = w.eventCount // ec is negative if inactive
      var r: Int = w.seed
      r ^= r << 13
      r ^= r >>> 17
      r ^= r << 5
      w.seed = r
      w.hint = -1 // update seed and clear hint
      var j: Int = ((m + m + 1) | MIN_SCAN) & MAX_SCAN

      var break: Boolean = false
      do {
        val q: WorkQueue = ws((r + j) & m)
        val b: Int = q.base
        //volatile
        val a: Array[CAtomicRef[ForkJoinTask[_]]] = q.array
        if(q != null && b - q.top < 0 && a != null) { // probably nonempty
          val i: Int = (a.length - 1) & b
          val t: ForkJoinTask[_] = a(i)
          if(q.base == b && ec >= 0 && t != null && a(i).compareAndSwapStrong(t, null.asInstanceOf[ForkJoinTask[_]])) {
            if({q.base = b + 1; q.base} - q.top < 0)
              signalWork(q)
            return t // taken
          }
          else if((ec < 0 || j < m) && (ctl >> AC_SHIFT).toInt <= 0) {
            w.hint = (r + j) & m // help signal below
            break = true // cannot take
          }
        }
        j -= 1
      } while(j >= 0 && !break)

      var h: Int = 0
      var e: Int = 0
      val ns: Int = w.nsteals
      var c: Long = 0
      val sc: Long = stealCount
      var q: WorkQueue = null
      if(ns != 0) {
        if(stealCount.compareAndSwapStrong(sc, sc + ns))
          w.nsteals = 0 // collect steals and rescan
      }
      else if(plock != ps) {} // skip
      else {
        c = ctl
        e = c.toInt
        if(e < 0)
          w.qlock.store(-1) // pool is terminating
        else {
          h = w.hint
          if(h < 0) {
            if(ec >= 0) {
              val nc: Long = ec.toLong | ((c - AC_UNIT) & (AC_MASK | TC_MASK))
              w.nextWait = e
              w.eventCount = ec | INT_SIGN
              if(ctl != c || !ctl.compareAndSwapStrong(c, nc))
                w.eventCount = ec // unmark on CAS failure
              else if((c >> AC_SHIFT).toInt == 1 - (config & SMASK))
                idleAwaitWork(w, nc, c)
            }
            else if(w.eventCount < 0 && ctl == c) {
              val wt: Thread = Thread.currentThread()
              Thread.interrupted() // clear status
              //U.putObject(wt, PARKBLOCKET, this)
              w.parker = wt // emulate LockSupport.park
              if(w.eventCount < 0) // recheck
                //U.park(false, 0L) // block
              w.parker = null
              //U.putObject(wt, PARKBLOCKER, null)
            }
          }
          q = ws(h)
          if((h >= 0 || {h = w.hint; h} >= 0) && q != null) { // signal others before retry
            var p: Thread = null
            c = ctl
            var u: Int = 0
            var i: Int = 0
            var v: WorkQueue = ws(i)
            var s: Int = 0
            var n: Int = (config & SMASK) - 1

            var break: Boolean = false
            while(!break) {
              val idleCount: Int = if(w.eventCount < 0) 0 else -1
              s = idleCount - q.base + q.top
              u = (c >>> 32).toInt
              e = c.toInt
              i = e & SMASK
              v = ws(i)
              val n1 = n
              n = s
              if(s <= n1 && n <= 0 || u >= 0 || e <= 0 || m < i || v == null)
                break = true
              val nc: Long = (v.nextWait & E_MASK).toLong | (u + UAC_UNIT).toLong << 32
              if(v.eventCount != (e | INT_SIGN) || !ctl.compareAndSwapStrong(c, nc) && !break)
                break = true
              if(!break) {
                v.hint = h
                v.eventCount = (e + E_SEQ) & E_MASK
                p = v.parker

                if (p != null)
                  //U.unpark(p)
                n -= 1
                if (n <= 0)
                  break = true
              }
            }
          }
        }
      }
    }
    null
  }

  private def idleAwaitWork(w: WorkQueue, currentCtl: Long, prevCtl: Long): Unit = {
    if(w != null && w.eventCount < 0 && !tryTerminate(false, false) && prevCtl.toInt != 0 &&
      ctl == currentCtl) {
      val dc: Int = -(currentCtl >>> TC_SHIFT).toShort
      val parkTime: Long = if(dc < 0) FAST_IDLE_TIMEOUT else (dc + 1) * IDLE_TIMEOUT
      val deadline: Long = System.nanoTime() + parkTime - TIMEOUT_SLOP
      val wt: Thread = Thread.currentThread()

      var break: Boolean = false
      while(ctl == currentCtl && !break) {
        Thread.interrupted() // timed variant of version in scan()
        //U.putObject(wt, PARKBLOCKER, this)
        w.parker = wt
        if(ctl == currentCtl)
          //U.park(false, parkTime)
        w.parker = null
        //U.putObject(wt, PARKBLOCKER, null)
        if(ctl != currentCtl)
          break = true
        if(deadline - System.nanoTime() <= 0L && ctl.compareAndSwapStrong(currentCtl, prevCtl) && !break) {
          w.eventCount = (w.eventCount + E_SEQ) | E_MASK
          w.hint = -1
          w.qlock.store(-1) // shrink
          break = true
        }
      }
    }
  }

  private def helpSignal(task: ForkJoinTask[_], origin: Int): Unit = {
    val ws: Array[WorkQueue] = workQueues
    var w: WorkQueue = null
    var p: Thread = null
    var c: Long = 0
    val m: Int = ws.length - 1
    var u: Int = (ctl >>> 32).toInt
    var e: Int = 0
    var i: Int = 0
    var s: Int = 0
    if(task != null && task.status >= 0 && u < 0 && (u >> UAC_SHIFT) < 0 &&
      ws != null && m >= 0) {
      var breakOuter: Boolean = false
      var breakInner: Boolean = false
      //Outer
      var k: Int = origin
      var j: Int = m
      while(j >= 0 && !breakOuter) {
        k = k + 1
        val q: WorkQueue = ws(k & m)
        var n: Int = m
        while(!breakInner) { // limit to at most m signals
          if(task.status < 0)
            breakOuter = true
          s = -q.base + q.top
          n = s
          if(q == null || s <= n && n <= 0)
            breakInner = true
          c = ctl
          u = (c >>> 32).toInt
          e = c.toInt
          i = e & E_MASK
          w = ws(i)
          if(u >= 0 || e <= 0 || m < i || w == null)
            breakOuter = true
          val nc: Long = (w.nextWait & E_MASK).toLong | (u + UAC_UNIT).toLong << 32
          if(w.eventCount != (e | INT_SIGN))
            breakOuter = true
          if(ctl.compareAndSwapStrong(c, nc) && !breakInner && !breakOuter) {
            w.eventCount = (e + E_SEQ) & E_MASK
            p = w.parker
            if(p != null)
              //U.unpark(p)
            n -= 1
            if(n <= 0)
              breakInner = true
          }
        }
        if(!breakOuter) j -= 1
      }
    }
  }

  private def tryHelpStealer(joiner: WorkQueue, task: ForkJoinTask[_]): Int = {
    var stat: Int = 0 // bound to avoid cycles
    var steps: Int = 0
    if(joiner != null && task != null) { // hoist null checks
      // label restart

      var breakRestart: Boolean = false
      var break: Boolean = false
      var continueRestart: Boolean = true
      while(!breakRestart && continueRestart) {
        var subtask: ForkJoinTask[_] = task // current target
        var j: WorkQueue = joiner
        var v: WorkQueue = null// v is stealer of subtask
        while(true) {
          val ws: Array[WorkQueue] = workQueues
          val m: Int = ws.length - 1
          val s: Int = task.status
          var h: Int = 0
          if(s < 0) {
            stat = s
            breakRestart = true
          }
          if(ws == null || m <= 0 && !breakRestart)
            breakRestart = true // shutting down
              h = (j.hint | 1) & m
          v = ws(h)
          if(v == null || v.currentSteal != subtask && !breakRestart) {
            var origin: Int = h
            while(!break) { // find stealer
              h = ((h + 2) & m) & 15
              if(h == 1 && subtask.status < 0 || j.currentJoin != subtask)
                continueRestart = false // occasional staleness check
                  v = ws(h)
              if(v != null && v.currentSteal == subtask && !breakRestart && !break && continueRestart) {
                j.hint = h // save hint
                break = true
              }
              if(h == origin && !breakRestart && !break)
                breakRestart = true // cannot find stealer
            }
          }
          while(!break && continueRestart && !breakRestart) { // help stealer or descend to its stealer
            // volatile
            var a: Array[CAtomicRef[ForkJoinTask[_]]] = null
            var b: Int = 0
            if(subtask.status < 0) // consistency checks
              continueRestart = true
                b = v.base - v.top
            a = v.array
            if(b < 0 && a != null && continueRestart) {
              val i: Int = (a.length - 1) & b
              val t: ForkJoinTask[_] = a(i)
              if(subtask.status < 0 || j.currentJoin != subtask || v.currentSteal != subtask)
                continueRestart = true // stale
                  stat = 1 // apparent progress
              if(continueRestart && t != null && v.base == b && a(i).compareAndSwapStrong(t, null.asInstanceOf[ForkJoinTask[_]])) {
                v.base = b + 1 // help stealer
                joiner.runSubtask(t)
              }
              else {
                steps = steps + 1
                if(v.base == b && steps == MAX_HELP && continueRestart && !breakRestart)
                  breakRestart = true // v apparently stalled
              }
            }
            else { // empty -- try to descend
              if(continueRestart && !breakRestart) {
                val next: ForkJoinTask[_] = v.currentJoin
                if (subtask.status < 0 || j.currentJoin != subtask || v.currentSteal != subtask)
                  continueRestart = true // stale
                else {
                  steps = steps + 1
                  if (next == null || steps == MAX_HELP)
                    breakRestart = true // dead-end or maybe cyclic
                  else {
                    subtask = next
                    j = v
                    break = true
                  }
                }
              }
            }
          }
        }
      }
    }
    stat
  }

  private def helpComplete(task: ForkJoinTask[_], mode: Int): Int = {
    val ws: Array[WorkQueue] = workQueues
    var q: WorkQueue = null
    val m: Int = ws.length - 1
    var n: Int = 0
    var s: Int = 0
    var u: Int = 0
    if(task != null && ws != null && m >= 0) {
      var j: Int = 1
      var origin: Int = j

      var break: Boolean = false
      while(!break) {
        s = task.status
        if(s < 0)
          return s
        q = ws(j & m)
        if(q != null && q.pollAndExecCC(task)) {
          origin = j
          u = (ctl >>> 32).toInt
          if(mode == SHARED_QUEUE && u >= 0 || (u >> UAC_SHIFT) >= 0)
            break = true
        }
        else {
          j = (j + 2) & m
          if(j == origin)
            break = true
        }
      }
    }
    0
  }

  def tryCompensate: Boolean = {
    val pc: Int = config & SMASK
    var tc: Int = 0
    val c: Long = ctl
    val e: Int = c.toInt
    val i: Int = e & SMASK
    val ws: Array[WorkQueue] = workQueues
    val w: WorkQueue = ws(i)
    var p: Thread = null
    if(ws != null && e >= 0) {
      if (e != 0 && i < ws.length && w != null && w.eventCount == (e | INT_SIGN)) {
        val nc: Long = (w.nextWait & E_MASK).toLong | (c & (AC_MASK | TC_MASK))
        if (ctl.compareAndSwapStrong(c, nc)) {
          w.eventCount = (e + E_SEQ) & E_MASK
          p = w.parker
          if (p != null)
            //U.unpark(p)
          return true // replace with idle worker
        }
      }
      else {
        tc = (c >>> TC_SHIFT).toShort
        if (tc >= 0 && (c >>> AC_SHIFT).toInt + pc > 1) {
          val nc: Long = ((c - AC_UNIT) & AC_MASK) | (c & ~AC_MASK)
          if (ctl.compareAndSwapStrong(c, nc))
            return true // no compensation
        }
        else if (tc + pc < MAX_CAP) {
          val nc: Long = ((c + TC_UNIT) & TC_MASK) | (c & ~TC_MASK)
          if (ctl.compareAndSwapStrong(c, nc)) {
            var fac: ForkJoinWorkerThreadFactory = null
            var ex: Throwable = null
            var wt: ForkJoinWorkerThread = null
            try {
              fac = factory
              wt = fac.newThread(this)
              if (fac != null && wt != null) {
                wt.start()
                return true
              }
            } catch {
              case rex: Throwable =>
                ex = rex
            }
            deregisterWorker(wt, ex) // clean up and return false
          }
        }
      }
    }
    false
  }

  def awaitJoin(joiner: WorkQueue, task: ForkJoinTask[_]): Int = {
    var s: Int = task.status
    if(joiner != null && task != null && s >= 0) {
      val prevJoin: ForkJoinTask[_] = joiner.currentJoin
      joiner.currentJoin = task
      do {
        s = task.status
      } while(s >= 0 && !joiner.isEmpty && joiner.tryRemoveAndExec(task)) // process local tasks
      s = task.status
      if(s >= 0 && s >= 0) {
        helpSignal(task, joiner.poolIndex)
        s = task.status
        if(s >= 0 && task.isInstanceOf[CountedCompleter[_]])
          s = helpComplete(task, LIFO_QUEUE)
      }
      s = task.status
      while(s >= 0 && s >= 0) {
        s = tryHelpStealer(joiner, task)
        if(!joiner.isEmpty || s == 0 && {s = task.status; s} >= 0) {
          helpSignal(task, joiner.poolIndex)
          s = task.status
          if(s >= 0 && tryCompensate) {
            s = task.status
            if(task.trySetSignal && s >= 0) {
              task.synchronized {
                if(task.status >= 0) {
                  try { // see ForkJoinTask
                    task.wait() // for explanation
                  } catch {
                    case ie: InterruptedException =>
                  }
                }
                else
                  task.notifyAll()
              }
            }
            val c: Long = ctl // re-activate
            do {} while(!ctl.compareAndSwapStrong(c, c + AC_UNIT))
          }
        }
      }
      joiner.currentJoin = prevJoin
    }
    s
  }

  def helpJoinOnce(joiner: WorkQueue, task: ForkJoinTask[_]): Unit = {
    var s: Int = task.status
    if(joiner != null && task != null && s >= 0) {
      val prevJoin: ForkJoinTask[_] = joiner.currentJoin
      joiner.currentJoin = task
      s = task.status
      do {} while(s >= 0 && !joiner.isEmpty && joiner.tryRemoveAndExec(task))
      s = task.status
      if(s >= 0 && {s = task.status; s} >= 0) {
        helpSignal(task, joiner.poolIndex)
        s = task.status
        if(s >= 0 && task.isInstanceOf[CountedCompleter[_]])
          s = helpComplete(task, LIFO_QUEUE)
      }
      if(s >= 0 && joiner.isEmpty) {
        do {} while(task.status >= 0 && tryHelpStealer(joiner, task) > 0)
      }
      joiner.currentJoin = prevJoin
    }
  }

  private def findNonEmptyStealQueue(r: Int): WorkQueue = {
    while(true) {
      val ps: Int = plock
      val ws: Array[WorkQueue] = workQueues
      val m: Int = ws.length - 1
      var q: WorkQueue = null
      if(ws != null && m >= 0) {
        var j: Int = (m + 1) << 2
        while(j >= 0) {
          q = ws((((r + j) << 1 | 1) & m))
          if(q != null && q.base - q.top < 0)
            return q
          j -= 1
        }
      }
      if(plock == ps)
        return null
    }
    // for the comppiler
    null.asInstanceOf[WorkQueue]
  }

  def helpQuiescePool(w: WorkQueue): Unit = {
    var active: Boolean = true
    while(true) {
      var c: Long = 0
      var b: Int = 0
      var q: WorkQueue = null
      var t: ForkJoinTask[_] = w.nextLocalTask
      while(t != null) {
        if(w.base - w.top < 0)
          signalWork(w)
        t.doExec
        t = w.nextLocalTask
      }
      q = findNonEmptyStealQueue(w.nextSeed)
      if(q != null) {
        if(!active) { // re-establish active count
          active = true
          c = ctl
          do {} while(!ctl.compareAndSwapStrong(c, c + AC_UNIT))
        }
        b = q.base
        t = q.pollAt(b)
        if(b - q.top < 0 && t != null) {
          if(q.base - q.top < 0)
            signalWork(q)
          w.runSubtask(t)
        }
      }
      else if(active) { // decrement active count without queuing
        c = ctl
        val nc: Long = c - AC_UNIT
        if((nc >> AC_SHIFT).toInt + (config & SMASK) == 0)
          return // bypass decrement-then-increment
        if(ctl.compareAndSwapStrong(c, nc))
          active = false
      }
      else {
        c = ctl
        if((c >> AC_SHIFT).toInt + (config & SMASK) == 0 && ctl.compareAndSwapStrong(c, c + AC_UNIT))
          return
      }
    }
  }

  def nextTaskFor(w: WorkQueue): ForkJoinTask[_] = {
    var t: ForkJoinTask[_] = null
    while(true) {
      var q: WorkQueue = null
      var b: Int = 0
      t = w.nextLocalTask
      if(t != null)
        return t
      q = findNonEmptyStealQueue(w.nextSeed)
      if(q == null)
        return null
      b = q.base
      t = q.pollAt(b)
      if(b - q.top < 0 && t != null) {
        if(q.base - q.top < 0)
          signalWork(q)
        return t
      }
    }
    // for the compiler
    null.asInstanceOf[ForkJoinTask[_]]
  }

  // Termination

  private def tryTerminate(now: Boolean, enable: Boolean): Boolean = {
    var ps: Int = 0
    if(this == common) // cannot shut down
      return false
    ps = plock
    if(ps >= 0) { // enable by setting plock
      if(!enable)
        return false
      if((ps & PL_LOCK) != 0 || !plock.compareAndSwapStrong(ps, {ps = ps + PL_LOCK; ps}))
        ps = acquirePlock
      val nps: Int = ((ps + PL_LOCK) & ~SHUTDOWN) | SHUTDOWN
      if(!plock.compareAndSwapStrong(ps, nps))
        releasePlock(nps)
    }
    var c: Long = 0
    while(true) {
      c = ctl
      if((c & STOP_BIT) != 0) { // already terminating
        if((c >> TC_SHIFT).toShort == -(config & SMASK)) {
          this.synchronized {
            notifyAll() // signal when 0 workers
          }
        }
        return true
      }
      if(!now) { // check if idle & no tasks
        var ws: Array[WorkQueue] = null
        var w: WorkQueue = null
        if((c >> AC_SHIFT).toInt != -(config & SMASK))
          return false
        ws = workQueues
        if(ws != null) {
          var i: Int = 0
          while(i < ws.length) {
            w = ws(i)
            if(w != null) {
              if(!w.isEmpty) { // signal unprocessed tasks
                signalWork(w)
                return false
              }
              if((i & 1) != 0 && w.eventCount >= 0)
                return false // unqueued inactive worker
            }
            i = i + 1
          }
        }
      }
      if(ctl.compareAndSwapStrong(c, c | STOP_BIT)) {
        var pass: Int = 0
        while(pass < 3) {
          val ws: Array[WorkQueue] = workQueues
          var w: WorkQueue = null
          var wt: Thread = null
          if(ws != null) {
            val n: Int = ws.length
            var i: Int = 0
            while(i < n) {
              w = ws(i)
              if(w != null) {
                w.qlock.store(-1)
                if(pass > 0) {
                  w.cancelAll()
                  wt = w.owner
                  if(pass > 1 && wt != null) {
                    if(!wt.isInterrupted) {
                      try {
                        wt.interrupt()
                      } catch {
                        case ignore: Throwable =>
                      }
                    }
                    //U.unpark(wt)
                  }
                }
              }
              i = i + 1
            }
            // Wake up workers parked on event queue
            val cc: Long = ctl
            val e: Int = cc.toInt
            i = e & SMASK
            var p: Thread = null
            w = ws(i)
            while((e & E_MASK) != 0 && i < n && i >= 0 && w != null) {
              val nc: Long = (w.nextWait & E_MASK).toLong | ((cc + AC_UNIT) & AC_MASK) |
                (cc & (TC_MASK | STOP_BIT))
              if(w.eventCount == (e | INT_SIGN) && ctl.compareAndSwapStrong(cc, nc)) {
                w.eventCount = (e + E_SEQ) & E_MASK
                w.qlock.store(-1)
                p = w.parker
                if(p != null) {
                  //U.unpark(p)
                }
              }
            }
          }
          pass = pass + 1
        }
      }
    }
    // for the compiler
    false
  }

  private def externalHelpComplete(q: WorkQueue, root: ForkJoinTask[_]): Unit = {
    var a: Array[CAtomicRef[ForkJoinTask[_]]] = null
    var m: Int = 0
    if(q != null && {a = q.array; a} != null && {m = a.length - 1; m} >= 0 &&
        root != null && root.status >= 0) {
      var breakWhile: Boolean = false
      while(!breakWhile) {
        var s: Int = 0
        var u: Int = 0
        var o: Object = null
        var task: CountedCompleter[_] = null
        if({s = q.top; s} - q.base > 0) {
          val j = m & (s - 1)
          if({o = a(j); o} != null && o.isInstanceOf[CountedCompleter[_]]) {
            val t: CountedCompleter[_] = o.asInstanceOf[CountedCompleter[_]]
            var r: CountedCompleter[_] = t
            var break: Boolean = false
            do {
              if(r == root) {
                if(q.qlock.compareAndSwapStrong(0, 1)) {
                  if(q.array == a && q.top == s &&
                      a(j).compareAndSwapStrong(t, null.asInstanceOf[ForkJoinTask[_]])) {
                    q.top = s - 1
                    task = t
                  }
                  q.qlock.store(0)
                }
                break = true
              }
            } while(!break && {r = r.completer; r} != null)
          }
        }
        if(task != null)
          task.doExec
        if(root.status < 0 || {u = (ctl >>> 32).toInt; u} >= 0 ||
          (u >> UAC_SHIFT) >= 0)
          breakWhile = true
        if(!breakWhile && task == null) {
          helpSignal(root, q.poolIndex)
          if(root.status >= 0)
            helpComplete(root, SHARED_QUEUE)
          breakWhile = true
        }
      }
    }
  }

  // Execution methods

  def invoke[T](task: ForkJoinTask[T]): T = {
    if(task == null)
      throw new NullPointerException()
    externalPush(task)
    task.join
  }

  def execute(task: ForkJoinTask[_]): Unit = {
    if(task == null)
      throw new NullPointerException()
    externalPush(task)
  }

  // AbstractExecutorService methods

  def execute(task: Runnable): Unit = {
    if(task == null)
      throw new NullPointerException()
    var job: ForkJoinTask[_] = null
    if(task.isInstanceOf[ForkJoinTask[_]]) // avoid re-wrap
      job = task.asInstanceOf[ForkJoinTask[_]]
    else
      job = new ForkJoinTask.AdaptedRunnableAction(task)
    externalPush(job)
  }

  def submit[T](task: ForkJoinTask[T]): ForkJoinTask[T] = {
    if(task == null)
      throw new NullPointerException()
    externalPush(task)
    task
  }

  override def submit[T](task: Callable[T]): ForkJoinTask[T] = {
    val job: ForkJoinTask[T] = new ForkJoinTask.AdaptedCallable[T](task)
    externalPush(job)
    job
  }

  override def submit[T](task: Runnable, result: T): ForkJoinTask[T] = {
    val job: ForkJoinTask[T] = new ForkJoinTask.AdaptedRunnable[T](task, result)
    externalPush(job)
    job
  }

  override def submit(task: Runnable): ForkJoinTask[_] = {
    if(task == null)
      throw new NullPointerException()
    var job: ForkJoinTask[_] = null
    if(task.isInstanceOf[ForkJoinTask[_]]) // avoid re-wrap
      job = task.asInstanceOf[ForkJoinTask[_]]
    else
      job = new ForkJoinTask.AdaptedRunnableAction(task)
    externalPush(job)
    job
  }

  override def invokeAll[T](tasks: util.Collection[_ <: Callable[T]]): util.List[Future[T]] = {
    // In previous versions of this class, this method constructed
    // a task to run ForkJoinTask.invokeAll, but now external
    // invocation of multiple tasks is at least as efficient.
    val futures: util.ArrayList[Future[T]] = new util.ArrayList[Future[T]](tasks.size())

    var done: Boolean = false
    try {
      val it = tasks.iterator()
      while(it.hasNext) {
        val f: ForkJoinTask[T] = new ForkJoinTask.AdaptedCallable[T](it.next())
        futures.add(f)
        externalPush(f)
      }
      var i: Int = 0
      val size: Int = futures.size()
      while(i < size) {
        futures.get(i).asInstanceOf[ForkJoinTask[_]].quietlyJoin()
        i = i + 1
      }
      done = true
      futures
    } finally {
      if(!done) {
        var i: Int = 0
        val size: Int = futures.size()
        while(i < size) {
          futures.get(i).cancel(false)
          i = i + 1
        }
      }
    }
  }

  def getFactory: ForkJoinWorkerThreadFactory = factory

  def getUncaughtExceptionHandler: Thread.UncaughtExceptionHandler = ueh

  def getParallelism: Int = config & SMASK

  def getPoolSize: Int = (config & SMASK) + (ctl >>> TC_SHIFT).toShort

  def getAsyncMode: Boolean = (config >>> 16) == FIFO_QUEUE

  def getRunningThreadCount: Int = {
    var rc: Int = 0
    val ws: Array[WorkQueue] = workQueues
    var w: WorkQueue = null
    if(ws != null) {
      var i: Int = 1
      while(i < ws.length) {
        w = ws(i)
        if(w != null && w.isApparentlyUnblocked)
          rc = rc + 1
        i = i + 2
      }
    }
    rc
  }

  def getActiveThreadCount: Int = {
    val r: Int = (config & SMASK) + (ctl >>> AC_SHIFT).toInt
    if(r <= 0) 0 else r // suppress momentarily negative values
  }

  def isQuiescent: Boolean = (ctl >> AC_SHIFT).toInt + (config & SMASK) == 0

  def getStealCount: Long = {
    var count: Long = stealCount
    val ws: Array[WorkQueue] = workQueues
    var w: WorkQueue = null
    if(ws != null) {
      var i: Int = 1
      while(i < ws.length) {
        w = ws(i)
        if(w != null)
          count = count + w.nsteals
        i = i + 2
      }
    }
    count
  }

  def getQueuedTaskCount: Long = {
    var count: Long = 0L
    val ws: Array[WorkQueue] = workQueues
    var w: WorkQueue = null
    if(ws != null) {
      var i: Int = 1
      while(i < ws.length) {
        w = ws(i)
        if(w != null)
          count = count + w.queueSize
        i = i + 2
      }
    }
    count
  }

  def getQueuedSubmissionCount: Int = {
    var count: Int = 0
    val ws: Array[WorkQueue] = workQueues
    var w: WorkQueue = null
    if(ws != null) {
      var i: Int = 0
      while(i < ws.length) {
        w = ws(i)
        if(w != null)
          count = count + w.queueSize
        i = i + 2
      }
    }
    count
  }

  def hasQueuedSubmissions: Boolean = {
    val ws: Array[WorkQueue] = workQueues
    var w: WorkQueue = null
    if(ws != null) {
      var i: Int = 0
      while(i < ws.length) {
        w = ws(i)
        if(w != null && !w.isEmpty)
          return true
        i = i + 2
      }
    }
    false
  }

  protected def pollSubmissions: ForkJoinTask[_] = {
    val ws: Array[WorkQueue] = workQueues
    var w: WorkQueue = null
    var t: ForkJoinTask[_] = null
    if(ws != null) {
      var i: Int = 0
      while(i < ws.length) {
        w = ws(i)
        t = w.poll
        if(w != null && t != null)
          return t
        i = i + 2
      }
    }
    null
  }

  protected def drainTasksTo(c: util.Collection[_ >: ForkJoinTask[_]]): Int = {
    var count: Int = 0
    val ws: Array[WorkQueue] = workQueues
    var w: WorkQueue = null
    var t: ForkJoinTask[_] = null
    if(ws != null) {
      var i: Int = 0
      while(i < ws.length) {
        w = ws(i)
        if(w != null) {
          t = w.poll
          while(t != null) {
            c.add(t)
            count = count + 1
            t = w.poll
          }
        }
        i = i + 1
      }
    }
    count
  }


  override def toString: String = {
    // Use a single pass through workQueues to collect counts
    var qt: Long = 0L
    var qs: Long = 0L
    var rc: Int = 0
    var st: Long = stealCount
    val c: Long = ctl
    val ws: Array[WorkQueue] = workQueues
    var w: WorkQueue = null
    if(ws != null) {
      var i: Int = 0
      while(i < ws.length) {
        w = ws(i)
        if(w != null) {
          val size: Int = w.queueSize
          if((i & 1) == 0)
            qs = qs + size
          else {
            qt = qt + size
            st = st + w.nsteals
            if(w.isApparentlyUnblocked)
              rc +=1
          }
        }
        i = i + 1
      }
    }
    val pc: Int = (config & SMASK)
    val tc: Int = pc + (c >>> TC_SHIFT).toShort
    var ac = pc + (c >> AC_SHIFT).toInt
    if(ac < 0) // ignore transient negative
      ac = 0
    var level: String = ""
    if((c & STOP_BIT) != 0)
      level = if(tc == 0) "Terminated" else "Terminating"
    else
      level = if(plock < 0) "Shutting down" else "Running"
    super.toString +
      "[" + level +
      ", parallelism = " + pc +
      ", size = " + tc +
      ", active = " + ac +
      ", running = " + rc +
      ", steals = " + st +
      ", tasks = " + qt +
      ", submissions = " + qs +
      "]"
  }

  override def shutdown(): Unit = {
    checkPermission()
    tryTerminate(false, true)
  }

  override def shutdownNow(): util.List[Runnable] = {
    checkPermission()
    tryTerminate(true, true)
    Collections.emptyList()
  }

  override def isTerminated: Boolean = {
    val c: Long = ctl
    (c & STOP_BIT) != 0L && (c >>> TC_SHIFT).toShort == -(config & SMASK)
  }

  def isTerminating: Boolean = {
    val c: Long = ctl
    (c & STOP_BIT) != 0L && (c >>> TC_SHIFT).toShort != -(config & SMASK)
  }

  override def isShutdown: Boolean = plock < 0

  override def awaitTermination(timeout: Long, unit: TimeUnit): Boolean = {
    if(Thread.interrupted())
      throw new InterruptedException()
    if(this == common) {
      awaitQuiescence(timeout, unit)
      return false
    }
    val nanos: Long = unit.toNanos(timeout)
    if(isTerminated)
      return true
    val startTime: Long = System.nanoTime()
    var terminated: Boolean = false
    this.synchronized {
      var waitTime: Long = nanos
      var millis: Long = 0L

      var break: Boolean = false
      while(true) {
        terminated = isTerminated
        millis = unit.toMillis(waitTime)
        if(isTerminated || waitTime <= 0L || millis <= 0L)
          break = true
        if(!break) {
          wait(millis)
          waitTime = nanos - (System.nanoTime() - startTime)
        }
      }
    }
    terminated
  }

  def awaitQuiescence(timeout: Long, unit: TimeUnit): Boolean = {
    val nanos: Long = unit.toNanos(timeout)
    val thread: Thread = Thread.currentThread()
    var wt: ForkJoinWorkerThread = null
    if(thread.isInstanceOf[ForkJoinWorkerThread] && {wt = thread.asInstanceOf[ForkJoinWorkerThread]; wt}.pool == this) {
      helpQuiescePool(wt.workQueue)
      return true
    }
    val startTime: Long = System.nanoTime()
    var ws: Array[WorkQueue] = null
    var r: Int = 0
    var m: Int = 0
    var found: Boolean = true
    while(!isQuiescent && {ws = workQueues; ws} != null && {m = ws.length - 1; m} >= 0) {
      if(!found) {
        if((System.nanoTime() - startTime) > nanos)
          return false
        var j: Int = (m + 1) << 2

        var break: Boolean = false
        while(j >= 0 && !break) {
          var t: ForkJoinTask[_] = null
          r = r + 1
          val q: WorkQueue = ws(r & m)
          val b: Int = q.base
          if(q != null && b - q.top < 0) {
            found = true
            t = q.pollAt(b)
            if(t != null) {
              if(q.base - q.top < 0)
                signalWork(q)
              t.doExec
            }
            break = true
          }
          j -= 1
        }
      }
    }
    true
  }

}

object ForkJoinPool {

  // security manager
  private def checkPermission(): Unit = {}

  trait ForkJoinWorkerThreadFactory {
    def newThread(pool: ForkJoinPool): ForkJoinWorkerThread
  }

  final class DefaultForkJoinWorkerThreadFactory extends ForkJoinWorkerThreadFactory {

    override def newThread(pool: ForkJoinPool): ForkJoinWorkerThread = new ForkJoinWorkerThread(pool)

  }

  final case class Submitter(var seed: Int)

  final class EmptyTask extends ForkJoinTask[Void] {

    status.store(ForkJoinTask.NORMAL)

    override def getRawResult: Void = null

    override def setRawResult(v: Void): Unit = {}

    override def exec(): Boolean = true

  }

  object EmptyTask {

    private final val serialVersionUID: Long = -7721805057305804111L

  }

  final class WorkQueue {

    import WorkQueue._

    //volatile
    var pad00, pad01, pad02, pad03, pad04, pad05, pad06: Long = 0

    var seed: Int = 0 // for random scanning; initialize nonzero
    //volatile
    var eventCount: Int = 0 // encoded inactivation count; < 0 if inactive
    var nextWait: Int = 0 // encoded record of next event waiter
    var hint: Int = 0 // steal or signal hint (index)
    var poolIndex: Int = 0 // index of this queue in pool (or 0)
    var mode: Int = 0 // 0: lifo, > 0: fifo, < 0: shared
    var nsteals: Int = 0 // number of steals
    //volatile
    var qlock = CAtomicInt() // 1: locked, -1: terminate; else 0
    //volatile
    var base: Int = 0 // index of next slot for poll
    var top: Int = 0 // index of next slot for push
    var array: Array[CAtomicRef[ForkJoinTask[_]]] = _ // the elements (initially unallocated)
    var pool: ForkJoinPool = null // the containing pool (may be null)
    var owner: ForkJoinWorkerThread = null// owning thread or null if shared
    //volatile
    var parker: Thread = null// == owner during call to park; else null
    //volatile
    var currentJoin: ForkJoinTask[_] = null // task being joined in awaitJoin
    var currentSteal: ForkJoinTask[_] = null // current non-local task being executed

    //volatile
    var pad10, pad11, pad12, pad13, pad14, pad15, pad16, pad17: Object = _
    var pad18, pad19, pad1a, pad1b, pad1c, pad1d: Object = _

    def this(pool: ForkJoinPool, owner: ForkJoinWorkerThread, mode: Int, seed: Int) {
      this()
      this.pool = pool
      this.owner = owner
      this.mode = mode
      this.seed = seed
      // Place indices in the center of array (that is not yet allocated)
      top = INITIAL_QUEUE_CAPACITY >>> 1
      base = top
    }

    def queueSize: Int = {
      val n: Int = base - top // non-owner callers must read base first
      if (n >= 0) 0
      else -n // ignore transient negative

    }

    def isEmpty: Boolean = {
      // volatile
      var a: Array[CAtomicRef[ForkJoinTask[_]]] = null
      var m: Int = 0
      var s: Int = top
      val n: Int = base - s
      a = array
      m = a.length
      n >= 0 || (n == -1 && (a == null || m < 0 || a(m & (s - 1)) == null))
    }

    def push(task: ForkJoinTask[_]): Unit = {
      var a: Array[CAtomicRef[ForkJoinTask[_]]] = null
      var p: ForkJoinPool = null
      val s: Int = top
      var m: Int = 0
      var n: Int = 0
      a = array
      if (a != null) { // ignore if queue removed
        m = a.length - 1
        val j = m & s
        a(j).store(task)
        top = s + 1
        n = top - base
        p = pool
        if (n <= 2) if (p != null) p.signalWork(this)
        else if (n >= m) growArray
      }
    }

    def growArray: Array[CAtomicRef[ForkJoinTask[_]]] = {
      //volatile
      val oldA: Array[CAtomicRef[ForkJoinTask[_]]] = array
      val size: Int = if (oldA != null) oldA.length << 1
      else INITIAL_QUEUE_CAPACITY
      if (size > MAXIMUM_QUEUE_CAPACITY)
        throw new RejectedExecutionException("Queue capacity exceeded")
      var oldMask: Int = 0
      var t: Int = 0
      var b: Int = 0
      array = new Array[CAtomicRef[ForkJoinTask[_]]](size)
      val a: Array[CAtomicRef[ForkJoinTask[_]]] = array
      oldMask = oldA.length - 1
      t = top
      b = base
      if (oldA != null && oldMask >= 0 && t - b > 0) {
        val mask = size - 1
        do {
          var x: ForkJoinTask[_] = null
          val oldj: Int = b & oldMask
          val j: Int = b & mask
          x = oldA(oldj)
          if (x != null && oldA(oldj).compareAndSwapStrong(x, null.asInstanceOf[ForkJoinTask[_]])) a(j).store(x)
          b = b + 1
        } while (b != t)
      }
      a
    }

    def pop: ForkJoinTask[_] = {
      //volatile
      var a: Array[CAtomicRef[ForkJoinTask[_]]] = null
      var t: ForkJoinTask[_] = null
      var m: Int = 0
      a = array
      m = a.length - 1
      if (a != null && m >= 0) {
        var s: Int = top - 1 - base
        var break: Boolean = false
        while (s >= 0 && !break) {
          val j: Int = m & s
          t = a(j)
          if (t == null)
            break = true
          if (a(j).compareAndSwapStrong(t, null.asInstanceOf[ForkJoinTask[_]]) && !break) {
            top = s
            t
          }
        }
        if (!break) s = top - 1 - base
      }
      null
    }

    def pollAt(b: Int): ForkJoinTask[_] = {
      var t: ForkJoinTask[_] = null
      //volatile
      var a: Array[CAtomicRef[ForkJoinTask[_]]] = null
      a = array
      if (a != null) {
        val j: Int = (a.length - 1) & b
        t = a(j)
        if (t != null && (base == b) && a(j).compareAndSwapStrong(t, null.asInstanceOf[ForkJoinTask[_]])) {
          base = b + 1
          return t
        }
      }
      null
    }

    def poll: ForkJoinTask[_] = {
      // volatile
      var a: Array[CAtomicRef[ForkJoinTask[_]]] = array
      var b: Int = base
      var t: ForkJoinTask[_] = null
      var break: Boolean = false
      while (b - top < 0 && a != null && !break) {
        val j =(a.length - 1) & b
        t = a(j)
        if (t != null) {
          if ((base == b) && a(j).compareAndSwapStrong(t, null.asInstanceOf[ForkJoinTask[_]])) {
            base = b + 1
            return t
          }
        }
        else if (base == b) {
          if (b + 1 == top) {
            break = true
            Thread.`yield` // wait for lagging update (very rare)
          }
        }
        if (!break) {
          b = base
          a = array
        }
      }

      null
    }


    def nextLocalTask: ForkJoinTask[_] = {
      if (mode == 0) pop
      else poll
    }

    def peek: ForkJoinTask[_] = {
      //volatile
      val a: Array[CAtomicRef[ForkJoinTask[_]]] = array
      var m: Int = 0
      m = a.length - 1
      if (a == null || m < 0) return null
      val i = if (mode == 0) top - 1
      else base
      val j = i & m
      a(j)
    }

    def tryUnpush(t: ForkJoinTask[_]): Boolean = {
      var a: Array[CAtomicRef[ForkJoinTask[_]]] = null
      var s: Int = 0
      a = array
      s = top
      if (a != null && s != base && a((a.length - 1) & {
        s -= 1
        s
      }).compareAndSwapStrong(t, null.asInstanceOf[ForkJoinTask[_]])) {
        top = s
        return true
      }
      false
    }

    def cancelAll(): Unit = {
      ForkJoinTask.cancelIgnoringExceptions(currentJoin)
      ForkJoinTask.cancelIgnoringExceptions(currentSteal)
      var t: ForkJoinTask[_] = poll
      while (t != null) {
        ForkJoinTask.cancelIgnoringExceptions(t)
        t = poll
      }
    }

    def nextSeed: Int = {
      var r = seed
      r ^= r << 13
      r ^= r >>> 17
      r ^= r << 5
      seed = r
      seed
    }

    private def popAndExecAll(): Unit = {
      // A bit faster than repeated pop calls
      // volatile
      var a: Array[CAtomicRef[ForkJoinTask[_]]] = array
      var m: Int = a.length - 1
      var s: Int = top - 1
      var j = m & s
      var t: ForkJoinTask[_] = a(j)
      while (a != null && m >= 0 && s - base >= 0 && t != null) {
        if (a(j).compareAndSwapStrong(t, null.asInstanceOf[ForkJoinTask[_]])) {
          top = s
          t.doExec
        }
        a = array
        m = a.length - 1
        s = top - 1
        j = m & s
        t = a(j)
      }
    }

    private def pollAndExecAll(): Unit = {
      var t: ForkJoinTask[_] = poll
      while (t != null) {
        t.doExec
        t = poll
      }
    }

    def tryRemoveAndExec(task: ForkJoinTask[_]): Boolean = {
      var stat: Boolean = true
      var removed: Boolean = false
      var empty: Boolean = true
      var a: Array[CAtomicRef[ForkJoinTask[_]]] = array
      var m: Int = a.length - 1
      var s: Int = top
      var b: Int = base
      var n: Int = s - b
      if (a != null && m >= 0 && n > 0) {
        var t: ForkJoinTask[_] = null
        var break: Boolean = false
        while (!break) { // traverse from s to b
          s -= 1
          val j = s & m
          t = a(j)
          if (t == null) { // inconsistent length
            break = true
          }
          else {
            if (t == task) if (s + 1 == top) { // pop
              if (!a(j).compareAndSwapStrong(task, null.asInstanceOf[ForkJoinTask[_]])) {
                break = true
                if (!break) {
                  top = s
                  removed = true
                }
              }
              else if (base == b && !break) { // replace with proxy
                removed = a(j).compareAndSwapStrong(task, new ForkJoinPool.EmptyTask)
              }
              break = true
            }
            else if ((t.status) >= 0 && !break) empty = false
            else if (s + 1 == top && !break) { // pop and throw away
              if (a(j).compareAndSwapStrong(t, null.asInstanceOf[ForkJoinTask[_]])) top = s
              break = true
            }
            n -= 1
            if (n == 0 && !break) {
              if (!empty && (base == b)) stat = false
              break = true
            }
          }
        }
      }
      if (removed) task.doExec
      stat
    }


    def pollAndExecCC(root: ForkJoinTask[_]): Boolean = {
      var a: Array[CAtomicRef[ForkJoinTask[_]]] = array
      var b: Int = base
      var o: Object = null
      var breakOuter: Boolean = false
      var breakInner: Boolean = false
      while (b - top < 0 && a != null && !breakOuter) {
        val j = (a.length - 1) & b
        o = a(j)
        if (o == null || !o.isInstanceOf[CountedCompleter[_]]) {
          breakOuter = true
          if (!breakOuter) {
            val t: CountedCompleter[_] = o.asInstanceOf[CountedCompleter[_]]
            var r: CountedCompleter[_] = t
            while (!breakInner) {
              if (r == root) {
                if ((base == b) && a(j).compareAndSwapStrong(t, null.asInstanceOf[ForkJoinTask[_]])) {
                  base = b + 1
                  t.doExec
                  return true
                }
                else breakInner = true // restart
              }
              r = r.completer
              if (r == null)
                breakOuter = true // not part of root computation
            }
          }
        }
      }
      false
    }


    def runTask(t: ForkJoinTask[_]): Unit = {
      if (t != null) {
        currentSteal = t
        currentSteal.doExec
        currentSteal = null
        nsteals = nsteals + 1
        if (base - top < 0) { // process remaining local tasks
          if (mode == 0) popAndExecAll()
          else pollAndExecAll()
        }
      }
    }

    def runSubtask(t: ForkJoinTask[_]): Unit = {
      if (t != null) {
        val ps = currentSteal
        currentSteal = t
        currentSteal.doExec
        currentSteal = ps
      }
    }

    def isApparentlyUnblocked: Boolean = {
      val wt: Thread = owner
      val s: Thread.State = wt.getState
      eventCount >= 0 && wt != null && (s ne Thread.State.BLOCKED) && (s ne Thread.State.WAITING) && (s ne Thread.State.TIMED_WAITING)
    }
  }

  object WorkQueue {

    final val INITIAL_QUEUE_CAPACITY: Int = 1 << 13
    final val MAXIMUM_QUEUE_CAPACITY: Int = 1 << 26 // 64M

  }

  // synchronized
  private final def nextPoolId: Int = {
    poolNumberSequence = poolNumberSequence + 1
    poolNumberSequence
  }

  // static constants

  private final val IDLE_TIMEOUT: Long = 2000L * 1000L * 1000L // 2sec

  private final val FAST_IDLE_TIMEOUT: Long = 200L * 1000L * 1000L

  private final val TIMEOUT_SLOP: Long = 2000000L

  private final val MAX_HELP: Int = 64

  private final val SEED_INCREMENT: Int = 0x61c88647

  // bit positions/shifts for fields
  private final val AC_SHIFT = 48
  private final val TC_SHIFT = 32
  private final val ST_SHIFT = 31
  private final val EC_SHIFT = 16

  // bounds
  private final val SMASK = 0xffff // short bits

  private final val MAX_CAP = 0x7fff // max #workers - 1

  private final val EVENMASK = 0xfffe // even short bits

  private final val SQMASK = 0x007e // max 64 (even) slots

  private final val SHORT_SIGN = 1 << 15
  private final val INT_SIGN = 1 << 31

  // masks
  private final val STOP_BIT = 0x0001L << ST_SHIFT
  private final val AC_MASK = SMASK.toLong << AC_SHIFT
  private final val TC_MASK = SMASK.toLong << TC_SHIFT

  // units for incrementing and decrementing
  private final val TC_UNIT = 1L << TC_SHIFT
  private final val AC_UNIT = 1L << AC_SHIFT

  // masks and units for dealing with u = (int)(ctl >>> 32)
  private final val UAC_SHIFT = AC_SHIFT - 32
  private final val UTC_SHIFT = TC_SHIFT - 32
  private final val UAC_MASK = SMASK << UAC_SHIFT
  private final val UTC_MASK = SMASK << UTC_SHIFT
  private final val UAC_UNIT = 1 << UAC_SHIFT
  private final val UTC_UNIT = 1 << UTC_SHIFT

  // masks and units for dealing with e = (int)ctl
  private final val E_MASK = 0x7fffffff // no STOP_BIT

  private final val E_SEQ = 1 << EC_SHIFT

  // plock bits
  private final val SHUTDOWN = 1 << 31
  private final val PL_LOCK = 2
  private final val PL_SIGNAL = 1
  private final val PL_SPINS = 1 << 8

  // access mode for WorkQueue
  val LIFO_QUEUE = 0
  val FIFO_QUEUE = 1
  val SHARED_QUEUE: Int = -1

  // bounds for #steps in scan loop -- must be power 2 minus 1
  private final val MIN_SCAN = 0x1ff // cover estimation slop

  private final val MAX_SCAN = 0x1ffff // 4 * max workers

  final val submitters: ThreadLocal[Submitter] = new ThreadLocal[Submitter]

  val defaultForkJoinWorkerThreadFactory: ForkJoinWorkerThreadFactory = new DefaultForkJoinWorkerThreadFactory()

  val fac: ForkJoinWorkerThreadFactory = defaultForkJoinWorkerThreadFactory

  private final val modifyThreadPermission: RuntimePermission = new RuntimePermission("modifyThread")

  final val par: Int = {
    var pp = Integer.parseInt(System.getProperty("java.util.concurrent.ForkJoinPool.common.parallelism"))
    if (pp <= 0)
      pp = Runtime.getRuntime.availableProcessors()
    if(pp > MAX_CAP)
      pp = MAX_CAP
    pp
  }

  val handler: Thread.UncaughtExceptionHandler = null

  // precompute ctl
  val np: Long = (-par).toLong
  val ct: Long = ((np << AC_SHIFT) & AC_MASK) | ((np << TC_SHIFT) & TC_MASK)

  final val common: ForkJoinPool = new ForkJoinPool(par, ct, fac, handler)

  final val commonParallelism: Int = par

  private var poolNumberSequence: Int = 0

  def getSurplusQueuedTaskCount: Int = {
    val t: Thread = Thread.currentThread()
    var wt: ForkJoinWorkerThread = null
    var pool: ForkJoinPool = null
    var q: WorkQueue = null
    if(t.isInstanceOf[ForkJoinWorkerThread]) {
      wt = t.asInstanceOf[ForkJoinWorkerThread]
      pool = wt.pool
      var p: Int = pool.config & SMASK
      q = wt.workQueue
      val n: Int = q.top - q.base
      val a: Int = (pool.ctl >> AC_SHIFT).toInt + p
      return n - {
        p >>>= 1
        if(a > p) 0
        else {
          p >>>= 1
          if(a > p) 1
          else {
            p >>>= 1
            if(a > p) 2
            else {
              p >>>= 1
              if(a > p) 4
              else 8
            }
          }
        }
      }
    }
    0
  }

  // external operations on common pool

  def commonSubmitterQueue: WorkQueue = {
    val p: ForkJoinPool = common
    if(p == null) return null

    val ws: Array[WorkQueue] = p.workQueues
    if(ws == null) return null

    val m: Int = ws.length - 1
    if(m < 0) return null

    val z: Submitter = submitters.get()
    if(z == null) return null

    ws(m & z.seed & SQMASK)
  }

  def tryExternalUnpush(t: ForkJoinTask[_]): Boolean = {
    var p: ForkJoinPool = null
    var ws: Array[WorkQueue] = null
    var q: WorkQueue = null
    var z: Submitter = null
    var a: Array[CAtomicRef[ForkJoinTask[_]]] = null
    var m: Int = 0
    var s: Int = 0

    if(t != null &&
        {z = submitters.get(); z} != null &&
        {p = common; p} != null &&
        {ws = p.workQueues; ws} != null &&
        {m = ws.length - 1; m} >= 0 &&
        {q = ws(m & z.seed & SQMASK); q} != null &&
        {s = q.top; s} != q.base &&
        {a = q.array; a} != null) {
      val j = (a.length - 1) & (s - 1)
      if((a(j)) == t && q.qlock.compareAndSwapStrong(0, 1)) {
        if(q.array == a && q.top == s && // recheck
            a(j).compareAndSwapStrong(t, null.asInstanceOf[ForkJoinTask[_]])) {
          q.top = s - 1
          q.qlock.store(0)
          return true
        }
      }
    }
    false
  }

  def externalHelpJoin(t: ForkJoinTask[_]): Unit = {
    // Some hard-to-avoid overlap with tryExternalUnpush
    var p: ForkJoinPool = null
    var ws: Array[WorkQueue] = null
    var q: WorkQueue = null
    var w: WorkQueue = null
    var z: Submitter = null
    var a: Array[CAtomicRef[ForkJoinTask[_]]] = null
    var m, s, n: Int = 0

    if(t != null &&
      {z = submitters.get(); z} != null &&
      {p = common; p} != null &&
      {ws = p.workQueues; ws} != null &&
      {m = ws.length - 1; m} >= 0 &&
      {q = ws(m & z.seed & SQMASK); q} != null &&
      {s = q.top; s} != q.base &&
      {a = q.array; a} != null) {

      val am = a.length - 1
      if({s = q.top; s} != q.base) {
        val j = am & (s - 1)
        if(a(j) == t && q.qlock.compareAndSwapStrong(0, 1)) {
          if(q.array == a && q.top == s &&
              a(j).compareAndSwapStrong(t, null.asInstanceOf[ForkJoinTask[_]])) {
            q.top = s - 1
            q.qlock.store(0)
            t.doExec
          }
          else
            q.qlock.store(0)
        }
      }
      if(t.status >= 0) {
        if(t.isInstanceOf[CountedCompleter[_]])
          p.externalHelpComplete(q, t)
        else p.helpSignal(t, q.poolIndex)
      }
    }
  }

  def commonPool: ForkJoinPool = common

  def getCommonPoolParallelism: Int = commonParallelism

  def quiesceCommonPool: Unit = common.awaitQuiescence(Long.MaxValue, TimeUnit.NANOSECONDS)


  trait ManagedBlocker {

    def block: Boolean

    def isReleasable: Boolean

  }

  def managedBlock(blocker: ManagedBlocker): Unit = {
    val t: Thread = Thread.currentThread()
    if(t.isInstanceOf[ForkJoinWorkerThread]) {
      val p: ForkJoinPool = t.asInstanceOf[ForkJoinWorkerThread].pool
      var breakOuter: Boolean = false
      while(!breakOuter && !blocker.isReleasable) { // variant of helpSignal
        val ws: Array[WorkQueue] = p.workQueues
        var q: WorkQueue = null
        var m: Int = ws.length - 1
        var u: Int = 0
        if(ws != null && m >= 0) {
          var i: Int = 0
          var break: Boolean = false
          while(!break && i <= m) {
            if(blocker.isReleasable)
              return
            q = ws(i)
            if(q != null && q.base - q.top < 0) {
              p.signalWork(q)
              u = (p.ctl >>> 32).toInt
              if(u >= 0 || (u >> UAC_SHIFT) >= 0)
                break = true
            }
            i = i + 1
          }
        }
        if(p.tryCompensate) {
          try {
            do {} while(!blocker.isReleasable && !blocker.block)
          } finally {
            p.incrementActiveCount()
          }
          breakOuter = true
        }
      }
    }
    else {
      do {} while(!blocker.isReleasable && !blocker.block)
    }
  }

  // AbstractExecutorService overrides.  These rely on undocumented
  // fact that ForkJoinTask.adapt returns ForkJoinTasks that also
  // implement RunnableFuture.

  protected def newTaskFor[T](runnable: Runnable, value: T): RunnableFuture[T] =
    new ForkJoinTask.AdaptedRunnable[T](runnable , value)

  protected def newTaskFor[T](callable: Callable[T]): RunnableFuture[T] =
    new ForkJoinTask.AdaptedCallable[T](callable)

}
