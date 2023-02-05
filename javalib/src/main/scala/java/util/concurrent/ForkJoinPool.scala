/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent

import java.lang.Thread.UncaughtExceptionHandler
import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import java.util.concurrent.ForkJoinPool.WorkQueue.getAndClearSlot
import java.util.{ArrayList, Collection, Collections, List, concurrent}
import java.util.function.Predicate
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.Condition
import scala.annotation._
import scala.scalanative.annotation._
import scala.scalanative.unsafe._
import scala.scalanative.libc.atomic.{CAtomicInt, CAtomicLongLong, CAtomicRef}
import scala.scalanative.runtime.{fromRawPtr, Intrinsics, ObjectArray}

class ForkJoinPool private (
    factory: ForkJoinPool.ForkJoinWorkerThreadFactory,
    val ueh: UncaughtExceptionHandler,
    saturate: Predicate[ForkJoinPool],
    keepAlive: Long,
    workerNamePrefix: String
) extends AbstractExecutorService {
  import ForkJoinPool._

  @volatile private[concurrent] var stealCount: Long = 0
  @volatile private[concurrent] var threadIds: Int = 0
  @volatile private[concurrent] var mode: Int = 0
  @volatile private[concurrent] var ctl: Long = 0L // main pool control

  final private[concurrent] var bounds: Int = 0
  final private[concurrent] val registrationLock = new ReentrantLock()

  private[concurrent] var scanRover: Int = 0 // advances across pollScan calls
  private[concurrent] var queues: Array[WorkQueue] = _ // main registry
  private[concurrent] var termination: Condition = _

  private val modeAtomic = new CAtomicInt(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "mode"))
  )
  private val ctlAtomic = new CAtomicLongLong(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "ctl"))
  )
  private val stealCountAtomic = new CAtomicLongLong(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "stealCount"))
  )
  private val threadIdsAtomic = new CAtomicInt(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "threadIds"))
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
  private def getAndBitwiseOrMode(v: Int): Int = modeAtomic.fetchOr(v)
  @alwaysinline
  private def getAndAddThreadIds(v: Int): Int = threadIdsAtomic.fetchAdd(v)

  private def createWorker(): Boolean = {
    var ex: Throwable = null
    var wt: ForkJoinWorkerThread = null

    try {
      if (factory != null) {
        wt = factory.newThread(this)
        if (wt != null) {
          wt.start()
          return true
        }
      }
    } catch {
      case rex: Throwable =>
        ex = rex
    }
    deregisterWorker(wt, ex)
    false
  }

  final private[concurrent] def nextWorkerThreadName(): String = {
    val tid = getAndAddThreadIds(1) + 1
    val prefix = Option(workerNamePrefix)
      .getOrElse("ForkJoinPool.commonPool-worker-") // commonPool has no prefix
    prefix + tid
  }

  final private[concurrent] def registerWorker(w: WorkQueue): Unit = {
    val lock = registrationLock
    ThreadLocalRandom.localInit()
    val seed = ThreadLocalRandom.getProbe()
    if (w != null && lock != null) {
      val modebits: Int = (mode & FIFO) | w.config
      w.array = new Array[ForkJoinTask[_]](INITIAL_QUEUE_CAPACITY)
      w.stackPred = seed // stash for runWorker

      if ((modebits & INNOCUOUS) != 0)
        w.initializeInnocuousWorker()
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
          w.config = id | modebits // now publishable
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
    val lock: ReentrantLock = registrationLock
    val w: WorkQueue = if (wt != null) wt.workQueue else null
    var cfg: Int = 0
    if (w != null && lock != null) {
      cfg = w.config
      val ns: Long = w.nsteals & 0xffffffffL
      lock.lock() // remove index from array
      val qs = queues
      val n: Int = if (qs != null) qs.length else 0
      val i: Int = cfg & (n - 1)
      if (n > 0 && qs(i) == w)
        qs(i) = null
      stealCount += ns // accumulate steals

      lock.unlock()
      var c: Long = ctl
      if (w.phase != QUIET) { // decrement counts
        while ({
          val c0 = c
          val newC = (RC_MASK & (c - RC_UNIT)) |
            (TC_MASK & (c - TC_UNIT)) |
            (SP_MASK & c)
          c = compareAndExchangeCtl(c, newC)
          c0 != c
        }) ()
      } else if (c.toInt == 0) { // was dropped on timeout
        cfg = 0 // suppress signal if last
      }
      while (w.pop() match {
            case null => false
            case t =>
              ForkJoinTask.cancelIgnoringExceptions(t)
              true
          }) ()
    }
    if (!tryTerminate(false, false) && w != null && (cfg & SRC) != 0)
      signalWork() // possibly replace worker
    if (ex != null) ForkJoinTask.rethrow(ex)
  }

  /*
   * Tries to create or release a worker if too few are running.
   */
  final private[concurrent] def signalWork(): Unit = {
    var c: Long = ctl
    while (c < 0L) {
      ((c.toInt & ~UNSIGNALLED): @switch) match {
        case 0 => // no idle workers
          if ((c & ADD_WORKER) == 0L) return // enough total workers
          else {
            val prevC = c
            c = compareAndExchangeCtl(
              c,
              (RC_MASK & (c + RC_UNIT)) | (TC_MASK & (c + TC_UNIT))
            )
            if (c == prevC) {
              createWorker()
              return
            }
          }

        case sp =>
          val i = sp & SMASK
          val qs = queues
          def unstartedOrTerminated = qs == null
          def terminated = qs.length <= i
          def terminating = qs(i) == null

          if (unstartedOrTerminated || terminated || terminating)
            return // break
          else {
            val v = qs(i)
            val vt = v.owner
            val nc = (v.stackPred & SP_MASK) | (UC_MASK & (c + RC_UNIT))
            val prevC = c
            c = compareAndExchangeCtl(c, nc)
            if (c == prevC) {
              // release idle worker
              v.phase = sp
              vt.foreach(LockSupport.unpark)
              return
            }
          }
      }
    }
  }

  final private[concurrent] def runWorker(w: WorkQueue): Unit = {
    if (w != null) { // skip on failed init
      w.config |= SRC // mark as valid source

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
      val q = qs(j)
      val a = if (q != null) q.array else null
      val cap = if (a != null) a.length else 0
      if (cap > 0) {
        val b = q.base
        val k: Int = (cap - 1) & b
        val nextBase: Int = b + 1
        val nextIndex: Int = (cap - 1) & nextBase
        val src: Int = j | SRC
        val t: ForkJoinTask[_] = WorkQueue.getSlot(a, k)
        if (q.base != b) { // inconsistent
          return prevSrc
        } else if (t != null && WorkQueue.casSlotToNull(a, k, t)) {
          q.base = nextBase
          val next: ForkJoinTask[_] = a(nextIndex)
          w.source = src
          if (src != prevSrc && next != null)
            signalWork() // propagate
          w.topLevelExec(t, q)
          return src
        } else if (a(nextIndex) != null) { // revisit
          return prevSrc
        }
      }

      i -= 1
      r += step
    }
    if (queues != qs) prevSrc
    else -1 // possibly resized
  }

  private def awaitWork(w: WorkQueue): Int = {
    if (w == null) return -1 // already terminated
    val phase = (w.phase + SS_SEQ) & ~UNSIGNALLED
    w.phase = phase | UNSIGNALLED // advance phase

    var prevCtl: Long = ctl
    var c: Long = 0L // enqueue
    while ({
      w.stackPred = prevCtl.toInt
      c = ((prevCtl - RC_UNIT) & UC_MASK) | (phase & SP_MASK)
      val prev = prevCtl
      prevCtl = compareAndExchangeCtl(prevCtl, c)
      prev != prevCtl
    }) ()

    Thread.interrupted() // clear status

    LockSupport.setCurrentBlocker(this) // prepare to block (exit also OK)

    var deadline = 0L // nonzero if possibly quiescent
    def setDeadline(v: Long): Unit = {
      deadline = v match {
        case 0L => 1L
        case _  => v
      }
    }

    var ac = (c >> RC_SHIFT).toInt
    val md = mode
    if (md < 0) { // pool is terminating
      return -1
    } else if ((md & SMASK) + ac <= 0) {
      var checkTermination = (md & SHUTDOWN) != 0
      setDeadline(System.currentTimeMillis() + keepAlive)
      val qs = queues // check for racing submission
      val n = if (qs == null) 0 else qs.length
      var i = 0
      var break = false
      while (!break && i < n) {
        if (ctl != c) { // already signalled
          checkTermination = false
          break = true
        } else {
          val q = qs(i)
          val a = if (q != null) q.array else null
          val cap = if (a != null) a.length else 0
          if (cap > 0) {
            val b = q.base
            if (b != q.top ||
                a((cap - 1) & b) != null ||
                q.source != 0) {
              if (compareAndSetCtl(c, prevCtl)) w.phase = phase // self-signal
              checkTermination = false
              break = true
            }
          }
        }

        i += 2
      }
      if (checkTermination && tryTerminate(false, false))
        return -1 // trigger quiescent termination
    }

    var alt = false
    var break = false
    while (!break) {
      val currentCtl = ctl
      if (w.phase >= 0) break = true
      else if (mode < 0) return -1
      else if ((ctl >> RC_SHIFT).toInt > ac)
        Thread.onSpinWait() // signal in progress
      else if (deadline != 0L &&
          deadline - System.currentTimeMillis() <= TIMEOUT_SLOP) {
        val prevC = c
        c = ctl
        if (prevC != c) { // ensure consistent
          ac = (c >> RC_SHIFT).toInt
        } else if (compareAndSetCtl(
              c,
              ((UC_MASK & (c - TC_UNIT)) | (w.stackPred & SP_MASK))
            )) {
          w.phase = QUIET
          return -1 // drop on timeout
        }
      } else if ({ alt = !alt; !alt }) { // check between parks
        Thread.interrupted()
      } else if (deadline != 0L) LockSupport.parkUntil(deadline)
      else LockSupport.park()
    }
    LockSupport.setCurrentBlocker(null)
    0
  }

  final private[concurrent] def isSaturated(): Boolean = {
    val maxTotal: Int = bounds >>> SWIDTH
    @tailrec
    def loop(): Boolean = {
      val c = ctl
      if ((c.toInt & ~UNSIGNALLED) != 0) false
      else if ((c >>> TC_SHIFT).toShort >= maxTotal) true
      else {
        val nc: Long = ((c + TC_UNIT) & TC_MASK) | (c & ~TC_MASK)
        if (compareAndSetCtl(c, nc)) !createWorker()
        else loop()
      }
    }
    loop()
  }

  final private[concurrent] def canStop(): Boolean = {
    var oldSum: Long = 0L
    var break = false
    while (!break) { // repeat until stable
      val qs = queues
      var md = mode
      if (qs == null || (md & STOP) != 0) return true
      val c = ctl
      if ((md & SMASK) + (ctl >> RC_SHIFT).toInt > 0) break = true
      else {
        var checkSum: Long = c
        var i = 1
        while (!break && i < qs.length) { // scan submitters
          val q = qs(i)
          val a = if (q != null) q.array else null
          val cap = if (a != null) a.length else 0
          if (cap > 0) {
            val s = q.top
            if (s != q.base ||
                a((cap - 1) & s) != null ||
                q.source != 0)
              break = true
            else checkSum += (i.toLong << 32) ^ s
          }
          i += 2
        }
        if (oldSum == checkSum && (queues eq qs)) return true
        else oldSum = checkSum
      }
    }
    (mode & STOP) != 0 // recheck mode on false return
  }

  private def tryCompensate(c: Long): Int = {
    val md = mode
    val b = bounds
    // counts are signed centered at parallelism level == 0
    val minActive: Int = (b & SMASK).toShort
    val maxTotal: Int = b >>> SWIDTH
    val active: Int = (c >> RC_SHIFT).toInt
    val total: Int = (c >>> TC_SHIFT).toShort
    val sp: Int = c.toInt & ~UNSIGNALLED

    if (total >= 0) {
      if (sp != 0) { // activate idle worker
        val qs = queues
        val n = if (qs != null) qs.length else 0
        val v: WorkQueue = if (n > 0) qs(sp & (n - 1)) else null
        if (v != null) {
          val nc: Long = (v.stackPred.toLong & SP_MASK) | (UC_MASK & c)
          if (compareAndSetCtl(c, nc)) {
            v.phase = sp
            v.owner.foreach(LockSupport.unpark)
            return UNCOMPENSATE
          }
        }
        return -1 // retry
      } else if (active > minActive) { // reduce parallelism
        val nc: Long = (RC_MASK & (c - RC_UNIT)) | (~RC_MASK & c)
        return if (compareAndSetCtl(c, nc)) UNCOMPENSATE
        else -1
      }
    }
    if (total < maxTotal) { // expand pool
      val nc: Long = ((c + TC_UNIT) & TC_MASK) | (c & ~TC_MASK)
      return if (!compareAndSetCtl(c, nc)) -1
      else {
        if (!createWorker()) 0
        else UNCOMPENSATE
      }
    } else if (!compareAndSetCtl(c, c)) return -1
    else if (saturate != null && saturate.test(this)) return 0
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
      w: WorkQueue
  ): Int = {
    var s = 0
    if (task != null && w != null) {
      val wsrc: Int = w.source
      val wid: Int = w.config & SMASK
      var r: Int = wid + 2
      var scan: Boolean = true
      var c: Long = 0L // track ctl stability

      var counter = 0
      while (true) {
        counter += 1
        s = task.status
        if (s < 0) return s
        else if ({ scan = !scan; scan }) { // previous scan was empty
          if (mode < 0) ForkJoinTask.cancelIgnoringExceptions(task)
          else if ({
            val prevC = c; c = ctl;
            c == prevC && { s = tryCompensate(c); s >= 0 }
          }) return s // block
        } else { // scan for subtasks
          val qs: Array[WorkQueue] = queues
          val n: Int =
            if (qs == null) 0
            else qs.length
          val m: Int = n - 1
          var i: Int = n
          var break = false
          while (!break && i > 0) {
            val j: Int = r & m
            val q: WorkQueue = qs(j)
            if (q != null) {
              val a = q.array
              val cap = if (a != null) a.length else 0
              if (cap > 0) {
                val b: Int = q.base
                val k: Int = (cap - 1) & b
                val nextBase: Int = b + 1
                val src: Int = j | SRC
                val t: ForkJoinTask[_] = WorkQueue.getSlot(a, k)
                val sq: Int = q.source & SMASK
                val eligible: Boolean =
                  sq == wid || {
                    val x = qs(sq & m)
                    x != null && {
                      val sx = x.source & SMASK
                      sx == wid || { // indirect
                        val y = qs(sx & m)
                        (y != null && (y.source & SMASK) == wid) // 2-indirect
                      }
                    }
                  }

                if ({ s = task.status; s < 0 }) return s
                else if ((q.source & SMASK) != sq || q.base != b) {
                  scan = true
                } else if (t == null) {
                  scan |=
                    a(nextBase & (cap - 1)) != null || q.top != b // lagging
                } else if (eligible) {
                  if (WorkQueue.casSlotToNull(a, k, t)) {
                    q.base = nextBase
                    w.source = src
                    t.doExec()
                    w.source = wsrc
                  }
                  scan = true
                  break = true
                }
              }
            }

            i -= 2
            r += 2
          }
        }
      }
    }
    s
  }

  final private[concurrent] def helpComplete(
      task: ForkJoinTask[_],
      w: WorkQueue,
      owned: Boolean
  ): Int = {
    var s: Int = 0
    if (task != null && w != null) {
      var r: Int = w.config
      var scan: Boolean = true
      var locals: Boolean = true
      var c: Long = 0L
      var breakOuter = false
      while (!breakOuter) {
        if (locals) { // try locals before scanning
          if ({ s = w.helpComplete(task, owned, 0); s < 0 }) breakOuter = true
          locals = false
        } else if ({ s = task.status; s < 0 }) breakOuter = true
        else if ({ scan = !scan; scan })
          if ({ val prevC = c; c = ctl; prevC == c }) breakOuter = true
          else {
            val qs: Array[WorkQueue] = queues
            val n: Int =
              if ((qs == null)) 0
              else qs.length
            var i: Int = n
            var break = false
            while (!break && !breakOuter && i > 0) {
              var j: Int = r & (n - 1)
              val q = qs(j)
              val a = if (q != null) q.array else null
              val b: Int = if (q != null) q.base else 0
              val cap: Int = if (a != null) a.length else 0
              var eligible: Boolean = false
              if (cap > 0) {
                val k: Int = (cap - 1) & b
                val nextBase: Int = b + 1
                val t: ForkJoinTask[_] = WorkQueue.getSlot(a, k)
                t match {
                  case cc: CountedCompleter[_] =>
                    var f: CountedCompleter[_] = cc
                    while ({
                      eligible = (f eq task)
                      !eligible && { f = f.completer; f != null }
                    }) ()
                  case _ => ()
                }
                if ({ s = task.status; s < 0 }) breakOuter = true
                else if (q.base != b) scan = true
                else if (t == null)
                  scan |= (a(nextBase & (cap - 1)) != null || q.top != b)
                else if (eligible) {
                  if (WorkQueue.casSlotToNull(a, k, t)) {
                    q.setBaseOpaque(nextBase)
                    t.doExec()
                    locals = true
                  }
                  scan = true
                  break = true
                }
              }

              i -= 1
              r += 1
            }
          }
      }
    }
    s
  }

  private def pollScan(submissionsOnly: Boolean): ForkJoinTask[_] = {
    VarHandle.acquireFence()
    scanRover += 0x61c88647 // Weyl increment raciness OK
    val r =
      if (submissionsOnly) scanRover & ~1 // even indices only
      else scanRover
    val step = if (submissionsOnly) 2 else 1
    var qs = queues
    var n = 0
    var break = false
    while (!break && { qs = queues; qs != null } && {
          n = qs.length; n > 0
        }) {
      var scan = false
      var i = 0
      while (i < n) {
        val j: Int = (n - 1) & (r + i)
        val q: WorkQueue = qs(j)
        val a = if (q != null) q.array else null
        val cap = if (a != null) a.length else 0
        if (cap > 0) {
          val b = q.base
          val k: Int = (cap - 1) & b
          val nextBase: Int = b + 1
          val t: ForkJoinTask[_] = WorkQueue.getSlot(a, k)
          if (q.base != b) scan = true
          else if (t == null)
            scan |= (q.top != b || a(nextBase & (cap - 1)) != null)
          else if (!WorkQueue.casSlotToNull(a, k, t)) scan = true
          else {
            q.setBaseOpaque(nextBase)
            return t
          }
        }

        i += step
      }
      if (!scan && (queues eq qs)) break = true
    }
    null
  }

  final private[concurrent] def helpQuiescePool(
      w: WorkQueue,
      nanos: Long,
      interruptible: Boolean
  ): Int = {
    if (w == null) return 0
    val startTime = System.nanoTime()
    var parkTime = 0L
    val prevSrc = w.source
    var wsrc = prevSrc
    val cfg = w.config
    var r = cfg + 1
    var active = true
    var locals = true
    while (true) {
      var busy = false
      var scan = false
      if (locals) { // run local tasks before (re)polling
        locals = false
        var u = null: ForkJoinTask[_]
        while ({
          u = w.nextLocalTask(cfg)
          u != null
        }) u.doExec()
      }
      val qs = queues
      val n = if (qs == null) 0 else qs.length
      var break = false
      var i = n
      while (!break && i > 0) {
        val j = (n - 1) & r
        val q = qs(j)
        val a = if (q != null) q.array else null
        val cap = if (a != null) a.length else 0
        if (q != w && cap > 0) {
          val b = q.base
          val k = (cap - 1) & b
          val nextBase = b + 1
          val src = j | SRC
          val t = WorkQueue.getSlot(a, k)
          if (q.base != b) {
            busy = true
            scan = true
          } else if (t != null) {
            busy = true
            scan = true
            if (!active) { // increment before taking
              active = true
              getAndAddCtl(RC_UNIT)
            }
            if (WorkQueue.casSlotToNull(a, k, t)) {
              q.base = nextBase
              w.source = src
              t.doExec()
              wsrc = prevSrc
              w.source = wsrc
              locals = true
            }
            break = true
          } else if (!busy) {
            if (q.top != b || a(nextBase & (cap - 1)) != null) {
              busy = true
              scan = true
            } else if (q.source != QUIET && q.phase >= 0)
              busy = true
          }
        }
        i -= 1
        r += 1
      }
      VarHandle.acquireFence()
      if (!scan && (queues eq qs)) {
        if (!busy) {
          w.source = prevSrc
          if (!active) getAndAddCtl(RC_UNIT)
          return 1
        }
        if (wsrc != QUIET) {
          wsrc = QUIET
          w.source = wsrc
        }
        if (active) { // decrement
          active = false
          parkTime = 0L
          getAndAddCtl(RC_MASK & -RC_UNIT)
        } else if (parkTime == 0L) {
          parkTime = 1L << 10 // initially about 1 usec
          Thread.`yield`()
        } else {
          val interrupted = interruptible && Thread.interrupted()
          if (interrupted || System.nanoTime() - startTime > nanos) {
            getAndAddCtl(RC_UNIT)
            return if (interrupted) -1 else 0
          } else {
            LockSupport.parkNanos(this, parkTime)
            if (parkTime < (nanos >>> 8) && parkTime < (1L << 20))
              parkTime <<= 1 // max sleep approx 1 sec or 1% nanos
          }
        }
      }
    }
    -1 // unreachable
  }

  final private[concurrent] def externalHelpQuiescePool(
      nanos: Long,
      interruptible: Boolean
  ): Int = {
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

  final private[concurrent] def nextTaskFor(w: WorkQueue): ForkJoinTask[_] =
    if (w == null) pollScan(false)
    else
      w.nextLocalTask(w.config) match {
        case null => pollScan(false)
        case t    => t
      }

  // External operations

  final private[concurrent] def submissionQueue(): WorkQueue = {

    @tailrec
    def loop(r: Int): WorkQueue = {
      val qs = queues
      val n = if (qs != null) qs.length else 0
      if ((mode & SHUTDOWN) != 0 || n <= 0) return null

      val id = r << 1
      val i = (n - 1) & id
      qs(i) match {
        case null =>
          Option(registrationLock)
            .foreach { lock =>
              val w = new WorkQueue(id | SRC)
              lock.lock() // install under lock
              if (qs(i) == null)
                qs(i) = w // else lost race discard
              lock.unlock()
            }
          loop(r)
        case q if !q.tryLock() => // move and restart
          loop(ThreadLocalRandom.advanceProbe(r))
        case q => q
      }
    }

    val r = ThreadLocalRandom.getProbe() match {
      case 0 => // initialize caller's probe
        ThreadLocalRandom.localInit()
        ThreadLocalRandom.getProbe()
      case probe => probe
    }
    loop(r) // even indices only
  }

  final private[concurrent] def externalPush(task: ForkJoinTask[_]): Unit = {
    submissionQueue() match {
      case null =>
        throw new RejectedExecutionException // shutdown or disabled
      case q =>
        if (q.lockedPush(task)) signalWork()
    }
  }

  private def externalSubmit[T](task: ForkJoinTask[T]): ForkJoinTask[T] = {
    if (task == null) throw new NullPointerException()

    Thread.currentThread() match {
      case worker: ForkJoinWorkerThread
          if worker.workQueue != null && (worker.pool eq this) =>
        worker.workQueue.push(task, this)
      case _ =>
        externalPush(task)
    }
    task
  }

  // Termination

  private def tryTerminate(now: Boolean, enable: Boolean): Boolean = {
    // try to set SHUTDOWN, then STOP, then help terminate
    var md: Int = mode
    if ((md & SHUTDOWN) == 0) {
      if (!enable) return false
      md = getAndBitwiseOrMode(SHUTDOWN)
    }
    if ((md & STOP) == 0) {
      if (!now && !canStop()) return false
      md = getAndBitwiseOrMode(STOP)
    }
    if ((md & TERMINATED) == 0) {
      while (pollScan(false) match {
            case null => false
            case t    => ForkJoinTask.cancelIgnoringExceptions(t); true
          }) ()

      // unblock other workers
      val qs = queues
      val n = if (qs != null) qs.length else 0
      if (n > 0) {
        for {
          j <- 1 until n by 2
          q = qs(j) if q != null
          thread <- q.owner if !thread.isInterrupted()
        } {
          try thread.interrupt()
          catch {
            case ignore: Throwable => ()
          }
        }
      }

      // signal when no workers
      if ((md & SMASK) + (ctl >>> TC_SHIFT).toShort <= 0 &&
          (getAndBitwiseOrMode(TERMINATED) & TERMINATED) == 0) {
        val lock = registrationLock
        lock.lock()
        if (termination != null) {
          termination.signalAll()
        }
        lock.unlock()
      }
    }
    true
  }

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
      keepAlive =
        Math.max(unit.toMillis(keepAliveTime), ForkJoinPool.TIMEOUT_SLOP),
      workerNamePrefix = {
        val pid: String = Integer.toString(ForkJoinPool.getAndAddPoolIds(1) + 1)
        s"$pid-worker-"
      }
    )
    if (factory == null || unit == null) throw new NullPointerException
    val p: Int = parallelism
    if (p <= 0 || p > MAX_CAP || p > maximumPoolSize || keepAliveTime <= 0L)
      throw new IllegalArgumentException
    val size: Int = 1 << (33 - Integer.numberOfLeadingZeros(p - 1))
    val corep: Int = Math.min(Math.max(corePoolSize, p), MAX_CAP)
    val maxSpares: Int = Math.min(maximumPoolSize, MAX_CAP) - p
    val minAvail: Int =
      Math.min(Math.max(minimumRunnable, 0), MAX_CAP)
    this.bounds = ((minAvail - p) & SMASK) | (maxSpares << SWIDTH)
    this.mode = p | (if (asyncMode) FIFO else 0)
    this.ctl = ((((-corep).toLong << TC_SHIFT) & TC_MASK) |
      ((-p.toLong << RC_SHIFT) & RC_MASK))
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
    externalSubmit(task)
    task.join()
  }

  def execute(task: ForkJoinTask[_]): Unit = {
    externalSubmit(task)
  }

  // AbstractExecutorService methods

  override def execute(task: Runnable): Unit = {
    // Scala3 compiler has problems with type intererenfe when passed to externalSubmit directlly
    val taskToUse: ForkJoinTask[_] = task match {
      case task: ForkJoinTask[_] => task
      case _                     => new ForkJoinTask.RunnableExecuteAction(task)
    }
    externalSubmit(taskToUse)
  }

  def submit[T](task: ForkJoinTask[T]): ForkJoinTask[T] = {
    externalSubmit(task)
  }

  override def submit[T](task: Callable[T]): ForkJoinTask[T] = {
    externalSubmit(new ForkJoinTask.AdaptedCallable[T](task))
  }

  override def submit[T](task: Runnable, result: T): ForkJoinTask[T] = {
    externalSubmit(new ForkJoinTask.AdaptedRunnable[T](task, result))
  }

  override def submit(task: Runnable): ForkJoinTask[_] = {
    val taskToUse = task match {
      case task: ForkJoinTask[_] => task
      case _ => new ForkJoinTask.AdaptedRunnableAction(task): ForkJoinTask[_]
    }
    externalSubmit(taskToUse)
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
        externalSubmit(f)
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
        externalSubmit(f)
      }
      val startTime = System.nanoTime()
      var ns = nanos
      def timedOut = ns < 0L
      for (i <- futures.size() - 1 to 0 by -1) {
        val f = futures.get(i)
        if (!f.isDone()) {
          if (timedOut)
            ForkJoinTask.cancelIgnoringExceptions(f)
          else {
            try f.get(ns, TimeUnit.NANOSECONDS)
            catch {
              case _: CancellationException | _: TimeoutException |
                  _: ExecutionException =>
                ()
            }
            ns = nanos - (System.nanoTime() - startTime)
          }
        }
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
  @throws[ExecutionException]
  override def invokeAny[T](tasks: Collection[_ <: Callable[T]]): T = {
    if (tasks.isEmpty()) throw new IllegalArgumentException()
    val n = tasks.size()
    val root = new InvokeAnyRoot[T](n)
    val fs = new ArrayList[InvokeAnyTask[T]](n)
    var break = false
    val it = tasks.iterator()
    while (!break && it.hasNext()) {
      it.next() match {
        case null => throw new NullPointerException()
        case c =>
          val f = new InvokeAnyTask[T](root, c)
          fs.add(f)
          if (isSaturated()) f.doExec()
          else externalSubmit(f)
          if (root.isDone()) break = true
      }
    }
    try root.get()
    finally {
      val it = fs.iterator()
      while (it.hasNext()) ForkJoinTask.cancelIgnoringExceptions(it.next())
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
    val root = new InvokeAnyRoot[T](n)
    val fs = new ArrayList[InvokeAnyTask[T]](n)
    var break = false
    val it = tasks.iterator()
    while (it.hasNext()) {
      it.next() match {
        case null => throw new NullPointerException()
        case c =>
          val f = new InvokeAnyTask(root, c)
          fs.add(f)
          if (isSaturated()) f.doExec()
          else externalSubmit(f)
          if (root.isDone()) break = true
      }
    }
    try root.get(nanos, TimeUnit.NANOSECONDS)
    finally {
      val it = fs.iterator()
      while (it.hasNext()) ForkJoinTask.cancelIgnoringExceptions(it.next())
    }
  }

  def getFactory(): ForkJoinWorkerThreadFactory = factory

  def getUncaughtExceptionHandler(): UncaughtExceptionHandler = ueh

  def getParallelism(): Int = {
    (mode & SMASK).max(1)
  }

  def getPoolSize(): Int = {
    ((mode & SMASK) + (ctl >>> TC_SHIFT).toShort)
  }

  def getAsyncMode(): Boolean = {
    (mode & FIFO) != 0
  }

  def getRunningThreadCount: Int = {
    VarHandle.acquireFence()
    val qs = queues
    var rc = 0
    if (queues != null) {
      for (i <- 1 until qs.length by 2) {
        val q = qs(i)
        if (q != null && q.isApparentlyUnblocked()) rc += 1
      }
    }
    rc
  }

  def getActiveThreadCount(): Int = {
    val r = (mode & SMASK) + (ctl >> RC_SHIFT).toInt
    r.max(0) // suppress momentarily negative values
  }

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
    VarHandle.acquireFence()
    var count = 0
    val qs = queues
    if (qs != null) {
      for {
        i <- 1 until qs.length by 2
        q = qs(i) if q != null
      } count += q.queueSize()
    }
    count
  }

  def getQueuedSubmissionCount(): Int = {
    VarHandle.acquireFence()
    var count = 0
    val qs = queues
    if (qs != null) {
      for {
        i <- 0 until qs.length by 2
        q = qs(i) if q != null
      } count += q.queueSize()
    }
    count
  }

  def hasQueuedSubmissions(): Boolean = {

    VarHandle.acquireFence()
    val qs = queues
    if (qs != null) {
      var i = 0
      while (i < qs.length) {
        val q = qs(i)
        if (q != null && !q.isEmpty()) return true
        i += 2
      }
    }
    false
  }

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
    val md: Int = mode // read volatile fields first
    val c: Long = ctl
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

    val pc = md & SMASK
    val tc = pc + (c >>> TC_SHIFT).toShort
    val ac = (pc + (c >> RC_SHIFT).toInt) match {
      case n if n < 0 => 0 // ignore transient negative
      case n          => n
    }

    @alwaysinline
    def modeSetTo(mode: Int): Boolean = (md & mode) != 0
    val level =
      if (modeSetTo(TERMINATED)) "Terminated"
      else if (modeSetTo(STOP)) "Terminating"
      else if (modeSetTo(SHUTDOWN)) "Shutting down"
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

  override def shutdown(): Unit = {
    if (this != common) tryTerminate(false, true)
  }

  override def shutdownNow(): List[Runnable] = {
    if (this ne common) tryTerminate(true, true)
    Collections.emptyList()
  }

  def isTerminated(): Boolean = {
    (mode & TERMINATED) != 0
  }

  def isTerminating(): Boolean = {
    (mode & (STOP | TERMINATED)) == STOP
  }

  override def isShutdown(): Boolean = {
    (mode & SHUTDOWN) != 0
  }

  @throws[InterruptedException]
  override def awaitTermination(timeout: Long, unit: TimeUnit): Boolean = {
    var nanos = unit.toNanos(timeout)
    var terminated = false
    if (this eq common) {
      val q = Thread.currentThread() match {
        case worker: ForkJoinWorkerThread if (worker.pool eq this) =>
          helpQuiescePool(worker.workQueue, nanos, true)
        case _ =>
          externalHelpQuiescePool(nanos, true)
      }
      if (q < 0) throw new InterruptedException()
    } else {
      def isTerminated() = (mode & TERMINATED) != 0
      terminated = isTerminated()
      val lock = registrationLock
      if (!terminated && lock != null) {
        lock.lock()
        if (termination == null) {
          termination = lock.newCondition()
        }
        try
          while ({
            terminated = isTerminated()
            !terminated && nanos > 0L
          }) {
            nanos = termination.awaitNanos(nanos)
          }
        finally lock.unlock()
      }
    }
    terminated
  }

  def awaitQuiescence(timeout: Long, unit: TimeUnit): Boolean = {
    val nanos: Long = unit.toNanos(timeout)
    val q = Thread.currentThread() match {
      case wt: ForkJoinWorkerThread if (wt.pool eq this) =>
        helpQuiescePool(wt.workQueue, nanos, false)
      case _ => externalHelpQuiescePool(nanos, false)
    }
    q > 0
  }

  @throws[InterruptedException]
  private def compensatedBlock(blocker: ManagedBlocker): Unit = {
    if (blocker == null) throw new NullPointerException()

    @tailrec
    def loop(): Unit = {
      val c = ctl
      if (blocker.isReleasable()) return ()

      val comp = tryCompensate(c)
      if (comp >= 0) {
        val post = if (comp == 0) 0L else RC_UNIT
        val done =
          try blocker.block()
          finally getAndAddCtl(post)
        if (done) return ()
        else loop()
      }
    }

    loop()
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

// Bounds
  private[concurrent] final val SWIDTH = 16 // width of short
  private[concurrent] final val SMASK = 0xffff // short bits == max index
  private[concurrent] final val MAX_CAP = 0x7fff // max #workers - 1
// Masks and units for WorkQueue.phase and ctl sp subfield
  final val UNSIGNALLED = 1 << 31 // must be negative
  final val SS_SEQ = 1 << 16 // version count

  // Mode bits and sentinels, some also used in WorkQueue fields
  final val FIFO = 1 << 16 // fifo queue or access mode
  final val SRC = 1 << 17 // set for valid queue ids
  final val INNOCUOUS = 1 << 18 // set for Innocuous workers
  final val QUIET = 1 << 19 // quiescing phase or source
  final val SHUTDOWN = 1 << 24
  final val TERMINATED = 1 << 25
  final val STOP = 1 << 31 // must be negative
  final val UNCOMPENSATE = 1 << 16 // tryCompensate return

  private[concurrent] final val INITIAL_QUEUE_CAPACITY: Int = 1 << 8

  private[concurrent] object WorkQueue {
    // Support for atomic operations
    import scala.scalanative.libc.atomic.memory_order._
    @alwaysinline
    private def arraySlotAtomicAccess[T <: AnyRef](
        a: Array[T],
        idx: Int
    ): CAtomicRef[T] = {
      val nativeArray = a.asInstanceOf[ObjectArray]
      val elemRef = nativeArray.at(idx).asInstanceOf[Ptr[T]]
      new CAtomicRef[T](elemRef)
    }

    private[concurrent] def getSlot(
        a: Array[ForkJoinTask[_]],
        i: Int
    ): ForkJoinTask[_] = {
      arraySlotAtomicAccess(a, i).load(memory_order_acquire)
    }

    @alwaysinline
    private[concurrent] def getAndClearSlot(
        a: Array[ForkJoinTask[_]],
        i: Int
    ): ForkJoinTask[_] = {
      arraySlotAtomicAccess(a, i).exchange(null: ForkJoinTask[_])
    }

    private[concurrent] def setSlotVolatile(
        a: Array[ForkJoinTask[_]],
        i: Int,
        v: ForkJoinTask[_]
    ): Unit = {
      arraySlotAtomicAccess(a, i).store(v)
    }

    private[concurrent] def casSlotToNull(
        a: Array[ForkJoinTask[_]],
        i: Int,
        c: ForkJoinTask[_]
    ): Boolean = {
      arraySlotAtomicAccess(a, i)
        .compareExchangeWeak(c, null: ForkJoinTask[_])
    }
  }

  final class WorkQueue private (
      private[concurrent] val owner: Option[ForkJoinWorkerThread]
  ) {
    // versioned, negative if inactive
    @volatile private[concurrent] var phase: Int = 0
    // source queue id, lock, or sentinel
    @volatile private[concurrent] var source: Int = 0
    private val sourceAtomic = new CAtomicInt(
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "source"))
    )

    // index, mode, ORed with SRC after init
    private[concurrent] var config: Int = 0

    // the queued tasks power of 2 size
    private[concurrent] var array: Array[ForkJoinTask[_]] = _

    // pool stack (ctl) predecessor link
    private[concurrent] var stackPred: Int = 0
    // index of next slot for poll
    @volatile private[concurrent] var base: Int = 0
    private[ForkJoinPool] val baseAtomic = new CAtomicInt(
      fromRawPtr[Int](Intrinsics.classFieldRawPtr(this, "base"))
    )
    private[concurrent] var top: Int = 0 // index of next slot for push
    private[concurrent] var nsteals: Int = 0 // steals from other queues

    def this(owner: ForkJoinWorkerThread, isInnocuous: Boolean) = {
      this(Some(owner))
      this.config = if (isInnocuous) INNOCUOUS else 0
    }

    def this(config: Int) = {
      this(owner = None)
      this.array = new Array[ForkJoinTask[_]](INITIAL_QUEUE_CAPACITY)
      this.config = config
      this.phase = -1
    }

    @alwaysinline
    final def tryLock(): Boolean = sourceAtomic.compareExchangeStrong(0, 1)

    @alwaysinline
    final def setBaseOpaque(b: Int): Unit = {
      import scala.scalanative.libc.atomic.memory_order.memory_order_relaxed
      baseAtomic.store(b, memory_order_relaxed)
    }

    final def getPoolIndex(): Int =
      (config & 0xffff) >>> 1 // ignore odd/even tag bit

    final def queueSize(): Int = {
      VarHandle.acquireFence() // ensure fresh reads by external callers
      val n = top - base
      n.max(0) // ignore transient negative
    }

    final def isEmpty(): Boolean =
      !((source != 0 && owner.isEmpty) || top - base > 0)

    final def push(task: ForkJoinTask[_], pool: ForkJoinPool): Unit = {
      val a = array
      val s = top
      top += 1
      val d = s - base
      val cap = if (a != null) a.length else 0
      // skip insert if disabled
      if (pool != null && cap > 0) {
        val m = cap - 1
        WorkQueue.setSlotVolatile(a, m & s, task)
        val shouldGrowArray = d == m
        if (shouldGrowArray)
          growArray()
        if (shouldGrowArray || a(m & (s - 1)) == null)
          pool.signalWork() // signal if was empty or resized
      }
    }

    final def lockedPush(task: ForkJoinTask[_]): Boolean = {
      val a = array
      val s = top
      top += 1
      val d = s - base
      val cap = if (a != null) a.length else 0
      if (cap > 0) {
        val m = cap - 1
        a(m & s) = task
        def shouldGrowArray = d == m
        if (shouldGrowArray) growArray()
        source = 0 // unlock
        if (shouldGrowArray || a(m & (s - 1)) == null)
          return true
      }
      false
    }

    final def growArray(): Unit = {
      val oldArray = array
      val oldCap = if (oldArray != null) oldArray.length else 0
      val newCap = oldCap << 1
      val s = top - 1
      if (oldCap > 0 && newCap > 0) { // skip if disabled
        val newArray: Array[ForkJoinTask[_]] =
          try new Array[ForkJoinTask[_]](newCap)
          catch {
            case ex: Throwable =>
              top = s
              if (owner.isEmpty) {
                source = 0 // unlock
              }
              throw new RejectedExecutionException("Queue capacity exceeded")
          }

        val newMask = newCap - 1
        val oldMask = oldCap - 1

        @tailrec
        def loop(k: Int, s: Int): Unit = {
          if (k > 0) {
            // poll old, push to new
            getAndClearSlot(oldArray, s & oldMask) match {
              case null => () // break, others already taken
              case task =>
                newArray(s & newMask) = task
                loop(k = k - 1, s = s - 1)
            }
          }
        }

        loop(oldCap, s)

        VarHandle.releaseFence() // fill before publish
        array = newArray
      }
    }

    private[concurrent] def pop(): ForkJoinTask[_] = {
      val a = array
      val cap = if (a != null) a.length else 0
      val curTop = top
      val s = curTop - 1
      val t =
        if (cap > 0 && base != curTop)
          WorkQueue.getAndClearSlot(a, (cap - 1) & s)
        else null
      if (t != null) top = s
      t
    }

    final def tryUnpush(task: ForkJoinTask[_]): Boolean = {
      val s = top
      val newS = s - 1
      val a = array
      val cap = if (a != null) a.length else 0
      if (cap > 0 &&
          base != s &&
          WorkQueue.casSlotToNull(a, (cap - 1) & newS, task)) {
        top = newS
        true
      } else false
    }

    final def externalTryUnpush(task: ForkJoinTask[_]): Boolean = {
      while (true) {
        val s = top
        val a = array
        val cap = if (a != null) a.length else 0
        val k = (cap - 1) & (s - 1)
        if (cap <= 0 || a(k) != task) return false
        else if (tryLock()) {
          if (top == s && array == a) {
            if (WorkQueue.casSlotToNull(a, k, task))
              top = s - 1
            source = 0
            return true
          }
          source = 0 // release lock for retry
        }
        Thread.`yield`() // trylock failure
      }
      false
    }

    final def tryRemove(task: ForkJoinTask[_], owned: Boolean): Boolean = {
      val p = top
      val a = array
      val cap = if (a != null) a.length else 0
      var taken = false

      if (task != null && cap > 0) {
        val m = cap - 1
        val s = p - 1
        val d = p - base

        @tailrec
        def loop(i: Int, d: Int): Unit = {
          val k = i & m
          a(k) match {
            case `task` =>
              if (owned || tryLock()) {
                if ((owned || (array == a && top == p)) &&
                    WorkQueue.casSlotToNull(a, k, task)) {
                  for (j <- i.until(s)) {
                    a(j & m) = getAndClearSlot(a, (j + 1) & m)
                  }
                  top = s
                  taken = true
                }
                if (!owned) source = 0
              }

            case _ =>
              if (d > 0) loop(i - 1, d - 1)
          }
        }

        loop(i = s, d = d)
      }

      taken
    }

    // variants of poll

    final def tryPoll(): ForkJoinTask[_] = {
      val a = array
      val cap = if (a != null) a.length else 0

      val b = base
      val k = (cap - 1) & b
      if (cap > 0) {
        val task = WorkQueue.getSlot(a, k)
        if (base == b &&
            task != null &&
            WorkQueue.casSlotToNull(a, k, task)) {
          setBaseOpaque(b + 1)
          return task
        }
      }
      null
    }

    final def nextLocalTask(cfg: Int): ForkJoinTask[_] = {
      val a = array
      val cap = if (a != null) a.length else 0
      val mask = cap - 1
      var currentTop = top

      @tailrec
      def loop(): ForkJoinTask[_] = {
        var currentBase = base

        val d = currentTop - currentBase
        if (d <= 0) null
        else {
          def tryTopSlot(): Option[ForkJoinTask[_]] = {
            currentTop -= 1
            Option(getAndClearSlot(a, currentTop & mask))
              .map { task =>
                top = currentTop
                task
              }
          }

          def tryBaseSlot(): Option[ForkJoinTask[_]] = {
            val b = currentBase
            currentBase += 1
            Option(getAndClearSlot(a, b & mask))
              .map { task =>
                setBaseOpaque(currentBase)
                task
              }
          }

          if (d == 1 || (cfg & FIFO) == 0)
            tryTopSlot().orNull
          else
            tryBaseSlot() match {
              case Some(value) => value
              case None        => loop()
            }
        }
      }

      if (cap > 0) loop()
      else null
    }

    final def nextLocalTask(): ForkJoinTask[_] =
      nextLocalTask(config)

    final def peek(): ForkJoinTask[_] = {
      VarHandle.acquireFence()
      // int cap Array[ForkJoinTask[_]]()  a
      val a = array
      val cap = if (a != null) a.length else 0
      if (cap > 0) {
        val mask = if ((config & FIFO) != 0) base else top - 1
        a((cap - 1) & mask)
      } else null: ForkJoinTask[_]
    }

    // specialized execution methods

    final def topLevelExec(task: ForkJoinTask[_], q: WorkQueue): Unit = {
      val cfg = config
      var currentTask = task
      var nStolen = 1
      while (currentTask != null) {
        currentTask.doExec()
        currentTask = nextLocalTask(cfg)
        currentTask match {
          case null if q != null =>
            currentTask = q.tryPoll()
            if (currentTask != null) {
              nStolen += 1
            }
          case _ => ()
        }
      }
      nsteals += nStolen
      source = 0
      if ((cfg & INNOCUOUS) != 0) {
        ThreadLocalRandom.eraseThreadLocals(Thread.currentThread())
      }
    }

    final private[concurrent] def helpComplete(
        task: ForkJoinTask[_],
        owned: Boolean,
        limit: Int
    ): Int = {
      var taken = false

      @tailrec def loop(limit: Int): Int = {
        val status = task.status
        val a = array
        val cap = if (a != null) a.length else 0
        val p = top
        val s = p - 1
        val k = (cap - 1) & s
        val t = if (cap > 0) a(k) else null

        @tailrec
        def doTryComplete(current: CountedCompleter[_]): Unit =
          current match {
            case `task` =>
              @alwaysinline def tryTakeTask() = {
                taken = WorkQueue.casSlotToNull(a, k, t); taken
              }
              if (owned) {
                if (tryTakeTask()) top = s
              } else if (tryLock()) {
                if (top == p && array == a && tryTakeTask()) top = s
                source = 0
              }

            case _ =>
              val next = current.completer
              if (next != null) doTryComplete(next)
          }

        t match {
          case completer: CountedCompleter[_] if status >= 0 =>
            taken = false
            doTryComplete(completer)
            if (!taken) status
            else {
              t.doExec()
              val nextLimit = limit - 1
              if (limit != 0 && nextLimit == 0) status
              else loop(nextLimit)
            }

          case _ => status
        }
      }

      if (task == null) 0
      else loop(limit)
    }

    final private[concurrent] def helpAsyncBlocker(
        blocker: ManagedBlocker
    ): Unit = {
      var cap: Int = 0
      var b: Int = 0
      var d: Int = 0
      var k: Int = 0
      var a: Array[ForkJoinTask[_]] = null
      var t: ForkJoinTask[_] = null
      while ({
        blocker != null && { b = base; d = top - b; d > 0 } && {
          a = array; a != null
        } && { cap = a.length; cap > 0 } && {
          k = (cap - 1) & b; t = WorkQueue.getSlot(a, k);
          t == null && d > 1 || t
            .isInstanceOf[CompletableFuture.AsynchronousCompletionTask]
        } && !blocker.isReleasable()
      }) {
        if (t != null &&
            base == { val b2 = b; b += 1; b2 } &&
            WorkQueue.casSlotToNull(a, k, t)) {
          setBaseOpaque(b)
          t.doExec()
        }
      }
    }

    // misc

    final def initializeInnocuousWorker(): Unit = {
      val t = Thread.currentThread()
      ThreadLocalRandom.eraseThreadLocals(t)
    }

    final def isApparentlyUnblocked(): Boolean = {
      owner
        .map(_.getState())
        .exists { s =>
          s != Thread.State.BLOCKED &&
          s != Thread.State.WAITING &&
          s != Thread.State.TIMED_WAITING
        }
    }
  }

  // TODO should be final, but it leads to problems with static forwarders
  final val defaultForkJoinWorkerThreadFactory: ForkJoinWorkerThreadFactory =
    new DefaultForkJoinWorkerThreadFactory()

  private[concurrent] object common
      extends ForkJoinPool(
        factory = new DefaultCommonPoolForkJoinWorkerThreadFactory(),
        ueh = null,
        saturate = null,
        keepAlive = DEFAULT_KEEPALIVE,
        workerNamePrefix = null
      ) {
    val parallelism = Runtime.getRuntime().availableProcessors() - 1
    this.mode = Math.min(Math.max(parallelism, 0), MAX_CAP)
    val p = this.mode
    val size = 1 << (33 - Integer.numberOfLeadingZeros((p - 1)))
    this.bounds = ((1 - p) & SMASK) | (COMMON_MAX_SPARES << SWIDTH)
    this.ctl = ((((-p).toLong) << TC_SHIFT) & TC_MASK) |
      ((((-p).toLong) << RC_SHIFT) & RC_MASK)
    this.queues = new Array[WorkQueue](size)
  }

  private[concurrent] lazy val COMMON_PARALLELISM =
    Math.max(common.mode & SMASK, 1)

  private[concurrent] lazy val COMMON_MAX_SPARES = DEFAULT_COMMON_MAX_SPARES

  private val poolIds: AtomicInteger = new AtomicInteger(0)

  private final val DEFAULT_KEEPALIVE = 60000L

  private final val TIMEOUT_SLOP = 20L

  /** The default value for COMMON_MAX_SPARES. Overridable using the
   *  "java.util.concurrent.common.maximumSpares" system property. The default
   *  value is far in excess of normal requirements, but also far short of
   *  MAX_CAP and typical OS thread limits, so allows JVMs to catch misuse/abuse
   *  before running out of resources needed to do so.
   */
  private val DEFAULT_COMMON_MAX_SPARES: Int = 256
  // Lower and upper word masks
  private val SP_MASK: Long = 0xffffffffL
  private val UC_MASK: Long = ~(SP_MASK)
  // Release counts
  private val RC_SHIFT: Int = 48
  private val RC_UNIT: Long = 0x0001L << RC_SHIFT
  private val RC_MASK: Long = 0xffffL << RC_SHIFT
  // Total counts
  private val TC_SHIFT: Int = 32
  private val TC_UNIT: Long = 0x0001L << TC_SHIFT
  private val TC_MASK: Long = 0xffffL << TC_SHIFT
  private val ADD_WORKER: Long = 0x0001L << (TC_SHIFT + 15) // sign

  private def getAndAddPoolIds(x: Int): Int =
    poolIds.getAndAdd(x)

  private[concurrent] def commonQueue(): WorkQueue = {
    val p = common
    val qs = if (p != null) p.queues else null
    val r: Int = ThreadLocalRandom.getProbe()
    var n: Int = if (qs != null) qs.length else 0
    if (n > 0 && r != 0) qs((n - 1) & (r << 1))
    else null
  }

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
        var p: Int = pool.mode & SMASK
        val a: Int = p + (pool.ctl >> RC_SHIFT).toInt
        val n: Int = q.top - q.base
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
  final private[concurrent] class InvokeAnyRoot[E](val n: Int)
      extends ForkJoinTask[E] {
    @volatile private[concurrent] var result: E = _
    final private[concurrent] val count: AtomicInteger = new AtomicInteger(n)
    final private[concurrent] def tryComplete(c: Callable[E]): Unit = { // called by InvokeAnyTasks
      var ex: Throwable = null
      var failed: Boolean = false
      if (c != null) { // raciness OK
        if (isCancelled()) failed = true
        else if (!isDone())
          try complete(c.call())
          catch {
            case tx: Throwable =>
              ex = tx
              failed = true
          }
      }
      if (failed && count.getAndDecrement() <= 1) {
        trySetThrown(
          if (ex != null) ex
          else new CancellationException
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

  def getCommonPoolParallelism(): Int = COMMON_PARALLELISM

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
