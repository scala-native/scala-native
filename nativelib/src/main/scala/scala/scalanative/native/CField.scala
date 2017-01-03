// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 1)
package scala.scalanative
package native

import scalanative.runtime.undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField1[P, F]

object CField1 {

  implicit def array[T, N <: Nat]: CField1[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct1[F1]: CField1[CStruct1[F1], F1] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct2[F1, F2]: CField1[CStruct2[F1, F2], F1] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct3[F1, F2, F3]: CField1[CStruct3[F1, F2, F3], F1] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct4[F1, F2, F3, F4]: CField1[CStruct4[F1, F2, F3, F4], F1] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct5[F1, F2, F3, F4, F5]
    : CField1[CStruct5[F1, F2, F3, F4, F5], F1] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct6[F1, F2, F3, F4, F5, F6]
    : CField1[CStruct6[F1, F2, F3, F4, F5, F6], F1] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct7[F1, F2, F3, F4, F5, F6, F7]
    : CField1[CStruct7[F1, F2, F3, F4, F5, F6, F7], F1] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct8[F1, F2, F3, F4, F5, F6, F7, F8]
    : CField1[CStruct8[F1, F2, F3, F4, F5, F6, F7, F8], F1] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct9[F1, F2, F3, F4, F5, F6, F7, F8, F9]
    : CField1[CStruct9[F1, F2, F3, F4, F5, F6, F7, F8, F9], F1] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct10[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10]
    : CField1[CStruct10[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10], F1] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11]
    : CField1[CStruct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11], F1] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12]
    : CField1[CStruct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12], F1] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13]
    : CField1[
      CStruct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13],
      F1] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct14[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14]: CField1[
    CStruct14[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14],
    F1] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct15[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15]: CField1[CStruct15[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15],
                                      F1] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct16[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16]: CField1[CStruct16[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16],
                                      F1] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct17[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17]: CField1[CStruct17[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17],
                                      F1] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct18[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18]: CField1[CStruct18[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18],
                                      F1] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct19[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19]: CField1[CStruct19[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19],
                                      F1] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct20[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20]: CField1[CStruct20[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20],
                                      F1] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField1[CStruct21[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20,
                                                F21],
                                      F1] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField1[CStruct22[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20,
                                                F21,
                                                F22],
                                      F1] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField2[P, F]

object CField2 {

  implicit def array[T, N <: Nat]: CField2[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct2[F1, F2]: CField2[CStruct2[F1, F2], F2] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct3[F1, F2, F3]: CField2[CStruct3[F1, F2, F3], F2] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct4[F1, F2, F3, F4]: CField2[CStruct4[F1, F2, F3, F4], F2] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct5[F1, F2, F3, F4, F5]
    : CField2[CStruct5[F1, F2, F3, F4, F5], F2] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct6[F1, F2, F3, F4, F5, F6]
    : CField2[CStruct6[F1, F2, F3, F4, F5, F6], F2] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct7[F1, F2, F3, F4, F5, F6, F7]
    : CField2[CStruct7[F1, F2, F3, F4, F5, F6, F7], F2] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct8[F1, F2, F3, F4, F5, F6, F7, F8]
    : CField2[CStruct8[F1, F2, F3, F4, F5, F6, F7, F8], F2] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct9[F1, F2, F3, F4, F5, F6, F7, F8, F9]
    : CField2[CStruct9[F1, F2, F3, F4, F5, F6, F7, F8, F9], F2] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct10[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10]
    : CField2[CStruct10[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10], F2] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11]
    : CField2[CStruct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11], F2] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12]
    : CField2[CStruct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12], F2] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13]
    : CField2[
      CStruct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13],
      F2] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct14[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14]: CField2[
    CStruct14[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14],
    F2] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct15[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15]: CField2[CStruct15[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15],
                                      F2] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct16[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16]: CField2[CStruct16[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16],
                                      F2] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct17[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17]: CField2[CStruct17[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17],
                                      F2] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct18[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18]: CField2[CStruct18[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18],
                                      F2] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct19[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19]: CField2[CStruct19[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19],
                                      F2] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct20[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20]: CField2[CStruct20[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20],
                                      F2] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField2[CStruct21[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20,
                                                F21],
                                      F2] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField2[CStruct22[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20,
                                                F21,
                                                F22],
                                      F2] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField3[P, F]

object CField3 {

  implicit def array[T, N <: Nat]: CField3[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct3[F1, F2, F3]: CField3[CStruct3[F1, F2, F3], F3] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct4[F1, F2, F3, F4]: CField3[CStruct4[F1, F2, F3, F4], F3] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct5[F1, F2, F3, F4, F5]
    : CField3[CStruct5[F1, F2, F3, F4, F5], F3] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct6[F1, F2, F3, F4, F5, F6]
    : CField3[CStruct6[F1, F2, F3, F4, F5, F6], F3] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct7[F1, F2, F3, F4, F5, F6, F7]
    : CField3[CStruct7[F1, F2, F3, F4, F5, F6, F7], F3] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct8[F1, F2, F3, F4, F5, F6, F7, F8]
    : CField3[CStruct8[F1, F2, F3, F4, F5, F6, F7, F8], F3] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct9[F1, F2, F3, F4, F5, F6, F7, F8, F9]
    : CField3[CStruct9[F1, F2, F3, F4, F5, F6, F7, F8, F9], F3] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct10[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10]
    : CField3[CStruct10[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10], F3] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11]
    : CField3[CStruct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11], F3] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12]
    : CField3[CStruct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12], F3] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13]
    : CField3[
      CStruct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13],
      F3] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct14[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14]: CField3[
    CStruct14[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14],
    F3] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct15[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15]: CField3[CStruct15[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15],
                                      F3] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct16[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16]: CField3[CStruct16[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16],
                                      F3] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct17[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17]: CField3[CStruct17[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17],
                                      F3] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct18[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18]: CField3[CStruct18[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18],
                                      F3] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct19[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19]: CField3[CStruct19[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19],
                                      F3] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct20[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20]: CField3[CStruct20[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20],
                                      F3] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField3[CStruct21[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20,
                                                F21],
                                      F3] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField3[CStruct22[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20,
                                                F21,
                                                F22],
                                      F3] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField4[P, F]

object CField4 {

  implicit def array[T, N <: Nat]: CField4[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct4[F1, F2, F3, F4]: CField4[CStruct4[F1, F2, F3, F4], F4] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct5[F1, F2, F3, F4, F5]
    : CField4[CStruct5[F1, F2, F3, F4, F5], F4] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct6[F1, F2, F3, F4, F5, F6]
    : CField4[CStruct6[F1, F2, F3, F4, F5, F6], F4] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct7[F1, F2, F3, F4, F5, F6, F7]
    : CField4[CStruct7[F1, F2, F3, F4, F5, F6, F7], F4] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct8[F1, F2, F3, F4, F5, F6, F7, F8]
    : CField4[CStruct8[F1, F2, F3, F4, F5, F6, F7, F8], F4] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct9[F1, F2, F3, F4, F5, F6, F7, F8, F9]
    : CField4[CStruct9[F1, F2, F3, F4, F5, F6, F7, F8, F9], F4] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct10[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10]
    : CField4[CStruct10[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10], F4] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11]
    : CField4[CStruct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11], F4] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12]
    : CField4[CStruct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12], F4] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13]
    : CField4[
      CStruct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13],
      F4] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct14[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14]: CField4[
    CStruct14[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14],
    F4] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct15[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15]: CField4[CStruct15[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15],
                                      F4] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct16[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16]: CField4[CStruct16[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16],
                                      F4] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct17[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17]: CField4[CStruct17[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17],
                                      F4] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct18[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18]: CField4[CStruct18[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18],
                                      F4] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct19[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19]: CField4[CStruct19[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19],
                                      F4] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct20[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20]: CField4[CStruct20[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20],
                                      F4] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField4[CStruct21[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20,
                                                F21],
                                      F4] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField4[CStruct22[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20,
                                                F21,
                                                F22],
                                      F4] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField5[P, F]

object CField5 {

  implicit def array[T, N <: Nat]: CField5[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct5[F1, F2, F3, F4, F5]
    : CField5[CStruct5[F1, F2, F3, F4, F5], F5] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct6[F1, F2, F3, F4, F5, F6]
    : CField5[CStruct6[F1, F2, F3, F4, F5, F6], F5] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct7[F1, F2, F3, F4, F5, F6, F7]
    : CField5[CStruct7[F1, F2, F3, F4, F5, F6, F7], F5] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct8[F1, F2, F3, F4, F5, F6, F7, F8]
    : CField5[CStruct8[F1, F2, F3, F4, F5, F6, F7, F8], F5] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct9[F1, F2, F3, F4, F5, F6, F7, F8, F9]
    : CField5[CStruct9[F1, F2, F3, F4, F5, F6, F7, F8, F9], F5] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct10[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10]
    : CField5[CStruct10[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10], F5] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11]
    : CField5[CStruct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11], F5] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12]
    : CField5[CStruct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12], F5] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13]
    : CField5[
      CStruct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13],
      F5] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct14[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14]: CField5[
    CStruct14[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14],
    F5] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct15[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15]: CField5[CStruct15[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15],
                                      F5] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct16[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16]: CField5[CStruct16[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16],
                                      F5] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct17[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17]: CField5[CStruct17[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17],
                                      F5] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct18[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18]: CField5[CStruct18[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18],
                                      F5] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct19[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19]: CField5[CStruct19[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19],
                                      F5] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct20[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20]: CField5[CStruct20[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20],
                                      F5] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField5[CStruct21[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20,
                                                F21],
                                      F5] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField5[CStruct22[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20,
                                                F21,
                                                F22],
                                      F5] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField6[P, F]

object CField6 {

  implicit def array[T, N <: Nat]: CField6[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct6[F1, F2, F3, F4, F5, F6]
    : CField6[CStruct6[F1, F2, F3, F4, F5, F6], F6] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct7[F1, F2, F3, F4, F5, F6, F7]
    : CField6[CStruct7[F1, F2, F3, F4, F5, F6, F7], F6] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct8[F1, F2, F3, F4, F5, F6, F7, F8]
    : CField6[CStruct8[F1, F2, F3, F4, F5, F6, F7, F8], F6] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct9[F1, F2, F3, F4, F5, F6, F7, F8, F9]
    : CField6[CStruct9[F1, F2, F3, F4, F5, F6, F7, F8, F9], F6] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct10[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10]
    : CField6[CStruct10[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10], F6] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11]
    : CField6[CStruct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11], F6] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12]
    : CField6[CStruct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12], F6] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13]
    : CField6[
      CStruct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13],
      F6] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct14[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14]: CField6[
    CStruct14[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14],
    F6] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct15[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15]: CField6[CStruct15[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15],
                                      F6] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct16[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16]: CField6[CStruct16[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16],
                                      F6] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct17[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17]: CField6[CStruct17[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17],
                                      F6] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct18[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18]: CField6[CStruct18[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18],
                                      F6] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct19[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19]: CField6[CStruct19[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19],
                                      F6] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct20[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20]: CField6[CStruct20[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20],
                                      F6] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField6[CStruct21[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20,
                                                F21],
                                      F6] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField6[CStruct22[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20,
                                                F21,
                                                F22],
                                      F6] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField7[P, F]

object CField7 {

  implicit def array[T, N <: Nat]: CField7[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct7[F1, F2, F3, F4, F5, F6, F7]
    : CField7[CStruct7[F1, F2, F3, F4, F5, F6, F7], F7] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct8[F1, F2, F3, F4, F5, F6, F7, F8]
    : CField7[CStruct8[F1, F2, F3, F4, F5, F6, F7, F8], F7] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct9[F1, F2, F3, F4, F5, F6, F7, F8, F9]
    : CField7[CStruct9[F1, F2, F3, F4, F5, F6, F7, F8, F9], F7] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct10[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10]
    : CField7[CStruct10[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10], F7] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11]
    : CField7[CStruct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11], F7] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12]
    : CField7[CStruct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12], F7] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13]
    : CField7[
      CStruct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13],
      F7] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct14[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14]: CField7[
    CStruct14[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14],
    F7] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct15[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15]: CField7[CStruct15[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15],
                                      F7] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct16[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16]: CField7[CStruct16[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16],
                                      F7] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct17[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17]: CField7[CStruct17[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17],
                                      F7] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct18[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18]: CField7[CStruct18[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18],
                                      F7] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct19[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19]: CField7[CStruct19[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19],
                                      F7] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct20[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20]: CField7[CStruct20[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20],
                                      F7] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField7[CStruct21[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20,
                                                F21],
                                      F7] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField7[CStruct22[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20,
                                                F21,
                                                F22],
                                      F7] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField8[P, F]

object CField8 {

  implicit def array[T, N <: Nat]: CField8[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct8[F1, F2, F3, F4, F5, F6, F7, F8]
    : CField8[CStruct8[F1, F2, F3, F4, F5, F6, F7, F8], F8] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct9[F1, F2, F3, F4, F5, F6, F7, F8, F9]
    : CField8[CStruct9[F1, F2, F3, F4, F5, F6, F7, F8, F9], F8] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct10[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10]
    : CField8[CStruct10[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10], F8] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11]
    : CField8[CStruct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11], F8] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12]
    : CField8[CStruct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12], F8] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13]
    : CField8[
      CStruct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13],
      F8] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct14[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14]: CField8[
    CStruct14[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14],
    F8] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct15[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15]: CField8[CStruct15[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15],
                                      F8] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct16[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16]: CField8[CStruct16[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16],
                                      F8] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct17[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17]: CField8[CStruct17[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17],
                                      F8] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct18[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18]: CField8[CStruct18[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18],
                                      F8] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct19[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19]: CField8[CStruct19[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19],
                                      F8] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct20[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20]: CField8[CStruct20[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20],
                                      F8] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField8[CStruct21[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20,
                                                F21],
                                      F8] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField8[CStruct22[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20,
                                                F21,
                                                F22],
                                      F8] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField9[P, F]

object CField9 {

  implicit def array[T, N <: Nat]: CField9[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct9[F1, F2, F3, F4, F5, F6, F7, F8, F9]
    : CField9[CStruct9[F1, F2, F3, F4, F5, F6, F7, F8, F9], F9] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct10[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10]
    : CField9[CStruct10[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10], F9] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11]
    : CField9[CStruct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11], F9] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12]
    : CField9[CStruct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12], F9] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13]
    : CField9[
      CStruct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13],
      F9] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct14[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14]: CField9[
    CStruct14[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14],
    F9] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct15[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15]: CField9[CStruct15[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15],
                                      F9] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct16[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16]: CField9[CStruct16[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16],
                                      F9] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct17[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17]: CField9[CStruct17[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17],
                                      F9] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct18[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18]: CField9[CStruct18[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18],
                                      F9] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct19[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19]: CField9[CStruct19[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19],
                                      F9] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct20[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20]: CField9[CStruct20[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20],
                                      F9] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField9[CStruct21[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20,
                                                F21],
                                      F9] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField9[CStruct22[F1,
                                                F2,
                                                F3,
                                                F4,
                                                F5,
                                                F6,
                                                F7,
                                                F8,
                                                F9,
                                                F10,
                                                F11,
                                                F12,
                                                F13,
                                                F14,
                                                F15,
                                                F16,
                                                F17,
                                                F18,
                                                F19,
                                                F20,
                                                F21,
                                                F22],
                                      F9] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField10[P, F]

object CField10 {

  implicit def array[T, N <: Nat]: CField10[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct10[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10]
    : CField10[CStruct10[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10], F10] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11]
    : CField10[CStruct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11], F10] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12]
    : CField10[CStruct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12],
               F10] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13]
    : CField10[
      CStruct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13],
      F10] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct14[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14]: CField10[
    CStruct14[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14],
    F10] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct15[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15]: CField10[CStruct15[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15],
                                       F10] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct16[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16]: CField10[CStruct16[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16],
                                       F10] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct17[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17]: CField10[CStruct17[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17],
                                       F10] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct18[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18]: CField10[CStruct18[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18],
                                       F10] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct19[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19]: CField10[CStruct19[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19],
                                       F10] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct20[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20]: CField10[CStruct20[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20],
                                       F10] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField10[CStruct21[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21],
                                       F10] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField10[CStruct22[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21,
                                                 F22],
                                       F10] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField11[P, F]

object CField11 {

  implicit def array[T, N <: Nat]: CField11[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11]
    : CField11[CStruct11[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11], F11] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12]
    : CField11[CStruct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12],
               F11] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13]
    : CField11[
      CStruct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13],
      F11] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct14[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14]: CField11[
    CStruct14[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14],
    F11] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct15[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15]: CField11[CStruct15[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15],
                                       F11] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct16[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16]: CField11[CStruct16[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16],
                                       F11] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct17[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17]: CField11[CStruct17[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17],
                                       F11] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct18[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18]: CField11[CStruct18[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18],
                                       F11] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct19[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19]: CField11[CStruct19[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19],
                                       F11] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct20[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20]: CField11[CStruct20[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20],
                                       F11] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField11[CStruct21[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21],
                                       F11] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField11[CStruct22[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21,
                                                 F22],
                                       F11] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField12[P, F]

object CField12 {

  implicit def array[T, N <: Nat]: CField12[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12]
    : CField12[CStruct12[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12],
               F12] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13]
    : CField12[
      CStruct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13],
      F12] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct14[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14]: CField12[
    CStruct14[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14],
    F12] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct15[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15]: CField12[CStruct15[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15],
                                       F12] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct16[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16]: CField12[CStruct16[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16],
                                       F12] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct17[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17]: CField12[CStruct17[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17],
                                       F12] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct18[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18]: CField12[CStruct18[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18],
                                       F12] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct19[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19]: CField12[CStruct19[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19],
                                       F12] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct20[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20]: CField12[CStruct20[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20],
                                       F12] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField12[CStruct21[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21],
                                       F12] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField12[CStruct22[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21,
                                                 F22],
                                       F12] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField13[P, F]

object CField13 {

  implicit def array[T, N <: Nat]: CField13[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13]
    : CField13[
      CStruct13[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13],
      F13] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct14[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14]: CField13[
    CStruct14[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14],
    F13] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct15[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15]: CField13[CStruct15[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15],
                                       F13] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct16[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16]: CField13[CStruct16[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16],
                                       F13] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct17[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17]: CField13[CStruct17[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17],
                                       F13] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct18[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18]: CField13[CStruct18[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18],
                                       F13] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct19[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19]: CField13[CStruct19[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19],
                                       F13] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct20[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20]: CField13[CStruct20[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20],
                                       F13] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField13[CStruct21[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21],
                                       F13] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField13[CStruct22[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21,
                                                 F22],
                                       F13] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField14[P, F]

object CField14 {

  implicit def array[T, N <: Nat]: CField14[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct14[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14]: CField14[
    CStruct14[F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14],
    F14] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct15[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15]: CField14[CStruct15[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15],
                                       F14] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct16[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16]: CField14[CStruct16[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16],
                                       F14] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct17[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17]: CField14[CStruct17[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17],
                                       F14] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct18[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18]: CField14[CStruct18[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18],
                                       F14] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct19[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19]: CField14[CStruct19[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19],
                                       F14] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct20[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20]: CField14[CStruct20[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20],
                                       F14] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField14[CStruct21[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21],
                                       F14] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField14[CStruct22[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21,
                                                 F22],
                                       F14] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField15[P, F]

object CField15 {

  implicit def array[T, N <: Nat]: CField15[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct15[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15]: CField15[CStruct15[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15],
                                       F15] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct16[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16]: CField15[CStruct16[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16],
                                       F15] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct17[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17]: CField15[CStruct17[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17],
                                       F15] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct18[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18]: CField15[CStruct18[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18],
                                       F15] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct19[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19]: CField15[CStruct19[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19],
                                       F15] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct20[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20]: CField15[CStruct20[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20],
                                       F15] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField15[CStruct21[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21],
                                       F15] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField15[CStruct22[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21,
                                                 F22],
                                       F15] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField16[P, F]

object CField16 {

  implicit def array[T, N <: Nat]: CField16[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct16[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16]: CField16[CStruct16[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16],
                                       F16] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct17[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17]: CField16[CStruct17[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17],
                                       F16] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct18[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18]: CField16[CStruct18[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18],
                                       F16] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct19[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19]: CField16[CStruct19[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19],
                                       F16] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct20[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20]: CField16[CStruct20[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20],
                                       F16] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField16[CStruct21[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21],
                                       F16] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField16[CStruct22[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21,
                                                 F22],
                                       F16] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField17[P, F]

object CField17 {

  implicit def array[T, N <: Nat]: CField17[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct17[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17]: CField17[CStruct17[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17],
                                       F17] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct18[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18]: CField17[CStruct18[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18],
                                       F17] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct19[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19]: CField17[CStruct19[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19],
                                       F17] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct20[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20]: CField17[CStruct20[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20],
                                       F17] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField17[CStruct21[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21],
                                       F17] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField17[CStruct22[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21,
                                                 F22],
                                       F17] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField18[P, F]

object CField18 {

  implicit def array[T, N <: Nat]: CField18[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct18[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18]: CField18[CStruct18[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18],
                                       F18] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct19[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19]: CField18[CStruct19[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19],
                                       F18] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct20[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20]: CField18[CStruct20[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20],
                                       F18] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField18[CStruct21[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21],
                                       F18] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField18[CStruct22[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21,
                                                 F22],
                                       F18] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField19[P, F]

object CField19 {

  implicit def array[T, N <: Nat]: CField19[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct19[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19]: CField19[CStruct19[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19],
                                       F19] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct20[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20]: CField19[CStruct20[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20],
                                       F19] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField19[CStruct21[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21],
                                       F19] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField19[CStruct22[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21,
                                                 F22],
                                       F19] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField20[P, F]

object CField20 {

  implicit def array[T, N <: Nat]: CField20[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct20[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20]: CField20[CStruct20[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20],
                                       F20] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField20[CStruct21[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21],
                                       F20] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField20[CStruct22[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21,
                                                 F22],
                                       F20] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField21[P, F]

object CField21 {

  implicit def array[T, N <: Nat]: CField21[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct21[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21]: CField21[CStruct21[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21],
                                       F21] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField21[CStruct22[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21,
                                                 F22],
                                       F21] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 7)

final abstract class CField22[P, F]

object CField22 {

  implicit def array[T, N <: Nat]: CField22[CArray[T, N], T] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 17)

  implicit def struct22[F1,
                        F2,
                        F3,
                        F4,
                        F5,
                        F6,
                        F7,
                        F8,
                        F9,
                        F10,
                        F11,
                        F12,
                        F13,
                        F14,
                        F15,
                        F16,
                        F17,
                        F18,
                        F19,
                        F20,
                        F21,
                        F22]: CField22[CStruct22[F1,
                                                 F2,
                                                 F3,
                                                 F4,
                                                 F5,
                                                 F6,
                                                 F7,
                                                 F8,
                                                 F9,
                                                 F10,
                                                 F11,
                                                 F12,
                                                 F13,
                                                 F14,
                                                 F15,
                                                 F16,
                                                 F17,
                                                 F18,
                                                 F19,
                                                 F20,
                                                 F21,
                                                 F22],
                                       F22] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CField.scala.gyb", line: 21)

}
