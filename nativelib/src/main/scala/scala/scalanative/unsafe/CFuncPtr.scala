// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 1)
package scala.scalanative
package unsafe

sealed trait CFuncPtr

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr0[R] extends CFuncPtr {
  def apply(): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr1[T1, R] extends CFuncPtr {
  def apply(arg1: T1): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr2[T1, T2, R] extends CFuncPtr {
  def apply(arg1: T1, arg2: T2): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr3[T1, T2, T3, R] extends CFuncPtr {
  def apply(arg1: T1, arg2: T2, arg3: T3): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr4[T1, T2, T3, T4, R] extends CFuncPtr {
  def apply(arg1: T1, arg2: T2, arg3: T3, arg4: T4): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr5[T1, T2, T3, T4, T5, R] extends CFuncPtr {
  def apply(arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr6[T1, T2, T3, T4, T5, T6, R] extends CFuncPtr {
  def apply(arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr7[T1, T2, T3, T4, T5, T6, T7, R] extends CFuncPtr {
  def apply(arg1: T1,
            arg2: T2,
            arg3: T3,
            arg4: T4,
            arg5: T5,
            arg6: T6,
            arg7: T7): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr8[T1, T2, T3, T4, T5, T6, T7, T8, R] extends CFuncPtr {
  def apply(arg1: T1,
            arg2: T2,
            arg3: T3,
            arg4: T4,
            arg5: T5,
            arg6: T6,
            arg7: T7,
            arg8: T8): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R] extends CFuncPtr {
  def apply(arg1: T1,
            arg2: T2,
            arg3: T3,
            arg4: T4,
            arg5: T5,
            arg6: T6,
            arg7: T7,
            arg8: T8,
            arg9: T9): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R] extends CFuncPtr {
  def apply(arg1: T1,
            arg2: T2,
            arg3: T3,
            arg4: T4,
            arg5: T5,
            arg6: T6,
            arg7: T7,
            arg8: T8,
            arg9: T9,
            arg10: T10): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R]
    extends CFuncPtr {
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
            arg11: T11): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R]
    extends CFuncPtr {
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
            arg12: T12): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R]
    extends CFuncPtr {
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
            arg13: T13): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R]
    extends CFuncPtr {
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
            arg14: T14): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr15[
    T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R]
    extends CFuncPtr {
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
            arg15: T15): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr16[
    T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R]
    extends CFuncPtr {
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
            arg16: T16): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr17[T1,
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
                 R]
    extends CFuncPtr {
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
            arg17: T17): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr18[T1,
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
                 R]
    extends CFuncPtr {
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
            arg18: T18): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr19[T1,
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
                 R]
    extends CFuncPtr {
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
            arg19: T19): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr20[T1,
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
                 R]
    extends CFuncPtr {
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
            arg20: T20): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr21[T1,
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
                 R]
    extends CFuncPtr {
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
            arg21: T21): R
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/unsafe.CFuncPtr.scala.gyb", line: 8)

trait CFuncPtr22[T1,
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
                 R]
    extends CFuncPtr {
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
            arg22: T22): R
}
