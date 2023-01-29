package java.lang

import java.lang.impl._
import java.lang.Thread._
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.LockSupport

import scala.scalanative.meta.LinktimeInfo.{isWindows, isMultithreadingEnabled}

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.runtime.{fromRawPtr, NativeThread}
import scala.scalanative.runtime.NativeThread.{State => _, _}
import scala.scalanative.runtime.NativeThread.State._
import scala.scalanative.libc.atomic.{CAtomicLongLong, atomic_thread_fence}
import scala.scalanative.libc.atomic.memory_order._

import scala.scalanative.runtime.JoinNonDeamonThreads

class Thread private[lang] (
    group: ThreadGroup,
    target: Runnable,
    stackSize: Long,
    private[java] val inheritableThreadLocals: ThreadLocal.Values
) extends Runnable {
  private[java] val threadId = getNextThreadId()
  @volatile private var interruptedState = false
  private var name: String = s"Thread-$threadId"
  private var priority: Int = Thread.NORM_PRIORITY
  private var daemon = false
  // Uncaught exception handler for this thread
  private var exceptionHandler: Thread.UncaughtExceptionHandler = _

  // ThreadLocal values : local and inheritable
  private[java] lazy val threadLocals: ThreadLocal.Values =
    new ThreadLocal.Values()
  private[java] var threadLocalRandomSeed: Long = 0
  private[java] var threadLocalRandomProbe: Int = 0
  private[java] var threadLocalRandomSecondarySeed: Int = 0

  private[java] val parkBlocker: AtomicReference[Object] =
    new AtomicReference[Object]()

  private[java] var nativeThread: NativeThread = _

  // constructors
  def this(
      group: ThreadGroup,
      target: Runnable,
      name: String,
      stacksize: scala.Long,
      inheritThreadLocals: Boolean
  ) = {
    this(
      group =
        if (group != null) group
        else Thread.currentThread().getThreadGroup(),
      target = target,
      stackSize = stacksize,
      inheritableThreadLocals = {
        val parent = Thread.currentThread()
        if (inheritThreadLocals && parent != null)
          new ThreadLocal.Values(parent.inheritableThreadLocals)
        else new ThreadLocal.Values()
      }
    )
    val parent = Thread.currentThread()
    if (parent != null) {
      this.daemon = parent.daemon
      this.priority = parent.priority
    }
    if (name != null) this.name = name
  }

  // since Java 9
  def this(
      group: ThreadGroup,
      target: Runnable,
      name: String,
      stacksize: scala.Long
  ) = this(group, target, name, stacksize, inheritThreadLocals = true)

  def this() = this(null, null, null, 0)

  def this(target: Runnable) =
    this(null, target, null, 0)

  def this(group: ThreadGroup, target: Runnable) =
    this(group, target, null, 0)

  def this(name: String) =
    this(null, null, name, 0)

  def this(group: ThreadGroup, name: String) =
    this(group, null, name, 0)

  def this(target: Runnable, name: String) =
    this(null, target, name, 0)

  def this(group: ThreadGroup, target: Runnable, name: String) =
    this(group, target, name, 0)

  // accessors
  // def getContextClassLoader(): ClassLoader = null
  // def setContextClassLoader(classLoader: ClassLoader): Unit = ()

  def getId(): scala.Long = threadId

  final def getName(): String = name
  final def setName(name: String): Unit = {
    if (name == null) throw new NullPointerException
    this.name = name
  }

  final def getPriority(): Int = priority
  final def setPriority(priority: Int): Unit = {
    if (priority > Thread.MAX_PRIORITY || priority < Thread.MIN_PRIORITY) {
      throw new IllegalArgumentException("Wrong Thread priority value")
    }
    this.priority = priority
    if (nativeThread != null) nativeThread.setPriority(priority)
  }

  def getStackTrace(): Array[StackTraceElement] =
    new Array[StackTraceElement](0)

  def getState(): State = {
    import NativeThread.State._
    if (nativeThread == null) State.NEW
    else
      nativeThread.state match {
        case New                                     => State.NEW
        case Running                                 => State.RUNNABLE
        case WaitingOnMonitorEnter                   => State.BLOCKED
        case Waiting | ParkedWaiting                 => State.WAITING
        case WaitingWithTimeout | ParkedWaitingTimed => State.TIMED_WAITING
        case Terminated                              => State.TERMINATED
      }
  }

  final def getThreadGroup(): ThreadGroup = group

  def getUncaughtExceptionHandler(): Thread.UncaughtExceptionHandler = {
    if (exceptionHandler != null) exceptionHandler
    else getThreadGroup()
  }
  def setUncaughtExceptionHandler(eh: Thread.UncaughtExceptionHandler): Unit =
    exceptionHandler = eh

  final def isAlive(): scala.Boolean = nativeThread != null && {
    nativeThread.state match {
      case New | Terminated => false
      case _                => true
    }
  }

  final def isDaemon(): scala.Boolean = daemon
  final def setDaemon(daemon: scala.Boolean): Unit = {
    if (isAlive()) throw new IllegalThreadStateException()
    this.daemon = daemon
  }

  def isInterrupted(): scala.Boolean = interruptedState
  def interrupt(): Unit = synchronized {
    interruptedState = true
    if (nativeThread != null) nativeThread.interrupt()
  }

  def run(): Unit = {
    if (target != null) target.run()
  }

  def start(): Unit = synchronized {
    if (nativeThread != null) {
      throw new IllegalThreadStateException(
        "This thread was already started!"
      )
    }
    if (!isMultithreadingEnabled)
      throw new IllegalStateException(
        "ScalaNative application linked with disabled multithreading support"
      )
    atomic_thread_fence(memory_order_seq_cst)
    nativeThread = Thread.nativeCompanion.create(this, stackSize)
    atomic_thread_fence(memory_order_release)
    while (nativeThread.state == New) Thread.onSpinWait()
    atomic_thread_fence(memory_order_acquire)
    nativeThread.setPriority(priority)
  }

  final def join(): Unit = synchronized {
    while (isAlive()) {
      if (interrupted()) throw new InterruptedException()
      wait()
    }
  }

  final def join(millis: scala.Long): Unit = join(millis, 0)

  final def join(ml: scala.Long, n: Int): Unit = {
    var nanos: Int = n
    var millis: scala.Long = ml
    if (millis < 0 || nanos < 0 || nanos > 999999)
      throw new IllegalArgumentException()
    if (millis == 0 && nanos == 0) join()
    else
      synchronized {
        if (interrupted()) throw new InterruptedException()
        val end = System.nanoTime() + 1000000 * millis + nanos.toLong
        var rest = 0L
        var continue = true
        while (isAlive() && { rest = end - System.nanoTime(); rest > 0 }) {
          wait(millis, nanos)
          nanos = (rest % 1000000).toInt
          millis = rest / 1000000
        }
      }
  }

//  @deprecated("Deprecated for removal", "1.2")
//  def countStackFrames(): Int = 0

  override protected[lang] def clone(): Object =
    throw new CloneNotSupportedException("Thread cannot be cloned")

  @deprecated("Deprecated for removal", "1.7")
  def destroy(): Unit = throw new NoSuchMethodError()

  @deprecated("Deprecated for removal", "1.7")
  final def stop(): Unit = stop(new ThreadDeath())

  @deprecated("Deprecated for removal", "1.7")
  final def stop(throwable: Throwable): Unit = {
    if (throwable == null)
      throw new NullPointerException("The argument is null!")
    if (isAlive()) {
      if (Thread.currentThread() == MainThread) throw throwable
      else throw new UnsupportedOperationException()
    }
  }

  @deprecated("Deprecated for removal", "1.7")
  final def suspend(): Unit =
    if (isAlive()) LockSupport.park(this)

  @deprecated("Deprecated for removal", "1.7")
  final def resume(): Unit =
    if (isAlive()) LockSupport.unpark(this)

  override def toString(): String = {
    val groupName = if (group != null) group.getName() else ""
    s"Thread[$threadId,$name,$priority,$groupName]"
  }

  @deprecated("Deprecated for removal", "17")
  def checkAccess(): Unit = ()
}

object Thread {
  trait UncaughtExceptionHandler {
    def uncaughtException(t: Thread, e: Throwable): Unit
  }

  sealed class State(name: String, ordinal: Int)
      extends _Enum[State](name, ordinal) {
    override def toString() = this.name
  }

  object State {
    final val NEW = new State("NEW", 0)
    final val RUNNABLE = new State("RUNNABLE", 1)
    final val BLOCKED = new State("BLOCKED", 2)
    final val WAITING = new State("WAITING", 3)
    final val TIMED_WAITING = new State("TIMED_WAITING", 4)
    final val TERMINATED = new State("TERMINATED", 5)

    private[this] val cachedValues =
      Array(NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED)
    def values(): Array[State] = cachedValues.clone()
    def valueOf(name: String): State = {
      cachedValues.find(_.name() == name).getOrElse {
        throw new IllegalArgumentException("No enum const Thread.State." + name)
      }
    }
  }

  final val MAX_PRIORITY: Int = 10
  final val MIN_PRIORITY: Int = 1
  final val NORM_PRIORITY: Int = 5

  final val MainThread = new Thread(
    group = new ThreadGroup(ThreadGroup.System, "main"),
    target = null: Runnable,
    stackSize = 0L,
    inheritableThreadLocals = new ThreadLocal.Values()
  ) {
    override private[java] val threadId: scala.Long = 0L
    nativeThread = nativeCompanion.create(this, 0L)
    setName("main")
    JoinNonDeamonThreads.registerExitHook()
  }

  @alwaysinline private def nativeCompanion: NativeThread.Companion =
    if (isWindows) WindowsThread
    else PosixThread

  def activeCount(): Int = currentThread()
    .getThreadGroup()
    .activeCount()

  @alwaysinline def currentThread(): Thread =
    NativeThread.currentThread match {
      case null   => MainThread
      case thread => thread
    }

  def dumpStack(): Unit = new Throwable().printStackTrace()

  def enumerate(list: Array[Thread]): Int = currentThread()
    .getThreadGroup()
    .enumerate(list)

  def getAllStackTraces(): java.util.Map[Thread, Array[StackTraceElement]] =
    throw new UnsupportedOperationException()

  private var defaultExceptionHandler: UncaughtExceptionHandler = _
  def getDefaultUncaughtExceptionHandler(): UncaughtExceptionHandler =
    defaultExceptionHandler
  def setDefaultUncaughtHandler(eh: UncaughtExceptionHandler): Unit =
    defaultExceptionHandler = eh

  def holdsLock(obj: Object): scala.Boolean = NativeThread.holdsLock(obj)

  def interrupted(): scala.Boolean = {
    val thread = currentThread()
    val isInterrupted = thread.interruptedState
    if (isInterrupted) {
      thread.interruptedState = false
    }
    isInterrupted
  }

  def onSpinWait(): Unit = NativeThread.onSpinWait()

  def sleep(millis: scala.Long): Unit = sleep(millis, 0)

  def sleep(millis: scala.Long, nanos: scala.Int): Unit = {
    if (millis < 0)
      throw new IllegalArgumentException("millis must be >= 0")
    if (nanos < 0 || nanos > 999999)
      throw new IllegalArgumentException("nanos value out of range")

    val nativeThread = nativeCompanion.currentNativeThread()
    if (millis == 0) nativeThread.sleepNanos(nanos)
    else
      nativeThread.sleep(nanos match {
        case 0 => millis
        case _ => millis + 1
      })
    if (interrupted()) throw new InterruptedException()
  }

  @alwaysinline def `yield`(): Unit = nativeCompanion.yieldThread()

  // Counter used to generate thread's ID, 0 resevered for main
  final protected var threadId = 1L
  private def getNextThreadId(): scala.Long = {
    val threadIdRef = new CAtomicLongLong(
      fromRawPtr(classFieldRawPtr(this, "threadId"))
    )
    threadIdRef.fetchAdd(1L)
  }

}
