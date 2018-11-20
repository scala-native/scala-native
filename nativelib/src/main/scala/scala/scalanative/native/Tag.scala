// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 1)
package scala.scalanative
package native

import scala.reflect.ClassTag
import scalanative.runtime.intrinsic

final abstract class Tag[P]

object Tag {
  implicit val Unit: Tag[Unit]                    = intrinsic
  implicit val Boolean: Tag[Boolean]              = intrinsic
  implicit val Char: Tag[Char]                    = intrinsic
  implicit val Byte: Tag[Byte]                    = intrinsic
  implicit val UByte: Tag[UByte]                  = intrinsic
  implicit val Short: Tag[Short]                  = intrinsic
  implicit val UShort: Tag[UShort]                = intrinsic
  implicit val Int: Tag[Int]                      = intrinsic
  implicit val UInt: Tag[UInt]                    = intrinsic
  implicit val Long: Tag[Long]                    = intrinsic
  implicit val ULong: Tag[ULong]                  = intrinsic
  implicit val Float: Tag[Float]                  = intrinsic
  implicit val Double: Tag[Double]                = intrinsic
  implicit def Ptr[T: Tag]: Tag[Ptr[T]]           = intrinsic
  implicit def Ref[T <: AnyRef: ClassTag]: Tag[T] = intrinsic

  implicit def Nat0: Tag[Nat._0] = intrinsic
  implicit def Nat1: Tag[Nat._1] = intrinsic
  implicit def Nat2: Tag[Nat._2] = intrinsic
  implicit def Nat3: Tag[Nat._3] = intrinsic
  implicit def Nat4: Tag[Nat._4] = intrinsic
  implicit def Nat5: Tag[Nat._5] = intrinsic
  implicit def Nat6: Tag[Nat._6] = intrinsic
  implicit def Nat7: Tag[Nat._7] = intrinsic
  implicit def Nat8: Tag[Nat._8] = intrinsic
  implicit def Nat9: Tag[Nat._9] = intrinsic
  implicit def NatDigit[N <: Nat.Base: Tag, M <: Nat: Tag]
    : Tag[Nat.Digit[N, M]] =
    intrinsic

  implicit def CArray[T: Tag, N <: Nat: Tag]: Tag[CArray[T, N]] = intrinsic

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct0: Tag[CStruct0] = intrinsic

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct1[T1: Tag]: Tag[CStruct1[T1]] = intrinsic

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct2[T1: Tag, T2: Tag]: Tag[CStruct2[T1, T2]] = intrinsic

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct3[T1: Tag, T2: Tag, T3: Tag]: Tag[CStruct3[T1, T2, T3]] =
    intrinsic

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct4[T1: Tag, T2: Tag, T3: Tag, T4: Tag]
    : Tag[CStruct4[T1, T2, T3, T4]] = intrinsic

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct5[T1: Tag, T2: Tag, T3: Tag, T4: Tag, T5: Tag]
    : Tag[CStruct5[T1, T2, T3, T4, T5]] = intrinsic

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct6[T1: Tag, T2: Tag, T3: Tag, T4: Tag, T5: Tag, T6: Tag]
    : Tag[CStruct6[T1, T2, T3, T4, T5, T6]] = intrinsic

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct7[T1: Tag,
                        T2: Tag,
                        T3: Tag,
                        T4: Tag,
                        T5: Tag,
                        T6: Tag,
                        T7: Tag]: Tag[CStruct7[T1, T2, T3, T4, T5, T6, T7]] =
    intrinsic

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 45)

  implicit def CStruct8[T1: Tag,
                        T2: Tag,
                        T3: Tag,
                        T4: Tag,
                        T5: Tag,
                        T6: Tag,
                        T7: Tag,
                        T8: Tag]
    : Tag[CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]] = intrinsic

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
    : Tag[CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]] = intrinsic

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
    : Tag[CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10]] = intrinsic

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
    : Tag[CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11]] = intrinsic

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
    intrinsic

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
    intrinsic

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
    intrinsic

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
              T15]] = intrinsic

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
              T16]] = intrinsic

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
              T17]] = intrinsic

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
              T18]] = intrinsic

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
              T19]] = intrinsic

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
              T20]] = intrinsic

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
              T21]] = intrinsic

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
              T22]] = intrinsic

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 49)
}
