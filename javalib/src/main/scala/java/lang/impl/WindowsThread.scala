package java.lang.impl

import scala.annotation.{switch, tailrec}

import scala.scalanative.annotation._
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled
import scala.scalanative.runtime._
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.HandleApi._
import scala.scalanative.windows.ProcessThreadsApi._
import scala.scalanative.windows.ProcessThreadsApiExt._
import scala.scalanative.windows.SynchApi._
import scala.scalanative.windows.SynchApiExt._
import scala.scalanative.windows.SysInfoApi._
import scala.scalanative.windows.WinBaseApi._

private[java] class WindowsThread(
    val thread: Thread,
    val userDefinedStackSize: scala.Long,
    override protected val isExternalThread: Boolean = false
) extends NativeThread {
  import NativeThread._
  import WindowsThread._

  override def companion: NativeThread.Companion = WindowsThread
  override val stackSize: Int =
    NativeThread.calculateStackSize(
      userDefinedStackSize = userDefinedStackSize,
      WindowsThread.defaultOSStackSize
    )

  private val parkEvent: Handle = checkedHandle("create park event") {
    CreateEventW(
      eventAttributes = null,
      manualReset = true,
      initialState = false,
      name = null
    )
  }

  private val sleepEvent: Handle =
    try
      checkedHandle("create sleep interrupt event") {
        CreateEventW(
          eventAttributes = null,
          manualReset = false,
          initialState = false,
          name = null
        )
      }
    catch {
      case ex: Throwable =>
        CloseHandle(parkEvent)
        throw ex
    }

  @volatile private var terminated = false

  private val handle: Handle = try {
    if (isMainThread) 0.toPtr // main thread
    else if (isExternalThread) {
      val duplicated = stackalloc[Handle]()
      if (!DuplicateHandle(
            GetCurrentProcess(),
            GetCurrentThread(),
            GetCurrentProcess(),
            duplicated,
            0.toUInt,
            false,
            DuplicateSameAccess.toUInt
          ))
        throw new RuntimeException("Cannot adopt native thread handle")
      !duplicated
    } else if (!isMultithreadingEnabled) {
      throw new LinkageError(
        "Multithreading support disabled - cannot create new threads"
      )
    } else
      checkedHandle("create thread") {
        GC.CreateThread(
          threadAttributes = null,
          stackSize = stackSize.toUSize,
          startRoutine = NativeThread.threadRoutine,
          routineArg = NativeThread.threadRoutineArgs(this),
          creationFlags = STACK_SIZE_PARAM_IS_A_RESERVATION, // Run immediately,
          threadId = null
        )
      }
  } catch {
    case ex: Throwable =>
      CloseHandle(parkEvent)
      CloseHandle(sleepEvent)
      throw ex
  }

  override protected def onTermination() = {
    if (if (isMultithreadingEnabled) true else isExternalThread) {
      this.synchronized {
        terminated = true
        CloseHandle(parkEvent)
        CloseHandle(sleepEvent)
        if (!isMainThread) CloseHandle(handle)
      }
    }
    super.onTermination()
  }

  override def setPriority(
      priority: CInt
  ): Unit = if (isMultithreadingEnabled) {
    this.synchronized {
      if (terminated) return
      SetThreadPriority(handle, priorityMapping(priority))
    }
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
      case _     =>
        throw new IllegalArgumentException("Not a valid java thread priority")
    }

  override def interrupt(): Unit = if (isMultithreadingEnabled) {
    this.synchronized {
      if (terminated) return
      // For JSR-166 / LockSupport
      SetEvent(parkEvent)
      // For Sleep
      SetEvent(sleepEvent)
    }
  }

  override protected def park(
      time: Long,
      isAbsolute: Boolean
  ): Unit = if (isMultithreadingEnabled) {
    // parkEvent is closed in onTermination. Its handle value can be reused.
    if (terminated) return
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
      val parkedState =
        if (parkTime == Infinite) State.ParkedWaiting
        else State.ParkedWaitingTimed
      withState[Unit](parkedState) {
        WaitForSingleObject(parkEvent, parkTime)
        ResetEvent(parkEvent)
      }
    }
  }

  @inline override def unpark(): Unit = if (isMultithreadingEnabled) {
    if (terminated) return
    withoutCarrierCompensation[Unit] {
      this.synchronized[Unit] {
        if (!terminated) SetEvent(parkEvent)
      }
    }
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

    withState(State.ParkedWaitingTimed)(loop(millisRemaining = millis))
    ResetEvent(sleepEvent)
  }

  override def sleepNanos(nanos: Int): Unit = {
    val deadline = System.nanoTime() + nanos
    val millis = nanos / NanosInMillisecond
    withState(State.ParkedWaitingTimed) {
      if (millis > 0) sleep(millis)
      while (!thread.isInterrupted() && System.nanoTime() < deadline) {
        if (!SwitchToThread()) Thread.onSpinWait()
      }
    }
  }
}

object WindowsThread extends NativeThread.Companion {
  override type Impl = WindowsThread

  @alwaysinline
  def create(thread: Thread, stackSize: Long) =
    new WindowsThread(thread, stackSize)

  def adoptCurrentThread(thread: Thread): WindowsThread =
    new WindowsThread(thread, 0L, isExternalThread = true)

  @alwaysinline
  override def yieldThread(): Unit = SwitchToThread()

  override lazy val defaultOSStackSize: Long = {
    if (!isMultithreadingEnabled) 0L
    else {
      import scala.scalanative.windows.SysInfoApiOps.SystemInfoOps
      val sysInfo = stackalloc[SystemInfo]()
      GetSystemInfo(sysInfo)
      sysInfo.allocationGranularity.toLong
    }
  }

  // Adoption has no identity yet and cannot wait for Scala module initialization.
  private final val DuplicateSameAccess = 0x00000002

  @alwaysinline private def NanosInMillisecond = 1000000

  private def checkedHandle(label: => String)(handle: Handle): Handle = {
    if (handle == null)
      throw new RuntimeException(s"Failed to start thread: $label")
    handle
  }
}
