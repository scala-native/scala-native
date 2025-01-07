package scala.scalanative
package runtime

import java.{lang => jl}

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
import scala.scalanative.concurrent.NativeExecutionContext
import scala.concurrent.duration._

trait NativeThread {
  import NativeThread._

  val thread: Thread

  def stackSize: Int
  def companion: NativeThread.Companion

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
    if (isMultithreadingEnabled) park(0, isAbsolute = false)
    else NativeExecutionContext.queueInternal.helpComplete()

  @alwaysinline
  final def parkNanos(nanos: Long): Unit = if (nanos > 0) {
    if (isMultithreadingEnabled) park(nanos, isAbsolute = false)
    else NativeExecutionContext.queueInternal.stealWork(nanos.nanos)
  }

  @alwaysinline
  final def parkUntil(deadlineEpoch: scala.Long): Unit =
    if (isMultithreadingEnabled) park(deadlineEpoch, isAbsolute = true)
    else {
      val timeout = (deadlineEpoch - System.currentTimeMillis()).millis
      NativeExecutionContext.queueInternal.stealWork(timeout)
    }

  @alwaysinline
  @nowarn // Thread.getId is deprecated since JDK 19
  protected final def isMainThread = thread.getId() == MainThreadId

  protected def onTermination(): Unit = if (isMultithreadingEnabled) {
    state = NativeThread.State.Terminated
    Registry.remove(this)
    MainThreadShutdownContext.onThreadFinished(this.thread)
  }
}

private object ThreadStackSize {
  final val Minimal = 64 * 1024
  final val JVMDefault = 1024 * 1024 // 1MB is JVM default

  // Additional stack size to compenstate memory for internals
  private def extraThreadStackSize: Long = NativeThread.StackOverflowGuards.size

  private val overrideDefaultThreadSize: Option[Long] = {
    System.getenv("SCALANATIVE_THREAD_SIZE") match {
      case null => None
      case default =>
        val numberPart = default.takeWhile(_.isDigit)
        val multiplier = default.stripPrefix(numberPart).toLowerCase() match {
          case ""         => 1
          case "k" | "kb" => 1024
          case "m" | "mb" => 1024 * 1024
          case other =>
            System.err.println(
              s"Invalid setting for SCALANATIVE_THREAD_SIZE env variable would be ignored: $other"
            )
            -1
        }
        if (multiplier > 0) Some(numberPart.toLong * multiplier)
        else None
    }
  }

  def resolve(userDefinedStackSize: Long, osDefaultStackSize: Long): Int = {
    val requiredSize = extraThreadStackSize + {
      if (userDefinedStackSize > 0)
        Math.max(userDefinedStackSize, ThreadStackSize.Minimal)
      else
        overrideDefaultThreadSize.getOrElse {
          Math.max(JVMDefault, osDefaultStackSize)
        }
    }
    // stack size for thread might need to be page size aligned
    val pageSize = Platform.pageSize
    val pageAlignedSize =
      if (requiredSize % pageSize == 0) requiredSize
      else (requiredSize + pageSize - 1) / pageSize * pageSize
    assert(pageAlignedSize <= Int.MaxValue, "Size of stack size > Int.MaxValue")
    pageAlignedSize.toInt
  }
}

object NativeThread {
  private def MainThreadId = 0L

  def calculateStackSize(
      userDefinedStackSize: Long,
      osDefaultStackSize: Long
  ): Int = ThreadStackSize.resolve(userDefinedStackSize, osDefaultStackSize)

  trait Companion {
    type Impl <: NativeThread
    def create(thread: Thread, stackSize: Long): Impl
    def yieldThread(): Unit
    def currentNativeThread(): Impl = NativeThread.currentNativeThread
      .asInstanceOf[Impl]
    def defaultOSStackSize: Long
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

    /** Returns `Some` when a thread with the given id is present in the
     *  registry and `None` otherwise.
     *
     *  @param id
     *    the id of the thread to find
     */
    def getById(id: Long): Option[NativeThread] =
      Option(_aliveThreads.get(id))

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
    val stackBottom = Intrinsics.stackalloc[Int]()
    TLS.assignCurrentThread(thread, nativeThread)
    TLS.setupCurrentThreadInfo(
      stackBottom = stackBottom,
      stackSize = nativeThread.stackSize,
      isMainThread = false
    )
    StackOverflowGuards.setup(isMainThread = false)

    nativeThread.state = State.Running
    atomic_thread_fence(memory_order_seq_cst)
    // Ensure Java Thread already assigned the Native Thread instance
    // Otherwise park/unpark events might be lost
    while (thread.getState() == Thread.State.NEW) onSpinWait()
    try thread.run()
    catch {
      case ex: jl.Throwable =>
        val handler = thread.getUncaughtExceptionHandler() match {
          case null    => Thread.getDefaultUncaughtExceptionHandler()
          case handler => handler
        }
        if (handler != null)
          executeUncaughtExceptionHandler(handler, thread, ex)
    } finally
      thread.synchronized {
        try nativeThread.onTermination()
        catch { case ex: jl.Throwable => () }
        nativeThread.state = NativeThread.State.Terminated
        thread.notifyAll()
      }
  }

  @exported("scalanative_throwPendingStackOverflowError")
  def throwPendingStackOverflowError(): Unit = {
    val exception = new StackOverflowError()
    exception.asInstanceOf[runtime.Throwable].onCatchHandler = (_: Throwable) =>
      try NativeThread.StackOverflowGuards.reset()
      catch { case ex: StackOverflowError => () }

    throw exception
  }

  @extern
  private[scalanative] object TLS {
    @name("scalanative_assignCurrentThread")
    def assignCurrentThread(
        thread: Thread,
        nativeThread: NativeThread
    ): Unit = extern

    @name("scalanative_currentNativeThread")
    def currentNativeThread: NativeThread = extern

    @name("scalanative_currentThread")
    def currentThread: Thread = extern

    @name("scalanative_setupCurrentThreadInfo")
    def setupCurrentThreadInfo(
        stackBottom: RawPtr,
        stackSize: Int, // ignored if main thread
        isMainThread: Boolean
    ): Unit = extern
  }

  @extern private[runtime] object StackOverflowGuards {
    @name("scalanative_stackOverflowGuardsSize")
    def size: Int = extern

    @name("scalanative_setupStackOverflowGuards")
    def setup(isMainThread: Boolean): Unit = extern

    @name("scalanative_resetStackOverflowGuards")
    def reset(): Unit = extern
  }

}
