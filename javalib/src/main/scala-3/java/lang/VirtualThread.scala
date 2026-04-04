// scalafmt: { maxColumn = 120}

package java.lang

import java.util.Objects
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.{CountDownLatch, TimeUnit}

import scala.annotation.tailrec
import scala.noinline

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic.{AtomicBool, AtomicInt, AtomicRef}
import scala.scalanative.runtime.{
  Continuations, Intrinsics, NativeThread, VirtualThread => RuntimeVirtualThread, VirtualThreadScheduler, fromRawPtr
}

private[java] final class VirtualThread(
    name: String,
    characteristics: Int,
    task: Runnable
) extends Thread(name, characteristics)
    with RuntimeVirtualThread {
  import java.lang.VirtualThread.State
  Objects.requireNonNull(task)

  val scheduler: VirtualThreadScheduler = Thread.currentThread() match {
    case vt: VirtualThread => vt.scheduler
    case _                 => DefaultVirtualThreadScheduler
  }

  protected def vtTask: Runnable = task

  override final def signalBlockPermit(): Unit = setBlockPermit(true)

  override final def carrierForUnpark: Thread = {
    val c = carrierThread
    if (c != null) c else this
  }

  private type Boundary = Continuations.BoundaryLabel[Unit]
  @volatile private var boundary: Boundary | Null = _
  @volatile private var resumeExecution: () => Unit | Null = _
  private val resumeLock = new {}
  private var nextResumeGeneration: Long = 0L
  @volatile private var activeResumeGeneration: Long = 0L
  val carrierThreadAccessLock = new {}

  @volatile var carrierThread: Thread | Null = _
  @volatile var termination: CountDownLatch | Null = _
  @volatile var state: VirtualThread.State = VirtualThread.State.New
  @volatile var runDispatchState: VirtualThread.DispatchState = VirtualThread.DispatchState.Idle

  /** When true, the next park() does not block. Set by unpark(), cleared in park(). */
  @volatile var parkPermit: scala.Boolean = false

  /** Set when unblocking from monitor wait or enter; consumed when run completes with Unblocked. */
  @volatile private var blockPermit: scala.Boolean = false

  // True while this VT is submitting a task to the scheduler.
  // Prevents yielding (unmounting) so the carrier stays stable during submit,
  // analogous to pinning around scheduler submission.
  @volatile private var pinnedForSubmit: scala.Boolean = false

  // Timed park/wait callbacks must be generation-scoped so a late timeout from
  // an earlier operation cannot wake a later park/wait on the same VT.
  private val timeoutLock = new {}
  private var timeoutGeneration: Long = 0L
  private var timeoutTaskResumeGeneration: Long = 0L
  @volatile private var timeoutTask: VirtualThreadScheduler.Cancellable | Null = _

  /** Set while in Object.wait() (blockForMonitorWait) so interrupt() can wake this VT. */
  @volatile private var currentWaitResume: () => Unit = _
  @volatile private var currentWaitResumeGeneration: Long = 0L

  @alwaysinline
  private def stateAtomic =
    new AtomicInt(fromRawPtr(Intrinsics.classFieldRawPtr(this, "state")))

  @alwaysinline
  private def runDispatchStateAtomic =
    new AtomicInt(fromRawPtr(Intrinsics.classFieldRawPtr(this, "runDispatchState")))

  @alwaysinline
  private def terminationAtomic =
    new AtomicRef[CountDownLatch](fromRawPtr(Intrinsics.classFieldRawPtr(this, "termination")))

  @alwaysinline
  private def parkPermitAtomic =
    new AtomicBool(fromRawPtr(Intrinsics.classFieldRawPtr(this, "parkPermit")))

  @alwaysinline
  private def blockPermitAtomic =
    new AtomicBool(fromRawPtr(Intrinsics.classFieldRawPtr(this, "blockPermit")))

  @inline private def compareAndSetState(expected: VirtualThread.State, value: VirtualThread.State): Boolean =
    stateAtomic.compareExchangeStrong(expected, value)

  @inline private def trySwitchDispatchState(
      expected: VirtualThread.DispatchState,
      value: VirtualThread.DispatchState
  ): Boolean =
    runDispatchStateAtomic.compareExchangeStrong(expected, value)

  @inline private def isRecursiveSuspendState(s: VirtualThread.State): Boolean =
    s == State.Parking || s == State.Parked ||
      s == State.TimedParking || s == State.TimedParked ||
      s == State.Yielding || s == State.Yielded ||
      s == State.Blocking || s == State.Blocked || s == State.TimedBlocked

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

  private def isActiveResume(resume: () => Unit, generation: Long): Boolean = {
    val current = resumeExecution
    generation != 0L &&
      activeResumeGeneration == generation &&
      current != null &&
      (current eq resume)
  }

  private inline def currentBoundaryOrThrow(op: String): Boundary =
    val current = boundary
    if current == null then throw new IllegalStateException(s"Missing continuation boundary during $op")
    current.nn

  @inline private def setParkPermit(value: scala.Boolean): Unit = {
    if (parkPermit != value) parkPermit = value
  }
  @inline private def getAndSetParkPermit(value: scala.Boolean): scala.Boolean =
    parkPermitAtomic.exchange(value)

  @inline private def setBlockPermit(value: scala.Boolean): Unit = {
    if (blockPermit != value) blockPermit = value
  }
  @inline private def getAndSetBlockPermit(value: scala.Boolean): scala.Boolean =
    blockPermitAtomic.exchange(value)

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
        // Wake if blocked in Object.wait() so wait() can see interrupt and throw
        val resume = currentWaitResume
        val generation = currentWaitResumeGeneration
        if (resume != null) {
          currentWaitResume = null
          currentWaitResumeGeneration = 0L
          scheduleWithResume(resume, generation)
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
        if (!task.isDone()) task.cancel()

  private def scheduleTimeout(delay: scala.Long, unit: TimeUnit, resumeGeneration: Long)(onTimeout: => Unit): Unit = {
    val timeoutToken = timeoutLock.synchronized {
      timeoutGeneration += 1L
      timeoutTaskResumeGeneration = resumeGeneration
      timeoutTask = null
      timeoutGeneration
    }

    val task = VirtualThread.scheduleDelayed(
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
      else if (!task.isDone()) task.cancel()
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

  /** Blocks for Object.wait() without consuming the LockSupport permit. For indefinite wait (nanos == 0) we do not park
   *  the carrier so many VTs can wait. For timed wait we schedule a delayed unblock, so the carrier is not pinned. Uses
   *  Blocked/TimedBlocked states so unblock() is the single entry point for notify.
   */
  override def blockForMonitorWait(
      nanos: scala.Long,
      setResume: RuntimeVirtualThread.SetResume
  ): Unit = {
    state = if (nanos == 0) State.Blocked else State.TimedBlocked
    Continuations.suspend[Unit] { resume =>
      val generation = publishResume(resume)
      setResume(resume, generation)
      currentWaitResume = resume
      currentWaitResumeGeneration = generation
      if (nanos == 0) {
        // Do not park the carrier; notify will call unblock(resume).
      } else {
        scheduleTimeout(nanos, TimeUnit.NANOSECONDS, generation) {
          // Timeout wakeup follows the same path as notify wakeup.
          scheduleWithResume(resume, generation)
        }
      }
    }(using currentBoundaryOrThrow("blockForMonitorWait"))
    currentWaitResume = null
    currentWaitResumeGeneration = 0L
    state = State.Running
  }

  /** Set blockPermit, CAS Blocked/TimedBlocked/Blocking→Unblocked, submit. Single entry point for notify (Object.wait)
   *  and for monitor exit (contended enter). Handles Blocking so "unblocked while blocking" (exit before we CAS to
   *  Blocked) is still submitted.
   */
  private[java] def unblock(resume: () => Unit, generation: Long): Unit = {
    if (!isActiveResume(resume, generation)) {
      return
    }
    var s = state
    var done = false
    while (!done) {
      if (s == State.Blocking || s == State.Blocked || s == State.TimedBlocked) {
        if (compareAndSetState(s, State.Unblocked)) {
          if (s == State.TimedBlocked) cancelTimeoutTask(generation)
          getAndSetBlockPermit(true)
          requestRun()
          done = true
        } else s = state
      } else if (s == State.Unblocked) done = true
      else done = true
    }
  }

  /** Called by the monitor when notifying a VT (wait) or when a VT acquires after block (enter). Routes through
   *  unblock() so the run loop consumes block permit, not park permit.
   */
  override def scheduleWithResume(
      resume: () => Unit,
      generation: scala.Long
  ): Unit =
    unblock(resume, generation)

  /** Block for contended monitor enter: suspend so the carrier can run other VTs; monitor exit will call
   *  scheduleWithResume to wake this thread.
   */
  override def blockForMonitorEnter(
      setResume: RuntimeVirtualThread.SetResume
  ): Unit = {
    // If a monitor exit signalled blockPermit (because resumeForEnter was null
    // during the transient window), consume the permit and retry tryLock instead
    // of suspending the continuation.
    if (getAndSetBlockPermit(false)) return
    state = State.Blocking
    Continuations.suspend[Unit] { resume =>
      val generation = publishResume(resume)
      setResume(resume, generation)
      compareAndSetState(State.Blocking, State.Blocked)
    }(using currentBoundaryOrThrow("blockForMonitorEnter"))
    state = State.Running
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
    if (lazily) scheduler.lazyExecute(executeContinuation)
    else scheduler.execute(executeContinuation)

  private def requestRun(lazily: scala.Boolean = false): Unit = {
    import VirtualThread.DispatchState
    var done = false
    while (!done) {
      runDispatchState match {
        case DispatchState.Idle =>
          if (trySwitchDispatchState(DispatchState.Idle, DispatchState.Queued)) {
            // Pin this VT (prevent unmounting) while submitting, so the carrier
            // stays stable during the submit.
            val needsPin = Thread.currentThread() eq this
            if (needsPin) pinnedForSubmit = true
            val useLazy =
              !needsPin && lazily &&
                scheduler.isCarrierThread(Thread.currentThread()) &&
                scheduler.isCarrierIdle(Thread.currentThread())
            try submitScheduledRun(useLazy)
            catch {
              case ex: Throwable =>
                trySwitchDispatchState(DispatchState.Queued, DispatchState.Idle)
                throw ex
            } finally {
              if (needsPin) pinnedForSubmit = false
            }
            done = true
          }
        case DispatchState.Running =>
          if (trySwitchDispatchState(DispatchState.Running, DispatchState.RunningQueued)) {
            done = true
          }
        case DispatchState.Queued | DispatchState.RunningQueued =>
          done = true
      }
    }
  }

  private def mount(): Unit = {
    val platformThread = Thread.currentPlatformThread
    if (platformThread == null || platformThread.isVirtual()) {
      throw new IllegalStateException(s"Invalid carrier for virtual thread mount: $platformThread")
    }
    val carrier = platformThread
    this.carrierThread = carrier

    if (interruptedState) {
      carrier.setInterrupt()
    } else if (carrier.isInterrupted()) {
      if (!interruptedState) {
        carrier.clearInterrupt()
      }
    }
    NativeThread.setCurrentThread(this)
  }

  private def unmount(): Unit = {
    if (this.state == State.Running) {
      throw new IllegalStateException("Cannot unmount virtual thread while Running")
    }
    val carrier = this.carrierThread
    if (carrier == null) {
      throw new IllegalStateException("carrierThread is null during unmount")
    }
    NativeThread.setCurrentThread(carrier)

    // break connection to carrier thread, synchronized with interrupt
    interruptLock.synchronized {
      this.carrierThread = null
    }
    carrier.clearInterrupt()
  }

  private def tryAcquireRunLoopToken(): Boolean = {
    import VirtualThread.DispatchState
    var acquired = false
    var done = false
    while (!done) {
      runDispatchState match {
        case DispatchState.Queued =>
          if (trySwitchDispatchState(DispatchState.Queued, DispatchState.Running)) {
            acquired = true
            done = true
          }
        case DispatchState.Running | DispatchState.RunningQueued | DispatchState.Idle =>
          done = true
      }
    }
    acquired
  }

  private def finishRunLoopIteration(): Boolean = {
    import VirtualThread.DispatchState
    var continue = false
    var done = false
    while (!done) {
      runDispatchState match {
        case DispatchState.RunningQueued =>
          if (trySwitchDispatchState(DispatchState.RunningQueued, DispatchState.Running)) {
            continue = true
            done = true
          }
        case DispatchState.Running =>
          if (trySwitchDispatchState(DispatchState.Running, DispatchState.Idle)) {
            continue = false
            done = true
          }
        case DispatchState.Queued =>
          if (trySwitchDispatchState(DispatchState.Queued, DispatchState.Running)) {
            continue = true
            done = true
          }
        case DispatchState.Idle =>
          continue = false
          done = true
      }
    }
    continue
  }

  private def recoverRunLoopOnUnexpectedExit(): Unit = {
    import VirtualThread.DispatchState
    var done = false
    while (!done) {
      runDispatchState match {
        case DispatchState.RunningQueued =>
          if (trySwitchDispatchState(DispatchState.RunningQueued, DispatchState.Queued)) {
            try submitScheduledRun(lazily = false)
            catch {
              case _: Throwable =>
                trySwitchDispatchState(DispatchState.Queued, DispatchState.Idle)
            }
            done = true
          }
        case DispatchState.Running =>
          if (trySwitchDispatchState(DispatchState.Running, DispatchState.Idle)) {
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
      if (Thread.currentThread().isVirtual() && (Thread.currentThread() ne platformThread)) {
        NativeThread.setCurrentThread(platformThread)
      }

      if (!tryAcquireRunLoopToken()) {
        // Re-queue once if VT is runnable so we don't drop the resumption.
        if (state == State.Unparked || state == State.Unblocked
            || (state == State.Yielded && resumeExecution != null)) {
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
            case State.Started | State.Unparked | State.Yielded | State.Unblocked =>
              runCurrent = compareAndSetState(initialState, State.Running)
              if (runCurrent) {
                if (initialState == State.Unparked) {
                  VirtualThread.this.cancelTimeoutTask(VirtualThread.this.activeResumeGeneration)
                  VirtualThread.this.setParkPermit(false) // consume park event
                } else if (initialState == State.Unblocked)
                  VirtualThread.this.setBlockPermit(false) // consume block event
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
                  vtTask.run()
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
              // Block path: Blocked/TimedBlocked (wait/enter) + blockPermit.
              if ((s == State.Blocked || s == State.TimedBlocked)
                  && getAndSetBlockPermit(false)) {
                if (!compareAndSetState(s, State.Unblocked))
                  setBlockPermit(true)
                else {
                  requestRun(lazily = true)
                  didAfterYieldSubmit = true
                }
              }
              resubmitYield = (state == State.Yielded || state == State.Unparked || state == State.Unblocked) &&
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
          if ((state == State.Yielded || state == State.Unparked || state == State.Unblocked)
              && resumeExecution != null) {
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
    case State.Yielding | State.Blocking                  => Thread.State.RUNNABLE
    case State.Parking | State.TimedParking               => Thread.State.RUNNABLE
    case State.Parked | State.Pinned                      => Thread.State.WAITING
    case State.TimedParked | State.TimedPinned            => Thread.State.TIMED_WAITING
    case State.Blocked                                    => Thread.State.WAITING
    case State.TimedBlocked                               => Thread.State.TIMED_WAITING
    case State.Unparked | State.Yielded | State.Unblocked => Thread.State.RUNNABLE
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
  type DispatchState = Int
  private object DispatchState {
    final val Idle = 0
    final val Queued = 1
    final val Running = 2
    final val RunningQueued = 3
  }

  private[java] def scheduleDelayed(
      task: Runnable,
      delay: scala.Long,
      unit: TimeUnit
  ): VirtualThreadScheduler.Cancellable =
    DefaultVirtualThreadScheduler.schedule(task, delay, unit)

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

    // Monitor block (Object.wait / contended monitor enter)
    final val Blocking = 13 // about to suspend for wait/enter
    final val Blocked = 14 // in Object.wait() or blocked on monitor enter
    final val TimedBlocked = 15
    final val Unblocked = 16 // unblocked, runnable (notify / monitor exit)

    final val Terminated = 99 // final state
  }

}
