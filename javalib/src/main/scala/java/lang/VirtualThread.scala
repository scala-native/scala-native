// scalafmt: { maxColumn = 120}

package java.lang

import java.lang.VirtualThread.DefaultScheduler
import java.util.Objects
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory
import java.util.concurrent._
import java.util.concurrent.locks.LockSupport

import scala.annotation.tailrec

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic.{AtomicInt, AtomicRef}
import scala.scalanative.runtime.{Continuations, Intrinsics, UnsupportedFeature, fromRawPtr}

private[lang] final class VirtualThread(
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
  @alwaysinline
  private def stateAtomic = new AtomicInt(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "state"))
  )
  @alwaysinline
  private def terminationAtomic = new AtomicRef[CountDownLatch](
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "termination"))
  )
  @inline def compareAndSetState(
      expected: VirtualThread.State,
      value: VirtualThread.State
  ): Boolean = stateAtomic.compareExchangeStrong(expected, value)

  override def run(): Unit = ???

  override def start(): Unit = {
    if (!compareAndSetState(State.New, State.Started))
      throw new IllegalThreadStateException("Already started")

    // setThreadContainer
    // container onStat
    // inheritExtentLocalBindings
    try submitRunContinuation()
    // Thread.currentThread() match {
    //   case vtc: VirtualThreadCarrier if scheduler eq DefaultScheduler =>
    //     try vtc.getPool().externalSubmit(ForkJoinTask.adapt(executeContinuation))
    //     catch { case rejected: RejectedExecutionException =>
    //       // submitFailed(rejected)
    //       throw rejected
    //     }
    //   case _ => submitRunContinuation(scheduler, false)
    // }
    catch {
      case ex: Exception =>
        afterDone()
        throw ex
    }
  }

  private def submitRunContinuation(lazySubmit: scala.Boolean = false): Unit =
    scheduler match {
      case pool: ForkJoinPool if lazySubmit => pool.lazySubmit(ForkJoinTask.adapt(executeContinuation))
      case _                                => scheduler.execute(executeContinuation)
    }
  private def lazySubmitRunContinuation(): Unit = submitRunContinuation(false)

  private def mount(): Unit = {
    val carrier = Thread.currentThread().asInstanceOf[VirtualThreadCarrier]
    this.carrierThread = carrier

    // sync up carrier thread interrupt status if needed
    if (interruptedState) {
      // carrierThread.
      carrier.setInterrupt()
    } else if (carrier.isInterrupted()) {
      // synchronized(interruptLock) {
      // need to recheck interrupt status
      if (!interruptedState) {
        carrier.clearInterrupt()
      }
      // }
    }
    carrier.mountedThread = this
  }

  private def unmount(): Unit = {
    // assert !Thread.holdsLock(interruptLock);

    val carrier = this.carrierThread.asInstanceOf[VirtualThreadCarrier]
    carrier.mountedThread = null

    // break connection to carrier thread, synchronized with interrupt
    interruptLock.synchronized {
      this.carrierThread = null
    }
    carrier.clearInterrupt();
  }

  private def executeContinuation: Runnable = new Runnable {
    override def run(): Unit = {
      // the carrier must be a platform thread
      if (Thread.currentThread().isVirtual()) {
        throw new WrongThreadException()
      }

      val initialState = state
      val initialRun = initialState match {
        case State.Started if compareAndSetState(State.Started, State.Running) =>
          ()
        case State.Runnable if compareAndSetState(State.Runnable, State.Running) =>
        // setParkPermit
        case _ => return // Not runnable
      }

      mount()
      try
        // initial run
        if (initialState == State.Started) Continuations.boundary[Unit] {
          boundary = summon[Boundary]
          task.run()
          afterDone()
          boundary = null.asInstanceOf[Boundary]
        }
        else {
          val continue = resumeExecution
          resumeExecution = null
          continue()
        }
      finally unmount()
    }
  }

  def tryYield(): Unit = {
    assert(Thread.currentThread() eq this)
    state = State.Yielding
    // TODO: detect if can yield (should pin)
    Continuations.suspend[Unit] { resumeContinuation =>
      resumeExecution = resumeContinuation
      onYield()
    }(using boundary)
  }

  def afterDone(): Unit = {
    state = State.Terminated
    termination match {
      case termination: CountDownLatch =>
        assert(termination.getCount() == 1)
        termination.countDown()
      case null => ()
    }
  }

  def onYield(): Unit = {
    unmount()
    state match {
      // case State.Parking | State.TimedParking => ???
      case State.Yielding =>
        // state = State.Runnable
        state = State.Runnable
        // Thread.currentThread() match {
        //   case vtc: VirtualThreadCarrier if vtc.getQueuedTaskCount() == 0 =>
        //      externalSubmitRunContinuation(vtc.getPool())
        //     ???
        //   case _ =>
        // }
        lazySubmitRunContinuation()

      // case State.Blocking =>
      //   ???
      // case State.Waiting | State.TimedWaiting => ???
    }
    // assert(Thread.currentThread().isInstanceOf[VirtualThreadCarrier])
    // assert(carrierThread == null)
  }

  def tryResume(): Unit = synchronized {
    val resume = this.resumeExecution
    this.resumeExecution = null
    resume()
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

  def joinNanos(nanos: scala.Long): scala.Boolean = {
    if (state == State.Terminated)
      return true

    // ensure termination object exists, then re-check state
    val termination = getTermination()
    if (state == State.Terminated)
      return true

    // wait for virtual thread to terminate
    if (nanos == 0) {
      termination.await()
      true
    } else {
      termination.await(nanos, TimeUnit.NANOSECONDS)
    }
    // assert state () == TERMINATED;
    // return true;
  }

  override def threadState(): Thread.State = state match {
    case State.New                                => Thread.State.NEW
    case State.Started                            => Thread.State.RUNNABLE
    case State.Runnable | State.RunnableSuspended => Thread.State.RUNNABLE
    case State.Running                            =>
      carrierThreadAccessLock.synchronized {
        val carrier = this.carrierThread
        if (carrierThread != null) carrierThread.threadState()
        else Thread.State.RUNNABLE
      }
    case State.Parking | State.Yielding                      => Thread.State.RUNNABLE
    case State.Parked | State.ParkedSuspended | State.Pinned => Thread.State.WAITING
    case State.Terminated                                    => Thread.State.TERMINATED
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
    // final val TimedParking = 6
    // final val TimedParked = 7 // unmounted
    // final val TimedPinned = 8 // mounted
    // final val Unparked = 7 // unmounted but runnable

    // Thread.yield
    final val Yielding = 7 // Thread.yield
    // final val Yielded = 11 // unmounted but runnable

    // monitor enter
    // final val Blocking = 12
    // final val Blocked = 13 // unmounted
    // final val Unblocked = 14 // unmounted but runnable

    // monitor wait/timed-wait
    // final val Waiting = 15
    // final val Wait = 16 // waiting in Object.wait
    // final val TimedWaiting = 17
    // final val TimedWait = 18 // waiting in timed-Object.wait

    final val Terminated = 99 // final state

    // can be suspended from scheduling when unmounted
    final val Suspended = 1 << 8
    final val RunnableSuspended = Runnable | Suspended
    final val ParkedSuspended = Parked | Suspended
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
  lazy val DefaultTasksScheduler = {
    val paralellism = Runtime.getRuntime().availableProcessors()
    val queueCount = Integer.highestOneBit(paralellism / 4).max(1)
    Array.fill(queueCount) {
      val executor: ScheduledThreadPoolExecutor =
        new ScheduledThreadPoolExecutor(
          1,
          task => {
            val t = new Thread("VirtualThread_unparker")
            t.setName("VirtualThread-unparker")
            t.setDaemon(false)
            t
          }
        )
      executor.setRemoveOnCancelPolicy(true)
      executor
    }
  }
}

private class VirtualThreadCarrier(scheduler: ForkJoinPool) extends ForkJoinWorkerThread(scheduler) {
  var mountedThread: VirtualThread = _
}
