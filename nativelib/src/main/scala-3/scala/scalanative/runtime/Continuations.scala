package scala.scalanative.runtime

import scala.scalanative.unsafe.*
import scala.scalanative.runtime.libc.{malloc, free}
import scala.scalanative.unsafe.Size.intToSize
import scala.scalanative.runtime.Intrinsics.*
import scala.collection.mutable

object Continuations:
  import ContImpl.*

  inline def boundary[T](inline f: BoundaryLabel[T] ?=> T): T =
    val call: ContFnT = (x: ContImpl.BoundaryLabel) => pObj(f(using x))
    val resP = ContImpl.boundary(contFn, pObj(call))
    objP[T](resP)

  opaque type BoundaryLabel[-T] = ContImpl.BoundaryLabel

  inline def break(using BoundaryLabel[Unit]): Nothing =
    suspend[Nothing, Unit](_ => ())
  inline def break[T](value: T)(using BoundaryLabel[T]): Nothing =
    suspend[Nothing, T](_ => value)

  inline def suspend[R, T](
      inline f: (R => T) => T
  )(using label: BoundaryLabel[T]): R =
    suspendCont[R, T](cont => f(r => cont(r)))

  def suspend[T](
      f: (() => T) => T
  )(using label: BoundaryLabel[T]): Unit =
    suspendCont[Unit, T](cont => f(() => cont(())))

  inline def suspendCont[R, T](
      inline f: Continuation[R, T] => T
  )(using label: BoundaryLabel[T]): R =
    val cont = Continuation()
    val call = (c: ContImpl.Continuation) =>
      cont.cont = c
      pObj(f(cont))
    val resP = ContImpl.suspend(label, suspendFn, pObj(call), pObj(cont))
    objP[R](resP)

  class Continuation[-R, +T] extends (R => T):
    private[Continuations] var cont: ContImpl.Continuation = pNull
    private val allocs = mutable.ArrayBuffer[ObjectArray]()

    def apply(x: R): T = objP[T](resume(cont, pObj(x)))

    private[Continuations] def alloc(size: CUnsignedLong): Ptr[Byte] =
      val obj = ObjectArray.alloc(size.toInt) // round up the blob size
      allocs += obj
      obj.at(0).asInstanceOf[Ptr[Byte]]
  end Continuation

  // STATIC FUNCTIONS THAT CALL PASSED-IN FUNCTION OBJECTS

  private val suspendFn: SuspendFn = CFuncPtr2.fromScalaFunction((cont, f) =>
    val fn = objP[ContImpl.Continuation => Ptr[Byte]](f)
    fn(cont)
  )

  private val contFn: ContFn = CFuncPtr2.fromScalaFunction((label, f) =>
    val fp = objP[ContFnT](f)
    fp(label)
  )

  private def allocateBlob(size: CUnsignedLong, cont: Ptr[Byte]): Ptr[Byte] =
    objP[Continuation[_, _]](cont).alloc(size)

  ContImpl.setAlloc(CFuncPtr2.fromScalaFunction(allocateBlob))

  // FOR WORKING WITH POINTERS

  type ContFnT = ContImpl.BoundaryLabel => Ptr[Byte]

  private inline def pNull[T]: Ptr[T] = fromRawPtr[T](castIntToRawPtr(0))

  private inline def pObj[T](o: T) = fromRawPtr[Byte](
    castObjectToRawPtr(o.asInstanceOf[Object])
  )

  private inline def objP[T](p: Ptr[Byte]) =
    castRawPtrToObject(p.rawptr).asInstanceOf[T]

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

  @name("cont_set_alloc") def setAlloc(
      fn: CFuncPtr2[CUnsignedLong, Ptr[Byte], Ptr[Byte]]
  ): Unit =
    extern
