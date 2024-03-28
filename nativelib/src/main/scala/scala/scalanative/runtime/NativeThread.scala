package scala.scalanative
package runtime

import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.runtime.GC.{ThreadRoutineArg, ThreadStartRoutine}
import scala.scalanative.annotation.alwaysinline
import scala.scalanative.unsafe._
import scala.scalanative.meta.LinktimeInfo.{isMultithreadingEnabled, isWindows}
import scala.scalanative.runtime.ffi.stdatomic.atomic_thread_fence
import scala.scalanative.runtime.ffi.stdatomic.memory_order._
import scala.annotation.nowarn

import java.util.concurrent.ConcurrentHashMap
import java.{util => ju}

trait NativeThread {
  import NativeThread._

  val thread: Thread

  private[runtime] var isFillingStackTrace: scala.Boolean = false
  @volatile private var _state: State = State.New
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
    MainThreadShutdownContext.onThreadFinished(this.thread)
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

  def onSpinWait(): Unit = LLVMIntrinsics.`llvm.donothing`

  @inline def holdsLock(obj: Object): Boolean = if (isMultithreadingEnabled) {
    getMonitor(obj.asInstanceOf[_Object]).isLockedBy(currentThread)
  } else false

  def threadRoutineArgs(thread: NativeThread): ThreadRoutineArg =
    fromRawPtr[scala.Byte](castObjectToRawPtr(thread))

  object Registry {
    // Replace with ConcurrentHashMap when thread-safe
    private val _aliveThreads = new ConcurrentHashMap[Long, NativeThread]

    private[NativeThread] def add(thread: NativeThread): Unit =
      _aliveThreads.put(thread.thread.getId(): @nowarn, thread)

    private[NativeThread] def remove(thread: NativeThread): Unit = {
      _aliveThreads.remove(thread.thread.getId(): @nowarn)
    }

    @nowarn
    def aliveThreads: Iterable[NativeThread] = {
      import scala.collection.JavaConverters._
      _aliveThreads.values.asScala
    }
  }

  def threadRoutine: ThreadStartRoutine = CFuncPtr1.fromScalaFunction {
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
        if (handler != null)
          executeUncaughtExceptionHandler(handler, thread, ex)
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
}
