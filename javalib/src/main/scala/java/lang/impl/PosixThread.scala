package java.lang.impl

import scala.annotation._
import scala.scalanative.annotation._

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.runtime._
import scala.scalanative.runtime.Intrinsics.{elemRawPtr, classFieldRawPtr}
import scala.scalanative.runtime.GC
import scala.scalanative.meta.LinktimeInfo._

import scala.scalanative.posix.sys.types._
import scala.scalanative.posix.time._
import scala.scalanative.posix.timeOps._
import scala.scalanative.posix.sched._
import scala.scalanative.posix.schedOps._
import scala.scalanative.posix.pthread._
import scala.scalanative.posix.errno._
import scala.scalanative.posix.poll._
import scala.scalanative.posix.unistd._
import scala.scalanative.libc.atomic._
import scala.scalanative.libc.atomic.memory_order.memory_order_seq_cst

private[java] class PosixThread(val thread: Thread, stackSize: Long)
    extends NativeThread {
  import NativeThread._
  import PosixThread._

  private[this] val _state = new scala.Array[scala.Byte](StateSize)
  @volatile private[impl] var sleepInterruptEvent: CInt = UnsetEvent
  @volatile private var counter: Int = 0
  // index of currently used condition
  @volatile private var conditionIdx = ConditionUnset

  private val handle: pthread_t =
    if (isMainThread) 0.toUSize // main thread
    else if (!isMultithreadingEnabled)
      throw new LinkageError(
        "Multithreading support disabled - cannot create new threads"
      )
    else {
      val id = stackalloc[pthread_t]()
      val attrs = stackalloc[Byte](pthread_attr_t_size)
        .asInstanceOf[Ptr[pthread_attr_t]]

      checkStatus("mutex init") {
        pthread_mutex_init(lock, mutexAttr)
      }
      checkStatus("relative time condition init") {
        pthread_cond_init(
          condition(ConditionRelativeIdx),
          conditionRelativeCondAttr
        )
      }
      checkStatus("absolute time condition init") {
        pthread_cond_init(condition(ConditionAbsoluteIdx), null)
      }
      checkStatus("thread attrs init") {
        pthread_attr_init(attrs)
      }
      try {
        checkStatus("thread attrs - set detach") {
          pthread_attr_setdetachstate(attrs, PTHREAD_CREATE_DETACHED)
        }
        if (stackSize > 0L) {
          checkStatus("thread attrs - set stack size") {
            pthread_attr_setstacksize(attrs, stackSize.toUInt)
          }
        }
        checkStatus("thread create") {
          GC.pthread_create(
            thread = id,
            attr = attrs,
            startroutine = NativeThread.threadRoutine,
            args = NativeThread.threadRoutineArgs(this)
          )
        }
        !id
      } finally if (attrs != null) pthread_attr_destroy(attrs)
    }

  override def onTermination(): Unit = {
    super.onTermination()
    if (isMultithreadingEnabled) {
      pthread_cond_destroy(condition(0))
      pthread_cond_destroy(condition(1))
      pthread_mutex_destroy(lock)
    }
  }

  override def setPriority(
      priority: CInt
  ): Unit = if (isMultithreadingEnabled) {
    val schedParam = stackalloc[sched_param]()
    val policy = stackalloc[CInt]()
    if (0 == pthread_getschedparam(handle, policy, schedParam)) {
      schedParam.priority = priorityMapping(priority, !policy)
      pthread_setschedparam(handle, !policy, schedParam)
    }
  }

  override def interrupt(): Unit = if (isMultithreadingEnabled) {
    // for LockSupport.park
    this.unpark()
    // for Thread.sleep
    if (sleepInterruptEvent != UnsetEvent) {
      val eventSize = 8.toUInt
      val buf = stackalloc[Byte](eventSize)
      !buf = 1
      write(sleepInterruptEvent, buf, eventSize)
    }
  }

  override protected def park(
      time: Long,
      isAbsolute: Boolean
  ): Unit = if (isMultithreadingEnabled) {
    // fast-path check, return if can skip parking
    if (counterAtomic.exchange(0) > 0) return
    // Avoid parking if there's an interrupt pending
    if (thread.isInterrupted()) return
    // Don't wait at all
    if (time < 0 || (isAbsolute && time == 0)) return
    val absTime = stackalloc[timespec]()
    if (time > 0) toAbsoluteTime(absTime, time, isAbsolute)
    // Interference with ongoing unpark
    if (pthread_mutex_trylock(lock) != 0) return

    try {
      if (counter > 0) { // no wait needed
        counter = 0
        return
      }

      assert(conditionIdx == ConditionUnset, "conditiond idx")
      if (time == 0) {
        conditionIdx = ConditionRelativeIdx
        state = NativeThread.State.ParkedWaiting
        val status = pthread_cond_wait(condition(conditionIdx), lock)
        assert(
          status == 0 ||
            (scalanative.runtime.Platform.isMac() && status == ETIMEDOUT),
          "park, wait"
        )
      } else {
        conditionIdx =
          if (isAbsolute) ConditionAbsoluteIdx else ConditionRelativeIdx
        state = NativeThread.State.ParkedWaitingTimed
        val status =
          pthread_cond_timedwait(condition(conditionIdx), lock, absTime)
        assert(status == 0 || status == ETIMEDOUT, "park, timed-wait")
      }

      conditionIdx = ConditionUnset
      counter = 0
    } finally {
      state = NativeThread.State.Running
      val status = pthread_mutex_unlock(lock)
      assert(status == 0, "park, unlock")
      atomic_thread_fence(memory_order_seq_cst)
    }
  }

  override def unpark(): Unit = if (isMultithreadingEnabled) {
    pthread_mutex_lock(lock)
    val s = counter
    counter = 1
    val index = conditionIdx
    pthread_mutex_unlock(lock)

    if (s < 1 && index != ConditionUnset) {
      pthread_cond_signal(condition(index))
    }
  }

  override def sleep(millis: Long): Unit =
    if (isMultithreadingEnabled) sleepInterruptible(millis)
    else sleepNonInterruptible(millis, 0)

  private def sleepInterruptible(_millis: Long): Unit = {
    var millis = _millis
    if (millis <= 0) return
    val deadline = System.currentTimeMillis() + millis

    import scala.scalanative.posix.pollOps._
    import scala.scalanative.posix.pollEvents._

    type PipeFDs = CArray[CInt, Nat._2]
    val pipefd = stackalloc[PipeFDs](1.toUInt)
    checkStatus("create sleep interrupt event") {
      pipe(pipefd.at(0))
    }
    this.sleepInterruptEvent = !pipefd.at(1)
    if (!thread.isInterrupted()) try {
      val fds = stackalloc[struct_pollfd]()
      fds.fd = !pipefd.at(0)
      fds.events = POLLIN

      try
        while (millis > 0) {
          state = State.ParkedWaitingTimed
          poll(fds, 1.toUInt, (millis min Int.MaxValue).toInt)
          state = State.Running
          if (Thread.interrupted()) throw new InterruptedException()

          millis = deadline - System.currentTimeMillis()
        }
      finally this.sleepInterruptEvent = UnsetEvent
    } finally {
      close(!pipefd.at(0))
      close(!pipefd.at(1))
    }
  }

  private def sleepNonInterruptible(
      millis: scala.Long,
      nanos: scala.Int
  ): Unit = {
    @tailrec def doSleep(requestedTime: Ptr[timespec]): Unit = {
      val remaining = stackalloc[timespec]()
      val status = nanosleep(requestedTime, remaining)
      if (!thread.isInterrupted()) {
        if (status == -1 && errno == EINTR)
          doSleep(remaining)
      }
    }
    val requestedTime = stackalloc[timespec]()
    requestedTime.tv_sec = (millis / 1000).toSize
    requestedTime.tv_nsec = ((millis % 1000) * 1e6.toInt + nanos).toSize
    state = State.ParkedWaitingTimed
    doSleep(requestedTime)
    state = State.Running
  }

  override def sleepNanos(nanos: Int): Unit = {
    val millis = nanos / NanosInMillisecond
    val remainingNanos = nanos % NanosInMillisecond
    if (millis > 0) sleepInterruptible(millis)
    if (!thread.isInterrupted() && remainingNanos > 0) {
      sleepNonInterruptible(0, nanos)
    }
  }

  @alwaysinline private def lock: Ptr[pthread_mutex_t] = _state
    .at(LockOffset)
    .asInstanceOf[Ptr[pthread_mutex_t]]

  @alwaysinline private def conditions =
    _state
      .at(ConditionsOffset)
      .asInstanceOf[Ptr[pthread_cond_t]]

  @alwaysinline private def condition(idx: Int): Ptr[pthread_cond_t] =
    (idx: @switch) match {
      case 0 => conditions
      case 1 =>
        val base = toRawPtr(conditions)
        val offset = toRawSize(pthread_cond_t_size)
        fromRawPtr(elemRawPtr(base, offset))
    }

  @alwaysinline private def counterAtomic = new CAtomicInt(
    fromRawPtr(classFieldRawPtr(this, "counter"))
  )

  @inline private def priorityMapping(
      threadPriority: Int,
      schedulerPolicy: CInt
  ): Int = {
    val minPriority = sched_get_priority_min(schedulerPolicy)
    val maxPriority = sched_get_priority_max(schedulerPolicy)
    assert(
      minPriority >= 0 && maxPriority >= 0,
      "Failed to resolve priority range"
    )
    val priorityRange = maxPriority - minPriority
    val javaPriorityRange = Thread.MAX_PRIORITY - Thread.MIN_PRIORITY
    val priority =
      (((threadPriority - Thread.MIN_PRIORITY) * priorityRange) / javaPriorityRange) + minPriority
    assert(
      priority >= minPriority && priority <= maxPriority,
      "priority out of range"
    )
    priority
  }

  private def toAbsoluteTime(
      abstime: Ptr[timespec],
      _timeout: Long,
      isAbsolute: Boolean
  ) = {
    val timeout = if (_timeout < 0) 0 else _timeout
    val clock =
      if (isAbsolute || !PosixThread.usesClockMonotonicCondAttr) CLOCK_REALTIME
      else CLOCK_MONOTONIC
    val now = stackalloc[timespec]()
    clock_gettime(clock, now)
    if (isAbsolute) unpackAbsoluteTime(abstime, timeout, now.tv_sec.toLong)
    else calculateRelativeTime(abstime, timeout, now)
  }

  private def calculateRelativeTime(
      abstime: Ptr[timespec],
      timeout: Long,
      now: Ptr[timespec]
  ) = {
    val maxSeconds = now.tv_sec.toLong + MaxSeconds
    val seconds = timeout / NanonsInSecond
    if (seconds > maxSeconds) {
      abstime.tv_sec = maxSeconds.toSize
      abstime.tv_nsec = 0
    } else {
      abstime.tv_sec = now.tv_sec + seconds.toSize
      val nanos = now.tv_nsec + (timeout % NanonsInSecond)
      abstime.tv_nsec =
        if (nanos < NanonsInSecond) nanos.toSize
        else {
          abstime.tv_sec += 1
          (nanos - NanonsInSecond).toSize
        }
    }
  }

  @alwaysinline private def MillisInSecond = 1000
  @alwaysinline private def NanosInMillisecond = 1000000
  @alwaysinline private def NanonsInSecond = 1000000000
  @alwaysinline private def MaxSeconds = 100000000

  private def unpackAbsoluteTime(
      abstime: Ptr[timespec],
      deadline: Long,
      nowSeconds: Long
  ) = {
    val maxSeconds = nowSeconds + MaxSeconds
    val seconds = deadline / MillisInSecond
    val millis = deadline % MillisInSecond

    if (seconds >= maxSeconds) {
      abstime.tv_sec = maxSeconds.toSize
      abstime.tv_nsec = 0
    } else {
      abstime.tv_sec = seconds.toSize
      abstime.tv_nsec = (millis * NanosInMillisecond).toSize
    }

    assert(abstime.tv_sec <= maxSeconds, "tvSec")
    assert(abstime.tv_nsec <= NanonsInSecond, "tvNSec")
  }
}

private[lang] object PosixThread extends NativeThread.Companion {
  override type Impl = PosixThread

  private[this] val _state = new scala.Array[scala.Byte](CompanionStateSize)

  if (isMultithreadingEnabled) {
    checkStatus("relative-time conditions attrs init") {
      pthread_condattr_init(conditionRelativeCondAttr)
    }
    checkStatus("mutex attributes - init") {
      pthread_mutexattr_init(mutexAttr)
    }
    checkStatus("mutex attributes - set type") {
      pthread_mutexattr_settype(mutexAttr, PTHREAD_MUTEX_NORMAL)
    }
  }

  // MacOS does not define `pthread_condattr_setclock`, use realtime (default) clocks instead
  val usesClockMonotonicCondAttr =
    if (isMac || isFreeBSD) false
    else {
      if (isMultithreadingEnabled) {
        checkStatus("relative-time conditions attrs - set clock") {
          pthread_condattr_setclock(conditionRelativeCondAttr, CLOCK_MONOTONIC)
        }
      }
      true
    }

  @alwaysinline def conditionRelativeCondAttr = _state
    .at(ConditionRelativeAttrOffset)
    .asInstanceOf[Ptr[pthread_condattr_t]]

  @alwaysinline def mutexAttr =
    _state
      .at(MutexAttrOffset)
      .asInstanceOf[Ptr[pthread_mutexattr_t]]

  @alwaysinline private def UnsetEvent = -1

  @alwaysinline def create(thread: Thread, stackSize: Long): PosixThread =
    new PosixThread(thread, stackSize)

  @alwaysinline def yieldThread(): Unit = sched_yield()

  // PosixThread class state
  @alwaysinline private def LockOffset = 0
  @alwaysinline private def ConditionsOffset = pthread_mutex_t_size.toInt
  @alwaysinline private def ConditionUnset = -1
  @alwaysinline private def ConditionRelativeIdx = 0
  @alwaysinline private def ConditionAbsoluteIdx = 1
  private def StateSize =
    (pthread_mutex_t_size + pthread_cond_t_size * 2.toUInt).toInt

  // PosixThread companion class state
  @alwaysinline private def ConditionRelativeAttrOffset = 0
  @alwaysinline private def MutexAttrOffset = pthread_condattr_t_size.toInt
  def CompanionStateSize =
    (pthread_condattr_t_size + pthread_mutexattr_t_size).toInt

  @alwaysinline private def checkStatus(
      label: => String,
      expectedStatus: CInt = 0
  )(status: CInt) = {
    if (status != expectedStatus)
      throw new RuntimeException(
        s"Cannot initialize thread: $label, status=$status"
      )
  }
}
