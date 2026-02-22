// scalafmt: { maxColumn = 120}

package java.lang

import java.lang.VirtualThread.DefaultScheduler
import java.util.Objects
import java.util.concurrent.*
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory
import java.util.concurrent.locks.LockSupport

import scala.annotation.tailrec

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic.{AtomicBool, AtomicInt, AtomicRef}
import scala.scalanative.runtime.{Continuations, Intrinsics, NativeThread, fromRawPtr}

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
  private var boundary: Boundary = _
  private var resumeExecution: () => Unit /* | Null */ = _
  val carrierThreadAccessLock = new {}

  @volatile var carrierThread: Thread = _
  @volatile var termination: CountDownLatch = _
  @volatile var state: VirtualThread.State = VirtualThread.State.New
  @volatile var wasUnparked: scala.Boolean = false

  // timeout for parking/waiting operations
  private var timeoutTask: Future[_] = _

  @alwaysinline
  private def stateAtomic =
    new AtomicInt(fromRawPtr(Intrinsics.classFieldRawPtr(this, "state")))

  @alwaysinline
  private def terminationAtomic =
    new AtomicRef[CountDownLatch](fromRawPtr(Intrinsics.classFieldRawPtr(this, "termination")))

  @alwaysinline
  private def wasUnparkedAtomic =
    new AtomicBool(fromRawPtr(Intrinsics.classFieldRawPtr(this, "wasUnparked")))

  @inline private def compareAndSetState(expected: VirtualThread.State, value: VirtualThread.State): Boolean =
    stateAtomic.compareExchangeStrong(expected, value)

  @inline private def setWasUnparked(value: scala.Boolean) = {
    if (wasUnparked != value) wasUnparked = value
  }
  @inline private def getAndSetWasUnparked(value: scala.Boolean) =
    if (wasUnparked != value) wasUnparkedAtomic.exchange(value)
    else value

  override def run(): Unit = () // no-op, must be started via start(), JVM compliant

  override def startInternal(): Unit = {
    if (!compareAndSetState(State.New, State.Started))
      throw new IllegalThreadStateException("Already started")

    try submitRunContinuation()
    catch {
      case ex: Exception =>
        afterDone()
        throw ex
    }
  }

  override def isInterrupted(): scala.Boolean = interruptedState
  override def getAndClearInterrupt(): scala.Boolean = {
    assert(Thread.currentThread() eq this)
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
      setWasUnparked(true) // consume parking permit
    }
  }

  private def cancelTimeoutTask(): Unit = timeoutTask match {
    case null => ()
    case task =>
      task.cancel(false)
      timeoutTask = null
  }

  private[lang] def sleepNanos(nanos: scala.Long): Unit = {
    assert(Thread.currentThread() eq this)
    assert(nanos >= 0)

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
      } finally setWasUnparked(true)
  }

  private[java] def parkNanos(nanos: scala.Long): Unit = {
    assert(Thread.currentThread() eq this)

    if (getAndSetWasUnparked(false)) return
    if (interruptedState) return
    if (nanos <= 0) return

    state match {
      case State.Running =>
        val startTime = System.nanoTime()
        state = State.TimedParking
        Continuations.suspend[Unit] { resumeContinuation =>
          resumeExecution = resumeContinuation
          timeoutTask = VirtualThread.schedule(() => unpark(), nanos, TimeUnit.NANOSECONDS)

          // may have been unparked
          if (wasUnparked && compareAndSetState(State.TimedParking, State.Unparked)) {
            submitRunContinuation(lazily = true)
          } else {
            state = State.TimedParked
          }
        }(using boundary)
        state = State.Running

      case s @ (State.Parking | State.TimedParking | State.Yielding) =>
        // recursive park during suspension
        state = State.TimedPinned
        NativeThread.currentNativeThread.parkNanos(nanos)
        state = s
    }
  }

  private[java] def park(): Unit = {
    assert(Thread.currentThread() eq this)

    if (getAndSetWasUnparked(false)) return
    if (interruptedState) return

    state match {
      case State.Running =>
        state = State.Parking
        Continuations.suspend[Unit] { resumeContinuation =>
          resumeExecution = resumeContinuation
          // may have been unparked
          if (wasUnparked && compareAndSetState(State.Parking, State.Unparked)) {
            submitRunContinuation(lazily = true)
          } else {
            state = State.Parked
          }
        }(using boundary)
        state = State.Running

      case s @ (State.Parking | State.TimedParking | State.Yielding) =>
        // recursive park during suspension, need to pin carrier thread
        state = State.Pinned
        NativeThread.currentNativeThread.park()
        setWasUnparked(false) // consume
        state = s
    }

  }

  private[java] def unpark(): Unit = {
    if (getAndSetWasUnparked(true)) return
    if (Thread.currentThread() eq this) return

    state match {
      case s @ (State.Parked | State.TimedParked) if compareAndSetState(s, State.Unparked) =>
        submitRunContinuation()
      case State.Pinned | State.TimedPinned =>
        LockSupport.unpark(carrierThread)
      case _ => ()
    }
  }

  private def submitRunContinuation(lazily: scala.Boolean = false): Unit =
    scheduler match {
      case pool: ForkJoinPool if lazily => pool.lazySubmit(ForkJoinTask.adapt(executeContinuation))
      case _                            => scheduler.execute(executeContinuation)
    }

  private def mount(): Unit = {
    assert(
      NativeThread.currentThread.isInstanceOf[VirtualThreadCarrier],
      s"${NativeThread.currentThread} is not VThreadCarrier"
    )
    val carrier = Thread.currentPlatformThread
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
    assert(this.state != State.Running)
    val carrier = this.carrierThread.asInstanceOf[VirtualThreadCarrier]
    NativeThread.setCurrentThread(carrier)

    // break connection to carrier thread, synchronized with interrupt
    interruptLock.synchronized {
      this.carrierThread = null
    }
    carrier.clearInterrupt();
  }

  private def executeContinuation: Runnable = new VirtualThreadContinuation()
  private class VirtualThreadContinuation extends Runnable {
    val vThread = VirtualThread.this

    override def run(): Unit = {
      // the carrier must be a platform thread
      if (Thread.currentThread().isVirtual()) {
        throw new WrongThreadException()
      }

      val initialState = state
      val initialRun = initialState match {
        case State.Started | State.Unparked | State.Yielded =>
          if (!compareAndSetState(initialState, State.Running))
            return

          initialState match {
            case State.Unparked =>
              VirtualThread.this.cancelTimeoutTask()
              VirtualThread.this.setWasUnparked(false) // consume event
            case _ => ()
          }

        case _ => return // Not runnable
      }

      mount()
      try
        // initial run
        if (initialState == State.Started) Continuations.boundary[Unit] {
          boundary = summon[Boundary]
          // Initial invocation of the task, might suspend
          task.run()
          // We get here only when whole fiber is completed
          afterDone()
          boundary = null.asInstanceOf[Boundary]
        }
        else {
          // Consume continuation and resume, might suspend
          val continue = resumeExecution
          resumeExecution = null
          continue()
          // Not done yet
        }
      catch {
        case ex: Throwable =>
          getUncaughtExceptionHandler().uncaughtException(VirtualThread.this, ex)
          afterDone()
      } finally unmount()
    }
  }

  private[lang] def tryYield(): Unit = {
    assert(Thread.currentThread() eq this)
    state = State.Yielding
    Continuations.suspend[Unit] { resumeContinuation =>
      resumeExecution = resumeContinuation
      submitRunContinuation(lazily = true)
      state = State.Yielded
    }(using boundary)
    state = State.Running
  }

  private def afterDone(): Unit = {
    state = State.Terminated
    termination match {
      case termination: CountDownLatch =>
        assert(termination.getCount() == 1)
        termination.countDown()
      case null => ()
    }
    assert(state == State.Terminated)
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
        if (carrierThread != null) carrierThread.threadState()
        else Thread.State.RUNNABLE
      }
    case State.Yielding                        => Thread.State.RUNNABLE
    case State.Parking | State.TimedParking    => Thread.State.RUNNABLE
    case State.Parked | State.Pinned           => Thread.State.WAITING
    case State.TimedParked | State.TimedPinned => Thread.State.TIMED_WAITING
    case State.Unparked | State.Yielded        => Thread.State.RUNNABLE
    case State.Terminated                      => Thread.State.TERMINATED
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
    val queueCount = Integer.highestOneBit(paralellism / 4).max(1)
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

// Non public
class VirtualThreadCarrier(scheduler: ForkJoinPool) extends ForkJoinWorkerThread(scheduler) {
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
