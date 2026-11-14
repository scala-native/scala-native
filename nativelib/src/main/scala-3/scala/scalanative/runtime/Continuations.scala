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
 *  The two do not mix across a boundary: capturing a `multi` continuation
 *  across a `single` boundary, or suspending or breaking a `single` one across
 *  a `multi` boundary, throws `IllegalStateException`.
 *
 *  The members of `Continuations` itself are those of `single`.
 */
object Continuations {

  // ==========================================================================
  // API
  // ==========================================================================

  /** What [[single]] and [[multi]] share. */
  sealed abstract class Api {

    /** A marker for a given `boundary`. Use `break` or `suspend` to suspend the
     *  continuation up to the specified `boundary`. This value MUST NOT escape
     *  the `boundary` that created it.
     *
     *  Abstract here and opaque in each implementation: Scala 3.1 does not
     *  finish compiling a call made through an `Api` value when the opaque type
     *  is a member of this class.
     */
    type BoundaryLabel[T]

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
  }

  // ==========================================================================
  // One-shot continuations
  // ==========================================================================

  /** One-shot continuations: resuming a continuation a second time throws
   *  `IllegalStateException`.
   *
   *  `onSuspend` runs after the suspended computation left its stack, in the
   *  context that receives the result: the caller of `boundary`, or of the
   *  continuation that resumed it. It cannot `suspend` to the label it was
   *  given.
   */
  object single extends Api {
    opaque type BoundaryLabel[T] = CUnsignedLong

    /** What `boundary` runs; use `boundary`. */
    def enter[T](body: this.BoundaryLabel[T] => T): T =
      support.require()
      val result = FFI.boundary[T](runBody, body.asInstanceOf[Body])
      if result == null then
        throw new OutOfMemoryError("Cannot allocate a continuation stack")
      result.get

    /** What `suspend` runs; use `suspend`. */
    def capture[R, T](
        label: this.BoundaryLabel[T],
        onSuspend: (R => T) => T
    ): R =
      checkNoMultiShotBoundary(label)
      val call: OnSuspend = native =>
        val continuation =
          try new Continuation[R, T]
          catch
            case ex: Throwable =>
              FFI.discard(native)
              throw ex
        continuation.inner = native
        onSuspend(continuation)
      FFI.suspend[R](label, runOnSuspend, call)

    /** What `break` runs; use `break`. */
    def leave[T](label: this.BoundaryLabel[T], value: T): Nothing =
      checkNoMultiShotBoundary(label)
      FFI.leave(label, Success(value))
      throw new IllegalStateException("Left boundary was resumed")

    /** Loaded by `enter` only, so that a program reports continuations as
     *  unsupported when it can enter a boundary, not when it merely contains
     *  code that could suspend, as `LockSupport.park` does for virtual threads.
     */
    private object support:
      if !isContinuationsSupported then UnsupportedFeature.continuations()
      def require(): Unit = ()

    /** Set once `multi` is initialised; from then on suspending or breaking
     *  across one of its boundaries is refused.
     */
    @volatile private[Continuations] var multiShotInUse = false

    private def checkNoMultiShotBoundary(label: Label): Unit =
      if multiShotInUse && FFI.crossesMultiShot(label) then
        throw new IllegalStateException(
          "A `single` continuation cannot suspend or break across a `multi` boundary"
        )

    /** delimcc_oneshot.c */
    @extern @define("__SCALANATIVE_DELIMCC_ONESHOT")
    private[Continuations] object FFI {
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
      def resume[R, T](stacks: SuspendedStacks, value: R): Try[T] = extern

      @name("scalanative_continuation_oneshot_discard")
      def discard(continuation: NativeContinuation): Unit = extern

      @name("scalanative_continuation_oneshot_watch_multishot")
      def watchMultiShot(probe: Ptr[?]): Unit = extern

      @name("scalanative_continuation_oneshot_crosses_multishot")
      def crossesMultiShot(label: Label): Boolean = extern
    }
  }

  /** A one-shot continuation; `true` once resumed.
   *
   *  It is a class of `Continuations` and not of `single`, because code
   *  compiled against 0.5.12 and older creates it under this name and sets
   *  `inner` too, see [[Impl]].
   */
  private[runtime] class Continuation[-R, +T]
      extends AtomicBoolean
      with (R => T) {
    private var stacks: SuspendedStacks = null

    def inner: NativeContinuation = fromRawPtr(stacks.stacks)

    /** From here on the GC finds the suspended stacks through `stacks`. Passing
     *  it to `captured` makes sure it is allocated before.
     */
    def inner_=(native: NativeContinuation): Unit =
      stacks =
        try new SuspendedStacks(toRawPtr(native))
        catch
          case ex: Throwable =>
            single.FFI.discard(native)
            throw ex
      single.FFI.captured(native, stacks)

    def apply(value: R): T =
      if !compareAndSet(false, true) then
        throw new IllegalStateException("Continuation was already resumed")
      single.FFI.resume[R, T](stacks, value).get
  }

  // ==========================================================================
  // Multi-shot continuations
  // ==========================================================================

  /** Multi-shot continuations.
   *
   *  The suspended computation is copied to the stack of every resumer, so it
   *  must not hold addresses of its own stack: no `stackalloc` between
   *  `boundary` and `suspend`.
   */
  object multi extends Api {
    opaque type BoundaryLabel[T] = CUnsignedLong

    // Called from C on behalf of the continuation given to `FFI.suspend`.
    private val allocate
        : CFuncPtr2[CUnsignedLong, MultiShotContinuation[?, ?], Ptr[?]] =
      CFuncPtr2.fromScalaFunction((size, owner) => owner.alloc(size))

    if isMultiShotContinuationsSupported then
      FFI.init(allocate)
      if isContinuationsSupported then
        single.FFI.watchMultiShot(FFI.boundaryProbe())
        single.multiShotInUse = true
    else UnsupportedFeature.continuations()

    protected def enter[T](body: this.BoundaryLabel[T] => T): T =
      FFI.boundary[T](runBody, body.asInstanceOf[Body]).get

    protected def capture[R, T](
        label: this.BoundaryLabel[T],
        onSuspend: (R => T) => T
    ): R =
      if !FFI.onCurrentStack(label) then
        throw new IllegalStateException(
          "A `multi` continuation cannot be captured: its boundary was entered on another stack, across a `single` boundary or a resume"
        )
      val continuation = new MultiShotContinuation[R, T]
      val call: OnSuspend = native =>
        continuation.native = native
        onSuspend(continuation)
      try FFI.suspend[R](label, runOnSuspend, call, continuation)
      finally reachabilityFence(continuation)

    protected def leave[T](label: this.BoundaryLabel[T], value: T): Nothing =
      capture[Nothing, T](label, _ => value)

    /** Drops the boundaries known to the current thread. */
    def handlersReset(): Unit = FFI.handlersReset()

    /** Owns the copies of the suspended stack, which the GC has to scan. */
    private final class MultiShotContinuation[-R, +T] extends (R => T) {
      var native: NativeContinuation = null
      private val memory = mutable.ArrayBuffer.empty[BlobArray]

      def alloc(size: CUnsignedLong): Ptr[?] =
        val blob = BlobArray.alloc(size.toInt)
        memory += blob
        blob.atUnsafe(0)

      def apply(value: R): T =
        try FFI.resume[R, T](native, value).get
        finally reachabilityFence(this)
    }

    /** delimcc_multishot.c */
    @extern @define("__SCALANATIVE_DELIMCC_MULTISHOT")
    private object FFI {
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

      @name("scalanative_continuation_multishot_on_current_stack")
      def onCurrentStack(label: Label): Boolean = extern

      @name("scalanative_continuation_multishot_boundary_probe")
      def boundaryProbe(): Ptr[?] = extern
    }
  }

  // ==========================================================================
  // Shared by both implementations
  // ==========================================================================

  private type Label = CUnsignedLong
  private type NativeContinuation = Ptr[?]
  private type Body = Label => Any
  private type OnSuspend = NativeContinuation => Any

  // Called from C. A Throwable must not unwind through the native frames, so
  // it is carried in the result and rethrown by `Try.get`.

  private val runBody: CFuncPtr2[Label, Body, Try[Any]] =
    CFuncPtr2.fromScalaFunction((label, body) => catching(body(label)))

  private val runOnSuspend: CFuncPtr2[NativeContinuation, OnSuspend, Try[Any]] =
    CFuncPtr2.fromScalaFunction((native, onSuspend) =>
      catching(onSuspend(native))
    )

  private inline def catching(inline body: Any): Try[Any] =
    try Success(body)
    catch case ex: Throwable => Failure(ex)

  @noinline private def reachabilityFence(target: Any): Unit = ()

  // ==========================================================================
  // Default interface: `Continuations.boundary` is `single.boundary`
  // ==========================================================================

  // Written out instead of exported from `single`: Scala 3.1 does not see an
  // exported `BoundaryLabel` as the type the exported methods expect.
  type BoundaryLabel[T] = single.BoundaryLabel[T]

  inline def boundary[T](inline body: BoundaryLabel[T] ?=> T): T =
    single.enter[T](label => body(using label))

  inline def suspend[R, T](
      inline onSuspend: (R => T) => T
  )(using label: BoundaryLabel[T]): R =
    single.capture[R, T](label, continuation => onSuspend(continuation))

  inline def suspend[T](
      inline onSuspend: (() => T) => T
  )(using label: BoundaryLabel[T]): Unit =
    single.capture[Unit, T](
      label,
      continuation => onSuspend(() => continuation(()))
    )

  inline def break(using label: BoundaryLabel[Unit]): Nothing =
    single.leave[Unit](label, ())

  inline def break[T](inline value: T)(using
      label: BoundaryLabel[T]
  ): Nothing =
    single.leave[T](label, value)

  @deprecated(
    "One-shot continuations keep no state in the thread; use `multi.handlersReset` for multi-shot ones",
    since = "0.5.13"
  )
  def handlersReset(): Unit = ()

  // ==========================================================================
  // Deprecated: what code compiled against 0.5.12 and older links to
  // ==========================================================================

  private final val legacyExpansion =
    "Only for code compiled against Scala Native 0.5.12 and older; will be removed"

  /** What `boundary` and `suspend` expand to in code compiled against 0.5.12
   *  and older, where they were inline methods: these externs, the accessors
   *  below, and [[Continuation]]. Such code runs on the one-shot runtime.
   */
  @deprecated(legacyExpansion, since = "0.5.13")
  @extern @define("__SCALANATIVE_DELIMCC_ONESHOT")
  private[runtime] object Impl {
    @name("scalanative_continuation_boundary")
    def boundary[T](
        run: CFuncPtr2[Label, Label => Try[T], Try[T]],
        body: Label => Try[T]
    ): Try[T] = extern

    @name("scalanative_continuation_suspend")
    def suspend[R, T](
        label: Label,
        run: CFuncPtr2[
          NativeContinuation,
          NativeContinuation => Try[T],
          Try[T]
        ],
        onSuspend: NativeContinuation => Try[T],
        owner: Continuation[R, T]
    ): R = extern
  }

  @deprecated(legacyExpansion, since = "0.5.13")
  private[runtime] def inline$Impl: Impl.type = Impl
  @deprecated(legacyExpansion, since = "0.5.13")
  private[runtime] def inline$reachabilityFence(target: Any): Unit = ()
  @deprecated(legacyExpansion, since = "0.5.13")
  private[runtime] def inline$inner$i1[T, R](
      continuation: Continuation[R, T]
  ): NativeContinuation = continuation.inner
  @deprecated(legacyExpansion, since = "0.5.13")
  private[runtime] def inline$inner_$eq$i1[T, R](
      continuation: Continuation[R, T],
      native: NativeContinuation
  ): Unit = continuation.inner = native
  @deprecated(legacyExpansion, since = "0.5.13")
  private[runtime] def inline$boundaryBodyFnAny
      : CFuncPtr2[Label, Label => Try[Any], Try[Any]] = legacyRunBody
  @deprecated(legacyExpansion, since = "0.5.13")
  private[runtime] def inline$suspendFnAny: CFuncPtr2[
    NativeContinuation,
    NativeContinuation => Try[Any],
    Try[Any]
  ] = legacyRunOnSuspend

  // The old callbacks return a `Try` that only holds non-fatal throwables.
  private val legacyRunBody: CFuncPtr2[Label, Label => Try[Any], Try[Any]] =
    CFuncPtr2.fromScalaFunction((label, body) =>
      try body(label)
      catch case ex: Throwable => Failure(ex)
    )

  private val legacyRunOnSuspend: CFuncPtr2[
    NativeContinuation,
    NativeContinuation => Try[Any],
    Try[Any]
  ] =
    CFuncPtr2.fromScalaFunction((native, onSuspend) =>
      try onSuspend(native)
      catch case ex: Throwable => Failure(ex)
    )
}

/** Entry point used by delimcc_multishot.c / eh.cpp. Kept outside of
 *  `Continuations` so that exporting it does not make `Continuations` (and
 *  delimcc) always reachable.
 */
private[runtime] object ContinuationsExceptions:
  @exported("scalanative_continuation_exception_to_failure")
  def continuationExceptionToFailure(exception: Throwable): Try[Any] =
    Failure(exception)
