package scala.scalanative.runtime

import scala.scalanative.unsafe.*
import scala.scalanative.runtime.libc.{malloc, free}
import scala.scalanative.unsafe.Size.intToSize
import scala.scalanative.runtime.Intrinsics.castIntToRawPtr

object Continuations:
  import ContImpl.*
  inline def boundary[T](inline f: BoundaryLabel[T] ?=> T)(using Tag[T]): T =
    val fPtr = fromRawPtr[ContImpl.BoundaryLabel ?=> Ptr[Byte]](
      malloc(sizeOf[Object].toUSize)
    )
    !fPtr = (x: ContImpl.BoundaryLabel) ?=>
      val r = f
      val resPtr = fromRawPtr[T](malloc(sizeof[T]))
      !resPtr = r
      resPtr.asInstanceOf[Ptr[Byte]]
    val resP = ContImpl.boundary(contFn, fPtr.asInstanceOf[Ptr[Byte]])
    val res = !resP.asInstanceOf[Ptr[T]]
    free(resP)
    res

  opaque type BoundaryLabel[+T] = ContImpl.BoundaryLabel

  opaque type Continuation[-R, +T] = ContImpl.Continuation

  inline def suspend[R, T](
      inline f: (R => T) => T
  )(using label: BoundaryLabel[T])(using
      Tag[T],
      Tag[R]
  ): R =
    suspendCont(cont => f(r => resumeCont(cont, r)))

  inline def suspend[T](
      inline f: (() => T) => T
  )(using label: BoundaryLabel[T])(using Tag[T]): Unit =
    val fPtr =
      fromRawPtr[ContImpl.Continuation => Ptr[Byte]](
        malloc(sizeOf[Object].toUSize)
      )
    !fPtr = (cont: ContImpl.Continuation) =>
      val r = f(() => resumeCont(cont))
      val resPtr = fromRawPtr[T](malloc(sizeof[T]))
      !resPtr = r
      resPtr.asInstanceOf[Ptr[Byte]]
    ContImpl.suspend(label, suspendFn, fPtr.asInstanceOf[Ptr[Byte]])

  inline def suspendCont[R, T](
      inline f: Continuation[R, T] => T
  )(using label: BoundaryLabel[T])(using
      Tag[T],
      Tag[R]
  ): R =
    val fPtr =
      fromRawPtr[ContImpl.Continuation => Ptr[Byte]](
        malloc(sizeOf[Object].toUSize)
      )
    !fPtr = (cont: ContImpl.Continuation) =>
      val r = f(cont)
      val resPtr = fromRawPtr[T](malloc(sizeof[T]))
      !resPtr = r
      resPtr.asInstanceOf[Ptr[Byte]]
    val resP = ContImpl.suspend(label, suspendFn, fPtr.asInstanceOf[Ptr[Byte]])
    val res = !resP.asInstanceOf[Ptr[R]]
    free(resP)
    res

  inline def resumeCont[R, T](cont: Continuation[R, T], input: R)(using
      Tag[T],
      Tag[R]
  ): T =
    val rPtr = fromRawPtr[R](malloc(sizeOf[R].toUSize))
    !rPtr = input
    val resP =
      ContImpl.resume(cont, rPtr.asInstanceOf[Ptr[Byte]]).asInstanceOf[Ptr[T]]
    val res = !resP
    free(resP)
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
    free(resP)
    res

  private val suspendFn: SuspendFn = CFuncPtr2.fromScalaFunction((cont, f) =>
    val fp = f.asInstanceOf[Ptr[ContImpl.Continuation => Ptr[Byte]]]
    val fn = !fp
    free(fp)
    fn(cont)
  )

  private val contFn: ContFn = CFuncPtr2.fromScalaFunction((label, f) =>
    given ContImpl.BoundaryLabel = label
    val fp = f.asInstanceOf[Ptr[ContImpl.BoundaryLabel ?=> Ptr[Byte]]]
    val fn = !fp
    free(fp)
    fn
  )
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
