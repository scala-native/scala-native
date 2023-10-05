package scala.scalanative.runtime

import scala.scalanative.unsafe.*
import scala.scalanative.runtime.libc.{malloc, free}
import scala.scalanative.runtime.Intrinsics.*
import scala.collection.mutable

import scala.util.Try
import scala.scalanative.meta.LinktimeInfo.isWindows

object Continuations:
  import Impl.*

  /** A marker for a given `boundary`. Use `break` or `suspend` to suspend the
   *  continuation up to the specified `boundary`. This value MUST NOT escape
   *  the `boundary` that created it.
   */
  opaque type BoundaryLabel[T] = Impl.BoundaryLabel

  /** The C implementation lets us set up how the Continuation structs (holding
   *  the reified stack fragment) is allocated, through a custom function that
   *  would allocate a blob of memory of the given size.
   *
   *  We want our implementation to use the allocations done by
   *  `Continuation.alloc`, so that the GC is aware of the stack fragment.
   */
  Impl.init(CFuncPtr2.fromScalaFunction(allocateBlob))

  /** Marks the given body as suspendable with a `BoundaryLabel` that `suspend`
   *  can refer to. Forwards the return value of `body`, or the return value of
   *  the `suspend` call, if it happens during the execution of `f`.
   *
   *  __Safety__: the passed-in `BoundaryLabel` cannot be used outside of the
   *  scope of the body. Suspending to a `BoundaryLabel` not created by a
   *  `boundary` call higher on the same call stack is undefined behaviour.
   */
  inline def boundary[T](inline body: BoundaryLabel[T] ?=> T): T =
    // Disable on Windows
    if isWindows then UnsupportedFeature.continuations()

    val call: ContinuationBody[T] = (x: Impl.BoundaryLabel) =>
      Try(body(using x))
    Impl.boundary(boundaryBodyFn, call).get

  /** Suspends the current running stack up to the corresponding `boundary` into
   *  a continuation `cont: R => T` and passes it into `f`. The return value of
   *  `f(cont)` is returned to `boundary`.
   *
   *  Same as `suspendCont`, but hides the fact that the passed in function is a
   *  continuation.
   */
  inline def suspend[R, T](
      inline onSuspend: (R => T) => T
  )(using label: BoundaryLabel[T]): R =
    suspendContinuation[R, T](onSuspend)

  /** Same as `suspend` where the continuation expects no parameters. */
  inline def suspend[T](
      inline onSuspend: (() => T) => T
  )(using label: BoundaryLabel[T]): Unit =
    suspendContinuation[Unit, T](k => onSuspend(() => k(())))

  /** Immediately return to the `boundary`. */
  inline def break(using BoundaryLabel[Unit]): Nothing =
    suspend[Nothing, Unit](_ => ())

  /** Immediately return to the `boundary`, returning the given value. */
  inline def break[T](inline value: T)(using BoundaryLabel[T]): Nothing =
    suspend[Nothing, T](_ => value)

  /** Suspends the computation up to the corresponding `BoundaryLabel`, passing
   *  the stored Continuation to `onSuspend` and passed its result to
   *  `boundary`'s caller. Returns when the continuation gets resumed.
   */
  private inline def suspendContinuation[R, T](
      inline onSuspend: Continuation[R, T] => T
  )(using label: BoundaryLabel[T]): R =
    val continuation = Continuation[R, T]()
    // We want to reify the post-suspension body here,
    // while creating the full continuation object.
    val call: SuspendFn[R, T] = innerContinuation =>
      continuation.inner = innerContinuation
      Try(onSuspend(continuation))
    Impl.suspend(label, suspendFn, call, continuation)

  /** A `Continuation` holds the C implementation continuation pointer,
   *  alongside a list of `ObjectArray`s, used for storing suspended fragments
   *  of the stack.
   *
   *  These fragments need to be treated as possibly containing pointers into
   *  the GC heap, and so needs to be scanned by the GC. We store them in an
   *  `ObjectArray` to simulate just that.
   */
  private[Continuations] class Continuation[-R, +T] extends (R => T):
    private[Continuations] var inner: Impl.Continuation = fromRawPtr(
      castIntToRawPtr(0)
    ) // null
    private val allocas = mutable.ArrayBuffer[ObjectArray]()

    def apply(x: R): T =
      resume(inner, x).get

    private[Continuations] def alloc(size: CUnsignedLong): Ptr[Byte] =
      val obj = ObjectArray.alloc(size.toInt) // round up the blob size
      allocas += obj
      obj.at(0).asInstanceOf[Ptr[Byte]]
  end Continuation

  // STATIC FUNCTIONS THAT CALL PASSED-IN FUNCTION OBJECTS

  /** Transformed version of the suspend lambda, to be passed to cont_suspend.
   *  Takes:
   *    - `continuation`: the reified continuation
   *    - `onSuspend`: The suspend lambda as Continuation => Ptr[Byte] (the
   *      returned object, cast to a pointer)
   *
   *  Returns Ptr[Byte] / void*.
   */
  inline def suspendFn[R, T] = suspendFnAny.asInstanceOf[SuspendFnPtr[R, T]]
  private val suspendFnAny: SuspendFnPtr[Any, Any] =
    CFuncPtr2.fromScalaFunction((continuation, onSuspend) =>
      onSuspend(continuation)
    )

  /** Transformed version of the boundary body, to be passed to cont_boundary.
   *  Takes:
   *    - `label`: the boundary label
   *    - `arg`: The boundary body as BoundaryLabel ?=> Ptr[Byte] (the returned
   *      object, cast to a pointer)
   *
   *  Returns Ptr[Byte] / void*.
   */
  inline def boundaryBodyFn[T] =
    boundaryBodyFnAny.asInstanceOf[ContinuationBodyPtr[T]]
  private val boundaryBodyFnAny: ContinuationBodyPtr[Any] =
    CFuncPtr2.fromScalaFunction((label, arg) => arg(label))

  /** Allocate a blob of memory of `size` bytes, from `continuation`'s
   *  implementation of `Continuation.alloc`.
   */
  private def allocateBlob(
      size: CUnsignedLong,
      continuation: Continuation[Any, Any]
  ): Ptr[Byte] = continuation.alloc(size)

  /** Continuations implementation imported from C (see `delimcc.h`) */
  @extern private object Impl:
    private type ContinuationLabel = CUnsignedLong
    type BoundaryLabel = ContinuationLabel

    type Continuation = Ptr[Byte]

    // We narrow the arguments of `boundary` to functions returning Try[T]
    type ContinuationBody[T] = BoundaryLabel => Try[T]
    type ContinuationBodyPtr[T] =
      CFuncPtr2[BoundaryLabel, /* arg */ ContinuationBody[T], Try[T]]

    @name("scalanative_continuation_boundary")
    def boundary[T](
        body: ContinuationBodyPtr[T],
        arg: ContinuationBody[T]
    ): Try[T] = extern

    // Similar to `boundary`, we narrow the functions passed to `suspend` to ones returning Try[T]
    type SuspendFn[R, T] = Continuation => Try[T]
    type SuspendFnPtr[R, T] =
      CFuncPtr2[Continuation, /* arg */ SuspendFn[R, T], Try[T]]

    @name("scalanative_continuation_suspend")
    def suspend[R, T](
        l: BoundaryLabel,
        f: SuspendFnPtr[R, T],
        arg: SuspendFn[R, T],
        allocArg: Continuations.Continuation[R, T]
    ): R =
      extern

    @name("scalanative_continuation_resume")
    def resume[R, T](continuation: Continuation, arg: R): Try[T] = extern

    @name("scalanative_continuation_init") def init(
        continuation_alloc_fn: CFuncPtr2[
          CUnsignedLong,
          Continuations.Continuation[Any, Any],
          Ptr[Byte]
        ]
    ): Unit =
      extern
  end Impl
end Continuations
