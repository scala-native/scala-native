package scala.scalanative

import scalanative.annotation.alwaysinline
import scalanative.unsafe._
import scalanative.unsigned.USize
import scalanative.runtime.Intrinsics._
import scalanative.runtime.monitor._
import scalanative.runtime.ffi.stdatomic.{atomic_thread_fence, memory_order}
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled
import java.util.concurrent.locks.LockSupport
import java.{lang => jl}

package object runtime {
  def filename = ExecInfo.filename
  def startTime: Long = ExecInfo.startTime
  def uptime: Long = System.currentTimeMillis() - startTime

  /** Used as a stub right hand of intrinsified methods. */
  @noinline private[scalanative] def intrinsic: Nothing = throwUndefined()

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
    NativeThread.TLS.setupCurrentThreadInfo(
      stackBottom = Intrinsics.stackalloc[Byte](),
      isMainThread = true,
      stackSize = 0 /* detect */
    )
    StackOverflowGuards.setup(isMainThread = true)

    val mainThread = Thread.currentThread()
    if (mainThread == null) {
      ffi.printf(
        c"Scala Native Fatal Error: failed to initialize main java.lang.Thread\n"
      )
      System.exit(1)
    }

    val argv = fromRawPtr[CString](rawargv)
    val args = new scala.Array[String](argc - 1)

    ExecInfo.filename = fromCString(argv(0))
    var c = 0
    while (c < argc - 1) {
      // use the default Charset (UTF_8 atm)
      args(c) = fromCString(argv(c + 1))
      c += 1
    }
    ExecInfo.startTime = System.currentTimeMillis()
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
    def pollNonDaemonThreads = {
      val it = NativeThread.Registry.aliveThreadsIterator
      var exists = false
      while (!exists && it.hasNext()) {
        val thread = it.next().thread
        exists = (thread ne shutdownThread) &&
          !thread.isDaemon() &&
          thread.isAlive()
      }
      exists
    }

    def queue = concurrent.NativeExecutionContext.queueInternal
    def shouldWaitForThreads =
      if (isMultithreadingEnabled) gracefully && pollNonDaemonThreads
      else false
    def shouldRunQueuedTasks = gracefully && queue.nonEmpty

    // Both runnable from the NativeExecutionContext.queue and the running threads can spawn new runnables
    while ({
      // drain the queue
      queue.helpComplete()
      // queue is empty, threads might be still running
      if (isMultithreadingEnabled) {
        if (shouldWaitForThreads) LockSupport.park()
        // When unparked thread has either finished execution or there are new tasks enqueued
      }
      shouldWaitForThreads || shouldRunQueuedTasks
    }) ()
    StackOverflowGuards.close()
  }

  private[scalanative] final def executeUncaughtExceptionHandler(
      handler: Thread.UncaughtExceptionHandler,
      thread: Thread,
      throwable: jl.Throwable
  ): Unit = {
    try handler.uncaughtException(thread, throwable)
    catch {
      case ex: jl.Throwable =>
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
  @deprecated(
    "Usage in the users code is discouraged, public method would be removed in the future. Use `scala.scalanative` package private method `scala.scalanative.concurrent.NativeExecutionContext.queueInternal.helpComplete()) instead",
    since = "0.5.0"
  )
  @noinline def loop(): Unit =
    concurrent.NativeExecutionContext.queueInternal.helpComplete()

  // It should be val but we don't want any fields in runtime package object
  @deprecated(
    "Use `scala.scalanative.concurrent.NativeExecutionContext",
    since = "0.5.0"
  )
  def ExecutionContext = concurrent.NativeExecutionContext

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

  @noinline
  @exported("scalanative_throwStackOverflowError")
  private[runtime] def throwPendingStackOverflowError(): Unit = {
    val exception = new StackOverflowError()
    exception.asInstanceOf[runtime.Throwable].onCatchHandler = (_: Throwable) =>
      try StackOverflowGuards.reset()
      catch { case ex: StackOverflowError => () }
    throw exception
  }

  @extern private[runtime] object StackOverflowGuards {
    @name("scalanative_StackOverflowGuards_size")
    def size: Int = extern

    @name("scalanative_StackOverflowGuards_setup")
    def setup(isMainThread: Boolean): Unit = extern

    @name("scalanative_StackOverflowGuards_reset")
    def reset(): Unit = extern

    @name("scalanative_StackOverflowGuards_close")
    def close(): Unit = extern
  }

}
