package scala.scalanative.runtime

import scala.scalanative.unsafe.*
import scala.scalanative.runtime.libc.{malloc, free}
import scala.scalanative.unsafe.Size.intToSize
import scala.scalanative.runtime.Intrinsics.castIntToRawPtr

object Continuations:
  import ContImpl.*

  private def allocObj[T](using Tag[T]) =
    val arr = ObjectArray.alloc(1)
    arr.at(0).asInstanceOf[Ptr[T]]

  inline def boundary[T](inline f: BoundaryLabel[T] ?=> T)(using Tag[T]): T =
    val fPtr = allocObj[ContImpl.BoundaryLabel ?=> Ptr[Byte]]
    !fPtr = (x: ContImpl.BoundaryLabel) ?=>
      val r = f
      val resPtr = allocObj[T]
      !resPtr = r
      resPtr.asInstanceOf[Ptr[Byte]]
    val resP = ContImpl.boundary(contFn, fPtr.asInstanceOf[Ptr[Byte]])
    !resP.asInstanceOf[Ptr[T]]

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
    val fPtr = allocObj[ContImpl.Continuation => Ptr[Byte]]
    !fPtr = (cont: ContImpl.Continuation) =>
      val r = f(() => resumeCont(cont))
      val resPtr = allocObj[T]
      !resPtr = r
      resPtr.asInstanceOf[Ptr[Byte]]
    ContImpl.suspend(label, suspendFn, fPtr.asInstanceOf[Ptr[Byte]])

  inline def suspendCont[R, T](
      inline f: Continuation[R, T] => T
  )(using label: BoundaryLabel[T])(using
      Tag[T],
      Tag[R]
  ): R =
    val fPtr = allocObj[ContImpl.Continuation => Ptr[Byte]]
    !fPtr = (cont: ContImpl.Continuation) =>
      val r = f(cont)
      val resPtr = allocObj[T]
      !resPtr = r
      resPtr.asInstanceOf[Ptr[Byte]]
    val resP = ContImpl.suspend(label, suspendFn, fPtr.asInstanceOf[Ptr[Byte]])
    !resP.asInstanceOf[Ptr[R]]

  inline def resumeCont[R, T](cont: Continuation[R, T], input: R)(using
      Tag[T],
      Tag[R]
  ): T =
    val rPtr = allocObj[R]
    !rPtr = input
    val resP =
      ContImpl.resume(cont, rPtr.asInstanceOf[Ptr[Byte]]).asInstanceOf[Ptr[T]]
    val res = !resP
    res

  // special case for R = Unit
  inline private def resumeCont[T](cont: Continuation[Unit, T])(using
      Tag[T]
  ): T =
    val resP =
      ContImpl
        .resume(cont, fromRawPtr[Byte](castIntToRawPtr(0)))
        .asInstanceOf[Ptr[T]]
    val res = !resP
    res

  private val suspendFn: SuspendFn = CFuncPtr2.fromScalaFunction((cont, f) =>
    val fp = f.asInstanceOf[Ptr[ContImpl.Continuation => Ptr[Byte]]]
    val fn = !fp
    fn(cont)
  )

  private val contFn: ContFn = CFuncPtr2.fromScalaFunction((label, f) =>
    given ContImpl.BoundaryLabel = label
    val fp = f.asInstanceOf[Ptr[ContImpl.BoundaryLabel ?=> Ptr[Byte]]]
    !fp
  )

  private def allocateBlob(size: CUnsignedLong): Ptr[Byte] =
    val obj = ObjectArray.alloc((size.toInt + 7) / 8) // round up the blob size
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
