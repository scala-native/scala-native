// scalafmt: { maxColumn = 120}

package java.lang

import java.lang.VirtualThread.DefaultScheduler
import java.util.Objects
import java.util.concurrent.*
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport

import scala.annotation.tailrec
import scala.noinline

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic.{AtomicBool, AtomicInt, AtomicRef}
import scala.scalanative.runtime.{Continuations, Intrinsics, NativeThread, fromRawPtr}
import scala.scalanative.unsigned.*

private[java] final class VirtualThread(
    name: String,
    characteristics: Int,
    task: Runnable
) extends Thread(name, characteristics) {
  import java.lang.VirtualThread.State
  Objects.requireNonNull(task)

  val scheduler: Executor = Thread.currentThread() match {
    case vt: VirtualThread => vt.scheduler
    case _                 => VirtualThread.DefaultScheduler
  }

  private type Boundary = Continuations.BoundaryLabel[Unit]
  @volatile private var boundary: Boundary | Null = _
  @volatile private var resumeExecution: () => Unit | Null = _
  private val resumeLock = new {}
  private var nextResumeGeneration: Long = 0L
  @volatile private var activeResumeGeneration: Long = 0L
  private val runDispatchState =
    new java.util.concurrent.atomic.AtomicInteger(VirtualThread.RunDispatchState.Idle)
  val carrierThreadAccessLock = new {}

  @volatile var carrierThread: Thread | Null = _
  @volatile var termination: CountDownLatch | Null = _
  @volatile var state: VirtualThread.State = VirtualThread.State.New

  /** When true, the next park() does not block. Set by unpark(), cleared in park(). */
  @volatile var parkPermit: scala.Boolean = false

  // True while this VT is submitting a task to the scheduler.
  // Prevents yielding (unmounting) so the carrier stays stable during submit,
  // analogous to pinning around scheduler submission.
  @volatile private var pinnedForSubmit: scala.Boolean = false

  // Timed park/wait callbacks must be generation-scoped so a late timeout from
  // an earlier operation cannot wake a later park/wait on the same VT.
  private val timeoutLock = new {}
  private var timeoutGeneration: Long = 0L
  private var timeoutTaskResumeGeneration: Long = 0L
  @volatile private var timeoutTask: Future[_] = _

  @alwaysinline
  private def stateAtomic =
    new AtomicInt(fromRawPtr(Intrinsics.classFieldRawPtr(this, "state")))

  @alwaysinline
  private def terminationAtomic =
    new AtomicRef[CountDownLatch](fromRawPtr(Intrinsics.classFieldRawPtr(this, "termination")))

  @alwaysinline
  private def parkPermitAtomic =
    new AtomicBool(fromRawPtr(Intrinsics.classFieldRawPtr(this, "parkPermit")))

  @inline private def compareAndSetState(expected: VirtualThread.State, value: VirtualThread.State): Boolean =
    stateAtomic.compareExchangeStrong(expected, value)

  @inline private def isRecursiveSuspendState(s: VirtualThread.State): Boolean =
    s == State.Parking || s == State.Parked ||
      s == State.TimedParking || s == State.TimedParked ||
      s == State.Yielding || s == State.Yielded

  @noinline private def reachabilityFence(target: Any): Unit = ()

  private def publishResume(resume: () => Unit): Long =
    resumeLock.synchronized {
      nextResumeGeneration += 1L
      val generation = nextResumeGeneration
      resumeExecution = resume
      activeResumeGeneration = generation
      generation
    }

  private def consumeResume(): () => Unit | Null =
    resumeLock.synchronized {
      val continue = resumeExecution
      resumeExecution = null
      activeResumeGeneration = 0L
      continue
    }

  private def isActiveResumeGeneration(generation: Long): Boolean =
    generation != 0L && activeResumeGeneration == generation && resumeExecution != null

  private inline def currentBoundaryOrThrow(op: String): Boundary =
    val current = boundary
    if current == null then throw new IllegalStateException(s"Missing continuation boundary during $op")
    current.nn

  @inline private def setParkPermit(value: scala.Boolean): Unit = {
    if (parkPermit != value) parkPermit = value
  }
  @inline private def getAndSetParkPermit(value: scala.Boolean): scala.Boolean =
    parkPermitAtomic.exchange(value)

  override def run(): Unit = () // no-op; execution is started via start() (Thread API contract).

  override def startInternal(): Unit = {
    if (!compareAndSetState(State.New, State.Started))
      throw new IllegalThreadStateException("Already started")

    try requestRun()
    catch {
      case ex: Exception =>
        afterDone()
        throw ex
    }
  }

  override def isInterrupted(): scala.Boolean = interruptedState
  override def getAndClearInterrupt(): scala.Boolean = {
    if (Thread.currentThread() ne this)
      throw new IllegalThreadStateException("getAndClearInterrupt must be called on the current thread")
    val value = interruptedState
    if (value) {
      interruptLock.synchronized {
        interruptedState = false
        carrierThread.clearInterrupt()
      }
    }
    value
  }
  override def interrupt(): Unit = {
    if (Thread.currentThread() ne this) {
      interruptLock.synchronized {
        interruptedState = true
        // we should prevent suspension of the current thread
        // when we we'd support Interruptible (in NIO we should interrupt the current blocker as well)
        carrierThread match {
          case null    => ()
          case carrier => carrier.setInterrupt()
        }
      }
      unpark()
      // Unblock if blocked and schedule execution of the continuation
    } else {
      interruptedState = true
      carrierThread.setInterrupt()
      setParkPermit(true) // consume parking permit
    }
  }

  private def cancelTimeoutTask(expectedResumeGeneration: Long): Unit =
    val task = timeoutLock.synchronized {
      if (timeoutTaskResumeGeneration == expectedResumeGeneration) {
        timeoutGeneration += 1L
        timeoutTaskResumeGeneration = 0L
        val current = timeoutTask
        timeoutTask = null
        current
      } else null
    }
    task match
      case null => ()
      case task =>
        if (!task.isDone()) task.cancel(false)

  private def scheduleTimeout(delay: scala.Long, unit: TimeUnit, resumeGeneration: Long)(onTimeout: => Unit): Unit = {
    val timeoutToken = timeoutLock.synchronized {
      timeoutGeneration += 1L
      timeoutTaskResumeGeneration = resumeGeneration
      timeoutTask = null
      timeoutGeneration
    }

    val task = VirtualThread.schedule(
      () => {
        val shouldRun = timeoutLock.synchronized {
          if (timeoutGeneration == timeoutToken && isActiveResumeGeneration(
                resumeGeneration
              )) {
            if (timeoutTaskResumeGeneration == resumeGeneration) {
              timeoutTask = null
              timeoutTaskResumeGeneration = 0L
            }
            true
          } else false
        }
        if (shouldRun) onTimeout
      },
      delay,
      unit
    )

    timeoutLock.synchronized {
      if (timeoutGeneration == timeoutToken && !task.isDone()) timeoutTask = task
      else if (!task.isDone()) task.cancel(false)
    }
  }

  private[lang] def sleepNanos(nanos: scala.Long): Unit = {
    if (Thread.currentThread() ne this)
      throw new IllegalThreadStateException("sleepNanos must be called on the current thread")
    if (nanos < 0) throw new IllegalArgumentException("nanos must be non-negative")

    if (getAndClearInterrupt()) throw new InterruptedException()
    if (nanos == 0) tryYield()
    else
      try {
        var remainingNanos = nanos
        val startNanos = System.nanoTime()
        while (remainingNanos > 0) {
          parkNanos(remainingNanos)
          if (getAndClearInterrupt()) throw new InterruptedException()
          remainingNanos = nanos - (System.nanoTime() - startNanos)
        }
      } finally setParkPermit(true)
  }

  private[java] def parkNanos(nanos: scala.Long): Unit = {
    if (Thread.currentThread() ne this)
      throw new IllegalThreadStateException("parkNanos must be called on the current thread")

    if (getAndSetParkPermit(false)) return
    if (interruptedState) return
    if (nanos <= 0) return

    state match {
      case State.Running if pinnedForSubmit =>
        // Pinned during submit: park on carrier instead of yielding
        state = State.TimedPinned
        NativeThread.currentNativeThread.parkNanos(nanos)
        state = State.Running

      case State.Running =>
        state = State.TimedParking
        // Close race with unpark that can arrive after initial permit check
        // but before we publish parking state and suspend.
        if (getAndSetParkPermit(false)) {
          state = State.Running
          return
        }
        Continuations.suspend[Unit] { resumeContinuation =>
          val generation = publishResume(resumeContinuation)
          scheduleTimeout(nanos, TimeUnit.NANOSECONDS, generation) {
            unpark(lazily = true) // timed park wakeup: prefer lazy scheduler submit
          }

          // Move into parked state only if no concurrent unpark already moved
          // us to Unparked while suspension was being established.
          compareAndSetState(State.TimedParking, State.TimedParked)
        }(using currentBoundaryOrThrow("parkNanos"))
        state = State.Running

      case s if isRecursiveSuspendState(s) =>
        // recursive park during suspension
        state = State.TimedPinned
        NativeThread.currentNativeThread.parkNanos(nanos)
        state = s
      case _ =>
        ()
    }
  }

  private[java] def park(): Unit = {
    if (Thread.currentThread() ne this)
      throw new IllegalThreadStateException("park must be called on the current thread")

    if (getAndSetParkPermit(false)) return
    if (interruptedState) return

    state match {
      case State.Running if pinnedForSubmit =>
        // Pinned during submit: park on carrier instead of yielding
        state = State.Pinned
        NativeThread.currentNativeThread.park()
        setParkPermit(false)
        state = State.Running

      case State.Running =>
        state = State.Parking
        // Close race with unpark that can arrive after initial permit check
        // but before we publish parking state and suspend.
        if (getAndSetParkPermit(false)) {
          state = State.Running
          return
        }
        Continuations.suspend[Unit] { resumeContinuation =>
          publishResume(resumeContinuation)
          // Move into parked state only if no concurrent unpark already moved
          // us to Unparked while suspension was being established.
          compareAndSetState(State.Parking, State.Parked)
        }(using currentBoundaryOrThrow("park"))
        state = State.Running

      case s if isRecursiveSuspendState(s) =>
        // recursive park during suspension, need to pin carrier thread
        state = State.Pinned
        NativeThread.currentNativeThread.park()
        setParkPermit(false) // consume
        state = s
      case _ =>
        ()
    }

  }

  /** Set parking permit first, then schedule only if this VT is already in Parked/TimedParked. If unpark races with the
   *  Parking transition, the post-yield path consumes the permit and performs the submit. For Pinned, unpark the
   *  carrier.
   *
   *  @param lazily
   *    when true, use ForkJoinPool.lazySubmit when the current thread is a pool carrier with an empty local queue
   *    (timeout and similar paths).
   */
  private def unpark(lazily: scala.Boolean): Unit = {
    // Permit first; only submit when permit was previously false (avoid redundant submit).
    // getAndSetParkPermit(true) returns the prior value; submit when it was false.
    val previousPermit = getAndSetParkPermit(true)
    if (Thread.currentThread() eq this) return

    var s = state
    var done = false
    while (!done) {
      s match {
        case State.Parked | State.TimedParked =>
          if (compareAndSetState(s, State.Unparked)) {
            if (!previousPermit) requestRun(lazily)
            done = true
          } else {
            s = state // re-read after CAS failure
          }
        case State.Pinned | State.TimedPinned =>
          val carrier = carrierThread
          if (carrier != null) LockSupport.unpark(carrier)
          done = true
        case _ =>
          done = true
      }
    }
  }
  private[java] def unpark(): Unit = unpark(false)

  private def submitScheduledRun(lazily: scala.Boolean = false): Unit =
    scheduler match {
      case pool: ForkJoinPool if lazily => pool.lazySubmit(ForkJoinTask.adapt(executeContinuation))
      case _                            => scheduler.execute(executeContinuation)
    }

  private def requestRun(lazily: scala.Boolean = false): Unit = {
    import VirtualThread.RunDispatchState
    var done = false
    while (!done) {
      runDispatchState.get() match {
        case RunDispatchState.Idle =>
          if (runDispatchState.compareAndSet(RunDispatchState.Idle, RunDispatchState.Queued)) {
            // Pin this VT (prevent unmounting) while submitting, so the carrier
            // stays stable during the submit.
            val needsPin = Thread.currentThread() eq this
            if (needsPin) pinnedForSubmit = true
            // Lazy submit only when requested and the submitter is a carrier on this pool
            // with an empty local queue. lazily=true is used from post-yield and timeout paths.
            val useLazy =
              !needsPin && lazily && (Thread.currentThread() match {
                case c: VirtualThreadCarrier if scheduler.isInstanceOf[ForkJoinPool] && (scheduler eq c.getPool()) =>
                  c.getQueuedTaskCount() == 0
                case _ => false
              })
            try submitScheduledRun(useLazy)
            catch {
              case ex: Throwable =>
                runDispatchState.compareAndSet(RunDispatchState.Queued, RunDispatchState.Idle)
                throw ex
            } finally {
              if (needsPin) pinnedForSubmit = false
            }
            done = true
          }
        case RunDispatchState.Running =>
          if (runDispatchState.compareAndSet(
                RunDispatchState.Running,
                RunDispatchState.RunningQueued
              )) {
            done = true
          }
        case RunDispatchState.Queued | RunDispatchState.RunningQueued =>
          done = true
      }
    }
  }

  private def mount(): Unit = {
    val platformThread = Thread.currentPlatformThread
    if (!platformThread.isInstanceOf[VirtualThreadCarrier]) {
      throw new IllegalStateException(s"$platformThread is not a VirtualThreadCarrier")
    }
    val carrier = platformThread
    this.carrierThread = carrier

    // sync up carrier thread interrupt status if needed
    if (interruptedState) {
      carrier.setInterrupt()
    } else if (carrier.isInterrupted()) {
      // synchronized(interruptLock) {
      // need to recheck interrupt status
      if (!interruptedState) {
        carrier.clearInterrupt()
      }
      // }
    }
    NativeThread.setCurrentThread(this)
  }

  private def unmount(): Unit = {
    if (this.state == State.Running) {
      throw new IllegalStateException("Cannot unmount virtual thread while Running")
    }
    val carrier = this.carrierThread.asInstanceOf[VirtualThreadCarrier]
    NativeThread.setCurrentThread(carrier)

    // break connection to carrier thread, synchronized with interrupt
    interruptLock.synchronized {
      this.carrierThread = null
    }
    carrier.clearInterrupt();
  }

  private def tryAcquireRunLoopToken(): Boolean = {
    import VirtualThread.RunDispatchState
    var acquired = false
    var done = false
    while (!done) {
      runDispatchState.get() match {
        case RunDispatchState.Queued =>
          if (runDispatchState.compareAndSet(RunDispatchState.Queued, RunDispatchState.Running)) {
            acquired = true
            done = true
          }
        case RunDispatchState.Running | RunDispatchState.RunningQueued | RunDispatchState.Idle =>
          done = true
      }
    }
    acquired
  }

  private def finishRunLoopIteration(): Boolean = {
    import VirtualThread.RunDispatchState
    var continue = false
    var done = false
    while (!done) {
      runDispatchState.get() match {
        case RunDispatchState.RunningQueued =>
          if (runDispatchState.compareAndSet(RunDispatchState.RunningQueued, RunDispatchState.Running)) {
            continue = true
            done = true
          }
        case RunDispatchState.Running =>
          if (runDispatchState.compareAndSet(RunDispatchState.Running, RunDispatchState.Idle)) {
            continue = false
            done = true
          }
        case RunDispatchState.Queued =>
          if (runDispatchState.compareAndSet(RunDispatchState.Queued, RunDispatchState.Running)) {
            continue = true
            done = true
          }
        case RunDispatchState.Idle =>
          continue = false
          done = true
      }
    }
    continue
  }

  private def recoverRunLoopOnUnexpectedExit(): Unit = {
    import VirtualThread.RunDispatchState
    var done = false
    while (!done) {
      runDispatchState.get() match {
        case RunDispatchState.RunningQueued =>
          if (runDispatchState.compareAndSet(RunDispatchState.RunningQueued, RunDispatchState.Queued)) {
            try submitScheduledRun(lazily = false)
            catch {
              case _: Throwable =>
                runDispatchState.compareAndSet(RunDispatchState.Queued, RunDispatchState.Idle)
            }
            done = true
          }
        case RunDispatchState.Running =>
          if (runDispatchState.compareAndSet(RunDispatchState.Running, RunDispatchState.Idle)) {
            done = true
          }
        case _ =>
          done = true
      }
    }
  }

  private def executeContinuation: Runnable = new VirtualThreadContinuation()
  private class VirtualThreadContinuation extends Runnable {
    val vThread = VirtualThread.this

    override def run(): Unit = {
      // Recovery guard: if a previous VT left TLS currentThread mapped to a VT,
      // reset to the platform carrier before dispatching this continuation.
      val platformThread = Thread.currentPlatformThread
      if (Thread.currentThread().isVirtual() && platformThread.isInstanceOf[VirtualThreadCarrier]) {
        NativeThread.setCurrentThread(platformThread)
      }

      if (!tryAcquireRunLoopToken()) {
        // Re-queue once if VT is runnable so we don't drop the resumption.
        if (state == State.Unparked || (state == State.Yielded && resumeExecution != null)) {
          requestRun()
        }
        return
      }

      var exitedNormally = false
      try
        var continueLoop = true
        while (continueLoop) {
          var runCurrent = false
          val initialState = state

          initialState match {
            case State.Started | State.Unparked | State.Yielded =>
              runCurrent = compareAndSetState(initialState, State.Running)
              if (runCurrent) {
                if (initialState == State.Unparked) {
                  VirtualThread.this.cancelTimeoutTask(VirtualThread.this.activeResumeGeneration)
                  VirtualThread.this.setParkPermit(false) // consume park event
                }
              }
            case _ => ()
          }

          if (runCurrent) {
            var resubmitYield = false
            var didAfterYieldSubmit = false
            mount()
            try
              // Every VT run/resume must start from a clean carrier handler TLS.
              // The continuation/boundary machinery re-installs the VT's own
              // captured chain; keeping stale handlers from a previous VT on
              // the carrier corrupts handler_pop/split_at during park/wait.
              Continuations.handlersReset()
              // initial run
              if (initialState == State.Started) {
                Continuations.boundary[Unit] {
                  boundary = summon[Boundary]
                  // Initial invocation of the task, might suspend
                  task.run()
                  // We get here only when whole fiber is completed
                  afterDone()
                  boundary = null
                }
              } else {
                // Consume continuation and resume, might suspend
                val continue = consumeResume()
                if (continue == null) {
                  throw new IllegalStateException("Missing continuation to resume")
                }
                try continue()
                finally reachabilityFence(continue)
              }
            catch {
              case ex: Throwable =>
                getUncaughtExceptionHandler().uncaughtException(VirtualThread.this, ex)
                afterDone()
            } finally {
              // A yielded continuation has already captured the VT handler chain.
              // Clear carrier-local delimcc TLS eagerly so no stale handlers leak
              // into the next VT dispatched on this worker.
              Continuations.handlersReset()
              // Post-yield on carrier: reconcile parked state with permit.
              val s = state
              // Park path: Parked/TimedParked (LockSupport.park) + parkPermit; skip when resumeExecution null (wait).
              if ((s == State.Parked || s == State.TimedParked)
                  && resumeExecution != null
                  && getAndSetParkPermit(false)) {
                if (!compareAndSetState(s, State.Unparked)) setParkPermit(true)
                else {
                  requestRun(lazily = true)
                  didAfterYieldSubmit = true
                }
              }
              resubmitYield = (state == State.Yielded || state == State.Unparked) &&
                (resumeExecution != null) && !didAfterYieldSubmit
              unmount()
            }

            if (resubmitYield) {
              // Resubmit after yield using lazy submit when permitted (carrier, empty queue).
              requestRun(lazily = true)
            }
          }

          // Defensive requeue: if a continuation is available but dispatch state
          // got desynchronized, ensure we keep scheduling progress.
          if ((state == State.Yielded || state == State.Unparked) && resumeExecution != null) {
            requestRun()
          }

          continueLoop = finishRunLoopIteration()
        }
        exitedNormally = true
      finally if (!exitedNormally) recoverRunLoopOnUnexpectedExit()
    }
  }

  private[lang] def tryYield(): Unit = {
    if (Thread.currentThread() ne this) {
      throw new IllegalThreadStateException("tryYield must be called on the current thread")
    }
    state match
      case State.Running =>
        // Avoid unmounting on yield: if the VT currently owns a monitor, a
        // suspended yield can strand thin-lock ownership and starve contenders.
        Thread.nativeCompanion.yieldThread()
      case s if isRecursiveSuspendState(s) =>
        // Recursive yield while suspension is in progress must not suspend again.
        state = State.Pinned
        Thread.nativeCompanion.yieldThread()
        state = s
      case _ =>
        Thread.nativeCompanion.yieldThread()
  }

  private def afterDone(): Unit = {
    state = State.Terminated
    // Registry tracks only platform threads; VTs were never added
    termination match {
      case termination: CountDownLatch =>
        val c = termination.getCount()
        if (c != 1L) {
          throw new IllegalStateException(s"Unexpected termination latch count: $c")
        }
        termination.countDown()
      case null => ()
    }
  }

  private def getTermination(): CountDownLatch = {
    this.termination match {
      case null =>
        val term = new CountDownLatch(1)
        if (terminationAtomic.compareExchangeStrong(null: CountDownLatch, term)) term
        else this.termination
      case termination => termination
    }
  }

  private[lang] def joinNanos(nanos: scala.Long): scala.Boolean = {
    if (state == State.Terminated)
      return true
    if (Thread.interrupted())
      throw new InterruptedException()

    // ensure termination object exists, then re-check state
    val termination = getTermination()
    if (state == State.Terminated)
      return true

    // wait for virtual thread to terminate
    if (nanos != 0)
      termination.await(nanos, TimeUnit.NANOSECONDS)
    else {
      termination.await()
      true
    }
  }

  override def threadState(): Thread.State = state match {
    case State.New                      => Thread.State.NEW
    case State.Started | State.Runnable => Thread.State.RUNNABLE
    case State.Running                  =>
      carrierThreadAccessLock.synchronized {
        val carrier = this.carrierThread
        if (carrier != null) carrier.threadState()
        else Thread.State.RUNNABLE
      }
    case State.Yielding                  => Thread.State.RUNNABLE
    case State.Parking | State.TimedParking               => Thread.State.RUNNABLE
    case State.Parked | State.Pinned                      => Thread.State.WAITING
    case State.TimedParked | State.TimedPinned            => Thread.State.TIMED_WAITING
    case State.Unparked | State.Yielded => Thread.State.RUNNABLE
    case State.Terminated                                 => Thread.State.TERMINATED
  }
  override def alive(): scala.Boolean = state match {
    case State.New | State.Terminated => false
    case _                            => true
  }
  override def isTerminated(): scala.Boolean = state == State.Terminated

  override def toString(): String = {
    val sb = new StringBuilder("VirtualThread[#")
    sb.append(threadId())
    val name = getName()
    if (name.nonEmpty) {
      sb.append(',')
      sb.append(name)
    }
    sb.append("]/")
    var carrier = carrierThread
    if (carrier != null) {
      carrierThreadAccessLock.synchronized {
        carrier = carrierThread
        if (carrier != null) {
          val state = carrier.threadState().toString()
          sb.append(state.toLowerCase())
          sb.append('@')
          sb.append(carrier.getName())
        }
      }
    }
    if (carrier == null) {
      sb.append(threadState().toString().toLowerCase())
    }
    sb.toString()
  }
  override def hashCode(): Int = threadId().toInt
  override def equals(that: Any): scala.Boolean = that match {
    case that: AnyRef => that eq this
    case _            => false
  }
}

object VirtualThread {
  private object RunDispatchState {
    final val Idle = 0
    final val Queued = 1
    final val Running = 2
    final val RunningQueued = 3
  }

  type State = Int
  object State {
    final val New = 0
    final val Started = 1
    final val Runnable = 2 // runnable-unmounted
    final val Running = 3 // runnable-mounted

    // untimed and timed parking
    final val Parking = 4
    final val Parked = 5 // unmounted
    final val Pinned = 6 // mounted
    final val TimedParking = 7
    final val TimedParked = 8 // unmounted
    final val TimedPinned = 9 // mounted
    final val Unparked = 10 // unmounted but runnable

    // Thread.yield
    final val Yielding = 11 // Thread.yield
    final val Yielded = 12 // unmounted but runnable

    final val Terminated = 99 // final state
  }

  private val shutdownHookRegistered = new AtomicBoolean(false)
  lazy val DefaultScheduler = {
    val factory: ForkJoinWorkerThreadFactory = new ForkJoinWorkerThreadFactory {
      override def newThread(pool: ForkJoinPool): ForkJoinWorkerThread =
        new VirtualThreadCarrier(pool)
    }
    val handler: Thread.UncaughtExceptionHandler = (_, _) => ()
    val paralellism = Runtime.getRuntime().availableProcessors()
    val maxPoolSize = paralellism max 256
    val minRunnable = (paralellism / 2).max(1)
    new ForkJoinPool(
      paralellism,
      factory,
      handler,
      true,
      0,
      maxPoolSize,
      minRunnable,
      _ => true,
      30,
      TimeUnit.SECONDS
    )
  }

  lazy val DelayedTasksSchedulers: scala.Array[ScheduledThreadPoolExecutor] = {
    val paralellism = Runtime.getRuntime().availableProcessors()
    // Continuation resume has higher per-task overhead on Scala Native than on a typical hosted JVM.
    // Under sleep-heavy workloads (for example 10k sleeping VTs), a small number
    // of timer queues serializes wakeups and leaves timed parks pending for too
    // long. Use a wider fan-out to keep delayed unparks flowing.
    val queueCount = Integer.highestOneBit(paralellism * 4).max(1)
    Array.fill(queueCount) {
      val executor: ScheduledThreadPoolExecutor =
        new ScheduledThreadPoolExecutor(
          1,
          task => {
            val t = new Thread(task)
            t.setName("VirtualThread-unparker")
            t.setDaemon(true)
            t
          }
        )
      executor.setRemoveOnCancelPolicy(true)
      executor
    }
  }
  private def schedule(task: Runnable, delay: scala.Long, unit: TimeUnit): ScheduledFuture[?] = {
    val tid = Thread.currentThread().threadId()
    val idx = (tid.toInt & DelayedTasksSchedulers.length - 1)
    DelayedTasksSchedulers(idx).schedule(task, delay, unit)
  }
}

class VirtualThreadCarrier(scheduler: ForkJoinPool)
    extends ForkJoinWorkerThread(Thread.VirtualThreadCarriersGroup, scheduler, true) {

  import VirtualThreadCarrier.*

  var mountedThread: VirtualThread = _

  private var compensation: CompensationState = CompensationState.NotCompensating
  private var compensationValue: scala.Long = 0L

  def startBlocking(): scala.Boolean = {
    compensation match
      case CompensationState.NotCompensating =>
        try {
          compensation = CompensationState.InProgress
          // TODO: spwawn additional FJP workers to compensate
          // compensationValue = getPool().startCompensating()
          compensation = CompensationState.Compensating
          true
        } catch {
          case ex: Throwable =>
            compensation = CompensationState.NotCompensating
            throw ex
        }
      case CompensationState.InProgress =>
        throw new IllegalStateException("Recursive blocking in VirtualThreadCarrier")
      case CompensationState.Compensating => false
  }
  def stopBlocking(): Unit = compensation match {
    case CompensationState.Compensating =>
      // TODO: finish compensating
      // getPool().finishCompensating(compensationValue)
      compensation = CompensationState.NotCompensating
      compensationValue = 0
    case _ => ()
  }
}

object VirtualThreadCarrier {
  opaque type CompensationState = Int
  object CompensationState {
    final val NotCompensating: CompensationState = 0
    final val InProgress: CompensationState = 1
    final val Compensating: CompensationState = 2
  }
}
