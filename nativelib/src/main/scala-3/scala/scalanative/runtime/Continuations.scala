package scala.scalanative.runtime

import scala.scalanative.unsafe.*
import scala.scalanative.runtime.libc.{malloc, free}
import scala.scalanative.unsafe.Size.intToSize
import scala.scalanative.runtime.Intrinsics.*

object Continuations:
  import ContImpl.*

  type ContFnT = ContImpl.BoundaryLabel => Ptr[Byte]

  private inline def pNull[T]: Ptr[T] = fromRawPtr[T](castIntToRawPtr(0))

  private inline def pObj[T](o: T) = fromRawPtr[Byte](
    castObjectToRawPtr(o.asInstanceOf[Object])
  )

  private inline def objP[T](p: Ptr[Byte]) =
    castRawPtrToObject(p.rawptr).asInstanceOf[T]

  inline def boundary[T](inline f: BoundaryLabel[T] ?=> T)(using Tag[T]): T =
    val call: ContFnT = (x: ContImpl.BoundaryLabel) => pObj(f(using x))
    val resP = ContImpl.boundary(contFn, pObj(call))
    objP[T](resP)

  opaque type BoundaryLabel[-T] = ContImpl.BoundaryLabel

  type Continuation[-R, +T] = ContImpl.Continuation

  inline def suspend[R, T](
      inline f: (R => T) => T
  )(using label: BoundaryLabel[T])(using
      Tag[T],
      Tag[R]
  ): R =
    suspendCont(cont => f(r => resumeCont(cont, r)))

  def suspend[T](
      f: (() => T) => T
  )(using label: BoundaryLabel[T])(using Tag[T]): Unit =
    val call = (cont: ContImpl.Continuation) => pObj(f(() => resumeCont(cont)))
    ContImpl.suspend(label, suspendFn, pObj(call))

  inline def suspendCont[R, T](
      inline f: Continuation[R, T] => T
  )(using label: BoundaryLabel[T])(using
      Tag[T],
      Tag[R]
  ): R =
    val call = (cont: ContImpl.Continuation) => pObj(f(cont))
    val resP = ContImpl.suspend(label, suspendFn, pObj(call))
    objP[R](resP)

  inline def resumeCont[R, T](cont: Continuation[R, T], input: R)(using
      Tag[T],
      Tag[R]
  ): T =
    objP[T](ContImpl.resume(cont, pObj(input)))

  // special case for R = Unit
  inline private def resumeCont[T](cont: Continuation[Unit, T])(using
      Tag[T]
  ): T =
    objP[T](ContImpl.resume(cont, pNull))

  private val suspendFn: SuspendFn = CFuncPtr2.fromScalaFunction((cont, f) =>
    val fn = objP[ContImpl.Continuation => Ptr[Byte]](f)
    fn(cont)
  )

  private val contFn: ContFn = CFuncPtr2.fromScalaFunction((label, f) =>
    val fp = objP[ContFnT](f)
    fp(label)
  )

  private def allocateBlob(size: CUnsignedLong): Ptr[Byte] =
    val obj = ObjectArray.alloc(size.toInt) // round up the blob size
    obj.at(0).asInstanceOf[Ptr[Byte]]

  ContImpl.setAlloc(CFuncPtr1.fromScalaFunction(allocateBlob))

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
  def suspend(l: BoundaryLabel, f: SuspendFn, arg: Ptr[Byte]): Ptr[Byte] =
    extern

  @name("cont_resume")
  def resume(cont: Continuation, arg: Ptr[Byte]): Ptr[Byte] = extern

  @name("cont_set_alloc")
  def setAlloc(fn: CFuncPtr1[CUnsignedLong, Ptr[Byte]]): Unit = extern
