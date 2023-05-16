package scala.scalanative.runtime

import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.runtime.GC.{ThreadRoutineArg, ThreadStartRoutine}
import scala.scalanative.annotation.alwaysinline
import scala.scalanative.unsafe._
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled
import scala.scalanative.runtime.libc.atomic_thread_fence
import scala.scalanative.runtime.libc.memory_order._
import scala.collection.concurrent.TrieMap
import scala.annotation.nowarn

trait NativeThread {
  import NativeThread._

  val thread: Thread

  @volatile private[this] var _state: State = State.New
  def state: State = _state
  protected[runtime] def state_=(newState: State): Unit = _state match {
    case State.Terminated => ()
    case _                => _state = newState
  }

  if (isMainThread) {
    TLS.assignCurrentThread(thread, this)
    state = State.Running
  } else if (isMultithreadingEnabled) {
    Registry.add(this)
  }

  protected def park(time: Long, isAbsolute: Boolean): Unit
  def unpark(): Unit
  def sleep(millis: Long): Unit
  def sleepNanos(nanos: Int): Unit
  def interrupt(): Unit
  def setPriority(priority: CInt): Unit

  @alwaysinline
  final def park(): Unit =
    park(0, isAbsolute = false)

  @alwaysinline
  final def parkNanos(nanos: Long): Unit = if (nanos > 0) {
    park(nanos, isAbsolute = false)
  }

  @alwaysinline
  final def parkUntil(deadlineEpoch: scala.Long): Unit =
    park(deadlineEpoch, isAbsolute = true)

  @alwaysinline
  @nowarn // Thread.getId is deprecated since JDK 19
  protected final def isMainThread = thread.getId() == MainThreadId

  protected def onTermination(): Unit = if (isMultithreadingEnabled) {
    state = NativeThread.State.Terminated
    Registry.remove(this)
  }
}

object NativeThread {
  private def MainThreadId = 0L

  trait Companion {
    type Impl <: NativeThread
    def create(thread: Thread, stackSize: Long): Impl
    def yieldThread(): Unit
    def currentNativeThread(): Impl = NativeThread.currentNativeThread
      .asInstanceOf[Impl]
  }

  sealed trait State
  object State {
    case object New extends State
    case object Running extends State
    case object Waiting extends State
    case object WaitingWithTimeout extends State
    case object WaitingOnMonitorEnter extends State
    sealed trait Parked extends State
    case object ParkedWaiting extends Parked
    case object ParkedWaitingTimed extends Parked
    case object Terminated extends State
  }

  @alwaysinline def currentThread: Thread = TLS.currentThread
  @alwaysinline def currentNativeThread: NativeThread = TLS.currentNativeThread

  @alwaysinline def onSpinWait(): Unit = Platform.yieldProcessor()

  @inline def holdsLock(obj: Object): Boolean = if (isMultithreadingEnabled) {
    getMonitor(obj).isLockedBy(currentThread)
  } else false

  def threadRoutineArgs(thread: NativeThread): ThreadRoutineArg =
    fromRawPtr[scala.Byte](castObjectToRawPtr(thread))

  object Registry {
    private val _aliveThreads = TrieMap.empty[Long, NativeThread]

    private[NativeThread] def add(thread: NativeThread): Unit =
      _aliveThreads.put(thread.thread.getId(): @nowarn, thread)

    private[NativeThread] def remove(thread: NativeThread): Unit =
      _aliveThreads.remove(thread.thread.getId(): @nowarn)

    def aliveThreads: scala.Array[NativeThread] =
      _aliveThreads.values.toArray

    def onMainThreadTermination() = {
      _aliveThreads.remove(MainThreadId)
    }
  }

  val threadRoutine: ThreadStartRoutine = CFuncPtr1.fromScalaFunction {
    (arg: ThreadRoutineArg) =>
      val thread = castRawPtrToObject(toRawPtr(arg))
        .asInstanceOf[NativeThread]
      NativeThread.threadEntryPoint(thread)
      0.toPtr
  }

  private def threadEntryPoint(nativeThread: NativeThread): Unit = {
    import nativeThread.thread
    TLS.assignCurrentThread(thread, nativeThread)
    nativeThread.state = State.Running
    atomic_thread_fence(memory_order_seq_cst)
    // Ensure Java Thread already assigned the Native Thread instance
    // Otherwise park/unpark events might be lost
    while (thread.getState() == Thread.State.NEW) onSpinWait()
    try thread.run()
    catch {
      case ex: Throwable =>
        val handler = thread.getUncaughtExceptionHandler() match {
          case null    => Thread.getDefaultUncaughtExceptionHandler()
          case handler => handler
        }
        if (handler != null) handler.uncaughtException(thread, ex)
    } finally
      thread.synchronized {
        try nativeThread.onTermination()
        catch { case ex: Throwable => () }
        nativeThread.state = NativeThread.State.Terminated
        thread.notifyAll()
      }
  }

  @extern
  private object TLS {
    @name("scalanative_assignCurrentThread")
    def assignCurrentThread(
        thread: Thread,
        nativeThread: NativeThread
    ): Unit = extern

    @name("scalanative_currentNativeThread")
    def currentNativeThread: NativeThread = extern

    @name("scalanative_currentThread")
    def currentThread: Thread = extern
  }

  @extern object Platform {
    @blocking
    @name("scalanative_yield_processor")
    def yieldProcessor(): Unit = extern
  }

}
