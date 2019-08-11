package java.lang

import java.util

import scala.scalanative.native.{
  CCast,
  CFunctionPtr,
  CInt,
  Ptr,
  ULong,
  signal,
  sizeof,
  stackalloc
}
import scala.scalanative.posix.pthread._
import scala.scalanative.posix.sched._
import scala.scalanative.posix.sys.types.{
  pthread_attr_t,
  pthread_key_t,
  pthread_t
}
import scala.scalanative.runtime.{
  CAtomicInt,
  CAtomicLong,
  NativeThread,
  ShadowLock,
  ThreadBase
}

// Ported from Harmony

class Thread private (
    parentThread: Thread, // only the main thread does not have a parent (= null)
    rawGroup: ThreadGroup,
    // Thread's target - a Runnable object whose run method should be invoked
    private val target: Runnable,
    rawName: String,
    // Stack size to be passes to VM for thread execution
    val stackSize: scala.Long,
    mainThread: scala.Boolean)
    extends ThreadBase
    with Runnable {

  import java.lang.Thread._

  private var livenessState = CAtomicInt(internalNew)

  private val threadId: scala.Long = getNextThreadId

  private[this] var name: String =
    if (rawName != THREAD) rawName.toString else THREAD + threadId

  private[lang] var group: ThreadGroup =
    if (rawGroup != null) rawGroup else parentThread.group
  group.checkGroup()

  private var daemon: scala.Boolean =
    if (!mainThread) parentThread.daemon else false

  private var priority: Int = if (!mainThread) parentThread.priority else 5

  // main thread is not started via Thread.start, set it up manually
  def initMainThread(): Unit = {
    group.add(this)
    livenessState.store(internalStarted)
    underlying = pthread_self()
    pthread_setspecific(myThreadKey, this.cast[Ptr[scala.Byte]])
  }

  // Indicates if the thread was already started
  def started: scala.Boolean = {
    val value = livenessState.load()
    value > internalNew
  }

  private var exceptionHandler: Thread.UncaughtExceptionHandler = _

  private var underlying: pthread_t = 0.asInstanceOf[ULong]

  private val sleepMutex   = new Object
  private val joinMutex    = new Object
  private val suspendMutex = new Object
  private var suspendState = internalNotSuspended

  var localValues: ThreadLocal.Values = _

  var inheritableValues: ThreadLocal.Values =
    if (parentThread != null && parentThread.inheritableValues != null) {
      new ThreadLocal.Values(parentThread.inheritableValues)
    } else null

  checkAccess()

  def this(group: ThreadGroup,
           target: Runnable,
           name: String,
           stacksize: scala.Long) = {
    this(Thread.currentThread(),
         group,
         target,
         name,
         stacksize,
         mainThread = false)
  }

  def this() = this(null, null, Thread.THREAD, 0)

  def this(target: Runnable) = this(null, target, Thread.THREAD, 0)

  def this(target: Runnable, name: String) = this(null, target, name, 0)

  def this(name: String) = this(null, null, name, 0)

  def this(group: ThreadGroup, target: Runnable) =
    this(group, target, Thread.THREAD, 0)

  def this(group: ThreadGroup, target: Runnable, name: String) =
    this(group, target, name, 0)

  def this(group: ThreadGroup, name: String) = this(group, null, name, 0)

  final def checkAccess(): Unit = ()

  override final def clone(): AnyRef =
    throw new CloneNotSupportedException("Thread.clone() is not meaningful")

  @deprecated
  def countStackFrames: Int =
    if (suspendState == internalSuspended) {
      getStackTrace.length
    } else {
      throw new IllegalThreadStateException()
    }

  final def getName: String = name

  final def getPriority: Int = priority

  final def getThreadGroup: ThreadGroup = group

  def getId: scala.Long = threadId

  def interrupt(): Unit = {
    checkAccess()
    livenessState.compareAndSwapStrong(internalStarting, internalInterrupted)
    livenessState.compareAndSwapStrong(internalStarted, internalInterrupted)
    sleepMutex.synchronized {
      sleepMutex.notify()
    }
  }

  final def isAlive: scala.Boolean = {
    val value = livenessState.load()
    value == internalStarted || value == internalStarting
  }

  final def isDaemon: scala.Boolean = daemon

  def isInterrupted: scala.Boolean = {
    val value = livenessState.load()
    value == internalInterrupted || value == internalInterruptedTerminated
  }

  final def join(): Unit = {
    if (isAlive) {
      joinMutex.synchronized {
        while (isAlive) joinMutex.wait()
      }
    }
  }

  final def join(ml: scala.Long): Unit = {
    var millis: scala.Long = ml
    if (millis == 0)
      join()
    else {
      val end: scala.Long         = System.currentTimeMillis() + millis
      var continue: scala.Boolean = true
      while (isAlive && continue) {
        joinMutex.synchronized {
          if (isAlive) {
            joinMutex.wait(millis)
          }
        }
        millis = end - System.currentTimeMillis()
        if (millis <= 0)
          continue = false
      }
    }
  }

  final def join(ml: scala.Long, n: Int): Unit = {
    var nanos: Int         = n
    var millis: scala.Long = ml
    if (millis < 0 || nanos < 0 || nanos > 999999)
      throw new IllegalArgumentException()
    else if (millis == 0 && nanos == 0)
      join()
    else {
      val end: scala.Long         = System.nanoTime() + 1000000 * millis + nanos.toLong
      var rest: scala.Long        = 0L
      var continue: scala.Boolean = true
      while (isAlive && continue) {
        joinMutex.synchronized {
          if (isAlive) {
            joinMutex.wait(millis, nanos)
          }
        }
        rest = end - System.nanoTime()
        if (rest <= 0)
          continue = false
        if (continue) {
          nanos = (rest % 1000000).toInt
          millis = rest / 1000000
        }
      }
    }
  }

  def run(): Unit = {
    if (target != null) {
      target.run()
    }
  }

  private var stackTraceTs = 0L
  // not initializing to empty to no trigger System class initialization
  private var lastStackTrace: Array[StackTraceElement] =
    new Array[StackTraceElement](0)
  def getStackTrace: Array[StackTraceElement] = {
    if (this == Thread.currentThread()) {
      lastStackTrace = new Throwable().getStackTrace
      joinMutex.synchronized {
        stackTraceTs += 1
        joinMutex.notifyAll()
      }
    } else {
      val oldTs = stackTraceTs
      pthread_kill(underlying, currentThreadStackTraceSignal)
      joinMutex.synchronized {
        while (stackTraceTs <= oldTs && isAlive) {
          // trigger getStackTrace on that thread
          joinMutex.wait()
        }
      }
    }
    lastStackTrace
  }

  private def classLoadersNotSupported =
    throw new NotImplementedError("Custom class loaders not supported")
  @deprecated
  def setContextClassLoader(classLoader: ClassLoader): Unit =
    classLoadersNotSupported
  @deprecated
  def getContextClassLoader: ClassLoader = classLoadersNotSupported

  final def setDaemon(daemon: scala.Boolean): Unit = {
    checkAccess()
    if (livenessState.load() != internalNew)
      throw new IllegalThreadStateException()
    // There is a chance to set the even if the thread is already started.
    // However, it is just a boolean variable, that can be safely changed
    // even when the thread has started.
    this.daemon = daemon
  }

  final def setName(name: String): Unit = {
    checkAccess()
    this.name = name.toString
  }

  final def setPriority(priority: Int): Unit = {
    checkAccess()
    if (priority > Thread.MAX_PRIORITY || priority < Thread.MIN_PRIORITY)
      throw new IllegalArgumentException("Wrong Thread priority value")
    val groupLocal = group
    if (groupLocal != null) {
      val maxPriorityInGroup = groupLocal.getMaxPriority
      // min(priority,maxPriorityInGroup)
      this.priority =
        if (priority > maxPriorityInGroup) maxPriorityInGroup else priority
      if (isAlive) {
        NativeThread.setPriority(underlying, Thread.toNativePriority(priority))
      }
    }
  }

  def start(): Unit = {
    if (!livenessState.compareAndSwapStrong(internalNew, internalStarting)._1) {
      //this thread was started
      throw new IllegalThreadStateException("This thread was already started!")
    }
    // adding the thread to the thread group
    group.add(this)

    val id = stackalloc[pthread_t]
    // pthread_attr_t is a struct, not a ULong
    val attrs = stackalloc[scala.Byte](pthread_attr_t_size)
      .asInstanceOf[Ptr[pthread_attr_t]]
    pthread_attr_init(attrs)
    NativeThread.attrSetPriority(attrs, Thread.toNativePriority(priority))
    if (stackSize > 0) {
      pthread_attr_setstacksize(attrs, stackSize)
    }

    val status =
      pthread_create(id, attrs, callRunRoutine, this.cast[Ptr[scala.Byte]])
    if (status != 0)
      throw new Exception(
        "Failed to create new thread, pthread error " + status)

    underlying = !id

  }

  def getState: State = {
    val value = livenessState.load()
    if (value == internalNew) {
      State.NEW
    } else if (value == internalStarting) {
      State.RUNNABLE
    } else if (value == internalStarted) {
      val lockState = getLockState
      if (lockState == ThreadBase.Blocked) {
        State.BLOCKED
      } else if (lockState == ThreadBase.Waiting) {
        State.WAITING
      } else if (lockState == ThreadBase.TimedWaiting) {
        State.TIMED_WAITING
      } else {
        State.RUNNABLE
      }
    } else {
      State.TERMINATED
    }
  }
  @deprecated
  def destroy(): Unit = stop()

  @deprecated
  final def stop(): Unit = stop(new ThreadDeath())

  @deprecated
  final def stop(throwable: Throwable): Unit = {
    if (throwable == null)
      throw new NullPointerException("The argument is null!")

    val shouldTerminate = joinMutex.synchronized {
      val terminated = livenessState
        .compareAndSwapStrong(internalStarted, internalTerminated)
        ._1
      val interruptedTerminated = livenessState
        .compareAndSwapStrong(internalInterrupted,
                              internalInterruptedTerminated)
        ._1
      group.remove(this)
      notifyAll()
      terminated || interruptedTerminated
    }

    if (shouldTerminate) {
      val status: Int = pthread_cancel(underlying)
      if (status != 0) {
        throw new InternalError("Pthread error " + status)
      }
    }

  }

  @deprecated
  final def suspend(): Unit = {
    checkAccess()
    if (this == Thread.currentThread()) {
      if (suspendState == internalSuspending) {
        suspendMutex.synchronized {
          if (suspendState == internalSuspending) {
            suspendState = internalSuspended
            while (suspendState == internalSuspended) {
              suspendMutex.wait()
            }
            suspendState = internalNotSuspended
          }
        }
      }
    } else {
      @inline
      def goodToSuspend =
        suspendState == internalNotSuspended ||
          suspendState == internalResuming
      if (goodToSuspend) {
        suspendMutex.synchronized {
          if (goodToSuspend) {
            suspendState = internalSuspending
            pthread_kill(underlying, suspendSignal)
          }
        }
      }
    }
  }

  @deprecated
  final def resume(): Unit = {
    checkAccess()
    suspendMutex.synchronized {
      suspendState = internalResuming
      suspendMutex.notifyAll()
    }
  }

  override def toString: String = {
    val threadGroup: ThreadGroup = group
    val s: String                = if (threadGroup == null) "" else threadGroup.name
    "Thread[" + name + "," + priority + "," + s + "]"
  }

  def getUncaughtExceptionHandler: Thread.UncaughtExceptionHandler = {
    if (exceptionHandler != null)
      return exceptionHandler
    getThreadGroup
  }

  def setUncaughtExceptionHandler(eh: Thread.UncaughtExceptionHandler): Unit =
    exceptionHandler = eh

  def threadModuleBase = Thread
}

object Thread extends scala.scalanative.runtime.ThreadModuleBase {

  val myThreadKey: pthread_key_t = {
    val ptr = stackalloc[pthread_key_t]
    pthread_key_create(ptr, null)
    !ptr
  }

  // defined as Ptr[Void] => Ptr[Void]
  // called as Ptr[Thread] => Ptr[Void]
  private def callRun(p: Ptr[scala.Byte]): Ptr[scala.Byte] = {
    val thread = p.cast[Thread]
    pthread_setspecific(myThreadKey, p)
    if (thread.underlying == 0L.asInstanceOf[ULong]) {
      // the parent hasn't set the underlying thread id yet
      // make sure it is initialized
      thread.underlying = pthread_self()
    }
    thread.livenessState
      .compareAndSwapStrong(internalStarting, internalStarted)
    try {
      thread.run()
    } catch {
      case e: Throwable =>
        thread.getUncaughtExceptionHandler.uncaughtException(thread, e)
    } finally {
      post(thread)
    }

    null.asInstanceOf[Ptr[scala.Byte]]
  }

  private def post(thread: Thread) = {
    shutdownMutex.safeSynchronized {
      thread.group.remove(thread)
      shutdownMutex.notifyAll()
    }
    // workaround release all locks that might be kept after exception
    thread.asInstanceOf[ThreadBase].freeAllLocks()
    thread.joinMutex.synchronized {
      thread.livenessState
        .compareAndSwapStrong(internalStarted, internalTerminated)
      thread.livenessState
        .compareAndSwapStrong(internalInterrupted,
                              internalInterruptedTerminated)
      thread.joinMutex.notifyAll()
    }
    pthread_setspecific(myThreadKey, null.asInstanceOf[Ptr[scala.Byte]])
  }

  private val callRunRoutine = CFunctionPtr.fromFunction1(callRun)

  // internal liveness state values
  // waiting and blocked handled separately
  private final val internalNew                   = 0
  private final val internalStarting              = 1
  private final val internalStarted               = 2
  private final val internalInterrupted           = 3
  private final val internalTerminated            = 4
  private final val internalInterruptedTerminated = 5

  // seperate states for suspending
  private final val internalNotSuspended = 0
  private final val internalSuspending   = 1
  private final val internalSuspended    = 2
  private final val internalResuming     = 3

  // for compatibility match Java Enums as close as possible
  final class State private (override val toString: String)

  object State {
    final val NEW           = new State("NEW")
    final val RUNNABLE      = new State("RUNNABLE")
    final val BLOCKED       = new State("BLOCKED")
    final val WAITING       = new State("WAITING")
    final val TIMED_WAITING = new State("TIMED_WAITING")
    final val TERMINATED    = new State("TERMINATED")
  }

  final val MAX_PRIORITY: Int  = 10
  final val MIN_PRIORITY: Int  = 1
  final val NORM_PRIORITY: Int = 5

  private def toNativePriority(priority: Int) = {
    val range = NativeThread.THREAD_MAX_PRIORITY - NativeThread.THREAD_MIN_PRIORITY
    if (range == 0) {
      NativeThread.THREAD_MAX_PRIORITY
    } else {
      priority * range / 10
    }
  }

  private final val STACK_TRACE_INDENT: String = "    "

  private var defaultExceptionHandler: UncaughtExceptionHandler = _

  // Counter used to generate thread's ID
  private val threadOrdinalNum = CAtomicLong(0L)

  // used to generate a default thread name
  private final val THREAD: String = "Thread-"

  def activeCount: Int = currentThread().group.activeCount()

  def currentThreadInternal(): Thread with ThreadBase =
    pthread_getspecific(myThreadKey)
      .cast[Thread]
      .asInstanceOf[Thread with ThreadBase]

  def currentThread(): Thread = {
    val value = currentThreadInternal()
    if (value != null) {
      value
    } else {
      if (mainThread.underlying == 0L.asInstanceOf[ULong]) {
        // main thread uninitialized, so it must be the only thread
        mainThread
      } else {
        // the thread is handling a signal before it has initialized
        findSelf(pthread_self(), mainThreadGroup).getOrElse {
          throw new IllegalThreadStateException(
            "Unknown thread. Is it a non-Scala thread?")
        }
      }
    }
  }

  private def findSelf(id: pthread_t, group: ThreadGroup): Option[Thread] = {
    val threads: scala.collection.immutable.List[Thread]     = group.threads
    val groups: scala.collection.immutable.List[ThreadGroup] = group.groups
    threads.find(_.underlying == id).orElse {
      val it: scala.collection.Iterator[Option[Thread]] =
        groups.iterator.map(g => findSelf(id, g))
      it.find(_.isDefined).flatten
    }
  }

  def dumpStack(): Unit = {
    val stack: Array[StackTraceElement] = new Throwable().getStackTrace
    System.err.println("Stack trace")
    var i: Int = 0
    while (i < stack.length) {
      System.err.println(STACK_TRACE_INDENT + stack(i))
      i += 1
    }
  }

  def enumerate(list: Array[Thread]): Int =
    currentThread().getThreadGroup.enumerateThreads(list, 0, true)

  def holdsLock(obj: Object): scala.Boolean =
    currentThread().asInstanceOf[ThreadBase].holdsLock(obj)

  def `yield`(): Unit = sched_yield()

  def getAllStackTraces: java.util.Map[Thread, Array[StackTraceElement]] = {
    var threadsCount: Int          = mainThreadGroup.activeCount() + 1
    var count: Int                 = 0
    var liveThreads: Array[Thread] = Array.empty
    var break: scala.Boolean       = false
    while (!break) {
      liveThreads = new Array[Thread](threadsCount)
      count = mainThreadGroup.enumerateThreads(liveThreads, 0, recurse = true)
      if (count == threadsCount) {
        threadsCount *= 2
      } else
        break = true
    }

    val map: java.util.Map[Thread, Array[StackTraceElement]] =
      new util.HashMap[Thread, Array[StackTraceElement]](count + 1)
    var i: Int = 0
    while (i < count) {
      val ste: Array[StackTraceElement] = liveThreads(i).getStackTrace
      if (ste.length != 0)
        map.put(liveThreads(i), ste)
      i += 1
    }

    map
  }

  def getDefaultUncaughtExceptionHandler: UncaughtExceptionHandler =
    defaultExceptionHandler

  def setDefaultUncaughtExceptionHandler(eh: UncaughtExceptionHandler): Unit =
    defaultExceptionHandler = eh

  private def getNextThreadId: scala.Long = threadOrdinalNum.addFetch(1)

  def interrupted(): scala.Boolean = {
    val current = currentThread()
    if (current.livenessState.load() == internalInterrupted) {
      current.livenessState
        .compareAndSwapStrong(internalInterrupted, internalStarted)
        ._1
    } else {
      false
    }
  }

  def sleep(millis: scala.Long, nanos: scala.Int): Unit = {
    val sleepMutex: Object = currentThread().sleepMutex
    sleepMutex.synchronized {
      sleepMutex.wait(millis, nanos)
    }
    if (interrupted()) {
      throw new InterruptedException("Interrupted during sleep")
    }
  }

  def sleep(millis: scala.Long): Unit = sleep(millis, 0)

  trait UncaughtExceptionHandler {
    def uncaughtException(t: Thread, e: Throwable)
  }

  private val mainThreadGroup: ThreadGroup =
    new ThreadGroup(parent = null, name = "system", mainGroup = true)

  private val mainThread = new Thread(parentThread = null,
                                      mainThreadGroup,
                                      target = null,
                                      "main",
                                      0,
                                      mainThread = true)

  private def currentThreadStackTrace(signal: CInt): Unit =
    try {
      currentThread().getStackTrace
    } catch {
      case t: Throwable => t.printStackTrace()
    }

  private val currentThreadStackTracePtr =
    CFunctionPtr.fromFunction1(currentThreadStackTrace _)
  private val currentThreadStackTraceSignal = signal.SIGRTMIN + 7
  signal.signal(currentThreadStackTraceSignal, currentThreadStackTracePtr)

  private def currentThreadSuspend(signal: CInt): Unit =
    try {
      currentThread().suspend()
    } catch {
      case t: Throwable => t.printStackTrace()
    }

  private val currentThreadSuspendPtr =
    CFunctionPtr.fromFunction1(currentThreadSuspend _)
  private val suspendSignal = signal.SIGRTMIN + 8
  signal.signal(suspendSignal, currentThreadSuspendPtr)

  def mainThreadEnds(): Unit = post(mainThread)

  private val shutdownMutex = new ShadowLock

  def shutdownCheckLoop(): Unit = {
    if (mainThreadGroup.nonDaemonThreadExists) {
      shutdownMutex.synchronized {
        while (mainThreadGroup.nonDaemonThreadExists) {
          shutdownMutex.wait()
        }
      }
    }
  }

  def initMainThread(): Unit = mainThread.initMainThread()
}
