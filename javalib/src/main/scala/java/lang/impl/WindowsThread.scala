package java.lang.impl

import scala.scalanative.annotation._
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import scala.scalanative.runtime._
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

import scala.scalanative.windows.HandleApi._
import scala.scalanative.windows.ProcessThreadsApi._
import scala.scalanative.windows.ProcessThreadsApiExt._
import scala.scalanative.windows.SynchApi._
import scala.scalanative.windows.SynchApiExt._
import scala.scalanative.windows.WinBaseApi._
import scala.annotation.tailrec
import scala.annotation.switch

private[java] class WindowsThread(val thread: Thread, stackSize: Long)
    extends NativeThread {
  import WindowsThread._
  import NativeThread._

  private val parkEvent: Handle = checkedHandle("create park event") {
    CreateEventW(
      eventAttributes = null,
      manualReset = true,
      initialState = false,
      name = null
    )
  }

  private val sleepEvent: Handle =
    checkedHandle("create sleep interrupt event") {
      CreateEventW(
        eventAttributes = null,
        manualReset = false,
        initialState = false,
        name = null
      )
    }

  private val handle: Handle = {
    if (isMainThread) 0.toPtr // main thread
    else if (!isMultithreadingEnabled)
      throw new LinkageError(
        "Multithreading support disabled - cannot create new threads"
      )
    else
      checkedHandle("create thread") {
        val effectiveStackSize =
          if (stackSize > 0) stackSize
          else 0 // System default (1MB)

        GC.CreateThread(
          threadAttributes = null,
          stackSize = effectiveStackSize.toUSize,
          startRoutine = NativeThread.threadRoutine,
          routineArg = NativeThread.threadRoutineArgs(this),
          creationFlags = STACK_SIZE_PARAM_IS_A_RESERVATION, // Run immediately,
          threadId = null
        )
      }
  }

  override protected def onTermination() = {
    super.onTermination()
    if (isMultithreadingEnabled) {
      CloseHandle(parkEvent)
      CloseHandle(sleepEvent)
      if (!isMainThread) CloseHandle(handle)
    }
  }

  override def setPriority(
      priority: CInt
  ): Unit = if (isMultithreadingEnabled) {
    SetThreadPriority(handle, priorityMapping(priority))
  }

  // java.lang.Thread priority to OS priority mapping
  private def priorityMapping(threadPriority: Int): Int =
    (threadPriority: @switch) match {
      case 0     => THREAD_PRIORITY_IDLE
      case 1 | 2 => THREAD_PRIORITY_LOWEST
      case 3 | 4 => THREAD_PRIORITY_BELOW_NORMAL
      case 5     => THREAD_PRIORITY_NORMAL
      case 6 | 7 => THREAD_PRIORITY_ABOVE_NORMAL
      case 8 | 9 => THREAD_PRIORITY_HIGHEST
      case 10    => THREAD_PRIORITY_TIME_CRITICAL
      case _ =>
        throw new IllegalArgumentException("Not a valid java thread priority")
    }

  override def interrupt(): Unit = if (isMultithreadingEnabled) {
    // For JSR-166 / LockSupport
    SetEvent(parkEvent)
    // For Sleep
    SetEvent(sleepEvent)
  }

  override protected def park(
      time: Long,
      isAbsolute: Boolean
  ): Unit = if (isMultithreadingEnabled) {
    val parkTime =
      if (time < 0) return
      else if (time == 0 && !isAbsolute) Infinite
      else if (isAbsolute) {
        val relTime = time - System.currentTimeMillis()
        if (relTime <= 0) return
        else relTime.toUInt
      } else {
        val millis = time / NanosInMillisecond
        millis.max(1).toUInt
      }

    if (thread.isInterrupted() ||
        WaitForSingleObject(parkEvent, 0.toUInt) == WAIT_OBJECT_0)
      ResetEvent(parkEvent)
    else {
      state =
        if (parkTime == Infinite) State.ParkedWaiting
        else State.ParkedWaitingTimed
      WaitForSingleObject(parkEvent, parkTime)
      ResetEvent(parkEvent)
      state = State.Running
    }
  }

  @inline override def unpark(): Unit = if (isMultithreadingEnabled) {
    SetEvent(parkEvent)
  }

  override def sleep(millis: scala.Long): Unit = {
    val deadline = System.currentTimeMillis() + millis
    @inline @tailrec def loop(millisRemaining: Long): Unit = {
      if (!thread.isInterrupted() && millisRemaining > 0L) {
        val status = WaitForSingleObject(sleepEvent, millisRemaining.toUInt)
        if (status == WAIT_TIMEOUT) ()
        else loop(deadline - System.currentTimeMillis())
      }
    }

    state = State.ParkedWaitingTimed
    try loop(millisRemaining = millis)
    finally state = State.Running
    ResetEvent(sleepEvent)
  }

  override def sleepNanos(nanos: Int): Unit = {
    val deadline = System.nanoTime() + nanos
    val millis = nanos / NanosInMillisecond
    state = State.ParkedWaitingTimed
    if (millis > 0) sleep(millis)
    while (!thread.isInterrupted() && System.nanoTime() < deadline) {
      if (!SwitchToThread()) Thread.onSpinWait()
    }
    state = State.Running
  }
}

object WindowsThread extends NativeThread.Companion {
  override type Impl = WindowsThread

  @alwaysinline
  def create(thread: Thread, stackSize: Long) =
    new WindowsThread(thread, stackSize)

  @alwaysinline
  override def yieldThread(): Unit = SwitchToThread()

  @alwaysinline private def NanosInMillisecond = 1000000

  private def checkedHandle(label: => String)(handle: Handle): Handle = {
    if (handle == null)
      throw new RuntimeException(s"Failed to start thread: $label")
    handle
  }
}
