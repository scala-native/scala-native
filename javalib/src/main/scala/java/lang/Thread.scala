package java.lang

import java.util
import java.lang.Thread._

import scala.scalanative.native.{
CFunctionPtr,
CFunctionPtr1,
CInt,
Ptr,
stackalloc
}
import scala.scalanative.posix.sys.types.{pthread_attr_t, pthread_t}
import scala.scalanative.posix.pthread._
import scala.scalanative.posix.sched._

// Ported from Harmony

class Thread extends Runnable {

  //import java.lang.Thread._

  private var interruptedState = false

  // Thread's name
  private[this] var name: String = "main" // default name of the main thread

  // This thread's thread group
  var group: ThreadGroup = _

  // This thread's context class loader
  private var contextClassLoader: ClassLoader = _

  // Indicates whether this thread was marked as daemon
  private var daemon: scala.Boolean = false

  // Thread's priority
  private var priority: Int = _

  // Stack size to be passes to VM for thread execution //TODO ???
  private var stackSize: scala.Long = 0L

  // Indicates if the thread was already started
  var started: scala.Boolean = false

  // Indicates if the thread is alive
  // Note : this was originally named 'isAlive' in Harmony but
  // conflicted with the 'isAlive' method
  var alive: scala.Boolean = false

  // Thread's target - a Runnable object whose run method should be invoked
  private var target: Runnable = _

  // Uncaught exception handler for this thread
  private var exceptionHandler: Thread.UncaughtExceptionHandler = _

  // Thread's ID
  private var threadId: scala.Long = _

  // The underlying pthread ID
  private var underlying: Option[pthread_t] = None

  // Synchronization is done using internal lock
  val lock: Object = new Object()

  // ThreadLocal values : local and inheritable
  var localValues: ThreadLocal.Values = _

  var inheritableValues: ThreadLocal.Values = _

  def this(group: ThreadGroup,
           target: Runnable,
           name: String,
           stacksize: scala.Long) = {
    this()
    val currentThread: Thread = Thread.currentThread()

    var threadGroup: ThreadGroup = null
    if (group != null) {
      threadGroup = group
    } else if (threadGroup == null)
      threadGroup = currentThread.group

    threadGroup.checkGroup()

    this.group = threadGroup
    this.daemon = currentThread.daemon
    this.contextClassLoader = currentThread.contextClassLoader
    this.target = target
    this.stackSize = stacksize
    this.priority = currentThread.priority
    this.threadId = getNextThreadId
    // throws NullPointerException if the given name is null
    this.name = if (name != THREAD) name.toString else THREAD + threadId

    checkGCWatermark()
    checkAccess()

    val parent: Thread = currentThread
    if (parent != null && parent.inheritableValues != null)
      inheritableValues = new ThreadLocal.Values(parent.inheritableValues)
  }

  def this(target: Runnable) = this(null, target, THREAD, 0)

  def this(target: Runnable, name: String) = this(null, target, name, 0)

  def this(name: String) = this(null, null, name, 0)

  def this(group: ThreadGroup, target: Runnable) =
    this(group, target, THREAD, 0)

  def this(group: ThreadGroup, target: Runnable, name: String) =
    this(group, target, name, 0)

  def this(gp: ThreadGroup,
           name: String,
           nativeAddr: scala.Long,
           stackSize: scala.Long,
           priority: Int,
           daemon: scala.Boolean) = {
    this()
    val contextLoader: ClassLoader = null

    var group: ThreadGroup = gp

    if (group == null) {
      if (systemThreadGroup == null) {
        // This is main thread
        systemThreadGroup = new ThreadGroup()
        mainThreadGroup = new ThreadGroup(systemThreadGroup, "main")
        group = mainThreadGroup
      } else {
        group = mainThreadGroup
      }
    }

    this.group = group
    this.stackSize = stackSize
    this.priority = priority
    this.daemon = daemon
    this.threadId = getNextThreadId
    this.name = if (name != null) name else THREAD + threadId
    // Each thread created from JNI has bootstrap class loader as
    // its context class loader. The only exception is the main thread
    // which has system class loader as its context class loader.
    this.contextClassLoader = contextLoader
    this.target = null
    // The thread is actually running
    this.alive = true
    this.started = true

    // adding the thread to the thread group should be the last action
    group.add(this)

    val parent: Thread = Thread.currentThread()
    if (parent != null && parent.inheritableValues != null) {
      inheritableValues = new ThreadLocal.Values(parent.inheritableValues)
    }
  }

  def this(group: ThreadGroup, name: String) = this(group, null, name, 0)

  final def checkAccess(): Unit = ()

  @deprecated
  def countStackFrames: Int = 0 //deprecated

  @deprecated
  def destroy(): Unit =
  // this method is not implemented
    throw new NoSuchMethodError()

  def getContextClassLoader: ClassLoader =
    lock.synchronized(contextClassLoader)

  final def getName: String = name

  final def getPriority: Int = priority

  def getStackTrace: Array[StackTraceElement] = {
    /* // TODO a revoir
    val ste: Array[StackTraceElement] = VMStack.getThreadStackTrace(this)
    if(ste != null) ste else */
    new Array[StackTraceElement](0)
  }

  final def getThreadGroup: ThreadGroup = group

  def getId: scala.Long = threadId

  def getPthreadId: pthread_t = {
    if (started && underlying.isDefined)
      underlying.get
    else throw new NullPointerException("Thread isn't started yet")
  }

  def interrupt(): Unit = {
    lock.synchronized {
      checkAccess()
      val status: Int =
        if (underlying.isDefined) pthread_cancel(underlying.get) else 0
      if (status != 0)
        throw new InternalError("Pthread error " + status)
    }
  }

  final def isAlive: scala.Boolean = lock.synchronized(alive)

  final def isDaemon: scala.Boolean = daemon

  // TODO check
  def isInterrupted: scala.Boolean = interruptedState

  //synchronized
  final def join(): Unit = {
    while (isAlive) wait()
  }

  // synchronized
  final def join(ml: scala.Long): Unit = {
    var millis: scala.Long = ml
    if (millis == 0)
      join()
    else {
      val end: scala.Long         = System.currentTimeMillis() + millis
      var continue: scala.Boolean = true
      while (isAlive && continue) {
        wait(millis)
        millis = end - System.currentTimeMillis()
        if (millis <= 0)
          continue = false
      }
    }
  }

  //synchronized
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
        wait(millis, nanos)
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

  @deprecated
  final def resume(): Unit = {
    /*checkAccess()
    val status: Int = VMThreadManager.resume(this)
    if(status != VMThreadManager.TM_ERROR_NONE)
      throw new InternalError("Thread Manager internal error " + status)*/
  }

  private def toCRoutine(
                          f: => (() => Unit)): (Ptr[scala.Byte]) => Ptr[scala.Byte] = {
    def g(ptr: Ptr[scala.Byte]) = {
      f()
      null.asInstanceOf[Ptr[scala.Byte]]
    }
    g
  }

  def run(): Unit = {
    if (target != null) {
      target.run()
    }
  }

  def setContextClassLoader(classLoader: ClassLoader): Unit =
    lock.synchronized(contextClassLoader = classLoader)

  final def setDaemon(daemon: scala.Boolean): Unit = {
    lock.synchronized {
      checkAccess()
      if (isAlive)
        throw new IllegalThreadStateException()
      this.daemon = daemon
    }
  }

  final def setName(name: String): Unit = {
    checkAccess()
    // throws NullPointerException if the given name is null
    this.name = name.toString
  }

  final def setPriority(priority: Int): Unit = {
    checkAccess()
    if (priority > Thread.MAX_PRIORITY || priority < Thread.MIN_PRIORITY)
      throw new IllegalArgumentException("Wrong Thread priority value")
    val threadGroup: ThreadGroup = group
    this.priority =
      if (priority > threadGroup.maxPriority) threadGroup.maxPriority
      else priority
    if (underlying.isDefined) {
      val param: Ptr[sched_param] = stackalloc[sched_param]
      val policy: Ptr[CInt]       = stackalloc[CInt]
      pthread_getschedparam(underlying.get, policy, param)
      !param._1 = priority
      pthread_setschedparam(underlying.get, !policy, param)
    }
  }

  //synchronized
  def start(): Unit = { /*
    lock.synchronized{
      if(started)
        //this thread was started
        throw new IllegalThreadStateException("This thread was already started!")
      // adding the thread to the thread group
      group.add(this)

      val a: (Ptr[scala.Byte]) => Ptr[scala.Byte] = toCRoutine(run)

      val routine = CFunctionPtr.fromFunction1(a)

      val id = stackalloc[pthread_t]
      val status = pthread_create(id, null.asInstanceOf[Ptr[pthread_attr_t]],
        routine, null.asInstanceOf[Ptr[scala.Byte]])
      if(status != 0)
        throw new Exception("Failed to create new thread, pthread error " + status)

      started = true
      underlying = Some(!id)

    }*/ }

  type State = CInt

  final val NEW: State           = 0
  final val RUNNABLE: State      = 1
  final val BLOCKED: State       = 2
  final val WAITING: State       = 3
  final val TIMED_WAITING: State = 4
  final val TERMINATED: State    = 5

  def getState: State = {
    RUNNABLE
    /*
    var dead: scala.Boolean = false
    lock.synchronized{
      if(started && !isAlive) dead = true
    }
    if(dead) return TERMINATED

    val state = VMThreadManager.getState(this)

    if(0 != (state & VMThreadManager.TM_THREAD_STATE_TERMINATED)) State.TERMINATED
    else if(0 != (state & VMThreadManager.TM_THREAD_STATE_WAITING_WITH_TIMEOUT)) State.TIMED_WAITING
    else if(0 != (state & VMThreadManager.TM_THREAD_STATE_WAITING)
      || 0 != (state & VMThreadManager.TM_THREAD_STATE_PARKED)) State.WAITING
    else if(0 != (state & VMThreadManager.TM_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER)) State.BLOCKED
    else if(0 != (state & VMThreadManager.TM_THREAD_STATE_RUNNABLE)) State.RUNNABLE

    //TODO track down all situations where a thread is really in RUNNABLE state
    // but TM_THREAD_STATE_RUNNABLE is not set.  In the meantime, leave the following
    // TM_THREAD_STATE_ALIVE test as it is.
    else if(0 != (state & VMThreadManager.TM_THREAD_STATE_ALIVE)) State.RUNNABLE
    else State.NEW
   */
  }

  @deprecated
  final def stop(): Unit = {
    lock.synchronized {
      if (isAlive)
        stop(new ThreadDeath())
    }
  }

  @deprecated
  final def stop(throwable: Throwable): Unit = {
    if (throwable == null)
      throw new NullPointerException("The argument is null!")
    lock.synchronized {
      if (isAlive && underlying.isDefined) {
        val status: Int = pthread_cancel(underlying.get)
        if (status != 0)
          throw new InternalError("Pthread error " + status)
      }
    }
  }

  @deprecated
  final def suspend(): Unit = {}

  override def toString: String = {
    val threadGroup: ThreadGroup = group
    val s: String                = if (threadGroup == null) "" else threadGroup.name
    "Thread[" + name + "," + priority + "," + s + "]"
  }

  private def checkGCWatermark(): Unit = {
    currentGCWatermarkCount += 1
    if (currentGCWatermarkCount % GC_WATERMARK_MAX_COUNT == 0)
      System.gc()
  }

  def getUncaughtExceptionHandler: Thread.UncaughtExceptionHandler = {
    if (exceptionHandler != null)
      return exceptionHandler
    getThreadGroup
  }

  def setUncaughtExceptionHandler(eh: Thread.UncaughtExceptionHandler): Unit =
    exceptionHandler = eh
}

object Thread {

  /*private final val PTHREAD_DEFAULT_ATTR: Ptr[pthread_attr_t] = {
    val attr = stackalloc[pthread_attr_t]
    pthread_attr_init(attr)
    attr
  }

  private final val PTHREAD_DEFAULT_SCHED_PARAM = {
    val param: Ptr[sched_param] = stackalloc[sched_param]
    pthread_attr_getschedparam(PTHREAD_DEFAULT_ATTR, param)
    param
  }

  private final val PTHREAD_DEFAULT_POLICY = {
    val policy = stackalloc[CInt]
    pthread_attr_getschedpolicy(PTHREAD_DEFAULT_ATTR, policy)
  }*/

  final val MAX_PRIORITY: Int = {
    10 //sched_get_priority_max(PTHREAD_DEFAULT_POLICY)
  }

  final val MIN_PRIORITY: Int = {
    0 //sched_get_priority_min(PTHREAD_DEFAULT_POLICY)
  }

  final val NORM_PRIORITY: Int = 5 //!PTHREAD_DEFAULT_SCHED_PARAM._1

  final val STACK_TRACE_INDENT: String = "    "

  // Main thread group
  var mainThreadGroup: ThreadGroup = new ThreadGroup()

  private val MainThread = new Thread()

  MainThread.group = mainThreadGroup

  private val current: ThreadLocal[Thread] = {
    val a = new ThreadLocal[Thread]()
    a.set(MainThread)
    a
  }

  // Default uncaught exception handler
  private var defaultExceptionHandler: UncaughtExceptionHandler = _

  // Counter used to generate thread's ID
  private var threadOrdinalNum: scala.Long = 0L

  // used to generate a default thread name
  private final val THREAD: String = "Thread-"

  // System thread group for keeping helper threads
  var systemThreadGroup: ThreadGroup = _

  // Number of threads that was created w/o garbage collection //TODO
  private var currentGCWatermarkCount: Int = 0

  // Max number of threads to be created w/o GC, required collect dead Thread references
  private final val GC_WATERMARK_MAX_COUNT: Int = 700

  def activeCount: Int = currentThread().group.activeCount()

  def currentThread(): Thread = MainThread

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
    currentThread().group.enumerate(list)

  def holdsLock(obj: Object): scala.Boolean = {
    false
    // TODO
    /*
    if(obj == null)
      throw new NullPointerException()
    VMThreadManager.holdsLock(obj)
   */
  }

  def `yield`(): Unit = {
    //TODO I'm not sure if that's okay
    sched_yield()
  }

  def getAllStackTraces: java.util.Map[Thread, Array[StackTraceElement]] = {
    var parent: ThreadGroup =
      new ThreadGroup(currentThread().getThreadGroup, "Temporary")
    var newParent: ThreadGroup = parent.getParent
    parent.destroy()
    while (newParent != null) {
      parent = newParent
      newParent = parent.getParent
    }
    var threadsCount: Int          = parent.activeCount() + 1
    var count: Int                 = 0
    var liveThreads: Array[Thread] = Array.empty
    var break: scala.Boolean       = false
    while (!break) {
      liveThreads = new Array[Thread](threadsCount)
      count = parent.enumerate(liveThreads)
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

  def setDefaultUncaughtHandler(eh: UncaughtExceptionHandler): Unit =
    defaultExceptionHandler = eh

  //synchronized
  private def getNextThreadId: scala.Long = {
    threadOrdinalNum += 1
    threadOrdinalNum
  }

  def interrupted(): scala.Boolean = {
    val ret = currentThread().isInterrupted
    currentThread().interruptedState = false
    ret
  }

  def sleep(millis: scala.Long, nanos: scala.Int): Unit = {
    import scala.scalanative.posix.errno.EINTR
    import scala.scalanative.native._
    import scala.scalanative.posix.unistd

    def checkErrno() =
      if (errno.errno == EINTR) {
        throw new InterruptedException("Sleep was interrupted")
      }

    if (millis < 0) {
      throw new IllegalArgumentException("millis must be >= 0")
    }
    if (nanos < 0 || nanos > 999999) {
      throw new IllegalArgumentException("nanos value out of range")
    }

    val secs  = millis / 1000
    val usecs = (millis % 1000) * 1000 + nanos / 1000
    if (secs > 0 && unistd.sleep(secs.toUInt) != 0) checkErrno()
    if (usecs > 0 && unistd.usleep(usecs.toUInt) != 0) checkErrno()
  }

  def sleep(millis: scala.Long): Unit = sleep(millis, 0)

  trait UncaughtExceptionHandler {
    def uncaughtException(t: Thread, e: Throwable)
  }

}