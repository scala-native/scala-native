package java.lang

import java.lang.impl._
import java.lang.Thread._
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.ThreadFactory
import java.time.Duration

import scala.scalanative.meta.LinktimeInfo.{isWindows, isMultithreadingEnabled}

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.runtime.{fromRawPtr, NativeThread}
import scala.scalanative.runtime.NativeThread.{State => _, _}
import scala.scalanative.runtime.NativeThread.State._
import scala.scalanative.libc.atomic.{CAtomicLongLong, atomic_thread_fence}
import scala.scalanative.libc.atomic.memory_order._

import scala.scalanative.runtime.JoinNonDaemonThreads

class Thread private[lang] (
    @volatile private var name: String,
    private[java] val platformCtx: PlatformThreadContext /* | Null */
) extends Runnable {
  protected val tid = ThreadIdentifiers.next()

  @volatile private var interruptedState = false
  @volatile private[java] var parkBlocker: Object = _

  private var unhandledExceptionHandler: Thread.UncaughtExceptionHandler = _

  // ThreadLocal values : local and inheritable
  private[java] var threadLocals: ThreadLocal.Values = _
  private[java] var inheritableThreadLocals: ThreadLocal.Values = _

  private[java] var threadLocalRandomSeed: scala.Long = 0
  private[java] var threadLocalRandomProbe: Int = 0
  private[java] var threadLocalRandomSecondarySeed: Int = 0

  // Construct platform thread
  private[java] def this(
      group: ThreadGroup,
      name: String,
      characteristics: Int,
      task: Runnable,
      stackSize: scala.Long
  ) = {
    this(
      name = name,
      platformCtx = {
        val parent = Thread.currentThread()
        val threadGroup =
          if (group != null) group
          else parent.getThreadGroup()
        PlatformThreadContext(
          group = threadGroup,
          task = task,
          stackSize = stackSize,
          daemon = parent.isDaemon(),
          priority = parent.getPriority() min threadGroup.getMaxPriority()
        )
      }
    )
    if (name == null)
      throw new IllegalArgumentException("Thread name cannot be null")

    def hasFlag(flag: Int) = (characteristics & flag) != 0

    if (hasFlag(Characteristics.NoThreadLocal)) {
      threadLocals = ThreadLocal.Values.Unsupported
      inheritableThreadLocals = ThreadLocal.Values.Unsupported
    } else if (!hasFlag(Characteristics.NoInheritThreadLocal)) {
      val parent = Thread.currentThread()
      val parentLocals = parent.inheritableThreadLocals
      if (parentLocals != null && parentLocals != ThreadLocal.Values.Unsupported &&
          parentLocals.size > 0) {
        this.inheritableThreadLocals =
          new ThreadLocal.Values(parent.inheritableThreadLocals)
      }
    }
  }

  // Construct virtual thread
  private[java] def this(name: String, characteristics: Int) = {
    this(
      name = if (name != null) name else "",
      platformCtx = null
    )
    def hasFlag(flag: Int) = (characteristics & flag) != 0
    if (hasFlag(Characteristics.NoThreadLocal)) {
      threadLocals = ThreadLocal.Values.Unsupported
      inheritableThreadLocals = ThreadLocal.Values.Unsupported
    } else if (!hasFlag(Characteristics.NoInheritThreadLocal)) {
      val parent = Thread.currentThread()
      val parentLocals = parent.inheritableThreadLocals
      if (parentLocals != null && parentLocals != ThreadLocal.Values.Unsupported &&
          parentLocals.size > 0) {
        this.inheritableThreadLocals =
          new ThreadLocal.Values(parent.inheritableThreadLocals)
      }
    }
  }

  // constructors
  def this(
      group: ThreadGroup,
      task: Runnable,
      name: String,
      stackSize: scala.Long,
      inheritThreadLocals: scala.Boolean
  ) = this(
    group = group,
    name = name,
    characteristics =
      if (inheritThreadLocals) 0
      else Characteristics.NoInheritThreadLocal,
    task = task,
    stackSize = stackSize
  )

  // since Java 9
  def this(
      group: ThreadGroup,
      target: Runnable,
      name: String,
      stacksize: scala.Long
  ) = this(group, target, name, stacksize, inheritThreadLocals = true)

  def this() = this(null, null, nextThreadName(), 0)

  def this(target: Runnable) =
    this(null, target, nextThreadName(), 0)

  def this(group: ThreadGroup, target: Runnable) =
    this(group, target, nextThreadName(), 0)

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

  @deprecated(
    "This method is not final and may be overridden to return a value that is not the thread ID. Use threadId() instead.",
    "JDK 19"
  )
  def getId(): scala.Long = threadId()

  final def getName(): String = name
  final def setName(name: String): Unit = {
    if (name == null) throw new NullPointerException
    this.name = name
  }

  final def getPriority(): Int =
    if (isVirtual()) Thread.NORM_PRIORITY
    else platformCtx.priority

  final def setPriority(priority: Int): Unit = {
    if (priority > Thread.MAX_PRIORITY || priority < Thread.MIN_PRIORITY) {
      throw new IllegalArgumentException("Wrong Thread priority value")
    }
    if (!isVirtual()) {
      platformCtx.priority = priority
      if (platformCtx.nativeThread != null)
        platformCtx.nativeThread.setPriority(priority)
    }
  }

  def getStackTrace(): Array[StackTraceElement] =
    new Array[StackTraceElement](0)

  def getState(): State = {
    assert(!isVirtual(), "should be overriden by virtual threads")
    import NativeThread.State._
    val nativeThread = platformCtx.nativeThread
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

  final def getThreadGroup(): ThreadGroup = {
    if (isVirtual()) ??? // special group for virtual threads
    else
      getState() match {
        case State.TERMINATED => null
        case _                => platformCtx.group
      }
  }

  def getUncaughtExceptionHandler(): Thread.UncaughtExceptionHandler = {
    if (unhandledExceptionHandler != null) unhandledExceptionHandler
    else getThreadGroup()
  }
  def setUncaughtExceptionHandler(eh: Thread.UncaughtExceptionHandler): Unit =
    unhandledExceptionHandler = eh

  final def isAlive(): scala.Boolean = getState() match {
    case State.NEW | State.TERMINATED => false
    case _                            => true
  }

  final def isDaemon(): scala.Boolean =
    if (isVirtual()) true
    else platformCtx.daemon

  final def setDaemon(on: scala.Boolean): Unit = {
    if (isAlive()) throw new IllegalThreadStateException()
    if (isVirtual() && !on)
      throw new IllegalArgumentException(
        "VirtualThread cannot be non-deamon thread"
      )
    else platformCtx.daemon = on

  }

  def isInterrupted(): scala.Boolean = interruptedState
  def interrupt(): Unit = if (isAlive()) {
    synchronized {
      interruptedState = true
      if (isVirtual()) ??? // TODO
      else platformCtx.nativeThread.interrupt()
    }
  }

  def run(): Unit = {
    // Overriden in VirtualThread
    val task = platformCtx.task
    if (task != null) task.run()
  }

  def start(): Unit = synchronized {
    if (!isMultithreadingEnabled)
      throw new IllegalStateException(
        "ScalaNative application linked with disabled multithreading support"
      )
    if (isVirtual())
      throw new UnsupportedOperationException(
        "VirtualThreads are not yet supported"
      )
    else
      platformCtx.start(this)
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
    val groupName = getThreadGroup() match {
      case null  => ""
      case group => group.getName()
    }
    s"Thread[${threadId()},${getName()},${getPriority()},$groupName]"
  }

  @deprecated("Deprecated for removal", "17")
  def checkAccess(): Unit = ()

  // Since JDK 19
  final def isVirtual(): scala.Boolean = isInstanceOf[VirtualThread]

  @throws[InterruptedException](
    "if the current thread is interrupted while waiting"
  )
  @throws[IllegalThreadStateException]("if this thread has not been started")
  final def join(duration: Duration): scala.Boolean = {
    getState() match {
      case Thread.State.NEW =>
        throw new IllegalThreadStateException("Cannot join unstarted thread")
      case _ =>
        if (duration.isNegative() || duration.isZero()) {
          join(duration.getSeconds() * 1000, duration.getNano())
        }
        getState() == Thread.State.TERMINATED
    }
  }

  final def threadId(): scala.Long = tid
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

  // Since JDK 19
  trait Builder {

    /** Sets whether the thread is allowed to set values for its copy of
     *  thread-local variables.
     */
    def allowSetThreadLocals(allow: scala.Boolean): Builder

    /** Returns a ThreadFactory to create threads from the current state of the
     *  builder.
     */
    def factory(): ThreadFactory

    /** Sets whether the thread inherits the initial values of
     *  inheritable-thread-local variables from the constructing thread.
     */
    def inheritInheritableThreadLocals(inherit: scala.Boolean): Builder

    /** Sets the thread name. */
    def name(name: String): Builder

    /** Sets the thread name to be the concatenation of a string prefix and the
     *  string representation of a counter value.
     */
    @throws[IllegalArgumentException]("if start is negative")
    def name(prefix: String, start: scala.Long): Builder

    /** Creates a new Thread from the current state of the builder and schedules
     *  it to execute.
     */
    def start(task: Runnable): Thread

    /** Sets the uncaught exception handler. */
    def uncaughtExceptionHandler(
        ueh: Thread.UncaughtExceptionHandler
    ): Builder

    /** Creates a new Thread from the current state of the builder to run the
     *  given task.
     */
    def unstarted(task: Runnable): Thread
  }

  object Builder {
    trait OfPlatform extends Builder {

      /** Sets the daemon status to true. */
      def daemon(): OfPlatform = daemon(true)

      /** Sets the daemon status. */
      def daemon(on: scala.Boolean): OfPlatform

      /** Sets the thread group. */
      def group(group: ThreadGroup): OfPlatform

      /** Sets the thread priority. */
      @throws[IllegalArgumentException](
        "if the priority is less than Thread.MIN_PRIORITY or greater than Thread.MAX_PRIORITY"
      )
      def priority(priority: Int): OfPlatform

      /** Sets the desired stack size. */
      @throws[IllegalArgumentException]("if the stack size is negative")
      def stackSize(stackSize: scala.Long): OfPlatform

      /** Sets whether the thread is allowed to set values for its copy of
       *  thread-local variables.
       */
      override def allowSetThreadLocals(allow: scala.Boolean): OfPlatform

      /** Sets whether the thread inherits the initial values of
       *  inheritable-thread-local variables from the constructing thread.
       */
      override def inheritInheritableThreadLocals(
          inherit: scala.Boolean
      ): OfPlatform

      /** Sets the thread name. */
      override def name(name: String): OfPlatform

      /** Sets the thread name to be the concatenation of a string prefix and
       *  the string representation of a counter value.
       */
      @throws[IllegalArgumentException]("if start is negative")
      override def name(prefix: String, start: scala.Long): OfPlatform

      /** Sets the uncaught exception handler. */
      def uncaughtExceptionHandler(
          ueh: Thread.UncaughtExceptionHandler
      ): OfPlatform
    }

    trait OfVirtual extends Builder {

      /** Sets whether the thread is allowed to set values for its copy of
       *  thread-local variables.
       */
      override def allowSetThreadLocals(allow: scala.Boolean): OfVirtual

      /** Sets whether the thread inherits the initial values of
       *  inheritable-thread-local variables from the constructing thread.
       */
      override def inheritInheritableThreadLocals(
          inherit: scala.Boolean
      ): OfVirtual

      /** Sets the thread name. */
      override def name(name: String): OfVirtual

      /** Sets the thread name to be the concatenation of a string prefix and
       *  the string representation of a counter value.
       */
      @throws[IllegalArgumentException]("if start is negative")
      override def name(prefix: String, start: scala.Long): OfVirtual

      /** Sets the uncaught exception handler. */
      def uncaughtExceptionHandler(
          ueh: Thread.UncaughtExceptionHandler
      ): OfVirtual
    }
  }

  // Implementation detai
  private[java] object Characteristics {
    final val Default = 0
    final val NoThreadLocal = 1 << 1
    final val NoInheritThreadLocal = 1 << 2
  }

  final val MAX_PRIORITY: Int = 10
  final val MIN_PRIORITY: Int = 1
  final val NORM_PRIORITY: Int = 5

  final val MainThread = new Thread(
    name = "main",
    platformCtx = PlatformThreadContext(
      group = new ThreadGroup(ThreadGroup.System, "main"),
      task = null: Runnable,
      stackSize = 0L
    )
  ) {
    override protected val tid: scala.Long = 0L
    inheritableThreadLocals = new ThreadLocal.Values()
    platformCtx.nativeThread = nativeCompanion.create(this, 0L)
    JoinNonDaemonThreads.registerExitHook()
  }

  @alwaysinline private[lang] def nativeCompanion: NativeThread.Companion =
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

  @volatile private var defaultExceptionHandler: UncaughtExceptionHandler = _
  def getDefaultUncaughtExceptionHandler(): UncaughtExceptionHandler =
    defaultExceptionHandler
  def setDefaultUncaughtExceptionHandler(eh: UncaughtExceptionHandler): Unit =
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

  def sleep(millis: scala.Long, nanos: Int): Unit = {
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

  // Since JDK 19
  @throws[InterruptedException](
    "if the current thread is interrupted while sleeping"
  )
  def sleep(duration: Duration): Unit =
    sleep(millis = duration.getSeconds() * 1000, nanos = duration.getNano())

  def ofPlatform(): Builder.OfPlatform =
    new ThreadBuilders.PlatformThreadBuilder

  def ofVirtual(): Builder.OfVirtual =
    new ThreadBuilders.VirtualThreadBuilder

  def startVirtualThread(task: Runnable): Thread = {
    val thread = new VirtualThread(
      name = null,
      characteristics = Characteristics.Default,
      task = task
    )
    thread.start()
    thread
  }

  // Scala Native specific:
  private[lang] def nextThreadName(): String =
    s"Thread-${ThreadNamesNumbering.next()}"

  // Counter used to generate thread's ID, 0 resevered for main
  sealed abstract class Numbering {
    final protected var cursor = 1L
    final protected val cursorRef = new CAtomicLongLong(
      fromRawPtr(classFieldRawPtr(this, "cursor"))
    )
    def next(): scala.Long = cursorRef.fetchAdd(1L)
  }
  object ThreadNamesNumbering extends Numbering
  object ThreadIdentifiers extends Numbering
}

// ScalaNative specific
private[java] case class PlatformThreadContext(
    group: ThreadGroup,
    task: Runnable,
    stackSize: scala.Long,
    @volatile var priority: Int = Thread.NORM_PRIORITY,
    @volatile var daemon: scala.Boolean = false
) {
  var nativeThread: NativeThread = _

  def unpark(): Unit = if (nativeThread != null) nativeThread.unpark()

  def start(thread: Thread): Unit = {
    assert(thread.platformCtx == this)
    if (nativeThread != null) {
      throw new IllegalThreadStateException("This thread was already started!")
    }

    atomic_thread_fence(memory_order_seq_cst)
    nativeThread = Thread.nativeCompanion.create(thread, stackSize)
    atomic_thread_fence(memory_order_release)
    while (nativeThread.state == New) Thread.onSpinWait()
    atomic_thread_fence(memory_order_acquire)
    nativeThread.setPriority(priority)
  }
}
