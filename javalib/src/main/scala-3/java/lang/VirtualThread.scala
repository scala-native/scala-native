// scalafmt: { maxColumn = 120}

package java.lang

import java.util.Objects
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.{CountDownLatch, TimeUnit}

import scala.noinline

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic.{AtomicBool, AtomicInt, AtomicRef}
import scala.scalanative.runtime
import scala.scalanative.runtime.javalib.Proxy
import scala.scalanative.runtime.{Continuations, Intrinsics, NativeThread, VirtualThreadScheduler, fromRawPtr}

/** Loom-style virtual thread: a `Runnable` fiber that mounts on a platform carrier, suspends via
 *  `Continuations.suspend`, and resumes through `VirtualThreadScheduler`.
 *
 *  Two wakeup channels are kept distinct from `LockSupport` parking:
 *    - `parkPermit` / `Unparked` — async unpark and timed `park` (`LockSupport` semantics).
 *    - `blockPermit` / `Unblocked` — `Object.wait` / notify and contended monitor enter (`ObjectMonitor`).
 *
 *  Field updates that participate in races use libc atomics (`AtomicInt` / `AtomicBool`) on the corresponding
 *  `volatile` fields. See companion `State` for the `state` bit assignments.
 */
private[java] final class VirtualThread(
    name: String,
    characteristics: Int,
    private var task: Runnable | Null
) extends Thread(name, characteristics)
    with runtime.VirtualThread {
  import java.lang.VirtualThread.{Boundary, State}

  Objects.requireNonNull(task)

  // ---------------------------------------------------------------------------
  // Construction & scheduler inheritance
  // ---------------------------------------------------------------------------

  /** Scheduler shared with the parent VT when this VT is created from another VT; otherwise the default from `Proxy` /
   *  `DefaultVirtualThreadScheduler`.
   */
  val scheduler: VirtualThreadScheduler = Thread.currentThread() match {
    case vt: VirtualThread => vt.scheduler
    case _                 =>
      Proxy.defaultVirtualThreadScheduler match {
        case null      => DefaultVirtualThreadScheduler
        case scheduler => scheduler
      }
  }

  // ---------------------------------------------------------------------------
  // Continuation boundary & resume publication
  // Used by Continuations.suspend, park paths, monitor wait/enter, and the run loop.
  // ---------------------------------------------------------------------------

  @volatile private[lang] var boundary: Boundary | Null = compiletime.uninitialized

  private inline def currentBoundaryOrThrow(op: String): Boundary =
    val current = boundary
    if current == null then throw new IllegalStateException(s"Missing continuation boundary during $op")
    current.asInstanceOf[Boundary] // No .nn on Scala 3.1

  @volatile private[java] var resumeExecution: Continuation | Null = compiletime.uninitialized
  private val resumeLock = new {}
  private var nextResumeGeneration: Long = 0L
  @volatile private var activeResumeGeneration: Long = 0L

  private[java] def publishResume(resume: () => Unit): Long =
    resumeLock.synchronized {
      nextResumeGeneration += 1L
      val generation = nextResumeGeneration
      resumeExecution = resume
      activeResumeGeneration = generation
      generation
    }

  private[java] def consumeResume(): () => Unit | Null =
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

  // ---------------------------------------------------------------------------
  // Carrier attachment, join latch, and lifecycle state machine
  // ---------------------------------------------------------------------------

  private val carrierThreadAccessLock = new {}
  @volatile var carrierThread: Thread | Null = compiletime.uninitialized
  @volatile var termination: CountDownLatch | Null = compiletime.uninitialized
  @volatile var state: VirtualThread.State = VirtualThread.State.New
  @volatile var runDispatchState: VirtualThread.DispatchState = VirtualThread.DispatchState.Idle

  // ---------------------------------------------------------------------------
  // Atomic field access & state predicates
  // ---------------------------------------------------------------------------

  @alwaysinline
  private def stateAtomic =
    new AtomicInt(fromRawPtr(Intrinsics.classFieldRawPtr(this, "state")))

  @alwaysinline
  private def runDispatchStateAtomic =
    new AtomicInt(fromRawPtr(Intrinsics.classFieldRawPtr(this, "runDispatchState")))

  @alwaysinline
  private def terminationAtomic =
    new AtomicRef[CountDownLatch](fromRawPtr(Intrinsics.classFieldRawPtr(this, "termination")))

  @inline private[java] def compareAndSetState(expected: VirtualThread.State, value: VirtualThread.State): Boolean =
    stateAtomic.compareExchangeStrong(expected, value)

  @inline private[java] def compareAndSetDispatchState(
      expected: VirtualThread.DispatchState,
      value: VirtualThread.DispatchState
  ): Boolean =
    runDispatchStateAtomic.compareExchangeStrong(expected, value)

  @inline private def isRecursiveSuspendState(s: VirtualThread.State): Boolean =
    s == State.Parking || s == State.Parked
      || s == State.TimedParking || s == State.TimedParked
      || s == State.Yielding || s == State.Yielded
      || s == State.Blocking || s == State.Blocked
      || s == State.TimedBlocked

  @noinline private[java] def reachabilityFence(target: Any): Unit = ()

  // ---------------------------------------------------------------------------
  // LockSupport-style parking (park permit)
  // Cleared in park(); set by unpark(). Run loop may reconcile permit vs Parked state.
  // ---------------------------------------------------------------------------

  /** When true, the next park() does not block. Set by unpark(), cleared in park(). */
  @volatile private[lang] var parkPermit: scala.Boolean = false

  @alwaysinline
  private def parkPermitAtomic =
    new AtomicBool(fromRawPtr(Intrinsics.classFieldRawPtr(this, "parkPermit")))

  @alwaysinline private def getAndSetParkPermit(value: scala.Boolean): scala.Boolean =
    parkPermitAtomic.exchange(value)

  @alwaysinline private def setParkPermit(value: scala.Boolean): Unit =
    parkPermit = value

  // ---------------------------------------------------------------------------
  // Timed park / wait: generation-scoped timeout tasks
  // So a late timeout from an earlier operation cannot wake a later park/wait on the same VT.
  // ---------------------------------------------------------------------------

  private val timeoutLock = new {}
  private var timeoutGeneration: Long = 0L
  private var timeoutTaskResumeGeneration: Long = 0L
  @volatile private[java] var timeoutTask: VirtualThreadScheduler.Cancellable | Null = compiletime.uninitialized
  private var pendingTimeout: PendingTimeout | Null = compiletime.uninitialized

  private final class PendingTimeout(
      val delay: scala.Long,
      val unit: TimeUnit,
      val resumeGeneration: Long,
      val action: () => Unit
  )

  private def cancelTimeoutTask(expectedResumeGeneration: Long): Unit =
    val task = timeoutLock.synchronized {
      pendingTimeout match {
        case pending: PendingTimeout if pending.resumeGeneration == expectedResumeGeneration =>
          pendingTimeout = null
        case _ => ()
      }
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

  private def deferTimeout(delay: scala.Long, unit: TimeUnit, resumeGeneration: Long)(onTimeout: => Unit): Unit =
    timeoutLock.synchronized {
      pendingTimeout = new PendingTimeout(delay, unit, resumeGeneration, () => onTimeout)
    }

  private def scheduleDeferredTimeout(): Unit = {
    val request = timeoutLock.synchronized {
      val pending = pendingTimeout
      pendingTimeout = null
      pending match {
        case pending: PendingTimeout if isActiveResumeGeneration(pending.resumeGeneration) =>
          pending
        case _ => null
      }
    }
    if (request != null)
      scheduleTimeout(request.delay, request.unit, request.resumeGeneration) {
        request.action()
      }
  }

  private def scheduleTimeout(delay: scala.Long, unit: TimeUnit, resumeGeneration: Long)(onTimeout: => Unit): Unit = {
    val timeoutToken = timeoutLock.synchronized {
      timeoutGeneration += 1L
      timeoutTaskResumeGeneration = resumeGeneration
      timeoutTask = null
      timeoutGeneration
    }

    val task = scheduler.schedule(
      () => {
        val shouldRun = timeoutLock.synchronized {
          if (timeoutGeneration == timeoutToken && isActiveResumeGeneration(resumeGeneration)) {
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

  // ---------------------------------------------------------------------------
  // ObjectMonitor integration (Object.wait / contended monitor enter)
  // blockPermit: set when unblocking from wait or enter; consumed when run completes with Unblocked.
  // currentWaitResume*: only blockForMonitorWait + interrupt() (wake from wait).
  // ---------------------------------------------------------------------------

  @volatile private[lang] var blockPermit: scala.Boolean = false

  @alwaysinline
  private def blockPermitAtomic =
    new AtomicBool(fromRawPtr(Intrinsics.classFieldRawPtr(this, "blockPermit")))

  @alwaysinline private def getAndSetBlockPermit(value: scala.Boolean): scala.Boolean =
    blockPermitAtomic.exchange(value)

  @alwaysinline private def setBlockPermit(value: scala.Boolean): Unit =
    blockPermitAtomic.store(value)

  /** Set while in Object.wait() (blockForMonitorWait) so interrupt() can wake this VT. */
  @volatile private var currentWaitResume: () => Unit = compiletime.uninitialized
  @volatile private var currentWaitResumeGeneration: Long = 0L

  /** Blocks for Object.wait() without consuming the LockSupport permit. For indefinite wait (nanos == 0) we do not park
   *  the carrier so many VTs can wait. For timed wait we schedule a delayed unblock, so the carrier is not pinned. Uses
   *  Blocked/TimedBlocked states so unblock() is the single entry point for notify.
   */
  override protected def doBlockForMonitorWait(
      nanos: scala.Long,
      setResume: SetResumeContinuation
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
        deferTimeout(nanos, TimeUnit.NANOSECONDS, generation) {
          // Timeout wakeup follows the same path as notify wakeup.
          unblock(resume, generation)
        }
      }
    }(using currentBoundaryOrThrow("blockForMonitorWait"))
    currentWaitResume = null
    currentWaitResumeGeneration = 0L
    state = State.Running
  }

  /** Block for contended monitor enter: suspend so the carrier can run other VTs; monitor exit will call
   *  scheduleWithResume to wake this thread.
   */
  override protected def doBlockForMonitorEnter(
      setResume: SetResumeContinuation
  ): Unit = {
    // If a monitor exit signalled blockPermit (because resumeForEnter was null
    // during the transient window), consume the permit and retry tryLock instead
    // of suspending the continuation.
    if (getAndSetBlockPermit(false)) return
    state = State.MonitorBlocking
    Continuations.suspend[Unit] { resume =>
      val generation = publishResume(resume)
      setResume(resume, generation)
      compareAndSetState(State.MonitorBlocking, State.MonitorBlocked)
    }(using currentBoundaryOrThrow("blockForMonitorEnter"))
    state = State.Running
  }

  /** A notified Object.wait() VT is no longer waiting for notification; it is queued to re-enter the monitor. */
  override protected def doMarkBlockedOnMonitorEnter(): Unit = {
    var s = state
    var done = false
    while (!done) {
      if (s == State.Blocked || s == State.TimedBlocked) {
        if (s == State.TimedBlocked)
          cancelTimeoutTask(currentWaitResumeGeneration)
        if (compareAndSetState(s, State.MonitorBlocked)) done = true
        else s = state
      } else done = true
    }
  }

  /** Transitions out of monitor wait/enter suspension: CAS to `Unblocked`, arms `blockPermit`, and queues the run loop.
   *
   *  Single entry for `Object.notify` / timed wait expiry and for waking a contender after monitor exit. Handles
   *  `Blocking` so an exit that happens before the VT reaches `Blocked` still schedules a run.
   *
   *  @param resume
   *    continuation published by `publishResume` for this wait/enter
   *  @param generation
   *    generation paired with `resume` (stale wakeups are ignored)
   */
  private[java] def unblock(resume: () => Unit, generation: Long): Unit = {
    if (!isActiveResume(resume, generation)) {
      return
    }
    var s = state
    var done = false
    while (!done) {
      if (s == State.Blocking || s == State.Blocked ||
          s == State.TimedBlocked || s == State.MonitorBlocked ||
          s == State.MonitorBlocking) {
        if (compareAndSetState(s, State.Unblocked)) {
          if (s == State.TimedBlocked) cancelTimeoutTask(generation)
          getAndSetBlockPermit(true)
          requestRun()
          done = true
        } else s = state
      } else if (s == State.Unblocked) {
        requestRun()
        done = true
      } else done = true
    }
  }

  /** Called by the monitor when notifying a VT (wait) or when a VT acquires after block (enter). Routes through
   *  unblock() so the run loop consumes block permit, not park permit.
   */
  override protected def doScheduleWithResume(
      resume: () => Unit,
      generation: scala.Long
  ): Unit =
    unblock(resume, generation)

  // ---------------------------------------------------------------------------
  // Thread API: start, interrupt, basic hooks
  // ---------------------------------------------------------------------------

  /** JVM contract: body is not used; the fiber runs through `VirtualThreadContinuation` after `startInternal`. */
  override def run(): Unit = ()

  /** Atomically moves `New`→`Started` and submits the first continuation; on failure rolls `afterDone` if scheduled. */
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

  /** Clears this VT's interrupted flag and the carrier's platform interrupt when invoked on `this`. */
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

  /** If invoked from another thread: sets interrupt flag, interrupts the carrier, wakes `Object.wait` if applicable,
   *  and `unpark`s. If invoked on self: sets flag, clears carrier interrupt wiring for parking, and posts a park
   *  permit.
   */
  override def interrupt(): Unit = {
    if (Thread.currentThread() ne this) {
      interruptLock.synchronized {
        interruptedState = true
        // Interruptible I/O could forward interrupt to the current blocker (not implemented yet).
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
          unblock(resume, generation)
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

  // ---------------------------------------------------------------------------
  // Sleep & LockSupport park / unpark
  // pinnedForSubmit: true while submitting to the scheduler from this VT; park pins the carrier instead of yielding.
  // ---------------------------------------------------------------------------

  // True while this VT is submitting a task to the scheduler.
  // Prevents yielding (unmounting) so the carrier stays stable during submit,
  // analogous to pinning around scheduler submission.
  @volatile private var pinnedForSubmit: scala.Boolean = false

  /** Timed `LockSupport.parkNanos` for this VT: may yield the continuation or pin the native carrier when already
   *  inside scheduler submission or nested suspension.
   */
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
          deferTimeout(nanos, TimeUnit.NANOSECONDS, generation) {
            unpark(lazily = false) // timed park wakeup: use normal submit
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

  /** Untimed `LockSupport.park` for this VT; same pinning / recursion rules as [[parkNanos]]. */
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
    // Publish the permit before inspecting state. If the thread is already
    // parked, always request a run; dispatch-state coalescing absorbs duplicate
    // submissions, while skipping the request can strand a parked continuation
    // when a prior race left the permit set.
    getAndSetParkPermit(true)
    if (Thread.currentThread() eq this) return

    var s = state
    var done = false
    while (!done) {
      s match {
        case State.Parked | State.TimedParked =>
          if (compareAndSetState(s, State.Unparked)) {
            requestRun(lazily)
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

  /** Wakes a parked VT: sets the park permit and either schedules the continuation or unparks the carrier when pinned.
   */
  private[java] def unpark(): Unit = unpark(false)

  /** `Thread.sleep` nanosecond path: interruptible loop over [[parkNanos]], or `tryYield` for `nanos == 0`. */
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

  // ---------------------------------------------------------------------------
  // Scheduler: queue continuation onto carrier pool
  // ---------------------------------------------------------------------------

  /** Submits [[executeContinuation]] to `scheduler`, optionally using `lazyExecute` for carrier-local batching. */
  private def submitScheduledRun(lazily: scala.Boolean = false): Unit = {
    val taskContinuation = new VirtualThread.RunLoop(this)
    if (lazily) scheduler.lazyExecute(taskContinuation)
    else scheduler.execute(taskContinuation)
  }

  /** Ensures the run loop is queued: coordinates `runDispatchState`, pins during self-submit, and picks lazy vs eager
   *  `execute` when permitted.
   */
  private def requestRun(lazily: scala.Boolean = false): Unit = {
    import VirtualThread.DispatchState
    var done = false
    while (!done) {
      runDispatchState match {
        case DispatchState.Idle =>
          if (compareAndSetDispatchState(DispatchState.Idle, DispatchState.Queued)) {
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
                compareAndSetDispatchState(DispatchState.Queued, DispatchState.Idle)
                throw ex
            } finally {
              if (needsPin) pinnedForSubmit = false
            }
            done = true
          }
        case DispatchState.Running =>
          if (compareAndSetDispatchState(DispatchState.Running, DispatchState.RunningQueued)) {
            done = true
          }
        case DispatchState.Queued | DispatchState.RunningQueued =>
          done = true
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Carrier mount / unmount (native TLS)
  // ---------------------------------------------------------------------------

  /** Binds this VT as `NativeThread.currentThread`, copies interrupt visibility to the carrier, and records
   *  `carrierThread`.
   */
  private def mount(): Unit = {
    val platformThread = Thread.currentPlatformThread
    if (platformThread == null || platformThread.isVirtual()) {
      throw new IllegalStateException(s"Invalid carrier for virtual thread mount: $platformThread")
    }
    val carrier = platformThread
    interruptLock.synchronized {
      this.carrierThread = carrier
      if (interruptedState) {
        carrier.setInterrupt()
      } else if (carrier.isInterrupted()) {
        if (!interruptedState) {
          carrier.clearInterrupt()
        }
      }
    }
    NativeThread.setCurrentThread(this)
  }

  /** Restores the carrier as `NativeThread.currentThread`, clears `carrierThread` under `interruptLock`, and clears the
   *  carrier interrupt bit we used for proxying this VT’s interrupt.
   */
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

  // ---------------------------------------------------------------------------
  // Run loop: dispatch state machine + continuation runner
  // ---------------------------------------------------------------------------

  /** Claims the single worker token that allows this continuation instance to run (`Queued`→`Running`). */
  private def tryAcquireRunLoopToken(): Boolean = {
    import VirtualThread.DispatchState
    var acquired = false
    var done = false
    while (!done) {
      runDispatchState match {
        case DispatchState.Queued =>
          if (compareAndSetDispatchState(DispatchState.Queued, DispatchState.Running)) {
            acquired = true
            done = true
          }
        case DispatchState.Running | DispatchState.RunningQueued | DispatchState.Idle =>
          done = true
      }
    }
    acquired
  }

  /** Run-loop bottom half: either idles the dispatcher or chains another `Running` iteration if work was coalesced. */
  private def finishRunLoopIteration(): Boolean = {
    import VirtualThread.DispatchState
    var continue = false
    var done = false
    while (!done) {
      runDispatchState match {
        case DispatchState.RunningQueued =>
          if (compareAndSetDispatchState(DispatchState.RunningQueued, DispatchState.Running)) {
            continue = true
            done = true
          }
        case DispatchState.Running =>
          if (compareAndSetDispatchState(DispatchState.Running, DispatchState.Idle)) {
            continue = false
            done = true
          }
        case DispatchState.Queued =>
          if (compareAndSetDispatchState(DispatchState.Queued, DispatchState.Running)) {
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

  /** If `VirtualThreadContinuation.run` aborts without finishing the inner `try`, re-queues or resets dispatch state so
   *  the VT does not strand `RunningQueued` / `Running`.
   */
  private def recoverRunLoopOnUnexpectedExit(): Unit = {
    import VirtualThread.DispatchState
    var done = false
    while (!done) {
      runDispatchState match {
        case DispatchState.RunningQueued =>
          if (compareAndSetDispatchState(DispatchState.RunningQueued, DispatchState.Queued)) {
            try submitScheduledRun(lazily = false)
            catch {
              case _: Throwable =>
                compareAndSetDispatchState(DispatchState.Queued, DispatchState.Idle)
            }
            done = true
          }
        case DispatchState.Running =>
          if (compareAndSetDispatchState(DispatchState.Running, DispatchState.Idle)) {
            done = true
          }
        case _ =>
          done = true
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Thread.yield cooperative scheduling
  // ---------------------------------------------------------------------------

  /** Prefer native `yieldThread` without unmounting when `Running` (avoids holding monitors across a full suspend). */
  private[lang] def tryYield(): Unit = {
    if (Thread.currentThread() ne this) {
      throw new IllegalThreadStateException("tryYield must be called on the current thread")
    }
    state match
      case State.Running =>
        // Avoid unmounting on yield: if the VT currently owns a monitor, a
        // suspended yield can starve contenders.
        Thread.nativeCompanion.yieldThread()
      case s if isRecursiveSuspendState(s) =>
        // Recursive yield while suspension is in progress must not suspend again.
        state = State.Pinned
        Thread.nativeCompanion.yieldThread()
        state = s
      case _ =>
        Thread.nativeCompanion.yieldThread()
  }

  // ---------------------------------------------------------------------------
  // Termination & join
  // ---------------------------------------------------------------------------

  /** Marks `Terminated` and signals `termination` exactly once (if present) when the `task` finishes or fails fatally.
   */
  private def afterDone(): Unit = {
    state = State.Terminated
    task = null
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

  /** Lazily allocates the join latch (CAS on `termination`) shared with `join`/`joinNanos`. */
  private def getTermination(): CountDownLatch = {
    this.termination match {
      case null =>
        val term = new CountDownLatch(1)
        if (terminationAtomic.compareExchangeStrong(null: CountDownLatch, term)) term
        else this.termination
      case termination => termination
    }
  }

  /** `Thread` join with nanosecond timeout: blocks on `CountDownLatch` until termination or timeout. */
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

  // ---------------------------------------------------------------------------
  // java.lang.Thread introspection overrides
  // ---------------------------------------------------------------------------

  /** Maps internal `State` to `java.lang.Thread.State` (including carrier-derived state when `Running`). */
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
    case State.MonitorBlocking                            => Thread.State.RUNNABLE
    case State.Parking | State.TimedParking               => Thread.State.RUNNABLE
    case State.Parked | State.Pinned                      => Thread.State.WAITING
    case State.TimedParked | State.TimedPinned            => Thread.State.TIMED_WAITING
    case State.Blocked                                    => Thread.State.WAITING
    case State.MonitorBlocked                             => Thread.State.BLOCKED
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

/** Internal constants and opaque state tags for `java.lang.VirtualThread` (javalib implementation). */
object VirtualThread {
  private[java] type Boundary = Continuations.BoundaryLabel[Unit]
  private[java] type Continuation = () => Unit

  /** `Runnable` submitted to `VirtualThreadScheduler`: owns the outer run loop, `mount`/`unmount`, and either starts
   *  the root `task` inside a delimcc boundary or resumes a published continuation.
   */
  private class RunLoop(private var vThread: VirtualThread | Null) extends Runnable {

    override def run(): Unit = {
      val vt = vThread
      if (vt == null) return

      /* Clear delimcc handler TLS before any early return. If tryAcquireRunLoopToken
       * fails we still leave this worker with a clean chain; otherwise a stale head
       * from another VT (or a partial suspend) can make handler_pop / handler_split_at
       * see the wrong label and abort, which strands Object.wait continuations. */
      Continuations.handlersReset()
      // Recovery guard: if a previous VT left TLS currentThread mapped to a VT,
      // reset to the platform carrier before dispatching this continuation.
      val platformThread = Thread.currentPlatformThread
      if (Thread.currentThread().isVirtual() && (Thread.currentThread() ne platformThread)) {
        NativeThread.setCurrentThread(platformThread)
      }

      if (!vt.tryAcquireRunLoopToken()) {
        // Re-queue once if VT is runnable so we don't drop the resumption.
        if (vt.state == State.Unparked || vt.state == State.Unblocked ||
            (vt.state == State.Yielded && vt.resumeExecution != null)) {
          vt.requestRun()
        }
        return
      }

      var exitedNormally = false
      try {
        var continueLoop = true
        while (continueLoop) {
          var runCurrent = false
          val initialState = vt.state

          initialState match {
            case State.Started | State.Unparked | State.Yielded | State.Unblocked =>
              runCurrent = vt.compareAndSetState(initialState, State.Running)
              if (runCurrent) {
                if (initialState == State.Unparked) {
                  vt.cancelTimeoutTask(vt.activeResumeGeneration)
                  vt.setParkPermit(false) // consume park event
                } else if (initialState == State.Unblocked) {
                  vt.setBlockPermit(false) // consume block event
                }
              }
            case _ => ()
          }

          if (runCurrent) {
            var resubmitYield = false
            var didAfterYieldSubmit = false
            vt.mount()
            try {
              // Every VT run/resume must start from a clean carrier handler TLS.
              // The continuation/boundary machinery re-installs the VT's own
              // captured chain; keeping stale handlers from a previous VT on
              // the carrier corrupts handler_pop/split_at during park/wait.
              Continuations.handlersReset()
              // initial run
              if (initialState == State.Started) {
                Continuations.boundary[Unit] {
                  vt.boundary = summon[Boundary]
                  // Initial invocation of the task, might suspend
                  val t = vt.task
                  if (t != null) t.run()
                  // We get here only when whole fiber is completed
                  vt.afterDone()
                  vt.boundary = null
                }
              } else {
                // Consume continuation and resume, might suspend
                val continue = vt.consumeResume()
                if (continue == null) {
                  throw new IllegalStateException("Missing continuation to resume")
                }
                try continue()
                finally vt.reachabilityFence(continue)
              }
            } catch {
              case ex: Throwable =>
                vt.getUncaughtExceptionHandler().uncaughtException(vt, ex)
                vt.afterDone()
            } finally {
              // A yielded continuation has already captured the VT handler chain.
              // Clear carrier-local delimcc TLS eagerly so no stale handlers leak
              // into the next VT dispatched on this worker.
              Continuations.handlersReset()
              // Post-yield on carrier: reconcile parked state with permit.
              val s = vt.state
              // Park path: Parked/TimedParked (LockSupport.park) + parkPermit; skip when resumeExecution null (wait).
              if ((s == State.Parked || s == State.TimedParked) &&
                  vt.resumeExecution != null &&
                  vt.getAndSetParkPermit(false)) {
                if (!vt.compareAndSetState(s, State.Unparked)) vt.setParkPermit(true)
                else {
                  vt.requestRun(lazily = true)
                  didAfterYieldSubmit = true
                }
              }
              // Block path: wait/enter blocked states + blockPermit.
              if ((s == State.Blocked || s == State.TimedBlocked || s == State.MonitorBlocked) &&
                  vt.getAndSetBlockPermit(false)) {
                if (!vt.compareAndSetState(s, State.Unblocked)) {
                  vt.setBlockPermit(true)
                } else {
                  vt.requestRun(lazily = true)
                  didAfterYieldSubmit = true
                }
              }
              resubmitYield =
                (vt.state == State.Yielded || vt.state == State.Unparked || vt.state == State.Unblocked) &&
                  (vt.resumeExecution != null) && !didAfterYieldSubmit
              vt.unmount()
              vt.scheduleDeferredTimeout()
              if (vt.carrierThread != null) {
                vt.scheduler.afterYieldOnCarrier(vt.carrierThread.asInstanceOf[Thread])
              }
            }

            if (resubmitYield) {
              // Resubmit after yield using lazy submit when permitted (carrier, empty queue).
              vt.requestRun(lazily = true)
            }
          }

          // Defensive requeue: if a continuation is available but dispatch state
          // got desynchronized, ensure we keep scheduling progress.
          if ((vt.state == State.Yielded || vt.state == State.Unparked || vt.state == State.Unblocked) &&
              vt.resumeExecution != null) {
            vt.requestRun()
          }

          continueLoop = vt.finishRunLoopIteration()
        }
        exitedNormally = true
      } finally {
        if (!exitedNormally) vt.recoverRunLoopOnUnexpectedExit()
        if (vt.isTerminated()) vThread = null
      }
    }
  }

  /** Run-loop queue phase for scheduled continuations: prevents duplicate submits and supports coalesced wakeups. */
  opaque type DispatchState <: Int = Int
  private object DispatchState {
    final val Idle: DispatchState = 0
    final val Queued: DispatchState = 1
    final val Running: DispatchState = 2
    final val RunningQueued: DispatchState = 3
  }

  /** Fiber lifecycle and suspension flavour: parking vs monitor block vs yield vs terminal (encoded as `Int` for
   *  atomics and `@switch`-friendly comparisons).
   */
  opaque type State <: Int = Int
  private object State {
    final val New: State = 0
    final val Started: State = 1
    final val Runnable: State = 2 // runnable-unmounted
    final val Running: State = 3 // runnable-mounted

    // untimed and timed parking
    final val Parking: State = 4
    final val Parked: State = 5 // unmounted
    final val Pinned: State = 6 // mounted
    final val TimedParking: State = 7
    final val TimedParked: State = 8 // unmounted
    final val TimedPinned: State = 9 // mounted
    final val Unparked: State = 10 // unmounted but runnable

    // Thread.yield
    final val Yielding: State = 11 // Thread.yield
    final val Yielded: State = 12 // unmounted but runnable

    // Monitor block (Object.wait / contended monitor enter)
    final val Blocking: State = 13 // about to suspend for wait/enter
    final val Blocked: State = 14 // in Object.wait()
    final val TimedBlocked: State = 15
    final val Unblocked: State = 16 // unblocked, runnable (notify / monitor exit)
    final val MonitorBlocking: State = 17 // about to suspend for monitor enter
    final val MonitorBlocked: State = 18 // blocked on monitor enter

    final val Terminated: State = 99 // final state
  }

}
