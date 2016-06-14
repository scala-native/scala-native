// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 1)
package scala.scalanative
package native

import scalanative.runtime.undefined
import scala.reflect.ClassTag

/** C-style function pointer. */
sealed abstract class FunctionPtr

object FunctionPtr {

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction0[R](f: Function0[R]): FunctionPtr0[R] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction1[T1, R](f: Function1[T1, R]): FunctionPtr1[T1, R] =
    undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction2[T1, T2, R](
      f: Function2[T1, T2, R]): FunctionPtr2[T1, T2, R] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction3[T1, T2, T3, R](
      f: Function3[T1, T2, T3, R]): FunctionPtr3[T1, T2, T3, R] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction4[T1, T2, T3, T4, R](
      f: Function4[T1, T2, T3, T4, R]): FunctionPtr4[T1, T2, T3, T4, R] =
    undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction5[T1, T2, T3, T4, T5, R](
      f: Function5[T1, T2, T3, T4, T5, R])
    : FunctionPtr5[T1, T2, T3, T4, T5, R] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction6[T1, T2, T3, T4, T5, T6, R](
      f: Function6[T1, T2, T3, T4, T5, T6, R])
    : FunctionPtr6[T1, T2, T3, T4, T5, T6, R] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction7[T1, T2, T3, T4, T5, T6, T7, R](
      f: Function7[T1, T2, T3, T4, T5, T6, T7, R])
    : FunctionPtr7[T1, T2, T3, T4, T5, T6, T7, R] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction8[T1, T2, T3, T4, T5, T6, T7, T8, R](
      f: Function8[T1, T2, T3, T4, T5, T6, T7, T8, R])
    : FunctionPtr8[T1, T2, T3, T4, T5, T6, T7, T8, R] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](
      f: Function9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R])
    : FunctionPtr9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](
      f: Function10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R])
    : FunctionPtr10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R](
      f: Function11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R])
    : FunctionPtr11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R] =
    undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction12[
      T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R](
      f: Function12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R])
    : FunctionPtr12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R] =
    undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction13[
      T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R](
      f: Function13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R])
    : FunctionPtr13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R] =
    undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction14[
      T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R](
      f: Function14[
          T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R])
    : FunctionPtr14[
        T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R] =
    undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction15[
      T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R](
      f: Function15[
          T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R])
    : FunctionPtr15[
        T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R] =
    undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction16[T1,
                              T2,
                              T3,
                              T4,
                              T5,
                              T6,
                              T7,
                              T8,
                              T9,
                              T10,
                              T11,
                              T12,
                              T13,
                              T14,
                              T15,
                              T16,
                              R](
      f: Function16[T1,
                    T2,
                    T3,
                    T4,
                    T5,
                    T6,
                    T7,
                    T8,
                    T9,
                    T10,
                    T11,
                    T12,
                    T13,
                    T14,
                    T15,
                    T16,
                    R]): FunctionPtr16[T1,
                                       T2,
                                       T3,
                                       T4,
                                       T5,
                                       T6,
                                       T7,
                                       T8,
                                       T9,
                                       T10,
                                       T11,
                                       T12,
                                       T13,
                                       T14,
                                       T15,
                                       T16,
                                       R] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction17[T1,
                              T2,
                              T3,
                              T4,
                              T5,
                              T6,
                              T7,
                              T8,
                              T9,
                              T10,
                              T11,
                              T12,
                              T13,
                              T14,
                              T15,
                              T16,
                              T17,
                              R](
      f: Function17[T1,
                    T2,
                    T3,
                    T4,
                    T5,
                    T6,
                    T7,
                    T8,
                    T9,
                    T10,
                    T11,
                    T12,
                    T13,
                    T14,
                    T15,
                    T16,
                    T17,
                    R]): FunctionPtr17[T1,
                                       T2,
                                       T3,
                                       T4,
                                       T5,
                                       T6,
                                       T7,
                                       T8,
                                       T9,
                                       T10,
                                       T11,
                                       T12,
                                       T13,
                                       T14,
                                       T15,
                                       T16,
                                       T17,
                                       R] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction18[T1,
                              T2,
                              T3,
                              T4,
                              T5,
                              T6,
                              T7,
                              T8,
                              T9,
                              T10,
                              T11,
                              T12,
                              T13,
                              T14,
                              T15,
                              T16,
                              T17,
                              T18,
                              R](
      f: Function18[T1,
                    T2,
                    T3,
                    T4,
                    T5,
                    T6,
                    T7,
                    T8,
                    T9,
                    T10,
                    T11,
                    T12,
                    T13,
                    T14,
                    T15,
                    T16,
                    T17,
                    T18,
                    R]): FunctionPtr18[T1,
                                       T2,
                                       T3,
                                       T4,
                                       T5,
                                       T6,
                                       T7,
                                       T8,
                                       T9,
                                       T10,
                                       T11,
                                       T12,
                                       T13,
                                       T14,
                                       T15,
                                       T16,
                                       T17,
                                       T18,
                                       R] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction19[T1,
                              T2,
                              T3,
                              T4,
                              T5,
                              T6,
                              T7,
                              T8,
                              T9,
                              T10,
                              T11,
                              T12,
                              T13,
                              T14,
                              T15,
                              T16,
                              T17,
                              T18,
                              T19,
                              R](
      f: Function19[T1,
                    T2,
                    T3,
                    T4,
                    T5,
                    T6,
                    T7,
                    T8,
                    T9,
                    T10,
                    T11,
                    T12,
                    T13,
                    T14,
                    T15,
                    T16,
                    T17,
                    T18,
                    T19,
                    R]): FunctionPtr19[T1,
                                       T2,
                                       T3,
                                       T4,
                                       T5,
                                       T6,
                                       T7,
                                       T8,
                                       T9,
                                       T10,
                                       T11,
                                       T12,
                                       T13,
                                       T14,
                                       T15,
                                       T16,
                                       T17,
                                       T18,
                                       T19,
                                       R] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction20[T1,
                              T2,
                              T3,
                              T4,
                              T5,
                              T6,
                              T7,
                              T8,
                              T9,
                              T10,
                              T11,
                              T12,
                              T13,
                              T14,
                              T15,
                              T16,
                              T17,
                              T18,
                              T19,
                              T20,
                              R](
      f: Function20[T1,
                    T2,
                    T3,
                    T4,
                    T5,
                    T6,
                    T7,
                    T8,
                    T9,
                    T10,
                    T11,
                    T12,
                    T13,
                    T14,
                    T15,
                    T16,
                    T17,
                    T18,
                    T19,
                    T20,
                    R]): FunctionPtr20[T1,
                                       T2,
                                       T3,
                                       T4,
                                       T5,
                                       T6,
                                       T7,
                                       T8,
                                       T9,
                                       T10,
                                       T11,
                                       T12,
                                       T13,
                                       T14,
                                       T15,
                                       T16,
                                       T17,
                                       T18,
                                       T19,
                                       T20,
                                       R] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction21[T1,
                              T2,
                              T3,
                              T4,
                              T5,
                              T6,
                              T7,
                              T8,
                              T9,
                              T10,
                              T11,
                              T12,
                              T13,
                              T14,
                              T15,
                              T16,
                              T17,
                              T18,
                              T19,
                              T20,
                              T21,
                              R](
      f: Function21[T1,
                    T2,
                    T3,
                    T4,
                    T5,
                    T6,
                    T7,
                    T8,
                    T9,
                    T10,
                    T11,
                    T12,
                    T13,
                    T14,
                    T15,
                    T16,
                    T17,
                    T18,
                    T19,
                    T20,
                    T21,
                    R]): FunctionPtr21[T1,
                                       T2,
                                       T3,
                                       T4,
                                       T5,
                                       T6,
                                       T7,
                                       T8,
                                       T9,
                                       T10,
                                       T11,
                                       T12,
                                       T13,
                                       T14,
                                       T15,
                                       T16,
                                       T17,
                                       T18,
                                       T19,
                                       T20,
                                       T21,
                                       R] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 14)

  implicit def fromFunction22[T1,
                              T2,
                              T3,
                              T4,
                              T5,
                              T6,
                              T7,
                              T8,
                              T9,
                              T10,
                              T11,
                              T12,
                              T13,
                              T14,
                              T15,
                              T16,
                              T17,
                              T18,
                              T19,
                              T20,
                              T21,
                              T22,
                              R](
      f: Function22[T1,
                    T2,
                    T3,
                    T4,
                    T5,
                    T6,
                    T7,
                    T8,
                    T9,
                    T10,
                    T11,
                    T12,
                    T13,
                    T14,
                    T15,
                    T16,
                    T17,
                    T18,
                    T19,
                    T20,
                    T21,
                    T22,
                    R]): FunctionPtr22[T1,
                                       T2,
                                       T3,
                                       T4,
                                       T5,
                                       T6,
                                       T7,
                                       T8,
                                       T9,
                                       T10,
                                       T11,
                                       T12,
                                       T13,
                                       T14,
                                       T15,
                                       T16,
                                       T17,
                                       T18,
                                       T19,
                                       T20,
                                       T21,
                                       T22,
                                       R] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 18)
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr0[R] {
  def apply()(implicit ct1: ClassTag[R]): R = undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr1[T1, R] {
  def apply(arg1: T1)(implicit ct1: ClassTag[T1], ct2: ClassTag[R]): R =
    undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr2[T1, T2, R] {
  def apply(arg1: T1, arg2: T2)(
      implicit ct1: ClassTag[T1], ct2: ClassTag[T2], ct3: ClassTag[R]): R =
    undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr3[T1, T2, T3, R] {
  def apply(arg1: T1, arg2: T2, arg3: T3)(implicit ct1: ClassTag[T1],
                                          ct2: ClassTag[T2],
                                          ct3: ClassTag[T3],
                                          ct4: ClassTag[R]): R = undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr4[T1, T2, T3, T4, R] {
  def apply(arg1: T1, arg2: T2, arg3: T3, arg4: T4)(implicit ct1: ClassTag[T1],
                                                    ct2: ClassTag[T2],
                                                    ct3: ClassTag[T3],
                                                    ct4: ClassTag[T4],
                                                    ct5: ClassTag[R]): R =
    undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr5[T1, T2, T3, T4, T5, R] {
  def apply(arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5)(
      implicit ct1: ClassTag[T1],
      ct2: ClassTag[T2],
      ct3: ClassTag[T3],
      ct4: ClassTag[T4],
      ct5: ClassTag[T5],
      ct6: ClassTag[R]): R = undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr6[T1, T2, T3, T4, T5, T6, R] {
  def apply(arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6)(
      implicit ct1: ClassTag[T1],
      ct2: ClassTag[T2],
      ct3: ClassTag[T3],
      ct4: ClassTag[T4],
      ct5: ClassTag[T5],
      ct6: ClassTag[T6],
      ct7: ClassTag[R]): R = undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr7[T1, T2, T3, T4, T5, T6, T7, R] {
  def apply(
      arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7)(
      implicit ct1: ClassTag[T1],
      ct2: ClassTag[T2],
      ct3: ClassTag[T3],
      ct4: ClassTag[T4],
      ct5: ClassTag[T5],
      ct6: ClassTag[T6],
      ct7: ClassTag[T7],
      ct8: ClassTag[R]): R = undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr8[T1, T2, T3, T4, T5, T6, T7, T8, R] {
  def apply(arg1: T1,
            arg2: T2,
            arg3: T3,
            arg4: T4,
            arg5: T5,
            arg6: T6,
            arg7: T7,
            arg8: T8)(implicit ct1: ClassTag[T1],
                      ct2: ClassTag[T2],
                      ct3: ClassTag[T3],
                      ct4: ClassTag[T4],
                      ct5: ClassTag[T5],
                      ct6: ClassTag[T6],
                      ct7: ClassTag[T7],
                      ct8: ClassTag[T8],
                      ct9: ClassTag[R]): R = undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R] {
  def apply(arg1: T1,
            arg2: T2,
            arg3: T3,
            arg4: T4,
            arg5: T5,
            arg6: T6,
            arg7: T7,
            arg8: T8,
            arg9: T9)(implicit ct1: ClassTag[T1],
                      ct2: ClassTag[T2],
                      ct3: ClassTag[T3],
                      ct4: ClassTag[T4],
                      ct5: ClassTag[T5],
                      ct6: ClassTag[T6],
                      ct7: ClassTag[T7],
                      ct8: ClassTag[T8],
                      ct9: ClassTag[T9],
                      ct10: ClassTag[R]): R = undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R] {
  def apply(arg1: T1,
            arg2: T2,
            arg3: T3,
            arg4: T4,
            arg5: T5,
            arg6: T6,
            arg7: T7,
            arg8: T8,
            arg9: T9,
            arg10: T10)(implicit ct1: ClassTag[T1],
                        ct2: ClassTag[T2],
                        ct3: ClassTag[T3],
                        ct4: ClassTag[T4],
                        ct5: ClassTag[T5],
                        ct6: ClassTag[T6],
                        ct7: ClassTag[T7],
                        ct8: ClassTag[T8],
                        ct9: ClassTag[T9],
                        ct10: ClassTag[T10],
                        ct11: ClassTag[R]): R = undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R] {
  def apply(arg1: T1,
            arg2: T2,
            arg3: T3,
            arg4: T4,
            arg5: T5,
            arg6: T6,
            arg7: T7,
            arg8: T8,
            arg9: T9,
            arg10: T10,
            arg11: T11)(implicit ct1: ClassTag[T1],
                        ct2: ClassTag[T2],
                        ct3: ClassTag[T3],
                        ct4: ClassTag[T4],
                        ct5: ClassTag[T5],
                        ct6: ClassTag[T6],
                        ct7: ClassTag[T7],
                        ct8: ClassTag[T8],
                        ct9: ClassTag[T9],
                        ct10: ClassTag[T10],
                        ct11: ClassTag[T11],
                        ct12: ClassTag[R]): R = undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R] {
  def apply(arg1: T1,
            arg2: T2,
            arg3: T3,
            arg4: T4,
            arg5: T5,
            arg6: T6,
            arg7: T7,
            arg8: T8,
            arg9: T9,
            arg10: T10,
            arg11: T11,
            arg12: T12)(implicit ct1: ClassTag[T1],
                        ct2: ClassTag[T2],
                        ct3: ClassTag[T3],
                        ct4: ClassTag[T4],
                        ct5: ClassTag[T5],
                        ct6: ClassTag[T6],
                        ct7: ClassTag[T7],
                        ct8: ClassTag[T8],
                        ct9: ClassTag[T9],
                        ct10: ClassTag[T10],
                        ct11: ClassTag[T11],
                        ct12: ClassTag[T12],
                        ct13: ClassTag[R]): R = undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr13[
    T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R] {
  def apply(arg1: T1,
            arg2: T2,
            arg3: T3,
            arg4: T4,
            arg5: T5,
            arg6: T6,
            arg7: T7,
            arg8: T8,
            arg9: T9,
            arg10: T10,
            arg11: T11,
            arg12: T12,
            arg13: T13)(implicit ct1: ClassTag[T1],
                        ct2: ClassTag[T2],
                        ct3: ClassTag[T3],
                        ct4: ClassTag[T4],
                        ct5: ClassTag[T5],
                        ct6: ClassTag[T6],
                        ct7: ClassTag[T7],
                        ct8: ClassTag[T8],
                        ct9: ClassTag[T9],
                        ct10: ClassTag[T10],
                        ct11: ClassTag[T11],
                        ct12: ClassTag[T12],
                        ct13: ClassTag[T13],
                        ct14: ClassTag[R]): R = undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr14[
    T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R] {
  def apply(arg1: T1,
            arg2: T2,
            arg3: T3,
            arg4: T4,
            arg5: T5,
            arg6: T6,
            arg7: T7,
            arg8: T8,
            arg9: T9,
            arg10: T10,
            arg11: T11,
            arg12: T12,
            arg13: T13,
            arg14: T14)(implicit ct1: ClassTag[T1],
                        ct2: ClassTag[T2],
                        ct3: ClassTag[T3],
                        ct4: ClassTag[T4],
                        ct5: ClassTag[T5],
                        ct6: ClassTag[T6],
                        ct7: ClassTag[T7],
                        ct8: ClassTag[T8],
                        ct9: ClassTag[T9],
                        ct10: ClassTag[T10],
                        ct11: ClassTag[T11],
                        ct12: ClassTag[T12],
                        ct13: ClassTag[T13],
                        ct14: ClassTag[T14],
                        ct15: ClassTag[R]): R = undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr15[
    T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R] {
  def apply(arg1: T1,
            arg2: T2,
            arg3: T3,
            arg4: T4,
            arg5: T5,
            arg6: T6,
            arg7: T7,
            arg8: T8,
            arg9: T9,
            arg10: T10,
            arg11: T11,
            arg12: T12,
            arg13: T13,
            arg14: T14,
            arg15: T15)(implicit ct1: ClassTag[T1],
                        ct2: ClassTag[T2],
                        ct3: ClassTag[T3],
                        ct4: ClassTag[T4],
                        ct5: ClassTag[T5],
                        ct6: ClassTag[T6],
                        ct7: ClassTag[T7],
                        ct8: ClassTag[T8],
                        ct9: ClassTag[T9],
                        ct10: ClassTag[T10],
                        ct11: ClassTag[T11],
                        ct12: ClassTag[T12],
                        ct13: ClassTag[T13],
                        ct14: ClassTag[T14],
                        ct15: ClassTag[T15],
                        ct16: ClassTag[R]): R = undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr16[
    T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R] {
  def apply(arg1: T1,
            arg2: T2,
            arg3: T3,
            arg4: T4,
            arg5: T5,
            arg6: T6,
            arg7: T7,
            arg8: T8,
            arg9: T9,
            arg10: T10,
            arg11: T11,
            arg12: T12,
            arg13: T13,
            arg14: T14,
            arg15: T15,
            arg16: T16)(implicit ct1: ClassTag[T1],
                        ct2: ClassTag[T2],
                        ct3: ClassTag[T3],
                        ct4: ClassTag[T4],
                        ct5: ClassTag[T5],
                        ct6: ClassTag[T6],
                        ct7: ClassTag[T7],
                        ct8: ClassTag[T8],
                        ct9: ClassTag[T9],
                        ct10: ClassTag[T10],
                        ct11: ClassTag[T11],
                        ct12: ClassTag[T12],
                        ct13: ClassTag[T13],
                        ct14: ClassTag[T14],
                        ct15: ClassTag[T15],
                        ct16: ClassTag[T16],
                        ct17: ClassTag[R]): R = undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr17[T1,
                          T2,
                          T3,
                          T4,
                          T5,
                          T6,
                          T7,
                          T8,
                          T9,
                          T10,
                          T11,
                          T12,
                          T13,
                          T14,
                          T15,
                          T16,
                          T17,
                          R] {
  def apply(arg1: T1,
            arg2: T2,
            arg3: T3,
            arg4: T4,
            arg5: T5,
            arg6: T6,
            arg7: T7,
            arg8: T8,
            arg9: T9,
            arg10: T10,
            arg11: T11,
            arg12: T12,
            arg13: T13,
            arg14: T14,
            arg15: T15,
            arg16: T16,
            arg17: T17)(implicit ct1: ClassTag[T1],
                        ct2: ClassTag[T2],
                        ct3: ClassTag[T3],
                        ct4: ClassTag[T4],
                        ct5: ClassTag[T5],
                        ct6: ClassTag[T6],
                        ct7: ClassTag[T7],
                        ct8: ClassTag[T8],
                        ct9: ClassTag[T9],
                        ct10: ClassTag[T10],
                        ct11: ClassTag[T11],
                        ct12: ClassTag[T12],
                        ct13: ClassTag[T13],
                        ct14: ClassTag[T14],
                        ct15: ClassTag[T15],
                        ct16: ClassTag[T16],
                        ct17: ClassTag[T17],
                        ct18: ClassTag[R]): R = undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr18[T1,
                          T2,
                          T3,
                          T4,
                          T5,
                          T6,
                          T7,
                          T8,
                          T9,
                          T10,
                          T11,
                          T12,
                          T13,
                          T14,
                          T15,
                          T16,
                          T17,
                          T18,
                          R] {
  def apply(arg1: T1,
            arg2: T2,
            arg3: T3,
            arg4: T4,
            arg5: T5,
            arg6: T6,
            arg7: T7,
            arg8: T8,
            arg9: T9,
            arg10: T10,
            arg11: T11,
            arg12: T12,
            arg13: T13,
            arg14: T14,
            arg15: T15,
            arg16: T16,
            arg17: T17,
            arg18: T18)(implicit ct1: ClassTag[T1],
                        ct2: ClassTag[T2],
                        ct3: ClassTag[T3],
                        ct4: ClassTag[T4],
                        ct5: ClassTag[T5],
                        ct6: ClassTag[T6],
                        ct7: ClassTag[T7],
                        ct8: ClassTag[T8],
                        ct9: ClassTag[T9],
                        ct10: ClassTag[T10],
                        ct11: ClassTag[T11],
                        ct12: ClassTag[T12],
                        ct13: ClassTag[T13],
                        ct14: ClassTag[T14],
                        ct15: ClassTag[T15],
                        ct16: ClassTag[T16],
                        ct17: ClassTag[T17],
                        ct18: ClassTag[T18],
                        ct19: ClassTag[R]): R = undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr19[T1,
                          T2,
                          T3,
                          T4,
                          T5,
                          T6,
                          T7,
                          T8,
                          T9,
                          T10,
                          T11,
                          T12,
                          T13,
                          T14,
                          T15,
                          T16,
                          T17,
                          T18,
                          T19,
                          R] {
  def apply(arg1: T1,
            arg2: T2,
            arg3: T3,
            arg4: T4,
            arg5: T5,
            arg6: T6,
            arg7: T7,
            arg8: T8,
            arg9: T9,
            arg10: T10,
            arg11: T11,
            arg12: T12,
            arg13: T13,
            arg14: T14,
            arg15: T15,
            arg16: T16,
            arg17: T17,
            arg18: T18,
            arg19: T19)(implicit ct1: ClassTag[T1],
                        ct2: ClassTag[T2],
                        ct3: ClassTag[T3],
                        ct4: ClassTag[T4],
                        ct5: ClassTag[T5],
                        ct6: ClassTag[T6],
                        ct7: ClassTag[T7],
                        ct8: ClassTag[T8],
                        ct9: ClassTag[T9],
                        ct10: ClassTag[T10],
                        ct11: ClassTag[T11],
                        ct12: ClassTag[T12],
                        ct13: ClassTag[T13],
                        ct14: ClassTag[T14],
                        ct15: ClassTag[T15],
                        ct16: ClassTag[T16],
                        ct17: ClassTag[T17],
                        ct18: ClassTag[T18],
                        ct19: ClassTag[T19],
                        ct20: ClassTag[R]): R = undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr20[T1,
                          T2,
                          T3,
                          T4,
                          T5,
                          T6,
                          T7,
                          T8,
                          T9,
                          T10,
                          T11,
                          T12,
                          T13,
                          T14,
                          T15,
                          T16,
                          T17,
                          T18,
                          T19,
                          T20,
                          R] {
  def apply(arg1: T1,
            arg2: T2,
            arg3: T3,
            arg4: T4,
            arg5: T5,
            arg6: T6,
            arg7: T7,
            arg8: T8,
            arg9: T9,
            arg10: T10,
            arg11: T11,
            arg12: T12,
            arg13: T13,
            arg14: T14,
            arg15: T15,
            arg16: T16,
            arg17: T17,
            arg18: T18,
            arg19: T19,
            arg20: T20)(implicit ct1: ClassTag[T1],
                        ct2: ClassTag[T2],
                        ct3: ClassTag[T3],
                        ct4: ClassTag[T4],
                        ct5: ClassTag[T5],
                        ct6: ClassTag[T6],
                        ct7: ClassTag[T7],
                        ct8: ClassTag[T8],
                        ct9: ClassTag[T9],
                        ct10: ClassTag[T10],
                        ct11: ClassTag[T11],
                        ct12: ClassTag[T12],
                        ct13: ClassTag[T13],
                        ct14: ClassTag[T14],
                        ct15: ClassTag[T15],
                        ct16: ClassTag[T16],
                        ct17: ClassTag[T17],
                        ct18: ClassTag[T18],
                        ct19: ClassTag[T19],
                        ct20: ClassTag[T20],
                        ct21: ClassTag[R]): R = undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr21[T1,
                          T2,
                          T3,
                          T4,
                          T5,
                          T6,
                          T7,
                          T8,
                          T9,
                          T10,
                          T11,
                          T12,
                          T13,
                          T14,
                          T15,
                          T16,
                          T17,
                          T18,
                          T19,
                          T20,
                          T21,
                          R] {
  def apply(arg1: T1,
            arg2: T2,
            arg3: T3,
            arg4: T4,
            arg5: T5,
            arg6: T6,
            arg7: T7,
            arg8: T8,
            arg9: T9,
            arg10: T10,
            arg11: T11,
            arg12: T12,
            arg13: T13,
            arg14: T14,
            arg15: T15,
            arg16: T16,
            arg17: T17,
            arg18: T18,
            arg19: T19,
            arg20: T20,
            arg21: T21)(implicit ct1: ClassTag[T1],
                        ct2: ClassTag[T2],
                        ct3: ClassTag[T3],
                        ct4: ClassTag[T4],
                        ct5: ClassTag[T5],
                        ct6: ClassTag[T6],
                        ct7: ClassTag[T7],
                        ct8: ClassTag[T8],
                        ct9: ClassTag[T9],
                        ct10: ClassTag[T10],
                        ct11: ClassTag[T11],
                        ct12: ClassTag[T12],
                        ct13: ClassTag[T13],
                        ct14: ClassTag[T14],
                        ct15: ClassTag[T15],
                        ct16: ClassTag[T16],
                        ct17: ClassTag[T17],
                        ct18: ClassTag[T18],
                        ct19: ClassTag[T19],
                        ct20: ClassTag[T20],
                        ct21: ClassTag[T21],
                        ct22: ClassTag[R]): R = undefined
}

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/FunctionPtr.scala.gyb", line: 25)

final class FunctionPtr22[T1,
                          T2,
                          T3,
                          T4,
                          T5,
                          T6,
                          T7,
                          T8,
                          T9,
                          T10,
                          T11,
                          T12,
                          T13,
                          T14,
                          T15,
                          T16,
                          T17,
                          T18,
                          T19,
                          T20,
                          T21,
                          T22,
                          R] {
  def apply(arg1: T1,
            arg2: T2,
            arg3: T3,
            arg4: T4,
            arg5: T5,
            arg6: T6,
            arg7: T7,
            arg8: T8,
            arg9: T9,
            arg10: T10,
            arg11: T11,
            arg12: T12,
            arg13: T13,
            arg14: T14,
            arg15: T15,
            arg16: T16,
            arg17: T17,
            arg18: T18,
            arg19: T19,
            arg20: T20,
            arg21: T21,
            arg22: T22)(implicit ct1: ClassTag[T1],
                        ct2: ClassTag[T2],
                        ct3: ClassTag[T3],
                        ct4: ClassTag[T4],
                        ct5: ClassTag[T5],
                        ct6: ClassTag[T6],
                        ct7: ClassTag[T7],
                        ct8: ClassTag[T8],
                        ct9: ClassTag[T9],
                        ct10: ClassTag[T10],
                        ct11: ClassTag[T11],
                        ct12: ClassTag[T12],
                        ct13: ClassTag[T13],
                        ct14: ClassTag[T14],
                        ct15: ClassTag[T15],
                        ct16: ClassTag[T16],
                        ct17: ClassTag[T17],
                        ct18: ClassTag[T18],
                        ct19: ClassTag[T19],
                        ct20: ClassTag[T20],
                        ct21: ClassTag[T21],
                        ct22: ClassTag[T22],
                        ct23: ClassTag[R]): R = undefined
}
