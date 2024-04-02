// Based on https://github.com/scala-native/scala-native-loop
import LibUV._
import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe._
import scala.scalanative.concurrent.NativeExecutionContext
import scala.scalanative.runtime.Intrinsics._
import EventLoop.loop
import scala.util.{Success, Try}
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

object Test {
  def main(args: Array[String]): Unit = {
    import NativeExecutionContext.Implicits.queue
    recursiveTask()
    scheduleHandler()
  }

  def recursiveTask()(implicit ec: ExecutionContext): Unit = {
    var completed = false
    var counter = 0
    def recursive(): Future[Int] = {
      counter += 1
      Thread.sleep(1)
      if (!completed) Future(recursive()).flatMap(identity)
      else Future.successful(42)
    }
    val task = recursive()
    assert(!completed)
    assert(counter == 1)
    Timer.delay(10.millis).map { _ => completed = true }
    assert(await(task) == Success(42))
    assert(counter > 2)
  }

 def scheduleHandler(): Unit = {
    import NativeExecutionContext.{EventLoopExecutionContext, EventHandler}
    implicit val ec: EventLoopExecutionContext = NativeExecutionContext.queue
    val handler = ec.createEventHandler(
      0,
      (handler: EventHandler[Int, Unit]) => {
        println(handler.state)
        if (handler.state == 2) Right(())
        else Left(handler.state + 1)
      }
    )
    // Does not iterate when not signaled
    assert(!handler.result.isCompleted)
    assert(handler.state == 0)
    EventLoop.`yield`()
    assert(!handler.result.isCompleted)
    assert(handler.state == 0)

    // Can be iterated when signaled
    assert(!ec.isWorkStealingPossible)
    handler.signal()
    assert(ec.isWorkStealingPossible)
    EventLoop.`yield`()
    assert(handler.state == 1)

    // Can be iterated by scheduled task
    ec.schedule(100.millis, () => handler.signal())
    ec.untilNextScheduledTask.map(_.toMillis).foreach(Thread.sleep(_))
    EventLoop.drain()
    assert(handler.state == 2)

    Timer.delay(100.millis).foreach { _ =>
      println("cb"); handler.signal()
    }
    await(handler.result)
  }

  def await[T](task: Future[T]): Try[T] = {
    while (!task.isCompleted) EventLoop.`yield`()
    task.value.get
  }
}

object EventLoop {
  val loop: LibUV.Loop = uv_default_loop()

  // Schedule loop execution after main ends
  def queue = NativeExecutionContext.queue
  queue.execute(() => EventLoop.run())

  def `yield`(): Unit = {
    queue.stealWork(1)
    uv_run(loop, UV_RUN_NOWAIT)
  }

  def drain(): Unit = while (queue.isWorkStealingPossible) `yield`()

  def run(): Unit =
    while (uv_loop_alive(loop) != 0 || queue.isWorkStealingPossible) {
      drain()
      uv_run(loop, UV_RUN_ONCE)
    }
}

@inline final class Timer private (private val ptr: Ptr[Byte]) extends AnyVal {
  def clear(): Unit = {
    uv_timer_stop(ptr)
    HandleUtils.close(ptr)
  }
}
object Timer {

  private val timeoutCB: TimerCB = (handle: TimerHandle) => {
    val callback = HandleUtils.getData[() => Unit](handle)
    callback.apply()
  }
  private val repeatCB: TimerCB = (handle: TimerHandle) => {
    val callback = HandleUtils.getData[() => Unit](handle)
    callback.apply()
  }
  @inline
  private def startTimer(
      timeout: Long,
      repeat: Long,
      callback: () => Unit
  ): Timer = {
    val timerHandle = stdlib.malloc(uv_handle_size(UV_TIMER_T))
    uv_timer_init(EventLoop.loop, timerHandle)
    HandleUtils.setData(timerHandle, callback)
    val timer = new Timer(timerHandle)
    val withClearIfTimeout: () => Unit =
      if (repeat == 0L) { () =>
        {
          callback()
          timer.clear()
        }
      } else callback
    uv_timer_start(timerHandle, timeoutCB, timeout, repeat)
    timer
  }

  def delay(duration: FiniteDuration): Future[Unit] = {
    val promise = Promise[Unit]()
    timeout(duration)(() => promise.success(()))
    promise.future
  }

  def timeout(duration: FiniteDuration)(callback: () => Unit): Timer = {
    startTimer(duration.toMillis, 0L, callback)
  }
}

@link("uv")
@extern
object LibUV {
  type Loop = CVoidPtr
  type UVHandle = CVoidPtr
  type PipeHandle = CVoidPtr
  type PrepareHandle = CVoidPtr
  type PrepareCB = CFuncPtr1[PrepareHandle, Unit]
  type TimerHandle = CVoidPtr
  type CloseCB = CFuncPtr1[UVHandle, Unit]
  // type PollCB = CFuncPtr3[PollHandle, Int, Int, Unit]
  type TimerCB = CFuncPtr1[TimerHandle, Unit]

  // uv_run_mode
  final val UV_RUN_DEFAULT = 0
  final val UV_RUN_ONCE = 1
  final val UV_RUN_NOWAIT = 2

  def uv_run(loop: Loop, runMode: Int): Int = extern

  def uv_default_loop(): Loop = extern
  def uv_loop_alive(loop: Loop): CInt = extern
  def uv_loop_close(loop: Loop): CInt = extern
  def uv_is_active(handle: Ptr[Byte]): Int = extern
  def uv_handle_size(h_type: Int): CSize = extern
  def uv_req_size(r_type: Int): CSize = extern
  def uv_prepare_init(loop: Loop, handle: PrepareHandle): Int = extern
  def uv_prepare_start(handle: PrepareHandle, cb: PrepareCB): Int = extern
  def uv_prepare_stop(handle: PrepareHandle): Unit = extern
  def uv_close(handle: PipeHandle, closeCB: CloseCB): Unit = extern

  def uv_timer_init(loop: Loop, handle: TimerHandle): Int = extern
  def uv_timer_start(
      handle: TimerHandle,
      cb: TimerCB,
      timeout: Long,
      repeat: Long
  ): Int = extern
  def uv_timer_stop(handle: TimerHandle): Int = extern

  final val UV_TIMER_T = 13
}

private object HandleUtils {
  import scala.scalanative.runtime._
  private val references = new java.util.IdentityHashMap[Object, Int]()

  @inline def getData[T <: Object](handle: CVoidPtr): T = {
    // data is the first member of uv_loop_t
    val ptrOfPtr = handle.asInstanceOf[Ptr[Ptr[Byte]]]
    val dataPtr = !ptrOfPtr
    if (dataPtr == null) null.asInstanceOf[T]
    else {
      val rawptr = toRawPtr(dataPtr)
      castRawPtrToObject(rawptr).asInstanceOf[T]
    }
  }
  @inline def setData(handle: Ptr[Byte], obj: Object): Unit = {
    // data is the first member of uv_loop_t
    val ptrOfPtr = handle.asInstanceOf[Ptr[Ptr[Byte]]]
    if (obj != null) {
      references.put(obj, references.get(obj) + 1)
      val rawptr = castObjectToRawPtr(obj)
      !ptrOfPtr = fromRawPtr[Byte](rawptr)
    } else {
      !ptrOfPtr = null
    }
  }
  private val onCloseCB: CloseCB = (handle: UVHandle) => {
    stdlib.free(handle)
  }
  @inline def close(handle: Ptr[Byte]): Unit = {
    if (getData(handle) != null) {
      uv_close(handle, onCloseCB)
      val data = getData[Object](handle)
      val current = references.get(data)
      if (current > 1) references.put(data, current - 1)
      else references.remove(data)
      setData(handle, null)
    }
  }
}
