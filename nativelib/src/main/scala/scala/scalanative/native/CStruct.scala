// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 1)
package scala.scalanative
package native

import scalanative.runtime.{fromRawPtr, RawPtr}
import scalanative.runtime.Intrinsics._

sealed abstract class CStruct

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct0 private[scalanative] (
    private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct0 =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct0@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr: Ptr[CStruct0] =
    fromRawPtr[CStruct0](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct1[T1] private[scalanative] (
    private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct1[_] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct1@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr: Ptr[CStruct1[T1]] =
    fromRawPtr[CStruct1[T1]](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(implicit tag: Tag.CStruct1[T1]): T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(implicit tag: Tag.CStruct1[T1]): Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct2[T1, T2] private[scalanative] (
    private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct2[_, _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct2@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr: Ptr[CStruct2[T1, T2]] =
    fromRawPtr[CStruct2[T1, T2]](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(implicit tag: Tag.CStruct2[T1, T2]): T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(
      implicit tag: Tag.CStruct2[T1, T2]): Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(implicit tag: Tag.CStruct2[T1, T2]): T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(
      implicit tag: Tag.CStruct2[T1, T2]): Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct3[T1, T2, T3] private[scalanative] (
    private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct3[_, _, _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct3@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr: Ptr[CStruct3[T1, T2, T3]] =
    fromRawPtr[CStruct3[T1, T2, T3]](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(implicit tag: Tag.CStruct3[T1, T2, T3]): T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(
      implicit tag: Tag.CStruct3[T1, T2, T3]): Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(implicit tag: Tag.CStruct3[T1, T2, T3]): T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(
      implicit tag: Tag.CStruct3[T1, T2, T3]): Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 3. */
  @alwaysinline def _3(implicit tag: Tag.CStruct3[T1, T2, T3]): T3 = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.unary_!(tag._3)
  }

  /** Store a value to a field number 3. */
  @alwaysinline def _3_=(value: T3)(
      implicit tag: Tag.CStruct3[T1, T2, T3]): Unit = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.`unary_!_=`(value)(tag._3)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct4[T1, T2, T3, T4] private[scalanative] (
    private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct4[_, _, _, _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct4@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr: Ptr[CStruct4[T1, T2, T3, T4]] =
    fromRawPtr[CStruct4[T1, T2, T3, T4]](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(implicit tag: Tag.CStruct4[T1, T2, T3, T4]): T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(
      implicit tag: Tag.CStruct4[T1, T2, T3, T4]): Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(implicit tag: Tag.CStruct4[T1, T2, T3, T4]): T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(
      implicit tag: Tag.CStruct4[T1, T2, T3, T4]): Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 3. */
  @alwaysinline def _3(implicit tag: Tag.CStruct4[T1, T2, T3, T4]): T3 = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.unary_!(tag._3)
  }

  /** Store a value to a field number 3. */
  @alwaysinline def _3_=(value: T3)(
      implicit tag: Tag.CStruct4[T1, T2, T3, T4]): Unit = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.`unary_!_=`(value)(tag._3)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 4. */
  @alwaysinline def _4(implicit tag: Tag.CStruct4[T1, T2, T3, T4]): T4 = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.unary_!(tag._4)
  }

  /** Store a value to a field number 4. */
  @alwaysinline def _4_=(value: T4)(
      implicit tag: Tag.CStruct4[T1, T2, T3, T4]): Unit = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.`unary_!_=`(value)(tag._4)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct5[T1, T2, T3, T4, T5] private[scalanative] (
    private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct5[_, _, _, _, _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct5@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr: Ptr[CStruct5[T1, T2, T3, T4, T5]] =
    fromRawPtr[CStruct5[T1, T2, T3, T4, T5]](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(implicit tag: Tag.CStruct5[T1, T2, T3, T4, T5]): T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(
      implicit tag: Tag.CStruct5[T1, T2, T3, T4, T5]): Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(implicit tag: Tag.CStruct5[T1, T2, T3, T4, T5]): T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(
      implicit tag: Tag.CStruct5[T1, T2, T3, T4, T5]): Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 3. */
  @alwaysinline def _3(implicit tag: Tag.CStruct5[T1, T2, T3, T4, T5]): T3 = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.unary_!(tag._3)
  }

  /** Store a value to a field number 3. */
  @alwaysinline def _3_=(value: T3)(
      implicit tag: Tag.CStruct5[T1, T2, T3, T4, T5]): Unit = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.`unary_!_=`(value)(tag._3)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 4. */
  @alwaysinline def _4(implicit tag: Tag.CStruct5[T1, T2, T3, T4, T5]): T4 = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.unary_!(tag._4)
  }

  /** Store a value to a field number 4. */
  @alwaysinline def _4_=(value: T4)(
      implicit tag: Tag.CStruct5[T1, T2, T3, T4, T5]): Unit = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.`unary_!_=`(value)(tag._4)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 5. */
  @alwaysinline def _5(implicit tag: Tag.CStruct5[T1, T2, T3, T4, T5]): T5 = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.unary_!(tag._5)
  }

  /** Store a value to a field number 5. */
  @alwaysinline def _5_=(value: T5)(
      implicit tag: Tag.CStruct5[T1, T2, T3, T4, T5]): Unit = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.`unary_!_=`(value)(tag._5)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct6[T1, T2, T3, T4, T5, T6] private[scalanative] (
    private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct6[_, _, _, _, _, _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct6@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr: Ptr[CStruct6[T1, T2, T3, T4, T5, T6]] =
    fromRawPtr[CStruct6[T1, T2, T3, T4, T5, T6]](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(
      implicit tag: Tag.CStruct6[T1, T2, T3, T4, T5, T6]): T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(
      implicit tag: Tag.CStruct6[T1, T2, T3, T4, T5, T6]): Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(
      implicit tag: Tag.CStruct6[T1, T2, T3, T4, T5, T6]): T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(
      implicit tag: Tag.CStruct6[T1, T2, T3, T4, T5, T6]): Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 3. */
  @alwaysinline def _3(
      implicit tag: Tag.CStruct6[T1, T2, T3, T4, T5, T6]): T3 = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.unary_!(tag._3)
  }

  /** Store a value to a field number 3. */
  @alwaysinline def _3_=(value: T3)(
      implicit tag: Tag.CStruct6[T1, T2, T3, T4, T5, T6]): Unit = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.`unary_!_=`(value)(tag._3)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 4. */
  @alwaysinline def _4(
      implicit tag: Tag.CStruct6[T1, T2, T3, T4, T5, T6]): T4 = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.unary_!(tag._4)
  }

  /** Store a value to a field number 4. */
  @alwaysinline def _4_=(value: T4)(
      implicit tag: Tag.CStruct6[T1, T2, T3, T4, T5, T6]): Unit = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.`unary_!_=`(value)(tag._4)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 5. */
  @alwaysinline def _5(
      implicit tag: Tag.CStruct6[T1, T2, T3, T4, T5, T6]): T5 = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.unary_!(tag._5)
  }

  /** Store a value to a field number 5. */
  @alwaysinline def _5_=(value: T5)(
      implicit tag: Tag.CStruct6[T1, T2, T3, T4, T5, T6]): Unit = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.`unary_!_=`(value)(tag._5)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 6. */
  @alwaysinline def _6(
      implicit tag: Tag.CStruct6[T1, T2, T3, T4, T5, T6]): T6 = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.unary_!(tag._6)
  }

  /** Store a value to a field number 6. */
  @alwaysinline def _6_=(value: T6)(
      implicit tag: Tag.CStruct6[T1, T2, T3, T4, T5, T6]): Unit = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.`unary_!_=`(value)(tag._6)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct7[T1, T2, T3, T4, T5, T6, T7] private[scalanative] (
    private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct7[_, _, _, _, _, _, _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct7@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr: Ptr[CStruct7[T1, T2, T3, T4, T5, T6, T7]] =
    fromRawPtr[CStruct7[T1, T2, T3, T4, T5, T6, T7]](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(
      implicit tag: Tag.CStruct7[T1, T2, T3, T4, T5, T6, T7]): T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(
      implicit tag: Tag.CStruct7[T1, T2, T3, T4, T5, T6, T7]): Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(
      implicit tag: Tag.CStruct7[T1, T2, T3, T4, T5, T6, T7]): T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(
      implicit tag: Tag.CStruct7[T1, T2, T3, T4, T5, T6, T7]): Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 3. */
  @alwaysinline def _3(
      implicit tag: Tag.CStruct7[T1, T2, T3, T4, T5, T6, T7]): T3 = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.unary_!(tag._3)
  }

  /** Store a value to a field number 3. */
  @alwaysinline def _3_=(value: T3)(
      implicit tag: Tag.CStruct7[T1, T2, T3, T4, T5, T6, T7]): Unit = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.`unary_!_=`(value)(tag._3)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 4. */
  @alwaysinline def _4(
      implicit tag: Tag.CStruct7[T1, T2, T3, T4, T5, T6, T7]): T4 = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.unary_!(tag._4)
  }

  /** Store a value to a field number 4. */
  @alwaysinline def _4_=(value: T4)(
      implicit tag: Tag.CStruct7[T1, T2, T3, T4, T5, T6, T7]): Unit = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.`unary_!_=`(value)(tag._4)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 5. */
  @alwaysinline def _5(
      implicit tag: Tag.CStruct7[T1, T2, T3, T4, T5, T6, T7]): T5 = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.unary_!(tag._5)
  }

  /** Store a value to a field number 5. */
  @alwaysinline def _5_=(value: T5)(
      implicit tag: Tag.CStruct7[T1, T2, T3, T4, T5, T6, T7]): Unit = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.`unary_!_=`(value)(tag._5)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 6. */
  @alwaysinline def _6(
      implicit tag: Tag.CStruct7[T1, T2, T3, T4, T5, T6, T7]): T6 = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.unary_!(tag._6)
  }

  /** Store a value to a field number 6. */
  @alwaysinline def _6_=(value: T6)(
      implicit tag: Tag.CStruct7[T1, T2, T3, T4, T5, T6, T7]): Unit = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.`unary_!_=`(value)(tag._6)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 7. */
  @alwaysinline def _7(
      implicit tag: Tag.CStruct7[T1, T2, T3, T4, T5, T6, T7]): T7 = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.unary_!(tag._7)
  }

  /** Store a value to a field number 7. */
  @alwaysinline def _7_=(value: T7)(
      implicit tag: Tag.CStruct7[T1, T2, T3, T4, T5, T6, T7]): Unit = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.`unary_!_=`(value)(tag._7)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct8[T1, T2, T3, T4, T5, T6, T7, T8] private[scalanative] (
    private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct8[_, _, _, _, _, _, _, _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct8@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr: Ptr[CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]] =
    fromRawPtr[CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(
      implicit tag: Tag.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]): T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(
      implicit tag: Tag.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]): Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(
      implicit tag: Tag.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]): T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(
      implicit tag: Tag.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]): Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 3. */
  @alwaysinline def _3(
      implicit tag: Tag.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]): T3 = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.unary_!(tag._3)
  }

  /** Store a value to a field number 3. */
  @alwaysinline def _3_=(value: T3)(
      implicit tag: Tag.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]): Unit = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.`unary_!_=`(value)(tag._3)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 4. */
  @alwaysinline def _4(
      implicit tag: Tag.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]): T4 = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.unary_!(tag._4)
  }

  /** Store a value to a field number 4. */
  @alwaysinline def _4_=(value: T4)(
      implicit tag: Tag.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]): Unit = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.`unary_!_=`(value)(tag._4)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 5. */
  @alwaysinline def _5(
      implicit tag: Tag.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]): T5 = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.unary_!(tag._5)
  }

  /** Store a value to a field number 5. */
  @alwaysinline def _5_=(value: T5)(
      implicit tag: Tag.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]): Unit = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.`unary_!_=`(value)(tag._5)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 6. */
  @alwaysinline def _6(
      implicit tag: Tag.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]): T6 = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.unary_!(tag._6)
  }

  /** Store a value to a field number 6. */
  @alwaysinline def _6_=(value: T6)(
      implicit tag: Tag.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]): Unit = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.`unary_!_=`(value)(tag._6)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 7. */
  @alwaysinline def _7(
      implicit tag: Tag.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]): T7 = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.unary_!(tag._7)
  }

  /** Store a value to a field number 7. */
  @alwaysinline def _7_=(value: T7)(
      implicit tag: Tag.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]): Unit = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.`unary_!_=`(value)(tag._7)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 8. */
  @alwaysinline def _8(
      implicit tag: Tag.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]): T8 = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.unary_!(tag._8)
  }

  /** Store a value to a field number 8. */
  @alwaysinline def _8_=(value: T8)(
      implicit tag: Tag.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]): Unit = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.`unary_!_=`(value)(tag._8)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9] private[scalanative] (
    private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct9[_, _, _, _, _, _, _, _, _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct9@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr: Ptr[CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]] =
    fromRawPtr[CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(
      implicit tag: Tag.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]): T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(
      implicit tag: Tag.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]): Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(
      implicit tag: Tag.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]): T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(
      implicit tag: Tag.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]): Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 3. */
  @alwaysinline def _3(
      implicit tag: Tag.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]): T3 = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.unary_!(tag._3)
  }

  /** Store a value to a field number 3. */
  @alwaysinline def _3_=(value: T3)(
      implicit tag: Tag.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]): Unit = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.`unary_!_=`(value)(tag._3)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 4. */
  @alwaysinline def _4(
      implicit tag: Tag.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]): T4 = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.unary_!(tag._4)
  }

  /** Store a value to a field number 4. */
  @alwaysinline def _4_=(value: T4)(
      implicit tag: Tag.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]): Unit = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.`unary_!_=`(value)(tag._4)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 5. */
  @alwaysinline def _5(
      implicit tag: Tag.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]): T5 = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.unary_!(tag._5)
  }

  /** Store a value to a field number 5. */
  @alwaysinline def _5_=(value: T5)(
      implicit tag: Tag.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]): Unit = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.`unary_!_=`(value)(tag._5)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 6. */
  @alwaysinline def _6(
      implicit tag: Tag.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]): T6 = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.unary_!(tag._6)
  }

  /** Store a value to a field number 6. */
  @alwaysinline def _6_=(value: T6)(
      implicit tag: Tag.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]): Unit = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.`unary_!_=`(value)(tag._6)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 7. */
  @alwaysinline def _7(
      implicit tag: Tag.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]): T7 = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.unary_!(tag._7)
  }

  /** Store a value to a field number 7. */
  @alwaysinline def _7_=(value: T7)(
      implicit tag: Tag.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]): Unit = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.`unary_!_=`(value)(tag._7)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 8. */
  @alwaysinline def _8(
      implicit tag: Tag.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]): T8 = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.unary_!(tag._8)
  }

  /** Store a value to a field number 8. */
  @alwaysinline def _8_=(value: T8)(
      implicit tag: Tag.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]): Unit = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.`unary_!_=`(value)(tag._8)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 9. */
  @alwaysinline def _9(
      implicit tag: Tag.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]): T9 = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.unary_!(tag._9)
  }

  /** Store a value to a field number 9. */
  @alwaysinline def _9_=(value: T9)(
      implicit tag: Tag.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]): Unit = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.`unary_!_=`(value)(tag._9)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10] private[scalanative] (
    private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct10[_, _, _, _, _, _, _, _, _, _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct10@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr
    : Ptr[CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10]] =
    fromRawPtr[CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10]](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(
      implicit tag: Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
    : T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(
      implicit tag: Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
    : Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(
      implicit tag: Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
    : T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(
      implicit tag: Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
    : Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 3. */
  @alwaysinline def _3(
      implicit tag: Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
    : T3 = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.unary_!(tag._3)
  }

  /** Store a value to a field number 3. */
  @alwaysinline def _3_=(value: T3)(
      implicit tag: Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
    : Unit = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.`unary_!_=`(value)(tag._3)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 4. */
  @alwaysinline def _4(
      implicit tag: Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
    : T4 = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.unary_!(tag._4)
  }

  /** Store a value to a field number 4. */
  @alwaysinline def _4_=(value: T4)(
      implicit tag: Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
    : Unit = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.`unary_!_=`(value)(tag._4)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 5. */
  @alwaysinline def _5(
      implicit tag: Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
    : T5 = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.unary_!(tag._5)
  }

  /** Store a value to a field number 5. */
  @alwaysinline def _5_=(value: T5)(
      implicit tag: Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
    : Unit = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.`unary_!_=`(value)(tag._5)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 6. */
  @alwaysinline def _6(
      implicit tag: Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
    : T6 = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.unary_!(tag._6)
  }

  /** Store a value to a field number 6. */
  @alwaysinline def _6_=(value: T6)(
      implicit tag: Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
    : Unit = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.`unary_!_=`(value)(tag._6)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 7. */
  @alwaysinline def _7(
      implicit tag: Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
    : T7 = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.unary_!(tag._7)
  }

  /** Store a value to a field number 7. */
  @alwaysinline def _7_=(value: T7)(
      implicit tag: Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
    : Unit = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.`unary_!_=`(value)(tag._7)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 8. */
  @alwaysinline def _8(
      implicit tag: Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
    : T8 = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.unary_!(tag._8)
  }

  /** Store a value to a field number 8. */
  @alwaysinline def _8_=(value: T8)(
      implicit tag: Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
    : Unit = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.`unary_!_=`(value)(tag._8)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 9. */
  @alwaysinline def _9(
      implicit tag: Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
    : T9 = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.unary_!(tag._9)
  }

  /** Store a value to a field number 9. */
  @alwaysinline def _9_=(value: T9)(
      implicit tag: Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
    : Unit = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.`unary_!_=`(value)(tag._9)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 10. */
  @alwaysinline def _10(
      implicit tag: Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
    : T10 = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.unary_!(tag._10)
  }

  /** Store a value to a field number 10. */
  @alwaysinline def _10_=(value: T10)(
      implicit tag: Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
    : Unit = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.`unary_!_=`(value)(tag._10)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11] private[scalanative] (
    private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct11[_, _, _, _, _, _, _, _, _, _, _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct11@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr
    : Ptr[CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11]] =
    fromRawPtr[CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11]](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 3. */
  @alwaysinline def _3(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : T3 = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.unary_!(tag._3)
  }

  /** Store a value to a field number 3. */
  @alwaysinline def _3_=(value: T3)(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : Unit = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.`unary_!_=`(value)(tag._3)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 4. */
  @alwaysinline def _4(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : T4 = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.unary_!(tag._4)
  }

  /** Store a value to a field number 4. */
  @alwaysinline def _4_=(value: T4)(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : Unit = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.`unary_!_=`(value)(tag._4)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 5. */
  @alwaysinline def _5(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : T5 = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.unary_!(tag._5)
  }

  /** Store a value to a field number 5. */
  @alwaysinline def _5_=(value: T5)(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : Unit = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.`unary_!_=`(value)(tag._5)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 6. */
  @alwaysinline def _6(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : T6 = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.unary_!(tag._6)
  }

  /** Store a value to a field number 6. */
  @alwaysinline def _6_=(value: T6)(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : Unit = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.`unary_!_=`(value)(tag._6)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 7. */
  @alwaysinline def _7(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : T7 = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.unary_!(tag._7)
  }

  /** Store a value to a field number 7. */
  @alwaysinline def _7_=(value: T7)(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : Unit = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.`unary_!_=`(value)(tag._7)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 8. */
  @alwaysinline def _8(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : T8 = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.unary_!(tag._8)
  }

  /** Store a value to a field number 8. */
  @alwaysinline def _8_=(value: T8)(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : Unit = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.`unary_!_=`(value)(tag._8)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 9. */
  @alwaysinline def _9(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : T9 = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.unary_!(tag._9)
  }

  /** Store a value to a field number 9. */
  @alwaysinline def _9_=(value: T9)(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : Unit = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.`unary_!_=`(value)(tag._9)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 10. */
  @alwaysinline def _10(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : T10 = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.unary_!(tag._10)
  }

  /** Store a value to a field number 10. */
  @alwaysinline def _10_=(value: T10)(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : Unit = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.`unary_!_=`(value)(tag._10)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 11. */
  @alwaysinline def _11(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : T11 = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.unary_!(tag._11)
  }

  /** Store a value to a field number 11. */
  @alwaysinline def _11_=(value: T11)(
      implicit tag: Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
    : Unit = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.`unary_!_=`(value)(tag._11)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12] private[scalanative] (
    private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct12[_, _, _, _, _, _, _, _, _, _, _, _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct12@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr
    : Ptr[CStruct12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12]] =
    fromRawPtr[CStruct12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12]](
      rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(
      implicit tag: Tag.CStruct12[T1,
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
                                  T12]): T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(implicit tag: Tag.CStruct12[T1,
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
                                                                T12]): Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(
      implicit tag: Tag.CStruct12[T1,
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
                                  T12]): T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(implicit tag: Tag.CStruct12[T1,
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
                                                                T12]): Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 3. */
  @alwaysinline def _3(
      implicit tag: Tag.CStruct12[T1,
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
                                  T12]): T3 = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.unary_!(tag._3)
  }

  /** Store a value to a field number 3. */
  @alwaysinline def _3_=(value: T3)(implicit tag: Tag.CStruct12[T1,
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
                                                                T12]): Unit = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.`unary_!_=`(value)(tag._3)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 4. */
  @alwaysinline def _4(
      implicit tag: Tag.CStruct12[T1,
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
                                  T12]): T4 = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.unary_!(tag._4)
  }

  /** Store a value to a field number 4. */
  @alwaysinline def _4_=(value: T4)(implicit tag: Tag.CStruct12[T1,
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
                                                                T12]): Unit = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.`unary_!_=`(value)(tag._4)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 5. */
  @alwaysinline def _5(
      implicit tag: Tag.CStruct12[T1,
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
                                  T12]): T5 = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.unary_!(tag._5)
  }

  /** Store a value to a field number 5. */
  @alwaysinline def _5_=(value: T5)(implicit tag: Tag.CStruct12[T1,
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
                                                                T12]): Unit = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.`unary_!_=`(value)(tag._5)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 6. */
  @alwaysinline def _6(
      implicit tag: Tag.CStruct12[T1,
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
                                  T12]): T6 = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.unary_!(tag._6)
  }

  /** Store a value to a field number 6. */
  @alwaysinline def _6_=(value: T6)(implicit tag: Tag.CStruct12[T1,
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
                                                                T12]): Unit = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.`unary_!_=`(value)(tag._6)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 7. */
  @alwaysinline def _7(
      implicit tag: Tag.CStruct12[T1,
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
                                  T12]): T7 = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.unary_!(tag._7)
  }

  /** Store a value to a field number 7. */
  @alwaysinline def _7_=(value: T7)(implicit tag: Tag.CStruct12[T1,
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
                                                                T12]): Unit = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.`unary_!_=`(value)(tag._7)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 8. */
  @alwaysinline def _8(
      implicit tag: Tag.CStruct12[T1,
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
                                  T12]): T8 = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.unary_!(tag._8)
  }

  /** Store a value to a field number 8. */
  @alwaysinline def _8_=(value: T8)(implicit tag: Tag.CStruct12[T1,
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
                                                                T12]): Unit = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.`unary_!_=`(value)(tag._8)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 9. */
  @alwaysinline def _9(
      implicit tag: Tag.CStruct12[T1,
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
                                  T12]): T9 = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.unary_!(tag._9)
  }

  /** Store a value to a field number 9. */
  @alwaysinline def _9_=(value: T9)(implicit tag: Tag.CStruct12[T1,
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
                                                                T12]): Unit = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.`unary_!_=`(value)(tag._9)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 10. */
  @alwaysinline def _10(
      implicit tag: Tag.CStruct12[T1,
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
                                  T12]): T10 = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.unary_!(tag._10)
  }

  /** Store a value to a field number 10. */
  @alwaysinline def _10_=(value: T10)(
      implicit tag: Tag.CStruct12[T1,
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
                                  T12]): Unit = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.`unary_!_=`(value)(tag._10)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 11. */
  @alwaysinline def _11(
      implicit tag: Tag.CStruct12[T1,
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
                                  T12]): T11 = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.unary_!(tag._11)
  }

  /** Store a value to a field number 11. */
  @alwaysinline def _11_=(value: T11)(
      implicit tag: Tag.CStruct12[T1,
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
                                  T12]): Unit = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.`unary_!_=`(value)(tag._11)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 12. */
  @alwaysinline def _12(
      implicit tag: Tag.CStruct12[T1,
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
                                  T12]): T12 = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.unary_!(tag._12)
  }

  /** Store a value to a field number 12. */
  @alwaysinline def _12_=(value: T12)(
      implicit tag: Tag.CStruct12[T1,
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
                                  T12]): Unit = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.`unary_!_=`(value)(tag._12)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13] private[scalanative] (
    private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct13[_, _, _, _, _, _, _, _, _, _, _, _, _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct13@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr
    : Ptr[CStruct13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13]] =
    fromRawPtr[
      CStruct13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13]](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(
      implicit tag: Tag.CStruct13[T1,
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
                                  T13]): T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(implicit tag: Tag.CStruct13[T1,
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
                                                                T13]): Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(
      implicit tag: Tag.CStruct13[T1,
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
                                  T13]): T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(implicit tag: Tag.CStruct13[T1,
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
                                                                T13]): Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 3. */
  @alwaysinline def _3(
      implicit tag: Tag.CStruct13[T1,
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
                                  T13]): T3 = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.unary_!(tag._3)
  }

  /** Store a value to a field number 3. */
  @alwaysinline def _3_=(value: T3)(implicit tag: Tag.CStruct13[T1,
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
                                                                T13]): Unit = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.`unary_!_=`(value)(tag._3)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 4. */
  @alwaysinline def _4(
      implicit tag: Tag.CStruct13[T1,
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
                                  T13]): T4 = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.unary_!(tag._4)
  }

  /** Store a value to a field number 4. */
  @alwaysinline def _4_=(value: T4)(implicit tag: Tag.CStruct13[T1,
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
                                                                T13]): Unit = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.`unary_!_=`(value)(tag._4)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 5. */
  @alwaysinline def _5(
      implicit tag: Tag.CStruct13[T1,
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
                                  T13]): T5 = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.unary_!(tag._5)
  }

  /** Store a value to a field number 5. */
  @alwaysinline def _5_=(value: T5)(implicit tag: Tag.CStruct13[T1,
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
                                                                T13]): Unit = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.`unary_!_=`(value)(tag._5)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 6. */
  @alwaysinline def _6(
      implicit tag: Tag.CStruct13[T1,
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
                                  T13]): T6 = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.unary_!(tag._6)
  }

  /** Store a value to a field number 6. */
  @alwaysinline def _6_=(value: T6)(implicit tag: Tag.CStruct13[T1,
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
                                                                T13]): Unit = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.`unary_!_=`(value)(tag._6)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 7. */
  @alwaysinline def _7(
      implicit tag: Tag.CStruct13[T1,
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
                                  T13]): T7 = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.unary_!(tag._7)
  }

  /** Store a value to a field number 7. */
  @alwaysinline def _7_=(value: T7)(implicit tag: Tag.CStruct13[T1,
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
                                                                T13]): Unit = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.`unary_!_=`(value)(tag._7)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 8. */
  @alwaysinline def _8(
      implicit tag: Tag.CStruct13[T1,
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
                                  T13]): T8 = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.unary_!(tag._8)
  }

  /** Store a value to a field number 8. */
  @alwaysinline def _8_=(value: T8)(implicit tag: Tag.CStruct13[T1,
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
                                                                T13]): Unit = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.`unary_!_=`(value)(tag._8)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 9. */
  @alwaysinline def _9(
      implicit tag: Tag.CStruct13[T1,
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
                                  T13]): T9 = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.unary_!(tag._9)
  }

  /** Store a value to a field number 9. */
  @alwaysinline def _9_=(value: T9)(implicit tag: Tag.CStruct13[T1,
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
                                                                T13]): Unit = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.`unary_!_=`(value)(tag._9)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 10. */
  @alwaysinline def _10(
      implicit tag: Tag.CStruct13[T1,
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
                                  T13]): T10 = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.unary_!(tag._10)
  }

  /** Store a value to a field number 10. */
  @alwaysinline def _10_=(value: T10)(
      implicit tag: Tag.CStruct13[T1,
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
                                  T13]): Unit = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.`unary_!_=`(value)(tag._10)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 11. */
  @alwaysinline def _11(
      implicit tag: Tag.CStruct13[T1,
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
                                  T13]): T11 = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.unary_!(tag._11)
  }

  /** Store a value to a field number 11. */
  @alwaysinline def _11_=(value: T11)(
      implicit tag: Tag.CStruct13[T1,
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
                                  T13]): Unit = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.`unary_!_=`(value)(tag._11)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 12. */
  @alwaysinline def _12(
      implicit tag: Tag.CStruct13[T1,
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
                                  T13]): T12 = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.unary_!(tag._12)
  }

  /** Store a value to a field number 12. */
  @alwaysinline def _12_=(value: T12)(
      implicit tag: Tag.CStruct13[T1,
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
                                  T13]): Unit = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.`unary_!_=`(value)(tag._12)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 13. */
  @alwaysinline def _13(
      implicit tag: Tag.CStruct13[T1,
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
                                  T13]): T13 = {
    val ptr = new Ptr[T13](elemRawPtr(rawptr, tag.offset(12)))
    ptr.unary_!(tag._13)
  }

  /** Store a value to a field number 13. */
  @alwaysinline def _13_=(value: T13)(
      implicit tag: Tag.CStruct13[T1,
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
                                  T13]): Unit = {
    val ptr = new Ptr[T13](elemRawPtr(rawptr, tag.offset(12)))
    ptr.`unary_!_=`(value)(tag._13)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13,
T14] private[scalanative] (private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct14[_, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct14@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr: Ptr[
    CStruct14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14]] =
    fromRawPtr[
      CStruct14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14]](
      rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(
      implicit tag: Tag.CStruct14[T1,
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
                                  T14]): T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(implicit tag: Tag.CStruct14[T1,
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
                                                                T14]): Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(
      implicit tag: Tag.CStruct14[T1,
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
                                  T14]): T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(implicit tag: Tag.CStruct14[T1,
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
                                                                T14]): Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 3. */
  @alwaysinline def _3(
      implicit tag: Tag.CStruct14[T1,
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
                                  T14]): T3 = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.unary_!(tag._3)
  }

  /** Store a value to a field number 3. */
  @alwaysinline def _3_=(value: T3)(implicit tag: Tag.CStruct14[T1,
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
                                                                T14]): Unit = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.`unary_!_=`(value)(tag._3)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 4. */
  @alwaysinline def _4(
      implicit tag: Tag.CStruct14[T1,
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
                                  T14]): T4 = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.unary_!(tag._4)
  }

  /** Store a value to a field number 4. */
  @alwaysinline def _4_=(value: T4)(implicit tag: Tag.CStruct14[T1,
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
                                                                T14]): Unit = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.`unary_!_=`(value)(tag._4)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 5. */
  @alwaysinline def _5(
      implicit tag: Tag.CStruct14[T1,
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
                                  T14]): T5 = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.unary_!(tag._5)
  }

  /** Store a value to a field number 5. */
  @alwaysinline def _5_=(value: T5)(implicit tag: Tag.CStruct14[T1,
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
                                                                T14]): Unit = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.`unary_!_=`(value)(tag._5)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 6. */
  @alwaysinline def _6(
      implicit tag: Tag.CStruct14[T1,
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
                                  T14]): T6 = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.unary_!(tag._6)
  }

  /** Store a value to a field number 6. */
  @alwaysinline def _6_=(value: T6)(implicit tag: Tag.CStruct14[T1,
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
                                                                T14]): Unit = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.`unary_!_=`(value)(tag._6)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 7. */
  @alwaysinline def _7(
      implicit tag: Tag.CStruct14[T1,
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
                                  T14]): T7 = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.unary_!(tag._7)
  }

  /** Store a value to a field number 7. */
  @alwaysinline def _7_=(value: T7)(implicit tag: Tag.CStruct14[T1,
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
                                                                T14]): Unit = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.`unary_!_=`(value)(tag._7)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 8. */
  @alwaysinline def _8(
      implicit tag: Tag.CStruct14[T1,
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
                                  T14]): T8 = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.unary_!(tag._8)
  }

  /** Store a value to a field number 8. */
  @alwaysinline def _8_=(value: T8)(implicit tag: Tag.CStruct14[T1,
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
                                                                T14]): Unit = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.`unary_!_=`(value)(tag._8)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 9. */
  @alwaysinline def _9(
      implicit tag: Tag.CStruct14[T1,
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
                                  T14]): T9 = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.unary_!(tag._9)
  }

  /** Store a value to a field number 9. */
  @alwaysinline def _9_=(value: T9)(implicit tag: Tag.CStruct14[T1,
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
                                                                T14]): Unit = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.`unary_!_=`(value)(tag._9)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 10. */
  @alwaysinline def _10(
      implicit tag: Tag.CStruct14[T1,
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
                                  T14]): T10 = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.unary_!(tag._10)
  }

  /** Store a value to a field number 10. */
  @alwaysinline def _10_=(value: T10)(
      implicit tag: Tag.CStruct14[T1,
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
                                  T14]): Unit = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.`unary_!_=`(value)(tag._10)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 11. */
  @alwaysinline def _11(
      implicit tag: Tag.CStruct14[T1,
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
                                  T14]): T11 = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.unary_!(tag._11)
  }

  /** Store a value to a field number 11. */
  @alwaysinline def _11_=(value: T11)(
      implicit tag: Tag.CStruct14[T1,
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
                                  T14]): Unit = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.`unary_!_=`(value)(tag._11)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 12. */
  @alwaysinline def _12(
      implicit tag: Tag.CStruct14[T1,
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
                                  T14]): T12 = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.unary_!(tag._12)
  }

  /** Store a value to a field number 12. */
  @alwaysinline def _12_=(value: T12)(
      implicit tag: Tag.CStruct14[T1,
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
                                  T14]): Unit = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.`unary_!_=`(value)(tag._12)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 13. */
  @alwaysinline def _13(
      implicit tag: Tag.CStruct14[T1,
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
                                  T14]): T13 = {
    val ptr = new Ptr[T13](elemRawPtr(rawptr, tag.offset(12)))
    ptr.unary_!(tag._13)
  }

  /** Store a value to a field number 13. */
  @alwaysinline def _13_=(value: T13)(
      implicit tag: Tag.CStruct14[T1,
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
                                  T14]): Unit = {
    val ptr = new Ptr[T13](elemRawPtr(rawptr, tag.offset(12)))
    ptr.`unary_!_=`(value)(tag._13)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 14. */
  @alwaysinline def _14(
      implicit tag: Tag.CStruct14[T1,
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
                                  T14]): T14 = {
    val ptr = new Ptr[T14](elemRawPtr(rawptr, tag.offset(13)))
    ptr.unary_!(tag._14)
  }

  /** Store a value to a field number 14. */
  @alwaysinline def _14_=(value: T14)(
      implicit tag: Tag.CStruct14[T1,
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
                                  T14]): Unit = {
    val ptr = new Ptr[T14](elemRawPtr(rawptr, tag.offset(13)))
    ptr.`unary_!_=`(value)(tag._14)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13,
T14, T15] private[scalanative] (private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct15@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr: Ptr[
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
              T15]] =
    fromRawPtr[
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
                T15]](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(implicit tag: Tag.CStruct15[T1,
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
                                                                T15]): Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(implicit tag: Tag.CStruct15[T1,
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
                                                                T15]): Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 3. */
  @alwaysinline def _3(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): T3 = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.unary_!(tag._3)
  }

  /** Store a value to a field number 3. */
  @alwaysinline def _3_=(value: T3)(implicit tag: Tag.CStruct15[T1,
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
                                                                T15]): Unit = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.`unary_!_=`(value)(tag._3)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 4. */
  @alwaysinline def _4(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): T4 = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.unary_!(tag._4)
  }

  /** Store a value to a field number 4. */
  @alwaysinline def _4_=(value: T4)(implicit tag: Tag.CStruct15[T1,
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
                                                                T15]): Unit = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.`unary_!_=`(value)(tag._4)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 5. */
  @alwaysinline def _5(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): T5 = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.unary_!(tag._5)
  }

  /** Store a value to a field number 5. */
  @alwaysinline def _5_=(value: T5)(implicit tag: Tag.CStruct15[T1,
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
                                                                T15]): Unit = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.`unary_!_=`(value)(tag._5)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 6. */
  @alwaysinline def _6(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): T6 = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.unary_!(tag._6)
  }

  /** Store a value to a field number 6. */
  @alwaysinline def _6_=(value: T6)(implicit tag: Tag.CStruct15[T1,
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
                                                                T15]): Unit = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.`unary_!_=`(value)(tag._6)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 7. */
  @alwaysinline def _7(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): T7 = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.unary_!(tag._7)
  }

  /** Store a value to a field number 7. */
  @alwaysinline def _7_=(value: T7)(implicit tag: Tag.CStruct15[T1,
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
                                                                T15]): Unit = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.`unary_!_=`(value)(tag._7)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 8. */
  @alwaysinline def _8(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): T8 = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.unary_!(tag._8)
  }

  /** Store a value to a field number 8. */
  @alwaysinline def _8_=(value: T8)(implicit tag: Tag.CStruct15[T1,
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
                                                                T15]): Unit = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.`unary_!_=`(value)(tag._8)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 9. */
  @alwaysinline def _9(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): T9 = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.unary_!(tag._9)
  }

  /** Store a value to a field number 9. */
  @alwaysinline def _9_=(value: T9)(implicit tag: Tag.CStruct15[T1,
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
                                                                T15]): Unit = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.`unary_!_=`(value)(tag._9)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 10. */
  @alwaysinline def _10(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): T10 = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.unary_!(tag._10)
  }

  /** Store a value to a field number 10. */
  @alwaysinline def _10_=(value: T10)(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): Unit = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.`unary_!_=`(value)(tag._10)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 11. */
  @alwaysinline def _11(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): T11 = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.unary_!(tag._11)
  }

  /** Store a value to a field number 11. */
  @alwaysinline def _11_=(value: T11)(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): Unit = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.`unary_!_=`(value)(tag._11)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 12. */
  @alwaysinline def _12(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): T12 = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.unary_!(tag._12)
  }

  /** Store a value to a field number 12. */
  @alwaysinline def _12_=(value: T12)(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): Unit = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.`unary_!_=`(value)(tag._12)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 13. */
  @alwaysinline def _13(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): T13 = {
    val ptr = new Ptr[T13](elemRawPtr(rawptr, tag.offset(12)))
    ptr.unary_!(tag._13)
  }

  /** Store a value to a field number 13. */
  @alwaysinline def _13_=(value: T13)(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): Unit = {
    val ptr = new Ptr[T13](elemRawPtr(rawptr, tag.offset(12)))
    ptr.`unary_!_=`(value)(tag._13)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 14. */
  @alwaysinline def _14(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): T14 = {
    val ptr = new Ptr[T14](elemRawPtr(rawptr, tag.offset(13)))
    ptr.unary_!(tag._14)
  }

  /** Store a value to a field number 14. */
  @alwaysinline def _14_=(value: T14)(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): Unit = {
    val ptr = new Ptr[T14](elemRawPtr(rawptr, tag.offset(13)))
    ptr.`unary_!_=`(value)(tag._14)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 15. */
  @alwaysinline def _15(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): T15 = {
    val ptr = new Ptr[T15](elemRawPtr(rawptr, tag.offset(14)))
    ptr.unary_!(tag._15)
  }

  /** Store a value to a field number 15. */
  @alwaysinline def _15_=(value: T15)(
      implicit tag: Tag.CStruct15[T1,
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
                                  T15]): Unit = {
    val ptr = new Ptr[T15](elemRawPtr(rawptr, tag.offset(14)))
    ptr.`unary_!_=`(value)(tag._15)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13,
T14, T15, T16] private[scalanative] (private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct16@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr: Ptr[
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
              T16]] =
    fromRawPtr[
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
                T16]](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(implicit tag: Tag.CStruct16[T1,
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
                                                                T16]): Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(implicit tag: Tag.CStruct16[T1,
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
                                                                T16]): Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 3. */
  @alwaysinline def _3(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): T3 = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.unary_!(tag._3)
  }

  /** Store a value to a field number 3. */
  @alwaysinline def _3_=(value: T3)(implicit tag: Tag.CStruct16[T1,
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
                                                                T16]): Unit = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.`unary_!_=`(value)(tag._3)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 4. */
  @alwaysinline def _4(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): T4 = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.unary_!(tag._4)
  }

  /** Store a value to a field number 4. */
  @alwaysinline def _4_=(value: T4)(implicit tag: Tag.CStruct16[T1,
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
                                                                T16]): Unit = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.`unary_!_=`(value)(tag._4)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 5. */
  @alwaysinline def _5(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): T5 = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.unary_!(tag._5)
  }

  /** Store a value to a field number 5. */
  @alwaysinline def _5_=(value: T5)(implicit tag: Tag.CStruct16[T1,
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
                                                                T16]): Unit = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.`unary_!_=`(value)(tag._5)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 6. */
  @alwaysinline def _6(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): T6 = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.unary_!(tag._6)
  }

  /** Store a value to a field number 6. */
  @alwaysinline def _6_=(value: T6)(implicit tag: Tag.CStruct16[T1,
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
                                                                T16]): Unit = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.`unary_!_=`(value)(tag._6)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 7. */
  @alwaysinline def _7(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): T7 = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.unary_!(tag._7)
  }

  /** Store a value to a field number 7. */
  @alwaysinline def _7_=(value: T7)(implicit tag: Tag.CStruct16[T1,
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
                                                                T16]): Unit = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.`unary_!_=`(value)(tag._7)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 8. */
  @alwaysinline def _8(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): T8 = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.unary_!(tag._8)
  }

  /** Store a value to a field number 8. */
  @alwaysinline def _8_=(value: T8)(implicit tag: Tag.CStruct16[T1,
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
                                                                T16]): Unit = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.`unary_!_=`(value)(tag._8)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 9. */
  @alwaysinline def _9(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): T9 = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.unary_!(tag._9)
  }

  /** Store a value to a field number 9. */
  @alwaysinline def _9_=(value: T9)(implicit tag: Tag.CStruct16[T1,
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
                                                                T16]): Unit = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.`unary_!_=`(value)(tag._9)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 10. */
  @alwaysinline def _10(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): T10 = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.unary_!(tag._10)
  }

  /** Store a value to a field number 10. */
  @alwaysinline def _10_=(value: T10)(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): Unit = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.`unary_!_=`(value)(tag._10)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 11. */
  @alwaysinline def _11(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): T11 = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.unary_!(tag._11)
  }

  /** Store a value to a field number 11. */
  @alwaysinline def _11_=(value: T11)(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): Unit = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.`unary_!_=`(value)(tag._11)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 12. */
  @alwaysinline def _12(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): T12 = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.unary_!(tag._12)
  }

  /** Store a value to a field number 12. */
  @alwaysinline def _12_=(value: T12)(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): Unit = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.`unary_!_=`(value)(tag._12)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 13. */
  @alwaysinline def _13(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): T13 = {
    val ptr = new Ptr[T13](elemRawPtr(rawptr, tag.offset(12)))
    ptr.unary_!(tag._13)
  }

  /** Store a value to a field number 13. */
  @alwaysinline def _13_=(value: T13)(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): Unit = {
    val ptr = new Ptr[T13](elemRawPtr(rawptr, tag.offset(12)))
    ptr.`unary_!_=`(value)(tag._13)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 14. */
  @alwaysinline def _14(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): T14 = {
    val ptr = new Ptr[T14](elemRawPtr(rawptr, tag.offset(13)))
    ptr.unary_!(tag._14)
  }

  /** Store a value to a field number 14. */
  @alwaysinline def _14_=(value: T14)(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): Unit = {
    val ptr = new Ptr[T14](elemRawPtr(rawptr, tag.offset(13)))
    ptr.`unary_!_=`(value)(tag._14)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 15. */
  @alwaysinline def _15(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): T15 = {
    val ptr = new Ptr[T15](elemRawPtr(rawptr, tag.offset(14)))
    ptr.unary_!(tag._15)
  }

  /** Store a value to a field number 15. */
  @alwaysinline def _15_=(value: T15)(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): Unit = {
    val ptr = new Ptr[T15](elemRawPtr(rawptr, tag.offset(14)))
    ptr.`unary_!_=`(value)(tag._15)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 16. */
  @alwaysinline def _16(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): T16 = {
    val ptr = new Ptr[T16](elemRawPtr(rawptr, tag.offset(15)))
    ptr.unary_!(tag._16)
  }

  /** Store a value to a field number 16. */
  @alwaysinline def _16_=(value: T16)(
      implicit tag: Tag.CStruct16[T1,
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
                                  T16]): Unit = {
    val ptr = new Ptr[T16](elemRawPtr(rawptr, tag.offset(15)))
    ptr.`unary_!_=`(value)(tag._16)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13,
T14, T15, T16, T17] private[scalanative] (
    private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct17[_,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct17@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr: Ptr[
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
              T17]] =
    fromRawPtr[
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
                T17]](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(implicit tag: Tag.CStruct17[T1,
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
                                                                T17]): Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(implicit tag: Tag.CStruct17[T1,
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
                                                                T17]): Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 3. */
  @alwaysinline def _3(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): T3 = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.unary_!(tag._3)
  }

  /** Store a value to a field number 3. */
  @alwaysinline def _3_=(value: T3)(implicit tag: Tag.CStruct17[T1,
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
                                                                T17]): Unit = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.`unary_!_=`(value)(tag._3)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 4. */
  @alwaysinline def _4(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): T4 = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.unary_!(tag._4)
  }

  /** Store a value to a field number 4. */
  @alwaysinline def _4_=(value: T4)(implicit tag: Tag.CStruct17[T1,
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
                                                                T17]): Unit = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.`unary_!_=`(value)(tag._4)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 5. */
  @alwaysinline def _5(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): T5 = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.unary_!(tag._5)
  }

  /** Store a value to a field number 5. */
  @alwaysinline def _5_=(value: T5)(implicit tag: Tag.CStruct17[T1,
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
                                                                T17]): Unit = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.`unary_!_=`(value)(tag._5)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 6. */
  @alwaysinline def _6(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): T6 = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.unary_!(tag._6)
  }

  /** Store a value to a field number 6. */
  @alwaysinline def _6_=(value: T6)(implicit tag: Tag.CStruct17[T1,
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
                                                                T17]): Unit = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.`unary_!_=`(value)(tag._6)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 7. */
  @alwaysinline def _7(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): T7 = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.unary_!(tag._7)
  }

  /** Store a value to a field number 7. */
  @alwaysinline def _7_=(value: T7)(implicit tag: Tag.CStruct17[T1,
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
                                                                T17]): Unit = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.`unary_!_=`(value)(tag._7)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 8. */
  @alwaysinline def _8(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): T8 = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.unary_!(tag._8)
  }

  /** Store a value to a field number 8. */
  @alwaysinline def _8_=(value: T8)(implicit tag: Tag.CStruct17[T1,
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
                                                                T17]): Unit = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.`unary_!_=`(value)(tag._8)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 9. */
  @alwaysinline def _9(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): T9 = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.unary_!(tag._9)
  }

  /** Store a value to a field number 9. */
  @alwaysinline def _9_=(value: T9)(implicit tag: Tag.CStruct17[T1,
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
                                                                T17]): Unit = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.`unary_!_=`(value)(tag._9)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 10. */
  @alwaysinline def _10(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): T10 = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.unary_!(tag._10)
  }

  /** Store a value to a field number 10. */
  @alwaysinline def _10_=(value: T10)(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): Unit = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.`unary_!_=`(value)(tag._10)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 11. */
  @alwaysinline def _11(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): T11 = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.unary_!(tag._11)
  }

  /** Store a value to a field number 11. */
  @alwaysinline def _11_=(value: T11)(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): Unit = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.`unary_!_=`(value)(tag._11)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 12. */
  @alwaysinline def _12(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): T12 = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.unary_!(tag._12)
  }

  /** Store a value to a field number 12. */
  @alwaysinline def _12_=(value: T12)(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): Unit = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.`unary_!_=`(value)(tag._12)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 13. */
  @alwaysinline def _13(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): T13 = {
    val ptr = new Ptr[T13](elemRawPtr(rawptr, tag.offset(12)))
    ptr.unary_!(tag._13)
  }

  /** Store a value to a field number 13. */
  @alwaysinline def _13_=(value: T13)(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): Unit = {
    val ptr = new Ptr[T13](elemRawPtr(rawptr, tag.offset(12)))
    ptr.`unary_!_=`(value)(tag._13)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 14. */
  @alwaysinline def _14(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): T14 = {
    val ptr = new Ptr[T14](elemRawPtr(rawptr, tag.offset(13)))
    ptr.unary_!(tag._14)
  }

  /** Store a value to a field number 14. */
  @alwaysinline def _14_=(value: T14)(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): Unit = {
    val ptr = new Ptr[T14](elemRawPtr(rawptr, tag.offset(13)))
    ptr.`unary_!_=`(value)(tag._14)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 15. */
  @alwaysinline def _15(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): T15 = {
    val ptr = new Ptr[T15](elemRawPtr(rawptr, tag.offset(14)))
    ptr.unary_!(tag._15)
  }

  /** Store a value to a field number 15. */
  @alwaysinline def _15_=(value: T15)(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): Unit = {
    val ptr = new Ptr[T15](elemRawPtr(rawptr, tag.offset(14)))
    ptr.`unary_!_=`(value)(tag._15)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 16. */
  @alwaysinline def _16(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): T16 = {
    val ptr = new Ptr[T16](elemRawPtr(rawptr, tag.offset(15)))
    ptr.unary_!(tag._16)
  }

  /** Store a value to a field number 16. */
  @alwaysinline def _16_=(value: T16)(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): Unit = {
    val ptr = new Ptr[T16](elemRawPtr(rawptr, tag.offset(15)))
    ptr.`unary_!_=`(value)(tag._16)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 17. */
  @alwaysinline def _17(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): T17 = {
    val ptr = new Ptr[T17](elemRawPtr(rawptr, tag.offset(16)))
    ptr.unary_!(tag._17)
  }

  /** Store a value to a field number 17. */
  @alwaysinline def _17_=(value: T17)(
      implicit tag: Tag.CStruct17[T1,
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
                                  T17]): Unit = {
    val ptr = new Ptr[T17](elemRawPtr(rawptr, tag.offset(16)))
    ptr.`unary_!_=`(value)(tag._17)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13,
T14, T15, T16, T17, T18] private[scalanative] (
    private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct18[_,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct18@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr: Ptr[
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
              T18]] =
    fromRawPtr[
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
                T18]](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(implicit tag: Tag.CStruct18[T1,
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
                                                                T18]): Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(implicit tag: Tag.CStruct18[T1,
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
                                                                T18]): Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 3. */
  @alwaysinline def _3(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): T3 = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.unary_!(tag._3)
  }

  /** Store a value to a field number 3. */
  @alwaysinline def _3_=(value: T3)(implicit tag: Tag.CStruct18[T1,
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
                                                                T18]): Unit = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.`unary_!_=`(value)(tag._3)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 4. */
  @alwaysinline def _4(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): T4 = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.unary_!(tag._4)
  }

  /** Store a value to a field number 4. */
  @alwaysinline def _4_=(value: T4)(implicit tag: Tag.CStruct18[T1,
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
                                                                T18]): Unit = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.`unary_!_=`(value)(tag._4)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 5. */
  @alwaysinline def _5(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): T5 = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.unary_!(tag._5)
  }

  /** Store a value to a field number 5. */
  @alwaysinline def _5_=(value: T5)(implicit tag: Tag.CStruct18[T1,
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
                                                                T18]): Unit = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.`unary_!_=`(value)(tag._5)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 6. */
  @alwaysinline def _6(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): T6 = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.unary_!(tag._6)
  }

  /** Store a value to a field number 6. */
  @alwaysinline def _6_=(value: T6)(implicit tag: Tag.CStruct18[T1,
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
                                                                T18]): Unit = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.`unary_!_=`(value)(tag._6)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 7. */
  @alwaysinline def _7(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): T7 = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.unary_!(tag._7)
  }

  /** Store a value to a field number 7. */
  @alwaysinline def _7_=(value: T7)(implicit tag: Tag.CStruct18[T1,
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
                                                                T18]): Unit = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.`unary_!_=`(value)(tag._7)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 8. */
  @alwaysinline def _8(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): T8 = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.unary_!(tag._8)
  }

  /** Store a value to a field number 8. */
  @alwaysinline def _8_=(value: T8)(implicit tag: Tag.CStruct18[T1,
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
                                                                T18]): Unit = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.`unary_!_=`(value)(tag._8)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 9. */
  @alwaysinline def _9(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): T9 = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.unary_!(tag._9)
  }

  /** Store a value to a field number 9. */
  @alwaysinline def _9_=(value: T9)(implicit tag: Tag.CStruct18[T1,
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
                                                                T18]): Unit = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.`unary_!_=`(value)(tag._9)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 10. */
  @alwaysinline def _10(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): T10 = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.unary_!(tag._10)
  }

  /** Store a value to a field number 10. */
  @alwaysinline def _10_=(value: T10)(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): Unit = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.`unary_!_=`(value)(tag._10)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 11. */
  @alwaysinline def _11(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): T11 = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.unary_!(tag._11)
  }

  /** Store a value to a field number 11. */
  @alwaysinline def _11_=(value: T11)(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): Unit = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.`unary_!_=`(value)(tag._11)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 12. */
  @alwaysinline def _12(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): T12 = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.unary_!(tag._12)
  }

  /** Store a value to a field number 12. */
  @alwaysinline def _12_=(value: T12)(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): Unit = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.`unary_!_=`(value)(tag._12)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 13. */
  @alwaysinline def _13(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): T13 = {
    val ptr = new Ptr[T13](elemRawPtr(rawptr, tag.offset(12)))
    ptr.unary_!(tag._13)
  }

  /** Store a value to a field number 13. */
  @alwaysinline def _13_=(value: T13)(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): Unit = {
    val ptr = new Ptr[T13](elemRawPtr(rawptr, tag.offset(12)))
    ptr.`unary_!_=`(value)(tag._13)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 14. */
  @alwaysinline def _14(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): T14 = {
    val ptr = new Ptr[T14](elemRawPtr(rawptr, tag.offset(13)))
    ptr.unary_!(tag._14)
  }

  /** Store a value to a field number 14. */
  @alwaysinline def _14_=(value: T14)(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): Unit = {
    val ptr = new Ptr[T14](elemRawPtr(rawptr, tag.offset(13)))
    ptr.`unary_!_=`(value)(tag._14)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 15. */
  @alwaysinline def _15(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): T15 = {
    val ptr = new Ptr[T15](elemRawPtr(rawptr, tag.offset(14)))
    ptr.unary_!(tag._15)
  }

  /** Store a value to a field number 15. */
  @alwaysinline def _15_=(value: T15)(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): Unit = {
    val ptr = new Ptr[T15](elemRawPtr(rawptr, tag.offset(14)))
    ptr.`unary_!_=`(value)(tag._15)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 16. */
  @alwaysinline def _16(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): T16 = {
    val ptr = new Ptr[T16](elemRawPtr(rawptr, tag.offset(15)))
    ptr.unary_!(tag._16)
  }

  /** Store a value to a field number 16. */
  @alwaysinline def _16_=(value: T16)(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): Unit = {
    val ptr = new Ptr[T16](elemRawPtr(rawptr, tag.offset(15)))
    ptr.`unary_!_=`(value)(tag._16)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 17. */
  @alwaysinline def _17(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): T17 = {
    val ptr = new Ptr[T17](elemRawPtr(rawptr, tag.offset(16)))
    ptr.unary_!(tag._17)
  }

  /** Store a value to a field number 17. */
  @alwaysinline def _17_=(value: T17)(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): Unit = {
    val ptr = new Ptr[T17](elemRawPtr(rawptr, tag.offset(16)))
    ptr.`unary_!_=`(value)(tag._17)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 18. */
  @alwaysinline def _18(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): T18 = {
    val ptr = new Ptr[T18](elemRawPtr(rawptr, tag.offset(17)))
    ptr.unary_!(tag._18)
  }

  /** Store a value to a field number 18. */
  @alwaysinline def _18_=(value: T18)(
      implicit tag: Tag.CStruct18[T1,
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
                                  T18]): Unit = {
    val ptr = new Ptr[T18](elemRawPtr(rawptr, tag.offset(17)))
    ptr.`unary_!_=`(value)(tag._18)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13,
T14, T15, T16, T17, T18, T19] private[scalanative] (
    private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct19[_,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct19@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr: Ptr[
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
              T19]] =
    fromRawPtr[
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
                T19]](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(implicit tag: Tag.CStruct19[T1,
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
                                                                T19]): Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(implicit tag: Tag.CStruct19[T1,
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
                                                                T19]): Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 3. */
  @alwaysinline def _3(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): T3 = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.unary_!(tag._3)
  }

  /** Store a value to a field number 3. */
  @alwaysinline def _3_=(value: T3)(implicit tag: Tag.CStruct19[T1,
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
                                                                T19]): Unit = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.`unary_!_=`(value)(tag._3)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 4. */
  @alwaysinline def _4(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): T4 = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.unary_!(tag._4)
  }

  /** Store a value to a field number 4. */
  @alwaysinline def _4_=(value: T4)(implicit tag: Tag.CStruct19[T1,
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
                                                                T19]): Unit = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.`unary_!_=`(value)(tag._4)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 5. */
  @alwaysinline def _5(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): T5 = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.unary_!(tag._5)
  }

  /** Store a value to a field number 5. */
  @alwaysinline def _5_=(value: T5)(implicit tag: Tag.CStruct19[T1,
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
                                                                T19]): Unit = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.`unary_!_=`(value)(tag._5)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 6. */
  @alwaysinline def _6(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): T6 = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.unary_!(tag._6)
  }

  /** Store a value to a field number 6. */
  @alwaysinline def _6_=(value: T6)(implicit tag: Tag.CStruct19[T1,
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
                                                                T19]): Unit = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.`unary_!_=`(value)(tag._6)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 7. */
  @alwaysinline def _7(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): T7 = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.unary_!(tag._7)
  }

  /** Store a value to a field number 7. */
  @alwaysinline def _7_=(value: T7)(implicit tag: Tag.CStruct19[T1,
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
                                                                T19]): Unit = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.`unary_!_=`(value)(tag._7)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 8. */
  @alwaysinline def _8(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): T8 = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.unary_!(tag._8)
  }

  /** Store a value to a field number 8. */
  @alwaysinline def _8_=(value: T8)(implicit tag: Tag.CStruct19[T1,
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
                                                                T19]): Unit = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.`unary_!_=`(value)(tag._8)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 9. */
  @alwaysinline def _9(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): T9 = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.unary_!(tag._9)
  }

  /** Store a value to a field number 9. */
  @alwaysinline def _9_=(value: T9)(implicit tag: Tag.CStruct19[T1,
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
                                                                T19]): Unit = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.`unary_!_=`(value)(tag._9)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 10. */
  @alwaysinline def _10(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): T10 = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.unary_!(tag._10)
  }

  /** Store a value to a field number 10. */
  @alwaysinline def _10_=(value: T10)(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): Unit = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.`unary_!_=`(value)(tag._10)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 11. */
  @alwaysinline def _11(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): T11 = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.unary_!(tag._11)
  }

  /** Store a value to a field number 11. */
  @alwaysinline def _11_=(value: T11)(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): Unit = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.`unary_!_=`(value)(tag._11)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 12. */
  @alwaysinline def _12(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): T12 = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.unary_!(tag._12)
  }

  /** Store a value to a field number 12. */
  @alwaysinline def _12_=(value: T12)(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): Unit = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.`unary_!_=`(value)(tag._12)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 13. */
  @alwaysinline def _13(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): T13 = {
    val ptr = new Ptr[T13](elemRawPtr(rawptr, tag.offset(12)))
    ptr.unary_!(tag._13)
  }

  /** Store a value to a field number 13. */
  @alwaysinline def _13_=(value: T13)(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): Unit = {
    val ptr = new Ptr[T13](elemRawPtr(rawptr, tag.offset(12)))
    ptr.`unary_!_=`(value)(tag._13)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 14. */
  @alwaysinline def _14(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): T14 = {
    val ptr = new Ptr[T14](elemRawPtr(rawptr, tag.offset(13)))
    ptr.unary_!(tag._14)
  }

  /** Store a value to a field number 14. */
  @alwaysinline def _14_=(value: T14)(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): Unit = {
    val ptr = new Ptr[T14](elemRawPtr(rawptr, tag.offset(13)))
    ptr.`unary_!_=`(value)(tag._14)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 15. */
  @alwaysinline def _15(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): T15 = {
    val ptr = new Ptr[T15](elemRawPtr(rawptr, tag.offset(14)))
    ptr.unary_!(tag._15)
  }

  /** Store a value to a field number 15. */
  @alwaysinline def _15_=(value: T15)(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): Unit = {
    val ptr = new Ptr[T15](elemRawPtr(rawptr, tag.offset(14)))
    ptr.`unary_!_=`(value)(tag._15)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 16. */
  @alwaysinline def _16(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): T16 = {
    val ptr = new Ptr[T16](elemRawPtr(rawptr, tag.offset(15)))
    ptr.unary_!(tag._16)
  }

  /** Store a value to a field number 16. */
  @alwaysinline def _16_=(value: T16)(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): Unit = {
    val ptr = new Ptr[T16](elemRawPtr(rawptr, tag.offset(15)))
    ptr.`unary_!_=`(value)(tag._16)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 17. */
  @alwaysinline def _17(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): T17 = {
    val ptr = new Ptr[T17](elemRawPtr(rawptr, tag.offset(16)))
    ptr.unary_!(tag._17)
  }

  /** Store a value to a field number 17. */
  @alwaysinline def _17_=(value: T17)(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): Unit = {
    val ptr = new Ptr[T17](elemRawPtr(rawptr, tag.offset(16)))
    ptr.`unary_!_=`(value)(tag._17)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 18. */
  @alwaysinline def _18(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): T18 = {
    val ptr = new Ptr[T18](elemRawPtr(rawptr, tag.offset(17)))
    ptr.unary_!(tag._18)
  }

  /** Store a value to a field number 18. */
  @alwaysinline def _18_=(value: T18)(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): Unit = {
    val ptr = new Ptr[T18](elemRawPtr(rawptr, tag.offset(17)))
    ptr.`unary_!_=`(value)(tag._18)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 19. */
  @alwaysinline def _19(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): T19 = {
    val ptr = new Ptr[T19](elemRawPtr(rawptr, tag.offset(18)))
    ptr.unary_!(tag._19)
  }

  /** Store a value to a field number 19. */
  @alwaysinline def _19_=(value: T19)(
      implicit tag: Tag.CStruct19[T1,
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
                                  T19]): Unit = {
    val ptr = new Ptr[T19](elemRawPtr(rawptr, tag.offset(18)))
    ptr.`unary_!_=`(value)(tag._19)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13,
T14, T15, T16, T17, T18, T19, T20] private[scalanative] (
    private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct20[_,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct20@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr: Ptr[
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
              T20]] =
    fromRawPtr[
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
                T20]](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(implicit tag: Tag.CStruct20[T1,
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
                                                                T20]): Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(implicit tag: Tag.CStruct20[T1,
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
                                                                T20]): Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 3. */
  @alwaysinline def _3(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): T3 = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.unary_!(tag._3)
  }

  /** Store a value to a field number 3. */
  @alwaysinline def _3_=(value: T3)(implicit tag: Tag.CStruct20[T1,
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
                                                                T20]): Unit = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.`unary_!_=`(value)(tag._3)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 4. */
  @alwaysinline def _4(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): T4 = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.unary_!(tag._4)
  }

  /** Store a value to a field number 4. */
  @alwaysinline def _4_=(value: T4)(implicit tag: Tag.CStruct20[T1,
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
                                                                T20]): Unit = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.`unary_!_=`(value)(tag._4)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 5. */
  @alwaysinline def _5(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): T5 = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.unary_!(tag._5)
  }

  /** Store a value to a field number 5. */
  @alwaysinline def _5_=(value: T5)(implicit tag: Tag.CStruct20[T1,
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
                                                                T20]): Unit = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.`unary_!_=`(value)(tag._5)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 6. */
  @alwaysinline def _6(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): T6 = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.unary_!(tag._6)
  }

  /** Store a value to a field number 6. */
  @alwaysinline def _6_=(value: T6)(implicit tag: Tag.CStruct20[T1,
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
                                                                T20]): Unit = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.`unary_!_=`(value)(tag._6)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 7. */
  @alwaysinline def _7(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): T7 = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.unary_!(tag._7)
  }

  /** Store a value to a field number 7. */
  @alwaysinline def _7_=(value: T7)(implicit tag: Tag.CStruct20[T1,
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
                                                                T20]): Unit = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.`unary_!_=`(value)(tag._7)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 8. */
  @alwaysinline def _8(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): T8 = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.unary_!(tag._8)
  }

  /** Store a value to a field number 8. */
  @alwaysinline def _8_=(value: T8)(implicit tag: Tag.CStruct20[T1,
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
                                                                T20]): Unit = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.`unary_!_=`(value)(tag._8)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 9. */
  @alwaysinline def _9(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): T9 = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.unary_!(tag._9)
  }

  /** Store a value to a field number 9. */
  @alwaysinline def _9_=(value: T9)(implicit tag: Tag.CStruct20[T1,
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
                                                                T20]): Unit = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.`unary_!_=`(value)(tag._9)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 10. */
  @alwaysinline def _10(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): T10 = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.unary_!(tag._10)
  }

  /** Store a value to a field number 10. */
  @alwaysinline def _10_=(value: T10)(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): Unit = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.`unary_!_=`(value)(tag._10)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 11. */
  @alwaysinline def _11(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): T11 = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.unary_!(tag._11)
  }

  /** Store a value to a field number 11. */
  @alwaysinline def _11_=(value: T11)(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): Unit = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.`unary_!_=`(value)(tag._11)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 12. */
  @alwaysinline def _12(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): T12 = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.unary_!(tag._12)
  }

  /** Store a value to a field number 12. */
  @alwaysinline def _12_=(value: T12)(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): Unit = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.`unary_!_=`(value)(tag._12)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 13. */
  @alwaysinline def _13(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): T13 = {
    val ptr = new Ptr[T13](elemRawPtr(rawptr, tag.offset(12)))
    ptr.unary_!(tag._13)
  }

  /** Store a value to a field number 13. */
  @alwaysinline def _13_=(value: T13)(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): Unit = {
    val ptr = new Ptr[T13](elemRawPtr(rawptr, tag.offset(12)))
    ptr.`unary_!_=`(value)(tag._13)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 14. */
  @alwaysinline def _14(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): T14 = {
    val ptr = new Ptr[T14](elemRawPtr(rawptr, tag.offset(13)))
    ptr.unary_!(tag._14)
  }

  /** Store a value to a field number 14. */
  @alwaysinline def _14_=(value: T14)(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): Unit = {
    val ptr = new Ptr[T14](elemRawPtr(rawptr, tag.offset(13)))
    ptr.`unary_!_=`(value)(tag._14)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 15. */
  @alwaysinline def _15(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): T15 = {
    val ptr = new Ptr[T15](elemRawPtr(rawptr, tag.offset(14)))
    ptr.unary_!(tag._15)
  }

  /** Store a value to a field number 15. */
  @alwaysinline def _15_=(value: T15)(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): Unit = {
    val ptr = new Ptr[T15](elemRawPtr(rawptr, tag.offset(14)))
    ptr.`unary_!_=`(value)(tag._15)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 16. */
  @alwaysinline def _16(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): T16 = {
    val ptr = new Ptr[T16](elemRawPtr(rawptr, tag.offset(15)))
    ptr.unary_!(tag._16)
  }

  /** Store a value to a field number 16. */
  @alwaysinline def _16_=(value: T16)(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): Unit = {
    val ptr = new Ptr[T16](elemRawPtr(rawptr, tag.offset(15)))
    ptr.`unary_!_=`(value)(tag._16)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 17. */
  @alwaysinline def _17(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): T17 = {
    val ptr = new Ptr[T17](elemRawPtr(rawptr, tag.offset(16)))
    ptr.unary_!(tag._17)
  }

  /** Store a value to a field number 17. */
  @alwaysinline def _17_=(value: T17)(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): Unit = {
    val ptr = new Ptr[T17](elemRawPtr(rawptr, tag.offset(16)))
    ptr.`unary_!_=`(value)(tag._17)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 18. */
  @alwaysinline def _18(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): T18 = {
    val ptr = new Ptr[T18](elemRawPtr(rawptr, tag.offset(17)))
    ptr.unary_!(tag._18)
  }

  /** Store a value to a field number 18. */
  @alwaysinline def _18_=(value: T18)(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): Unit = {
    val ptr = new Ptr[T18](elemRawPtr(rawptr, tag.offset(17)))
    ptr.`unary_!_=`(value)(tag._18)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 19. */
  @alwaysinline def _19(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): T19 = {
    val ptr = new Ptr[T19](elemRawPtr(rawptr, tag.offset(18)))
    ptr.unary_!(tag._19)
  }

  /** Store a value to a field number 19. */
  @alwaysinline def _19_=(value: T19)(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): Unit = {
    val ptr = new Ptr[T19](elemRawPtr(rawptr, tag.offset(18)))
    ptr.`unary_!_=`(value)(tag._19)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 20. */
  @alwaysinline def _20(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): T20 = {
    val ptr = new Ptr[T20](elemRawPtr(rawptr, tag.offset(19)))
    ptr.unary_!(tag._20)
  }

  /** Store a value to a field number 20. */
  @alwaysinline def _20_=(value: T20)(
      implicit tag: Tag.CStruct20[T1,
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
                                  T20]): Unit = {
    val ptr = new Ptr[T20](elemRawPtr(rawptr, tag.offset(19)))
    ptr.`unary_!_=`(value)(tag._20)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13,
T14, T15, T16, T17, T18, T19, T20, T21] private[scalanative] (
    private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct21[_,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct21@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr: Ptr[
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
              T21]] =
    fromRawPtr[
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
                T21]](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(implicit tag: Tag.CStruct21[T1,
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
                                                                T21]): Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(implicit tag: Tag.CStruct21[T1,
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
                                                                T21]): Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 3. */
  @alwaysinline def _3(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T3 = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.unary_!(tag._3)
  }

  /** Store a value to a field number 3. */
  @alwaysinline def _3_=(value: T3)(implicit tag: Tag.CStruct21[T1,
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
                                                                T21]): Unit = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.`unary_!_=`(value)(tag._3)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 4. */
  @alwaysinline def _4(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T4 = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.unary_!(tag._4)
  }

  /** Store a value to a field number 4. */
  @alwaysinline def _4_=(value: T4)(implicit tag: Tag.CStruct21[T1,
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
                                                                T21]): Unit = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.`unary_!_=`(value)(tag._4)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 5. */
  @alwaysinline def _5(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T5 = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.unary_!(tag._5)
  }

  /** Store a value to a field number 5. */
  @alwaysinline def _5_=(value: T5)(implicit tag: Tag.CStruct21[T1,
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
                                                                T21]): Unit = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.`unary_!_=`(value)(tag._5)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 6. */
  @alwaysinline def _6(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T6 = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.unary_!(tag._6)
  }

  /** Store a value to a field number 6. */
  @alwaysinline def _6_=(value: T6)(implicit tag: Tag.CStruct21[T1,
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
                                                                T21]): Unit = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.`unary_!_=`(value)(tag._6)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 7. */
  @alwaysinline def _7(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T7 = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.unary_!(tag._7)
  }

  /** Store a value to a field number 7. */
  @alwaysinline def _7_=(value: T7)(implicit tag: Tag.CStruct21[T1,
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
                                                                T21]): Unit = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.`unary_!_=`(value)(tag._7)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 8. */
  @alwaysinline def _8(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T8 = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.unary_!(tag._8)
  }

  /** Store a value to a field number 8. */
  @alwaysinline def _8_=(value: T8)(implicit tag: Tag.CStruct21[T1,
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
                                                                T21]): Unit = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.`unary_!_=`(value)(tag._8)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 9. */
  @alwaysinline def _9(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T9 = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.unary_!(tag._9)
  }

  /** Store a value to a field number 9. */
  @alwaysinline def _9_=(value: T9)(implicit tag: Tag.CStruct21[T1,
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
                                                                T21]): Unit = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.`unary_!_=`(value)(tag._9)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 10. */
  @alwaysinline def _10(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T10 = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.unary_!(tag._10)
  }

  /** Store a value to a field number 10. */
  @alwaysinline def _10_=(value: T10)(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): Unit = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.`unary_!_=`(value)(tag._10)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 11. */
  @alwaysinline def _11(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T11 = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.unary_!(tag._11)
  }

  /** Store a value to a field number 11. */
  @alwaysinline def _11_=(value: T11)(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): Unit = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.`unary_!_=`(value)(tag._11)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 12. */
  @alwaysinline def _12(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T12 = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.unary_!(tag._12)
  }

  /** Store a value to a field number 12. */
  @alwaysinline def _12_=(value: T12)(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): Unit = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.`unary_!_=`(value)(tag._12)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 13. */
  @alwaysinline def _13(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T13 = {
    val ptr = new Ptr[T13](elemRawPtr(rawptr, tag.offset(12)))
    ptr.unary_!(tag._13)
  }

  /** Store a value to a field number 13. */
  @alwaysinline def _13_=(value: T13)(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): Unit = {
    val ptr = new Ptr[T13](elemRawPtr(rawptr, tag.offset(12)))
    ptr.`unary_!_=`(value)(tag._13)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 14. */
  @alwaysinline def _14(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T14 = {
    val ptr = new Ptr[T14](elemRawPtr(rawptr, tag.offset(13)))
    ptr.unary_!(tag._14)
  }

  /** Store a value to a field number 14. */
  @alwaysinline def _14_=(value: T14)(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): Unit = {
    val ptr = new Ptr[T14](elemRawPtr(rawptr, tag.offset(13)))
    ptr.`unary_!_=`(value)(tag._14)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 15. */
  @alwaysinline def _15(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T15 = {
    val ptr = new Ptr[T15](elemRawPtr(rawptr, tag.offset(14)))
    ptr.unary_!(tag._15)
  }

  /** Store a value to a field number 15. */
  @alwaysinline def _15_=(value: T15)(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): Unit = {
    val ptr = new Ptr[T15](elemRawPtr(rawptr, tag.offset(14)))
    ptr.`unary_!_=`(value)(tag._15)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 16. */
  @alwaysinline def _16(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T16 = {
    val ptr = new Ptr[T16](elemRawPtr(rawptr, tag.offset(15)))
    ptr.unary_!(tag._16)
  }

  /** Store a value to a field number 16. */
  @alwaysinline def _16_=(value: T16)(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): Unit = {
    val ptr = new Ptr[T16](elemRawPtr(rawptr, tag.offset(15)))
    ptr.`unary_!_=`(value)(tag._16)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 17. */
  @alwaysinline def _17(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T17 = {
    val ptr = new Ptr[T17](elemRawPtr(rawptr, tag.offset(16)))
    ptr.unary_!(tag._17)
  }

  /** Store a value to a field number 17. */
  @alwaysinline def _17_=(value: T17)(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): Unit = {
    val ptr = new Ptr[T17](elemRawPtr(rawptr, tag.offset(16)))
    ptr.`unary_!_=`(value)(tag._17)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 18. */
  @alwaysinline def _18(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T18 = {
    val ptr = new Ptr[T18](elemRawPtr(rawptr, tag.offset(17)))
    ptr.unary_!(tag._18)
  }

  /** Store a value to a field number 18. */
  @alwaysinline def _18_=(value: T18)(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): Unit = {
    val ptr = new Ptr[T18](elemRawPtr(rawptr, tag.offset(17)))
    ptr.`unary_!_=`(value)(tag._18)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 19. */
  @alwaysinline def _19(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T19 = {
    val ptr = new Ptr[T19](elemRawPtr(rawptr, tag.offset(18)))
    ptr.unary_!(tag._19)
  }

  /** Store a value to a field number 19. */
  @alwaysinline def _19_=(value: T19)(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): Unit = {
    val ptr = new Ptr[T19](elemRawPtr(rawptr, tag.offset(18)))
    ptr.`unary_!_=`(value)(tag._19)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 20. */
  @alwaysinline def _20(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T20 = {
    val ptr = new Ptr[T20](elemRawPtr(rawptr, tag.offset(19)))
    ptr.unary_!(tag._20)
  }

  /** Store a value to a field number 20. */
  @alwaysinline def _20_=(value: T20)(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): Unit = {
    val ptr = new Ptr[T20](elemRawPtr(rawptr, tag.offset(19)))
    ptr.`unary_!_=`(value)(tag._20)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 21. */
  @alwaysinline def _21(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): T21 = {
    val ptr = new Ptr[T21](elemRawPtr(rawptr, tag.offset(20)))
    ptr.unary_!(tag._21)
  }

  /** Store a value to a field number 21. */
  @alwaysinline def _21_=(value: T21)(
      implicit tag: Tag.CStruct21[T1,
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
                                  T21]): Unit = {
    val ptr = new Ptr[T21](elemRawPtr(rawptr, tag.offset(20)))
    ptr.`unary_!_=`(value)(tag._21)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 12)

final class CStruct22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13,
T14, T15, T16, T17, T18, T19, T20, T21, T22] private[scalanative] (
    private[scalanative] val rawptr: RawPtr)
    extends CStruct {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CStruct22[_,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _,
                            _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CStruct22@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toPtr: Ptr[
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
              T22]] =
    fromRawPtr[
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
                T22]](rawptr)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 1. */
  @alwaysinline def _1(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T1 = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.unary_!(tag._1)
  }

  /** Store a value to a field number 1. */
  @alwaysinline def _1_=(value: T1)(implicit tag: Tag.CStruct22[T1,
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
                                                                T22]): Unit = {
    val ptr = new Ptr[T1](elemRawPtr(rawptr, tag.offset(0)))
    ptr.`unary_!_=`(value)(tag._1)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 2. */
  @alwaysinline def _2(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T2 = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.unary_!(tag._2)
  }

  /** Store a value to a field number 2. */
  @alwaysinline def _2_=(value: T2)(implicit tag: Tag.CStruct22[T1,
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
                                                                T22]): Unit = {
    val ptr = new Ptr[T2](elemRawPtr(rawptr, tag.offset(1)))
    ptr.`unary_!_=`(value)(tag._2)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 3. */
  @alwaysinline def _3(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T3 = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.unary_!(tag._3)
  }

  /** Store a value to a field number 3. */
  @alwaysinline def _3_=(value: T3)(implicit tag: Tag.CStruct22[T1,
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
                                                                T22]): Unit = {
    val ptr = new Ptr[T3](elemRawPtr(rawptr, tag.offset(2)))
    ptr.`unary_!_=`(value)(tag._3)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 4. */
  @alwaysinline def _4(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T4 = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.unary_!(tag._4)
  }

  /** Store a value to a field number 4. */
  @alwaysinline def _4_=(value: T4)(implicit tag: Tag.CStruct22[T1,
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
                                                                T22]): Unit = {
    val ptr = new Ptr[T4](elemRawPtr(rawptr, tag.offset(3)))
    ptr.`unary_!_=`(value)(tag._4)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 5. */
  @alwaysinline def _5(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T5 = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.unary_!(tag._5)
  }

  /** Store a value to a field number 5. */
  @alwaysinline def _5_=(value: T5)(implicit tag: Tag.CStruct22[T1,
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
                                                                T22]): Unit = {
    val ptr = new Ptr[T5](elemRawPtr(rawptr, tag.offset(4)))
    ptr.`unary_!_=`(value)(tag._5)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 6. */
  @alwaysinline def _6(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T6 = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.unary_!(tag._6)
  }

  /** Store a value to a field number 6. */
  @alwaysinline def _6_=(value: T6)(implicit tag: Tag.CStruct22[T1,
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
                                                                T22]): Unit = {
    val ptr = new Ptr[T6](elemRawPtr(rawptr, tag.offset(5)))
    ptr.`unary_!_=`(value)(tag._6)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 7. */
  @alwaysinline def _7(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T7 = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.unary_!(tag._7)
  }

  /** Store a value to a field number 7. */
  @alwaysinline def _7_=(value: T7)(implicit tag: Tag.CStruct22[T1,
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
                                                                T22]): Unit = {
    val ptr = new Ptr[T7](elemRawPtr(rawptr, tag.offset(6)))
    ptr.`unary_!_=`(value)(tag._7)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 8. */
  @alwaysinline def _8(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T8 = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.unary_!(tag._8)
  }

  /** Store a value to a field number 8. */
  @alwaysinline def _8_=(value: T8)(implicit tag: Tag.CStruct22[T1,
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
                                                                T22]): Unit = {
    val ptr = new Ptr[T8](elemRawPtr(rawptr, tag.offset(7)))
    ptr.`unary_!_=`(value)(tag._8)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 9. */
  @alwaysinline def _9(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T9 = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.unary_!(tag._9)
  }

  /** Store a value to a field number 9. */
  @alwaysinline def _9_=(value: T9)(implicit tag: Tag.CStruct22[T1,
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
                                                                T22]): Unit = {
    val ptr = new Ptr[T9](elemRawPtr(rawptr, tag.offset(8)))
    ptr.`unary_!_=`(value)(tag._9)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 10. */
  @alwaysinline def _10(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T10 = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.unary_!(tag._10)
  }

  /** Store a value to a field number 10. */
  @alwaysinline def _10_=(value: T10)(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): Unit = {
    val ptr = new Ptr[T10](elemRawPtr(rawptr, tag.offset(9)))
    ptr.`unary_!_=`(value)(tag._10)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 11. */
  @alwaysinline def _11(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T11 = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.unary_!(tag._11)
  }

  /** Store a value to a field number 11. */
  @alwaysinline def _11_=(value: T11)(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): Unit = {
    val ptr = new Ptr[T11](elemRawPtr(rawptr, tag.offset(10)))
    ptr.`unary_!_=`(value)(tag._11)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 12. */
  @alwaysinline def _12(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T12 = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.unary_!(tag._12)
  }

  /** Store a value to a field number 12. */
  @alwaysinline def _12_=(value: T12)(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): Unit = {
    val ptr = new Ptr[T12](elemRawPtr(rawptr, tag.offset(11)))
    ptr.`unary_!_=`(value)(tag._12)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 13. */
  @alwaysinline def _13(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T13 = {
    val ptr = new Ptr[T13](elemRawPtr(rawptr, tag.offset(12)))
    ptr.unary_!(tag._13)
  }

  /** Store a value to a field number 13. */
  @alwaysinline def _13_=(value: T13)(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): Unit = {
    val ptr = new Ptr[T13](elemRawPtr(rawptr, tag.offset(12)))
    ptr.`unary_!_=`(value)(tag._13)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 14. */
  @alwaysinline def _14(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T14 = {
    val ptr = new Ptr[T14](elemRawPtr(rawptr, tag.offset(13)))
    ptr.unary_!(tag._14)
  }

  /** Store a value to a field number 14. */
  @alwaysinline def _14_=(value: T14)(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): Unit = {
    val ptr = new Ptr[T14](elemRawPtr(rawptr, tag.offset(13)))
    ptr.`unary_!_=`(value)(tag._14)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 15. */
  @alwaysinline def _15(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T15 = {
    val ptr = new Ptr[T15](elemRawPtr(rawptr, tag.offset(14)))
    ptr.unary_!(tag._15)
  }

  /** Store a value to a field number 15. */
  @alwaysinline def _15_=(value: T15)(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): Unit = {
    val ptr = new Ptr[T15](elemRawPtr(rawptr, tag.offset(14)))
    ptr.`unary_!_=`(value)(tag._15)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 16. */
  @alwaysinline def _16(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T16 = {
    val ptr = new Ptr[T16](elemRawPtr(rawptr, tag.offset(15)))
    ptr.unary_!(tag._16)
  }

  /** Store a value to a field number 16. */
  @alwaysinline def _16_=(value: T16)(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): Unit = {
    val ptr = new Ptr[T16](elemRawPtr(rawptr, tag.offset(15)))
    ptr.`unary_!_=`(value)(tag._16)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 17. */
  @alwaysinline def _17(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T17 = {
    val ptr = new Ptr[T17](elemRawPtr(rawptr, tag.offset(16)))
    ptr.unary_!(tag._17)
  }

  /** Store a value to a field number 17. */
  @alwaysinline def _17_=(value: T17)(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): Unit = {
    val ptr = new Ptr[T17](elemRawPtr(rawptr, tag.offset(16)))
    ptr.`unary_!_=`(value)(tag._17)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 18. */
  @alwaysinline def _18(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T18 = {
    val ptr = new Ptr[T18](elemRawPtr(rawptr, tag.offset(17)))
    ptr.unary_!(tag._18)
  }

  /** Store a value to a field number 18. */
  @alwaysinline def _18_=(value: T18)(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): Unit = {
    val ptr = new Ptr[T18](elemRawPtr(rawptr, tag.offset(17)))
    ptr.`unary_!_=`(value)(tag._18)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 19. */
  @alwaysinline def _19(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T19 = {
    val ptr = new Ptr[T19](elemRawPtr(rawptr, tag.offset(18)))
    ptr.unary_!(tag._19)
  }

  /** Store a value to a field number 19. */
  @alwaysinline def _19_=(value: T19)(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): Unit = {
    val ptr = new Ptr[T19](elemRawPtr(rawptr, tag.offset(18)))
    ptr.`unary_!_=`(value)(tag._19)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 20. */
  @alwaysinline def _20(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T20 = {
    val ptr = new Ptr[T20](elemRawPtr(rawptr, tag.offset(19)))
    ptr.unary_!(tag._20)
  }

  /** Store a value to a field number 20. */
  @alwaysinline def _20_=(value: T20)(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): Unit = {
    val ptr = new Ptr[T20](elemRawPtr(rawptr, tag.offset(19)))
    ptr.`unary_!_=`(value)(tag._20)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 21. */
  @alwaysinline def _21(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T21 = {
    val ptr = new Ptr[T21](elemRawPtr(rawptr, tag.offset(20)))
    ptr.unary_!(tag._21)
  }

  /** Store a value to a field number 21. */
  @alwaysinline def _21_=(value: T21)(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): Unit = {
    val ptr = new Ptr[T21](elemRawPtr(rawptr, tag.offset(20)))
    ptr.`unary_!_=`(value)(tag._21)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 32)

  /** Load a value of a field number 22. */
  @alwaysinline def _22(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): T22 = {
    val ptr = new Ptr[T22](elemRawPtr(rawptr, tag.offset(21)))
    ptr.unary_!(tag._22)
  }

  /** Store a value to a field number 22. */
  @alwaysinline def _22_=(value: T22)(
      implicit tag: Tag.CStruct22[T1,
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
                                  T22]): Unit = {
    val ptr = new Ptr[T22](elemRawPtr(rawptr, tag.offset(21)))
    ptr.`unary_!_=`(value)(tag._22)
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/CStruct.scala.gyb", line: 46)
}
