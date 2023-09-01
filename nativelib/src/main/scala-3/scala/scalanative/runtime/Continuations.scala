package scala.scalanative.runtime

import scala.scalanative.unsafe.*
import scala.scalanative.runtime.libc.{malloc, free}
import scala.scalanative.runtime.Intrinsics.*
import scala.collection.mutable

import scala.util.Try

object Continuations:
  import ContImpl.*

  opaque type BoundaryLabel[-T] = ContImpl.BoundaryLabel

  ContImpl.init(CFuncPtr2.fromScalaFunction(allocateBlob))

  /** Marks the given body as suspendable with a `BoundaryLabel` that `suspend`
   *  can refer to. Forwards the return value of `body`, or the return value of
   *  the `suspend` call, if it happens during the execution of `f`.
   *
   *  __Safety__: the passed-in `BoundaryLabel` cannot be used outside of the
   *  scope of the body. Suspending to a `BoundaryLabel` not created by a
   *  `boundary` call higher on the same call stack is undefined behaviour.
   */
  inline def boundary[T](inline body: BoundaryLabel[T] ?=> T): T =
    val call: ContFnT = (x: ContImpl.BoundaryLabel) =>
      objToPtr(Try(body(using x)))
    val resP = ContImpl.boundary(contFn, objToPtr(call))
    objFromPtr(resP).asInstanceOf[Try[T]].get

  /** Suspends the current running stack up to the corresponding `boundary` into
   *  a continuation `cont: R => T` and passes it into `f`. The return value of
   *  `f(cont)` is returned to `boundary`.
   *
   *  Same as `suspendCont`, but hides the fact that the passed in function is a
   *  continuation.
   */
  inline def suspend[R, T](
      inline f: (R => T) => T
  )(using label: BoundaryLabel[T]): R =
    suspendCont[R, T](f)

  /** Same as `suspend` where the continuation expects no parameters. */
  def suspend[T](
      f: (() => T) => T
  )(using label: BoundaryLabel[T]): Unit =
    suspendCont[Unit, T](cont => f(() => cont(())))

  /** Immediately return to the `boundary`. */
  inline def break(using BoundaryLabel[Unit]): Nothing =
    suspend[Nothing, Unit](_ => ())

  /** Immediately return to the `boundary`, returning the given value. */
  inline def break[T](value: T)(using BoundaryLabel[T]): Nothing =
    suspend[Nothing, T](_ => value)

  inline def suspendCont[R, T](
      inline f: Continuation[R, T] => T
  )(using label: BoundaryLabel[T]): R =
    val cont = Continuation()
    // We want to reify the post-suspension body here,
    // while creating the full continuation object.
    val call = (c: ContImpl.Continuation) =>
      cont.cont = c
      objToPtr(Try(f(cont)))
    val resP =
      ContImpl.suspend(label, suspendFn, objToPtr(call), objToPtr(cont))
    objFromPtr(resP).asInstanceOf[R]

  /** A `Continuation` holds the C implementation continuation pointer,
   *  alongside a list of `ObjectArray`s, used for storing suspended fragments
   *  of the stack.
   *
   *  These fragments need to be treated as possibly containing pointers into
   *  the GC heap, and so needs to be scanned by the GC. We store them in an
   *  `ObjectArray` to simulate just that.
   */
  class Continuation[-R, +T] extends (R => T):
    private[Continuations] var cont: ContImpl.Continuation = nullPtr
    private val allocs = mutable.ArrayBuffer[ObjectArray]()

    def apply(x: R): T =
      objFromPtr(resume(cont, objToPtr(x))).asInstanceOf[Try[T]].get

    private[Continuations] def alloc(size: CUnsignedLong): Ptr[Byte] =
      val obj = ObjectArray.alloc(size.toInt) // round up the blob size
      allocs += obj
      obj.at(0).asInstanceOf[Ptr[Byte]]
  end Continuation

  // STATIC FUNCTIONS THAT CALL PASSED-IN FUNCTION OBJECTS

  /** Transformed version of the suspend lambda, to be passed to cont_suspend.
   *  Takes:
   *    - `cont`: the reified continuation
   *    - `arg`: The suspend lambda as Continuation => Ptr[Byte] (the returned
   *      object, cast to a pointer)
   *
   *  Returns Ptr[Byte] / void*.
   */
  private val suspendFn: SuspendFn = CFuncPtr2.fromScalaFunction((cont, arg) =>
    val fn = objFromPtr(arg).asInstanceOf[ContImpl.Continuation => Ptr[Byte]]
    fn(cont)
  )

  /** Transformed version of the boundary body, to be passed to cont_boundary.
   *  Takes:
   *    - `label`: the boundary label
   *    - `arg`: The boundary body as BoundaryLabel ?=> Ptr[Byte] (the returned
   *      object, cast to a pointer)
   *
   *  Returns Ptr[Byte] / void*.
   */
  private val contFn: ContFn = CFuncPtr2.fromScalaFunction((label, arg) =>
    val fp = objFromPtr(arg).asInstanceOf[ContFnT]
    fp(label)
  )

  private def allocateBlob(size: CUnsignedLong, cont: Ptr[Byte]): Ptr[Byte] =
    objFromPtr(cont).asInstanceOf[Continuation[_, _]].alloc(size)

  // FOR WORKING WITH POINTERS

  type ContFnT = ContImpl.BoundaryLabel => Ptr[Byte]

  private inline def nullPtr[T]: Ptr[T] = fromRawPtr[T](castIntToRawPtr(0))

  // Cast object to/from pointer.
  private inline def objToPtr(o: Any) =
    fromRawPtr[Byte](castObjectToRawPtr(o.asInstanceOf[AnyRef]))
  private inline def objFromPtr(p: Ptr[Byte]): AnyRef =
    castRawPtrToObject(p.rawptr)

end Continuations

@extern private object ContImpl:
  private type ContLabel = CUnsignedLong
  // in reality this is a struct containing the id,
  // but it's not important
  type BoundaryLabel = ContLabel

  type Continuation = Ptr[Byte]

  type ContFn = CFuncPtr2[BoundaryLabel, /* arg */ Ptr[Byte], Ptr[Byte]]

  type SuspendFn = CFuncPtr2[Continuation, /* arg */ Ptr[Byte], Ptr[Byte]]

  @name("cont_boundary")
  def boundary(f: ContFn, arg: Ptr[Byte]): Ptr[Byte] = extern

  @name("cont_suspend")
  def suspend(
      l: BoundaryLabel,
      f: SuspendFn,
      arg: Ptr[Byte],
      allocArg: Ptr[Byte]
  ): Ptr[Byte] =
    extern

  @name("cont_resume")
  def resume(cont: Continuation, arg: Ptr[Byte]): Ptr[Byte] = extern

  @name("cont_init") def init(
      fn: CFuncPtr2[CUnsignedLong, Ptr[Byte], Ptr[Byte]]
  ): Unit =
    extern
