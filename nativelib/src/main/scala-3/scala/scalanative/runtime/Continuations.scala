package scala.scalanative.runtime

// `Throwable` alone is scala.scalanative.runtime.Throwable in this package.
import java.lang.Throwable
import java.util.concurrent.atomic.AtomicBoolean

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

import scala.scalanative.meta.LinktimeInfo.{
  isContinuationsSupported, isMultiShotContinuationsSupported
}
import scala.scalanative.unsafe.*

/** Delimited continuations.
 *
 *  Two implementations share one API:
 *    - [[Continuations.single]]: a continuation can be resumed once. The
 *      suspended computation keeps its own stack, so it can hold addresses of
 *      stack memory (`stackalloc`) and be resumed on any thread.
 *    - [[Continuations.multi]]: a continuation can be resumed many times. Its
 *      stack is copied on every resume, so the suspended computation must not
 *      hold addresses of stack memory.
 *
 *  The members of `Continuations` itself are those of `single`.
 */
object Continuations:
  export single.{BoundaryLabel, boundary, break, suspend}

  @deprecated(
    "One-shot continuations keep no state in the thread; use `multi.handlersReset` for multi-shot ones",
    since = "0.5.13"
  )
  def handlersReset(): Unit = ()

  sealed abstract class Api:
    /** A marker for a given `boundary`. Use `break` or `suspend` to suspend the
     *  continuation up to the specified `boundary`. This value MUST NOT escape
     *  the `boundary` that created it.
     */
    opaque type BoundaryLabel[T] = Label

    /** Marks the given body as suspendable with a `BoundaryLabel` that
     *  `suspend` can refer to. Forwards the return value of `body`, or the
     *  return value of the `suspend` call, if it happens during the execution
     *  of `body`.
     *
     *  __Safety__: suspending to a `BoundaryLabel` not created by a `boundary`
     *  call higher on the same call stack is undefined behaviour.
     */
    inline def boundary[T](inline body: BoundaryLabel[T] ?=> T): T =
      enter[T](label => body(using label))

    /** Suspends the current running stack up to the corresponding `boundary`
     *  into a continuation `cont: R => T` and passes it into `onSuspend`. The
     *  return value of `onSuspend(cont)` is returned to `boundary`. Returns the
     *  value passed to `cont` once it is resumed.
     */
    inline def suspend[R, T](
        inline onSuspend: (R => T) => T
    )(using label: BoundaryLabel[T]): R =
      capture[R, T](label, continuation => onSuspend(continuation))

    /** Same as `suspend` where the continuation expects no parameters. */
    inline def suspend[T](
        inline onSuspend: (() => T) => T
    )(using label: BoundaryLabel[T]): Unit =
      capture[Unit, T](
        label,
        continuation => onSuspend(() => continuation(()))
      )

    /** Immediately return to the `boundary`. */
    inline def break(using label: BoundaryLabel[Unit]): Nothing =
      leave[Unit](label, ())

    /** Immediately return to the `boundary`, returning the given value. */
    inline def break[T](inline value: T)(using
        label: BoundaryLabel[T]
    ): Nothing =
      leave[T](label, value)

    protected def enter[T](body: BoundaryLabel[T] => T): T
    protected def capture[R, T](
        label: BoundaryLabel[T],
        onSuspend: (R => T) => T
    ): R
    protected def leave[T](label: BoundaryLabel[T], value: T): Nothing

    protected final def toNative[T](label: BoundaryLabel[T]): Label = label
  end Api

  /** One-shot continuations: resuming a continuation a second time throws
   *  `IllegalStateException`.
   *
   *  `onSuspend` runs after the suspended computation left its stack, in the
   *  context that receives the result: the caller of `boundary`, or of the
   *  continuation that resumed it. It cannot `suspend` to the label it was
   *  given.
   */
  object single extends Api:
    if !isContinuationsSupported then UnsupportedFeature.continuations()

    protected def enter[T](body: this.BoundaryLabel[T] => T): T =
      OneShot.boundary[T](runBody, body.asInstanceOf[Body]).get

    protected def capture[R, T](
        label: this.BoundaryLabel[T],
        onSuspend: (R => T) => T
    ): R =
      val call: OnSuspend = native =>
        // From `captured` on, the GC finds the suspended stacks through
        // this object. Passing it makes sure it is allocated before.
        val stacks = new SuspendedStacks(toRawPtr(native))
        OneShot.captured(native, stacks)
        onSuspend(new OneShotContinuation[R, T](stacks))
      OneShot.suspend[R](toNative(label), runOnSuspend, call)

    protected def leave[T](label: this.BoundaryLabel[T], value: T): Nothing =
      OneShot.leave(toNative(label), Success(value))
      throw new IllegalStateException("Left boundary was resumed")
  end single

  /** Multi-shot continuations.
   *
   *  The suspended computation is copied to the stack of every resumer, so it
   *  must not hold addresses of its own stack: no `stackalloc` between
   *  `boundary` and `suspend`.
   */
  object multi extends Api:
    if isMultiShotContinuationsSupported then MultiShot.init(allocate)
    else UnsupportedFeature.continuations()

    protected def enter[T](body: this.BoundaryLabel[T] => T): T =
      MultiShot.boundary[T](runBody, body.asInstanceOf[Body]).get

    protected def capture[R, T](
        label: this.BoundaryLabel[T],
        onSuspend: (R => T) => T
    ): R =
      val continuation = new MultiShotContinuation[R, T]
      val call: OnSuspend = native =>
        continuation.native = native
        onSuspend(continuation)
      try
        MultiShot.suspend[R](toNative(label), runOnSuspend, call, continuation)
      finally reachabilityFence(continuation)

    protected def leave[T](label: this.BoundaryLabel[T], value: T): Nothing =
      capture[Nothing, T](label, _ => value)

    /** Drops the boundaries known to the current thread. */
    def handlersReset(): Unit = MultiShot.handlersReset()
  end multi

  private type Label = CUnsignedLong
  private type NativeContinuation = Ptr[?]
  private type Body = Label => Any
  private type OnSuspend = NativeContinuation => Any

  /** `true` once resumed. */
  private final class OneShotContinuation[-R, +T](stacks: SuspendedStacks)
      extends AtomicBoolean
      with (R => T):
    def apply(value: R): T =
      if !compareAndSet(false, true) then
        throw new IllegalStateException("Continuation was already resumed")
      val native = fromRawPtr[Any](stacks.stacks)
      stacks.stacks = null
      OneShot.resume[R, T](native, value).get

  /** Owns the copies of the suspended stack, which the GC has to scan. */
  private final class MultiShotContinuation[-R, +T] extends (R => T):
    var native: NativeContinuation = null
    private val memory = mutable.ArrayBuffer.empty[BlobArray]

    def alloc(size: CUnsignedLong): Ptr[?] =
      val blob = BlobArray.alloc(size.toInt)
      memory += blob
      blob.atUnsafe(0)

    def apply(value: R): T =
      try MultiShot.resume[R, T](native, value).get
      finally reachabilityFence(this)

  // Called from C. A Throwable must not unwind through the native frames, so
  // it is carried in the result and rethrown by `Try.get`.

  private val runBody: CFuncPtr2[Label, Body, Try[Any]] =
    CFuncPtr2.fromScalaFunction((label, body) => catching(body(label)))

  private val runOnSuspend: CFuncPtr2[NativeContinuation, OnSuspend, Try[Any]] =
    CFuncPtr2.fromScalaFunction((native, onSuspend) =>
      catching(onSuspend(native))
    )

  private val allocate
      : CFuncPtr2[CUnsignedLong, MultiShotContinuation[?, ?], Ptr[?]] =
    CFuncPtr2.fromScalaFunction((size, owner) => owner.alloc(size))

  private inline def catching(inline body: Any): Try[Any] =
    try Success(body)
    catch case ex: Throwable => Failure(ex)

  @noinline private def reachabilityFence(target: Any): Unit = ()

  /** delimcc_oneshot.c */
  @extern @define("__SCALANATIVE_DELIMCC_ONESHOT")
  private object OneShot:
    @name("scalanative_continuation_oneshot_boundary")
    def boundary[T](
        run: CFuncPtr2[Label, Body, Try[Any]],
        body: Body
    ): Try[T] = extern

    @name("scalanative_continuation_oneshot_suspend")
    def suspend[R](
        label: Label,
        run: CFuncPtr2[NativeContinuation, OnSuspend, Try[Any]],
        onSuspend: OnSuspend
    ): R = extern

    @name("scalanative_continuation_oneshot_leave")
    def leave(label: Label, result: Try[Any]): Unit = extern

    @name("scalanative_continuation_oneshot_captured")
    def captured(
        continuation: NativeContinuation,
        owner: SuspendedStacks
    ): Unit =
      extern

    @name("scalanative_continuation_oneshot_resume")
    def resume[R, T](continuation: NativeContinuation, value: R): Try[T] =
      extern
  end OneShot

  /** delimcc_multishot.c */
  @extern @define("__SCALANATIVE_DELIMCC_MULTISHOT")
  private object MultiShot:
    @name("scalanative_continuation_multishot_init")
    def init(
        alloc: CFuncPtr2[CUnsignedLong, MultiShotContinuation[?, ?], Ptr[?]]
    ): Unit = extern

    @name("scalanative_continuation_multishot_boundary")
    def boundary[T](
        run: CFuncPtr2[Label, Body, Try[Any]],
        body: Body
    ): Try[T] = extern

    @name("scalanative_continuation_multishot_suspend")
    def suspend[R](
        label: Label,
        run: CFuncPtr2[NativeContinuation, OnSuspend, Try[Any]],
        onSuspend: OnSuspend,
        owner: MultiShotContinuation[?, ?]
    ): R = extern

    @name("scalanative_continuation_multishot_resume")
    def resume[R, T](continuation: NativeContinuation, value: R): Try[T] =
      extern

    @name("scalanative_continuation_multishot_handlers_reset")
    def handlersReset(): Unit = extern
  end MultiShot
end Continuations

/** Entry point used by delimcc_multishot.c / eh.cpp. Kept outside of
 *  `Continuations` so that exporting it does not make `Continuations` (and
 *  delimcc) always reachable.
 */
private[runtime] object ContinuationsExceptions:
  @exported("scalanative_continuation_exception_to_failure")
  def continuationExceptionToFailure(exception: Throwable): Try[Any] =
    Failure(exception)
