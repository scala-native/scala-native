// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 1)

// BEWARE: This file is generated - direct edits will be lost.
// Do not edit this it directly other than to remove
// personally identifiable information in sourceLocation lines.
// All direct edits to this file will be lost the next time it
// is generated.
//
// See nativelib runtime/Arrays.scala.gyb for details.

package scala.scalanative
package runtime

import scalanative.unsafe._

abstract class CFuncIntrinsics {

/** Intrinsic method used to call directly function at given ptr for C-interop
 	It also unboxes values before passing them to called function using provided Tags
 */
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[R: Tag](ptr: RawPtr): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, R: Tag](ptr: RawPtr, arg1: T1): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, T3 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2, arg3: T3): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, T3 : Tag, T4 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2, arg3: T3, arg4: T4): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, T3 : Tag, T4 : Tag, T5 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, T3 : Tag, T4 : Tag, T5 : Tag, T6 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, T3 : Tag, T4 : Tag, T5 : Tag, T6 : Tag, T7 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, T3 : Tag, T4 : Tag, T5 : Tag, T6 : Tag, T7 : Tag, T8 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, T3 : Tag, T4 : Tag, T5 : Tag, T6 : Tag, T7 : Tag, T8 : Tag, T9 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, T3 : Tag, T4 : Tag, T5 : Tag, T6 : Tag, T7 : Tag, T8 : Tag, T9 : Tag, T10 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, T3 : Tag, T4 : Tag, T5 : Tag, T6 : Tag, T7 : Tag, T8 : Tag, T9 : Tag, T10 : Tag, T11 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10, arg11: T11): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, T3 : Tag, T4 : Tag, T5 : Tag, T6 : Tag, T7 : Tag, T8 : Tag, T9 : Tag, T10 : Tag, T11 : Tag, T12 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10, arg11: T11, arg12: T12): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, T3 : Tag, T4 : Tag, T5 : Tag, T6 : Tag, T7 : Tag, T8 : Tag, T9 : Tag, T10 : Tag, T11 : Tag, T12 : Tag, T13 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10, arg11: T11, arg12: T12, arg13: T13): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, T3 : Tag, T4 : Tag, T5 : Tag, T6 : Tag, T7 : Tag, T8 : Tag, T9 : Tag, T10 : Tag, T11 : Tag, T12 : Tag, T13 : Tag, T14 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10, arg11: T11, arg12: T12, arg13: T13, arg14: T14): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, T3 : Tag, T4 : Tag, T5 : Tag, T6 : Tag, T7 : Tag, T8 : Tag, T9 : Tag, T10 : Tag, T11 : Tag, T12 : Tag, T13 : Tag, T14 : Tag, T15 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10, arg11: T11, arg12: T12, arg13: T13, arg14: T14, arg15: T15): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, T3 : Tag, T4 : Tag, T5 : Tag, T6 : Tag, T7 : Tag, T8 : Tag, T9 : Tag, T10 : Tag, T11 : Tag, T12 : Tag, T13 : Tag, T14 : Tag, T15 : Tag, T16 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10, arg11: T11, arg12: T12, arg13: T13, arg14: T14, arg15: T15, arg16: T16): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, T3 : Tag, T4 : Tag, T5 : Tag, T6 : Tag, T7 : Tag, T8 : Tag, T9 : Tag, T10 : Tag, T11 : Tag, T12 : Tag, T13 : Tag, T14 : Tag, T15 : Tag, T16 : Tag, T17 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10, arg11: T11, arg12: T12, arg13: T13, arg14: T14, arg15: T15, arg16: T16, arg17: T17): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, T3 : Tag, T4 : Tag, T5 : Tag, T6 : Tag, T7 : Tag, T8 : Tag, T9 : Tag, T10 : Tag, T11 : Tag, T12 : Tag, T13 : Tag, T14 : Tag, T15 : Tag, T16 : Tag, T17 : Tag, T18 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10, arg11: T11, arg12: T12, arg13: T13, arg14: T14, arg15: T15, arg16: T16, arg17: T17, arg18: T18): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, T3 : Tag, T4 : Tag, T5 : Tag, T6 : Tag, T7 : Tag, T8 : Tag, T9 : Tag, T10 : Tag, T11 : Tag, T12 : Tag, T13 : Tag, T14 : Tag, T15 : Tag, T16 : Tag, T17 : Tag, T18 : Tag, T19 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10, arg11: T11, arg12: T12, arg13: T13, arg14: T14, arg15: T15, arg16: T16, arg17: T17, arg18: T18, arg19: T19): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, T3 : Tag, T4 : Tag, T5 : Tag, T6 : Tag, T7 : Tag, T8 : Tag, T9 : Tag, T10 : Tag, T11 : Tag, T12 : Tag, T13 : Tag, T14 : Tag, T15 : Tag, T16 : Tag, T17 : Tag, T18 : Tag, T19 : Tag, T20 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10, arg11: T11, arg12: T12, arg13: T13, arg14: T14, arg15: T15, arg16: T16, arg17: T17, arg18: T18, arg19: T19, arg20: T20): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, T3 : Tag, T4 : Tag, T5 : Tag, T6 : Tag, T7 : Tag, T8 : Tag, T9 : Tag, T10 : Tag, T11 : Tag, T12 : Tag, T13 : Tag, T14 : Tag, T15 : Tag, T16 : Tag, T17 : Tag, T18 : Tag, T19 : Tag, T20 : Tag, T21 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10, arg11: T11, arg12: T12, arg13: T13, arg14: T14, arg15: T15, arg16: T16, arg17: T17, arg18: T18, arg19: T19, arg20: T20, arg21: T21): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 23)
  def callCFuncPtr[T1 : Tag, T2 : Tag, T3 : Tag, T4 : Tag, T5 : Tag, T6 : Tag, T7 : Tag, T8 : Tag, T9 : Tag, T10 : Tag, T11 : Tag, T12 : Tag, T13 : Tag, T14 : Tag, T15 : Tag, T16 : Tag, T17 : Tag, T18 : Tag, T19 : Tag, T20 : Tag, T21 : Tag, T22 : Tag, R: Tag](ptr: RawPtr, arg1: T1, arg2: T2, arg3: T3, arg4: T4, arg5: T5, arg6: T6, arg7: T7, arg8: T8, arg9: T9, arg10: T10, arg11: T11, arg12: T12, arg13: T13, arg14: T14, arg15: T15, arg16: T16, arg17: T17, arg18: T18, arg19: T19, arg20: T20, arg21: T21, arg22: T22): R = intrinsic
// ###sourceLocation(file: "/home/wojciechmazur/projects/scalacenter/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CFuncIntrinsics.scala.gyb", line: 25)
}
