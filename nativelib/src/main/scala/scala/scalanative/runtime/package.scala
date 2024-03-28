package scala.scalanative

import scalanative.annotation.alwaysinline
import scalanative.unsafe._
import scalanative.unsigned.USize
import scalanative.runtime.Intrinsics._
import scalanative.runtime.monitor._
import scalanative.runtime.ffi.stdatomic.{atomic_thread_fence, memory_order}
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled
import java.util.concurrent.locks.LockSupport

package object runtime {
  def filename = ExecInfo.filename

  /** Used as a stub right hand of intrinsified methods. */
  private[scalanative] def intrinsic: Nothing = throwUndefined()

  // Called statically by the compiler, do not modify!
  /** Enter monitor of given object. */
  @alwaysinline
  private[runtime] def enterMonitor(obj: _Object): Unit =
    if (isMultithreadingEnabled) {
      getMonitor(obj).enter(obj)
    }

  // Called statically by the compiler, do not modify!
  /** Enter monitor of given object. */
  @alwaysinline
  private[runtime] def exitMonitor(obj: _Object): Unit =
    if (isMultithreadingEnabled) {
      getMonitor(obj).exit(obj)
    }

  /** Get monitor for given object. */
  @alwaysinline
  def getMonitor(obj: _Object) = {
    if (isMultithreadingEnabled)
      new BasicMonitor(
        elemRawPtr(
          castObjectToRawPtr(obj),
          castIntToRawSize(MemoryLayout.Object.LockWordOffset)
        )
      )
    else
      throw new IllegalStateException(
        "Monitors unavilable in single threaded mode"
      )
  }

  /** Initialize runtime with given arguments and return the rest as Java-style
   *  array.
   */
  private[runtime] def init(
      argc: Int,
      rawargv: RawPtr
  ): scala.Array[String] = {
    if (isMultithreadingEnabled) {
      assert(
        Thread.currentThread() != null,
        "failed to initialize main thread"
      )
    }

    val argv = fromRawPtr[CString](rawargv)
    val args = new scala.Array[String](argc - 1)

    // skip the executable name in argv(0)
    var c = 0
    while (c < argc - 1) {
      // use the default Charset (UTF_8 atm)
      args(c) = fromCString(argv(c + 1))
      c += 1
    }

    ExecInfo.filename = fromCString(argv(0))
    args
  }

  /* Internal shutdown method called after successfully running the main method.
   * Ensures that all scheduled tasks / non-deamon threads would finish before exit.
   */
  @noinline private[runtime] def onShutdown(): Unit = {
    import MainThreadShutdownContext._
    if (isMultithreadingEnabled) {
      shutdownThread = Thread.currentThread()
      atomic_thread_fence(memory_order.memory_order_seq_cst)
    }
    def pollNonDaemonThreads = NativeThread.Registry.aliveThreads.iterator
      .map(_.thread)
      .filter { thread =>
        (thread ne shutdownThread) && !thread.isDaemon() &&
        thread.isAlive()
      }

    def queue = NativeExecutionContext.QueueExecutionContext
    def shouldWaitForThreads =
      if (isMultithreadingEnabled) gracefully && pollNonDaemonThreads.hasNext
      else false
    def shouldRunQueuedTasks = gracefully && queue.hasNextTask

    // Both runnable from the NativeExecutionContext.queue and the running threads can spawn new runnables
    while ({
      // drain the queue
      queue.executeAvailableTasks()
      // queue is empty, threads might be still running
      if (isMultithreadingEnabled) {
        if (shouldWaitForThreads) LockSupport.park()
        // When unparked thread has either finished execution or there are new tasks enqueued
      }
      shouldWaitForThreads || shouldRunQueuedTasks
    }) ()
  }

  private[scalanative] final def executeUncaughtExceptionHandler(
      handler: Thread.UncaughtExceptionHandler,
      thread: Thread,
      throwable: Throwable
  ): Unit = {
    try handler.uncaughtException(thread, throwable)
    catch {
      case ex: Throwable =>
        val threadName = "\"" + thread.getName() + "\""
        System.err.println(
          s"\nException: ${ex.getClass().getName()} thrown from the UncaughtExceptionHandler in thread ${threadName}"
        )
    }
  }

  @alwaysinline def fromRawPtr[T](rawptr: RawPtr): Ptr[T] =
    Boxes.boxToPtr(rawptr)

  @alwaysinline def toRawPtr[T](ptr: Ptr[T]): RawPtr =
    Boxes.unboxToPtr(ptr)

  @alwaysinline def fromRawSize[T](rawSize: RawSize): Size =
    Boxes.boxToSize(rawSize)

  @alwaysinline def fromRawUSize[T](rawSize: RawSize): USize =
    Boxes.boxToUSize(rawSize)

  @alwaysinline def toRawSize(size: Size): RawSize =
    Boxes.unboxToSize(size)

  @alwaysinline def toRawSize(size: USize): RawSize =
    Boxes.unboxToUSize(size)

  /** Run the runtime's event loop. The method is called from the generated
   *  C-style after the application's main method terminates.
   */
  @noinline def loop(): Unit =
    NativeExecutionContext.QueueExecutionContext.executeAvailableTasks()

  /** Called by the generated code in case of division by zero. */
  @noinline
  private[scalanative] def throwDivisionByZero(): Nothing =
    throw new java.lang.ArithmeticException("/ by zero")

  /** Called by the generated code in case of incorrect class cast. */
  @noinline
  private[scalanative] def throwClassCast(from: RawPtr, to: RawPtr): Nothing = {
    val fromName = loadObject(
      elemRawPtr(from, castIntToRawSizeUnsigned(MemoryLayout.Rtti.NameOffset))
    )
    val toName = loadObject(
      elemRawPtr(to, castIntToRawSizeUnsigned(MemoryLayout.Rtti.NameOffset))
    )
    throw new java.lang.ClassCastException(
      s"$fromName cannot be cast to $toName"
    )
  }

  /** Called by the generated code in case of operations on null. */
  @noinline
  private[scalanative] def throwNullPointer(): Nothing =
    throw new NullPointerException()

  /** Called by the generated code in case of unexpected condition. */
  @noinline
  private[scalanative] def throwUndefined(): Nothing =
    throw new UndefinedBehaviorError

  /** Called by the generated code in case of out of bounds on array access. */
  private[scalanative] def throwOutOfBounds(i: Int, length: Int): Nothing =
    throw new ArrayIndexOutOfBoundsException(
      s"Index $i out of bounds for length $length"
    )

  /** Called by the generated code in case of missing method on reflective call.
   */
  @noinline
  private[scalanative] def throwNoSuchMethod(sig: String): Nothing =
    throw new NoSuchMethodException(sig)
}
