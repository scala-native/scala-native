// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 1)
package scala.scalanative
package native

import scala.reflect.ClassTag
import scalanative.runtime.undefined

final abstract class Tag[P]

object Tag {
  implicit val Unit: Tag[Unit]                    = undefined
  implicit val Boolean: Tag[Boolean]              = undefined
  implicit val Char: Tag[Char]                    = undefined
  implicit val Byte: Tag[Byte]                    = undefined
  implicit val UByte: Tag[UByte]                  = undefined
  implicit val Short: Tag[Short]                  = undefined
  implicit val UShort: Tag[UShort]                = undefined
  implicit val Int: Tag[Int]                      = undefined
  implicit val UInt: Tag[UInt]                    = undefined
  implicit val Long: Tag[Long]                    = undefined
  implicit val ULong: Tag[ULong]                  = undefined
  implicit val Float: Tag[Float]                  = undefined
  implicit val Double: Tag[Double]                = undefined
  implicit def Ptr[T: Tag]: Tag[Ptr[T]]           = undefined
  implicit def Ref[T <: AnyRef: ClassTag]: Tag[T] = undefined

  implicit def Nat0: Tag[Nat._0] = undefined
  implicit def Nat1: Tag[Nat._1] = undefined
  implicit def Nat2: Tag[Nat._2] = undefined
  implicit def Nat3: Tag[Nat._3] = undefined
  implicit def Nat4: Tag[Nat._4] = undefined
  implicit def Nat5: Tag[Nat._5] = undefined
  implicit def Nat6: Tag[Nat._6] = undefined
  implicit def Nat7: Tag[Nat._7] = undefined
  implicit def Nat8: Tag[Nat._8] = undefined
  implicit def Nat9: Tag[Nat._9] = undefined
  implicit def NatDigit[N <: Nat.Base: Tag, M <: Nat: Tag]
    : Tag[Nat.Digit[N, M]] =
    undefined

  implicit def CArray[T: Tag, N <: Nat: Tag]: Tag[CArray[T, N]] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct0: Tag[CStruct0] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct1[T1: Tag]: Tag[CStruct1[T1]] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct2[T1: Tag, T2: Tag]: Tag[CStruct2[T1, T2]] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct3[T1: Tag, T2: Tag, T3: Tag]: Tag[CStruct3[T1, T2, T3]] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct4[T1: Tag, T2: Tag, T3: Tag, T4: Tag]
    : Tag[CStruct4[T1, T2, T3, T4]] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct5[T1: Tag, T2: Tag, T3: Tag, T4: Tag, T5: Tag]
    : Tag[CStruct5[T1, T2, T3, T4, T5]] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct6[T1: Tag, T2: Tag, T3: Tag, T4: Tag, T5: Tag, T6: Tag]
    : Tag[CStruct6[T1, T2, T3, T4, T5, T6]] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct7[T1: Tag,
                        T2: Tag,
                        T3: Tag,
                        T4: Tag,
                        T5: Tag,
                        T6: Tag,
                        T7: Tag]: Tag[CStruct7[T1, T2, T3, T4, T5, T6, T7]] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct8[T1: Tag,
                        T2: Tag,
                        T3: Tag,
                        T4: Tag,
                        T5: Tag,
                        T6: Tag,
                        T7: Tag,
                        T8: Tag]
    : Tag[CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct9[T1: Tag,
                        T2: Tag,
                        T3: Tag,
                        T4: Tag,
                        T5: Tag,
                        T6: Tag,
                        T7: Tag,
                        T8: Tag,
                        T9: Tag]
    : Tag[CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct10[T1: Tag,
                         T2: Tag,
                         T3: Tag,
                         T4: Tag,
                         T5: Tag,
                         T6: Tag,
                         T7: Tag,
                         T8: Tag,
                         T9: Tag,
                         T10: Tag]
    : Tag[CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10]] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct11[T1: Tag,
                         T2: Tag,
                         T3: Tag,
                         T4: Tag,
                         T5: Tag,
                         T6: Tag,
                         T7: Tag,
                         T8: Tag,
                         T9: Tag,
                         T10: Tag,
                         T11: Tag]
    : Tag[CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11]] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct12[T1: Tag,
                         T2: Tag,
                         T3: Tag,
                         T4: Tag,
                         T5: Tag,
                         T6: Tag,
                         T7: Tag,
                         T8: Tag,
                         T9: Tag,
                         T10: Tag,
                         T11: Tag,
                         T12: Tag]
    : Tag[CStruct12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12]] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct13[T1: Tag,
                         T2: Tag,
                         T3: Tag,
                         T4: Tag,
                         T5: Tag,
                         T6: Tag,
                         T7: Tag,
                         T8: Tag,
                         T9: Tag,
                         T10: Tag,
                         T11: Tag,
                         T12: Tag,
                         T13: Tag]
    : Tag[CStruct13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13]] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct14[T1: Tag,
                         T2: Tag,
                         T3: Tag,
                         T4: Tag,
                         T5: Tag,
                         T6: Tag,
                         T7: Tag,
                         T8: Tag,
                         T9: Tag,
                         T10: Tag,
                         T11: Tag,
                         T12: Tag,
                         T13: Tag,
                         T14: Tag]: Tag[
    CStruct14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14]] =
    undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct15[T1: Tag,
                         T2: Tag,
                         T3: Tag,
                         T4: Tag,
                         T5: Tag,
                         T6: Tag,
                         T7: Tag,
                         T8: Tag,
                         T9: Tag,
                         T10: Tag,
                         T11: Tag,
                         T12: Tag,
                         T13: Tag,
                         T14: Tag,
                         T15: Tag]: Tag[
    CStruct15[T1,
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
              T15]] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct16[T1: Tag,
                         T2: Tag,
                         T3: Tag,
                         T4: Tag,
                         T5: Tag,
                         T6: Tag,
                         T7: Tag,
                         T8: Tag,
                         T9: Tag,
                         T10: Tag,
                         T11: Tag,
                         T12: Tag,
                         T13: Tag,
                         T14: Tag,
                         T15: Tag,
                         T16: Tag]: Tag[
    CStruct16[T1,
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
              T16]] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct17[T1: Tag,
                         T2: Tag,
                         T3: Tag,
                         T4: Tag,
                         T5: Tag,
                         T6: Tag,
                         T7: Tag,
                         T8: Tag,
                         T9: Tag,
                         T10: Tag,
                         T11: Tag,
                         T12: Tag,
                         T13: Tag,
                         T14: Tag,
                         T15: Tag,
                         T16: Tag,
                         T17: Tag]: Tag[
    CStruct17[T1,
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
              T17]] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct18[T1: Tag,
                         T2: Tag,
                         T3: Tag,
                         T4: Tag,
                         T5: Tag,
                         T6: Tag,
                         T7: Tag,
                         T8: Tag,
                         T9: Tag,
                         T10: Tag,
                         T11: Tag,
                         T12: Tag,
                         T13: Tag,
                         T14: Tag,
                         T15: Tag,
                         T16: Tag,
                         T17: Tag,
                         T18: Tag]: Tag[
    CStruct18[T1,
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
              T18]] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct19[T1: Tag,
                         T2: Tag,
                         T3: Tag,
                         T4: Tag,
                         T5: Tag,
                         T6: Tag,
                         T7: Tag,
                         T8: Tag,
                         T9: Tag,
                         T10: Tag,
                         T11: Tag,
                         T12: Tag,
                         T13: Tag,
                         T14: Tag,
                         T15: Tag,
                         T16: Tag,
                         T17: Tag,
                         T18: Tag,
                         T19: Tag]: Tag[
    CStruct19[T1,
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
              T19]] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct20[T1: Tag,
                         T2: Tag,
                         T3: Tag,
                         T4: Tag,
                         T5: Tag,
                         T6: Tag,
                         T7: Tag,
                         T8: Tag,
                         T9: Tag,
                         T10: Tag,
                         T11: Tag,
                         T12: Tag,
                         T13: Tag,
                         T14: Tag,
                         T15: Tag,
                         T16: Tag,
                         T17: Tag,
                         T18: Tag,
                         T19: Tag,
                         T20: Tag]: Tag[
    CStruct20[T1,
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
              T20]] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct21[T1: Tag,
                         T2: Tag,
                         T3: Tag,
                         T4: Tag,
                         T5: Tag,
                         T6: Tag,
                         T7: Tag,
                         T8: Tag,
                         T9: Tag,
                         T10: Tag,
                         T11: Tag,
                         T12: Tag,
                         T13: Tag,
                         T14: Tag,
                         T15: Tag,
                         T16: Tag,
                         T17: Tag,
                         T18: Tag,
                         T19: Tag,
                         T20: Tag,
                         T21: Tag]: Tag[
    CStruct21[T1,
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
              T21]] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct22[T1: Tag,
                         T2: Tag,
                         T3: Tag,
                         T4: Tag,
                         T5: Tag,
                         T6: Tag,
                         T7: Tag,
                         T8: Tag,
                         T9: Tag,
                         T10: Tag,
                         T11: Tag,
                         T12: Tag,
                         T13: Tag,
                         T14: Tag,
                         T15: Tag,
                         T16: Tag,
                         T17: Tag,
                         T18: Tag,
                         T19: Tag,
                         T20: Tag,
                         T21: Tag,
                         T22: Tag]: Tag[
    CStruct22[T1,
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
              T22]] = undefined

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 49)
}
