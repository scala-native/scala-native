// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 1)
package scala.scalanative
package native

import scala.reflect.ClassTag
import scalanative.runtime.{intrinsic, throwUndefined}

sealed abstract class Tag[P] {
  def size: Int
  def alignment: Int
  def offset(idx: Int): Int = throwUndefined()
}

object Tag {
  final case class Ptr[T](of: Tag[T]) extends Tag[native.Ptr[T]] {
    @inline final def size: Int      = 8
    @inline final def alignment: Int = 8
  }

  final case class Class[T <: AnyRef](of: java.lang.Class[T]) extends Tag[T] {
    @inline final def size: Int      = 8
    @inline final def alignment: Int = 8
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 40)

  object Unit extends Tag[scala.Unit] {
    @inline final def size: Int      = 8
    @inline final def alignment: Int = 8
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 40)

  object Boolean extends Tag[scala.Boolean] {
    @inline final def size: Int      = 1
    @inline final def alignment: Int = 1
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 40)

  object Char extends Tag[scala.Char] {
    @inline final def size: Int      = 2
    @inline final def alignment: Int = 2
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 40)

  object Byte extends Tag[scala.Byte] {
    @inline final def size: Int      = 1
    @inline final def alignment: Int = 1
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 40)

  object UByte extends Tag[native.UByte] {
    @inline final def size: Int      = 1
    @inline final def alignment: Int = 1
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 40)

  object Short extends Tag[scala.Short] {
    @inline final def size: Int      = 2
    @inline final def alignment: Int = 2
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 40)

  object UShort extends Tag[native.UShort] {
    @inline final def size: Int      = 2
    @inline final def alignment: Int = 2
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 40)

  object Int extends Tag[scala.Int] {
    @inline final def size: Int      = 4
    @inline final def alignment: Int = 4
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 40)

  object UInt extends Tag[native.UInt] {
    @inline final def size: Int      = 4
    @inline final def alignment: Int = 4
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 40)

  object Long extends Tag[scala.Long] {
    @inline final def size: Int      = 8
    @inline final def alignment: Int = 8
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 40)

  object ULong extends Tag[native.ULong] {
    @inline final def size: Int      = 8
    @inline final def alignment: Int = 8
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 40)

  object Float extends Tag[scala.Float] {
    @inline final def size: Int      = 4
    @inline final def alignment: Int = 4
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 40)

  object Double extends Tag[scala.Double] {
    @inline final def size: Int      = 8
    @inline final def alignment: Int = 8
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 47)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 49)

  object Nat0 extends Tag[native.Nat._0] {
    @noinline final def size: Int      = throwUndefined()
    @noinline final def alignment: Int = throwUndefined()
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 49)

  object Nat1 extends Tag[native.Nat._1] {
    @noinline final def size: Int      = throwUndefined()
    @noinline final def alignment: Int = throwUndefined()
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 49)

  object Nat2 extends Tag[native.Nat._2] {
    @noinline final def size: Int      = throwUndefined()
    @noinline final def alignment: Int = throwUndefined()
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 49)

  object Nat3 extends Tag[native.Nat._3] {
    @noinline final def size: Int      = throwUndefined()
    @noinline final def alignment: Int = throwUndefined()
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 49)

  object Nat4 extends Tag[native.Nat._4] {
    @noinline final def size: Int      = throwUndefined()
    @noinline final def alignment: Int = throwUndefined()
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 49)

  object Nat5 extends Tag[native.Nat._5] {
    @noinline final def size: Int      = throwUndefined()
    @noinline final def alignment: Int = throwUndefined()
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 49)

  object Nat6 extends Tag[native.Nat._6] {
    @noinline final def size: Int      = throwUndefined()
    @noinline final def alignment: Int = throwUndefined()
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 49)

  object Nat7 extends Tag[native.Nat._7] {
    @noinline final def size: Int      = throwUndefined()
    @noinline final def alignment: Int = throwUndefined()
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 49)

  object Nat8 extends Tag[native.Nat._8] {
    @noinline final def size: Int      = throwUndefined()
    @noinline final def alignment: Int = throwUndefined()
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 49)

  object Nat9 extends Tag[native.Nat._9] {
    @noinline final def size: Int      = throwUndefined()
    @noinline final def alignment: Int = throwUndefined()
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 56)

  final case class Digit[N <: native.Nat.Base, M <: native.Nat](n: Tag[N],
                                                                m: Tag[M])
      extends Tag[native.Nat.Digit[N, M]] {
    @inline final def size: Int      = throwUndefined()
    @inline final def alignment: Int = throwUndefined()
  }

  final case class CArray[T, N <: native.Nat](of: Tag[T], n: Tag[N])
      extends Tag[native.CArray[T, N]] {
    final def size: Int = {
      var mul = 1
      def natToInt(tag: Tag[_]): Int = tag match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 69)
        case Tag.Nat0 => 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 69)
        case Tag.Nat1 => 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 69)
        case Tag.Nat2 => 2
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 69)
        case Tag.Nat3 => 3
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 69)
        case Tag.Nat4 => 4
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 69)
        case Tag.Nat5 => 5
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 69)
        case Tag.Nat6 => 6
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 69)
        case Tag.Nat7 => 7
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 69)
        case Tag.Nat8 => 8
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 69)
        case Tag.Nat9 => 9
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 71)
        case Tag.Digit(n, m) =>
          val mint = natToInt(m)
          mul *= 10
          natToInt(n) * mul + mint
        case _ =>
          throwUndefined()
      }
      of.size * natToInt(n)
    }
    @inline final def alignment: Int           = of.alignment
    @inline override def offset(idx: Int): Int = of.size * idx
  }

  private[scalanative] sealed trait CStruct

  @inline private[scalanative] def align(offset: Int, alignment: Int) = {
    val alignmentMask = alignment - 1
    val padding =
      if ((offset & alignmentMask) == 0) 0
      else alignment - (offset & alignmentMask)
    offset + padding
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct0() extends Tag[native.CStruct0] with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct1[T1](_1: Tag[T1])
      extends Tag[native.CStruct1[T1]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct2[T1, T2](_1: Tag[T1], _2: Tag[T2])
      extends Tag[native.CStruct2[T1, T2]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct3[T1, T2, T3](_1: Tag[T1], _2: Tag[T2], _3: Tag[T3])
      extends Tag[native.CStruct3[T1, T2, T3]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _3.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 2 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _3.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct4[T1, T2, T3, T4](_1: Tag[T1],
                                            _2: Tag[T2],
                                            _3: Tag[T3],
                                            _4: Tag[T4])
      extends Tag[native.CStruct4[T1, T2, T3, T4]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _3.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _4.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 2 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _3.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 3 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _4.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct5[T1, T2, T3, T4, T5](_1: Tag[T1],
                                                _2: Tag[T2],
                                                _3: Tag[T3],
                                                _4: Tag[T4],
                                                _5: Tag[T5])
      extends Tag[native.CStruct5[T1, T2, T3, T4, T5]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _3.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _4.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _5.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 2 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _3.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 3 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _4.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 4 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _5.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct6[T1, T2, T3, T4, T5, T6](_1: Tag[T1],
                                                    _2: Tag[T2],
                                                    _3: Tag[T3],
                                                    _4: Tag[T4],
                                                    _5: Tag[T5],
                                                    _6: Tag[T6])
      extends Tag[native.CStruct6[T1, T2, T3, T4, T5, T6]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _3.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _4.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _5.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _6.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 2 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _3.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 3 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _4.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 4 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _5.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 5 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _6.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct7[T1, T2, T3, T4, T5, T6, T7](_1: Tag[T1],
                                                        _2: Tag[T2],
                                                        _3: Tag[T3],
                                                        _4: Tag[T4],
                                                        _5: Tag[T5],
                                                        _6: Tag[T6],
                                                        _7: Tag[T7])
      extends Tag[native.CStruct7[T1, T2, T3, T4, T5, T6, T7]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _3.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _4.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _5.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _6.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _7.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 2 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _3.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 3 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _4.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 4 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _5.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 5 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _6.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 6 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _7.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct8[T1, T2, T3, T4, T5, T6, T7, T8](_1: Tag[T1],
                                                            _2: Tag[T2],
                                                            _3: Tag[T3],
                                                            _4: Tag[T4],
                                                            _5: Tag[T5],
                                                            _6: Tag[T6],
                                                            _7: Tag[T7],
                                                            _8: Tag[T8])
      extends Tag[native.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _3.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _4.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _5.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _6.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _7.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _8.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 2 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _3.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 3 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _4.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 4 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _5.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 5 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _6.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 6 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _7.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 7 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _8.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9](_1: Tag[T1],
                                                                _2: Tag[T2],
                                                                _3: Tag[T3],
                                                                _4: Tag[T4],
                                                                _5: Tag[T5],
                                                                _6: Tag[T6],
                                                                _7: Tag[T7],
                                                                _8: Tag[T8],
                                                                _9: Tag[T9])
      extends Tag[native.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _3.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _4.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _5.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _6.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _7.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _8.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _9.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 2 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _3.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 3 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _4.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 4 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _5.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 5 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _6.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 6 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _7.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 7 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _8.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 8 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _9.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](
      _1: Tag[T1],
      _2: Tag[T2],
      _3: Tag[T3],
      _4: Tag[T4],
      _5: Tag[T5],
      _6: Tag[T6],
      _7: Tag[T7],
      _8: Tag[T8],
      _9: Tag[T9],
      _10: Tag[T10])
      extends Tag[native.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _3.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _4.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _5.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _6.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _7.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _8.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _9.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _10.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 2 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _3.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 3 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _4.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 4 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _5.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 5 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _6.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 6 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _7.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 7 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _8.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 8 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _9.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 9 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _10.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](
      _1: Tag[T1],
      _2: Tag[T2],
      _3: Tag[T3],
      _4: Tag[T4],
      _5: Tag[T5],
      _6: Tag[T6],
      _7: Tag[T7],
      _8: Tag[T8],
      _9: Tag[T9],
      _10: Tag[T10],
      _11: Tag[T11])
      extends Tag[
        native.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _3.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _4.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _5.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _6.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _7.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _8.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _9.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _10.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _11.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 2 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _3.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 3 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _4.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 4 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _5.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 5 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _6.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 6 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _7.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 7 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _8.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 8 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _9.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 9 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _10.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 10 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _11.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12](
      _1: Tag[T1],
      _2: Tag[T2],
      _3: Tag[T3],
      _4: Tag[T4],
      _5: Tag[T5],
      _6: Tag[T6],
      _7: Tag[T7],
      _8: Tag[T8],
      _9: Tag[T9],
      _10: Tag[T10],
      _11: Tag[T11],
      _12: Tag[T12])
      extends Tag[
        native.CStruct12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _3.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _4.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _5.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _6.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _7.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _8.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _9.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _10.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _11.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _12.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 2 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _3.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 3 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _4.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 4 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _5.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 5 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _6.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 6 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _7.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 7 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _8.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 8 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _9.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 9 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _10.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 10 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _11.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 11 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _12.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct13[T1,
                             T2,
                             T3,
                             T4,
                             T5,
                             T6,
                             T7,
                             T8,
                             T9,
                             T10,
                             T11,
                             T12,
                             T13](_1: Tag[T1],
                                  _2: Tag[T2],
                                  _3: Tag[T3],
                                  _4: Tag[T4],
                                  _5: Tag[T5],
                                  _6: Tag[T6],
                                  _7: Tag[T7],
                                  _8: Tag[T8],
                                  _9: Tag[T9],
                                  _10: Tag[T10],
                                  _11: Tag[T11],
                                  _12: Tag[T12],
                                  _13: Tag[T13])
      extends Tag[
        native.CStruct13[T1,
                         T2,
                         T3,
                         T4,
                         T5,
                         T6,
                         T7,
                         T8,
                         T9,
                         T10,
                         T11,
                         T12,
                         T13]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _3.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _4.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _5.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _6.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _7.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _8.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _9.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _10.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _11.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _12.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _13.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 2 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _3.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 3 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _4.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 4 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _5.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 5 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _6.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 6 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _7.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 7 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _8.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 8 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _9.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 9 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _10.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 10 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _11.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 11 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _12.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 12 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _13.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct14[T1,
                             T2,
                             T3,
                             T4,
                             T5,
                             T6,
                             T7,
                             T8,
                             T9,
                             T10,
                             T11,
                             T12,
                             T13,
                             T14](_1: Tag[T1],
                                  _2: Tag[T2],
                                  _3: Tag[T3],
                                  _4: Tag[T4],
                                  _5: Tag[T5],
                                  _6: Tag[T6],
                                  _7: Tag[T7],
                                  _8: Tag[T8],
                                  _9: Tag[T9],
                                  _10: Tag[T10],
                                  _11: Tag[T11],
                                  _12: Tag[T12],
                                  _13: Tag[T13],
                                  _14: Tag[T14])
      extends Tag[
        native.CStruct14[T1,
                         T2,
                         T3,
                         T4,
                         T5,
                         T6,
                         T7,
                         T8,
                         T9,
                         T10,
                         T11,
                         T12,
                         T13,
                         T14]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _3.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _4.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _5.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _6.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _7.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _8.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _9.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _10.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _11.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _12.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _13.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _14.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 2 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _3.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 3 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _4.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 4 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _5.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 5 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _6.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 6 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _7.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 7 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _8.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 8 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _9.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 9 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _10.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 10 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _11.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 11 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _12.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 12 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _13.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 13 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _14.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct15[T1,
                             T2,
                             T3,
                             T4,
                             T5,
                             T6,
                             T7,
                             T8,
                             T9,
                             T10,
                             T11,
                             T12,
                             T13,
                             T14,
                             T15](_1: Tag[T1],
                                  _2: Tag[T2],
                                  _3: Tag[T3],
                                  _4: Tag[T4],
                                  _5: Tag[T5],
                                  _6: Tag[T6],
                                  _7: Tag[T7],
                                  _8: Tag[T8],
                                  _9: Tag[T9],
                                  _10: Tag[T10],
                                  _11: Tag[T11],
                                  _12: Tag[T12],
                                  _13: Tag[T13],
                                  _14: Tag[T14],
                                  _15: Tag[T15])
      extends Tag[
        native.CStruct15[T1,
                         T2,
                         T3,
                         T4,
                         T5,
                         T6,
                         T7,
                         T8,
                         T9,
                         T10,
                         T11,
                         T12,
                         T13,
                         T14,
                         T15]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _3.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _4.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _5.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _6.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _7.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _8.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _9.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _10.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _11.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _12.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _13.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _14.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _15.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 2 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _3.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 3 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _4.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 4 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _5.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 5 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _6.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 6 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _7.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 7 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _8.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 8 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _9.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 9 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _10.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 10 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _11.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 11 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _12.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 12 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _13.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 13 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _14.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 14 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _15.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct16[T1,
                             T2,
                             T3,
                             T4,
                             T5,
                             T6,
                             T7,
                             T8,
                             T9,
                             T10,
                             T11,
                             T12,
                             T13,
                             T14,
                             T15,
                             T16](_1: Tag[T1],
                                  _2: Tag[T2],
                                  _3: Tag[T3],
                                  _4: Tag[T4],
                                  _5: Tag[T5],
                                  _6: Tag[T6],
                                  _7: Tag[T7],
                                  _8: Tag[T8],
                                  _9: Tag[T9],
                                  _10: Tag[T10],
                                  _11: Tag[T11],
                                  _12: Tag[T12],
                                  _13: Tag[T13],
                                  _14: Tag[T14],
                                  _15: Tag[T15],
                                  _16: Tag[T16])
      extends Tag[
        native.CStruct16[T1,
                         T2,
                         T3,
                         T4,
                         T5,
                         T6,
                         T7,
                         T8,
                         T9,
                         T10,
                         T11,
                         T12,
                         T13,
                         T14,
                         T15,
                         T16]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _3.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _4.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _5.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _6.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _7.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _8.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _9.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _10.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _11.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _12.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _13.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _14.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _15.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _16.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 2 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _3.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 3 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _4.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 4 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _5.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 5 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _6.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 6 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _7.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 7 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _8.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 8 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _9.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 9 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _10.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 10 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _11.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 11 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _12.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 12 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _13.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 13 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _14.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 14 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _15.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 15 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _16.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct17[T1,
                             T2,
                             T3,
                             T4,
                             T5,
                             T6,
                             T7,
                             T8,
                             T9,
                             T10,
                             T11,
                             T12,
                             T13,
                             T14,
                             T15,
                             T16,
                             T17](_1: Tag[T1],
                                  _2: Tag[T2],
                                  _3: Tag[T3],
                                  _4: Tag[T4],
                                  _5: Tag[T5],
                                  _6: Tag[T6],
                                  _7: Tag[T7],
                                  _8: Tag[T8],
                                  _9: Tag[T9],
                                  _10: Tag[T10],
                                  _11: Tag[T11],
                                  _12: Tag[T12],
                                  _13: Tag[T13],
                                  _14: Tag[T14],
                                  _15: Tag[T15],
                                  _16: Tag[T16],
                                  _17: Tag[T17])
      extends Tag[
        native.CStruct17[T1,
                         T2,
                         T3,
                         T4,
                         T5,
                         T6,
                         T7,
                         T8,
                         T9,
                         T10,
                         T11,
                         T12,
                         T13,
                         T14,
                         T15,
                         T16,
                         T17]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _3.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _4.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _5.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _6.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _7.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _8.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _9.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _10.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _11.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _12.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _13.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _14.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _15.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _16.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _17.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 2 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _3.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 3 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _4.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 4 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _5.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 5 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _6.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 6 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _7.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 7 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _8.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 8 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _9.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 9 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _10.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 10 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _11.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 11 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _12.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 12 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _13.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 13 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _14.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 14 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _15.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 15 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _16.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 16 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _17.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct18[T1,
                             T2,
                             T3,
                             T4,
                             T5,
                             T6,
                             T7,
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
                             T18](_1: Tag[T1],
                                  _2: Tag[T2],
                                  _3: Tag[T3],
                                  _4: Tag[T4],
                                  _5: Tag[T5],
                                  _6: Tag[T6],
                                  _7: Tag[T7],
                                  _8: Tag[T8],
                                  _9: Tag[T9],
                                  _10: Tag[T10],
                                  _11: Tag[T11],
                                  _12: Tag[T12],
                                  _13: Tag[T13],
                                  _14: Tag[T14],
                                  _15: Tag[T15],
                                  _16: Tag[T16],
                                  _17: Tag[T17],
                                  _18: Tag[T18])
      extends Tag[
        native.CStruct18[T1,
                         T2,
                         T3,
                         T4,
                         T5,
                         T6,
                         T7,
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
                         T18]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _18.alignment) + _18.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _3.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _4.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _5.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _6.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _7.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _8.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _9.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _10.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _11.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _12.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _13.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _14.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _15.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _16.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _17.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _18.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 2 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _3.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 3 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _4.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 4 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _5.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 5 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _6.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 6 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _7.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 7 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _8.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 8 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _9.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 9 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _10.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 10 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _11.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 11 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _12.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 12 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _13.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 13 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _14.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 14 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _15.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 15 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _16.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 16 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _17.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 17 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _18.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct19[T1,
                             T2,
                             T3,
                             T4,
                             T5,
                             T6,
                             T7,
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
                             T19](_1: Tag[T1],
                                  _2: Tag[T2],
                                  _3: Tag[T3],
                                  _4: Tag[T4],
                                  _5: Tag[T5],
                                  _6: Tag[T6],
                                  _7: Tag[T7],
                                  _8: Tag[T8],
                                  _9: Tag[T9],
                                  _10: Tag[T10],
                                  _11: Tag[T11],
                                  _12: Tag[T12],
                                  _13: Tag[T13],
                                  _14: Tag[T14],
                                  _15: Tag[T15],
                                  _16: Tag[T16],
                                  _17: Tag[T17],
                                  _18: Tag[T18],
                                  _19: Tag[T19])
      extends Tag[
        native.CStruct19[T1,
                         T2,
                         T3,
                         T4,
                         T5,
                         T6,
                         T7,
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
                         T19]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _18.alignment) + _18.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _19.alignment) + _19.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _3.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _4.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _5.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _6.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _7.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _8.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _9.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _10.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _11.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _12.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _13.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _14.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _15.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _16.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _17.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _18.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _19.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 2 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _3.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 3 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _4.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 4 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _5.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 5 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _6.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 6 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _7.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 7 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _8.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 8 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _9.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 9 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _10.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 10 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _11.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 11 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _12.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 12 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _13.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 13 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _14.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 14 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _15.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 15 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _16.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 16 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _17.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 17 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _18.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 18 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _18.alignment) + _18.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _19.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct20[T1,
                             T2,
                             T3,
                             T4,
                             T5,
                             T6,
                             T7,
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
                             T20](_1: Tag[T1],
                                  _2: Tag[T2],
                                  _3: Tag[T3],
                                  _4: Tag[T4],
                                  _5: Tag[T5],
                                  _6: Tag[T6],
                                  _7: Tag[T7],
                                  _8: Tag[T8],
                                  _9: Tag[T9],
                                  _10: Tag[T10],
                                  _11: Tag[T11],
                                  _12: Tag[T12],
                                  _13: Tag[T13],
                                  _14: Tag[T14],
                                  _15: Tag[T15],
                                  _16: Tag[T16],
                                  _17: Tag[T17],
                                  _18: Tag[T18],
                                  _19: Tag[T19],
                                  _20: Tag[T20])
      extends Tag[
        native.CStruct20[T1,
                         T2,
                         T3,
                         T4,
                         T5,
                         T6,
                         T7,
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
                         T20]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _18.alignment) + _18.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _19.alignment) + _19.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _20.alignment) + _20.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _3.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _4.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _5.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _6.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _7.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _8.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _9.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _10.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _11.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _12.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _13.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _14.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _15.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _16.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _17.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _18.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _19.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _20.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 2 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _3.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 3 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _4.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 4 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _5.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 5 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _6.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 6 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _7.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 7 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _8.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 8 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _9.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 9 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _10.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 10 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _11.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 11 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _12.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 12 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _13.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 13 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _14.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 14 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _15.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 15 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _16.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 16 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _17.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 17 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _18.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 18 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _18.alignment) + _18.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _19.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 19 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _18.alignment) + _18.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _19.alignment) + _19.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _20.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct21[T1,
                             T2,
                             T3,
                             T4,
                             T5,
                             T6,
                             T7,
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
                             T21](_1: Tag[T1],
                                  _2: Tag[T2],
                                  _3: Tag[T3],
                                  _4: Tag[T4],
                                  _5: Tag[T5],
                                  _6: Tag[T6],
                                  _7: Tag[T7],
                                  _8: Tag[T8],
                                  _9: Tag[T9],
                                  _10: Tag[T10],
                                  _11: Tag[T11],
                                  _12: Tag[T12],
                                  _13: Tag[T13],
                                  _14: Tag[T14],
                                  _15: Tag[T15],
                                  _16: Tag[T16],
                                  _17: Tag[T17],
                                  _18: Tag[T18],
                                  _19: Tag[T19],
                                  _20: Tag[T20],
                                  _21: Tag[T21])
      extends Tag[
        native.CStruct21[T1,
                         T2,
                         T3,
                         T4,
                         T5,
                         T6,
                         T7,
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
                         T21]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _18.alignment) + _18.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _19.alignment) + _19.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _20.alignment) + _20.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _21.alignment) + _21.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _3.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _4.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _5.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _6.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _7.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _8.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _9.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _10.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _11.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _12.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _13.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _14.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _15.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _16.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _17.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _18.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _19.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _20.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _21.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 2 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _3.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 3 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _4.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 4 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _5.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 5 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _6.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 6 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _7.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 7 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _8.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 8 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _9.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 9 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _10.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 10 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _11.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 11 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _12.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 12 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _13.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 13 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _14.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 14 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _15.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 15 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _16.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 16 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _17.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 17 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _18.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 18 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _18.alignment) + _18.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _19.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 19 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _18.alignment) + _18.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _19.alignment) + _19.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _20.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 20 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _18.alignment) + _18.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _19.alignment) + _19.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _20.alignment) + _20.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _21.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 99)

  final case class CStruct22[T1,
                             T2,
                             T3,
                             T4,
                             T5,
                             T6,
                             T7,
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
                             T22](_1: Tag[T1],
                                  _2: Tag[T2],
                                  _3: Tag[T3],
                                  _4: Tag[T4],
                                  _5: Tag[T5],
                                  _6: Tag[T6],
                                  _7: Tag[T7],
                                  _8: Tag[T8],
                                  _9: Tag[T9],
                                  _10: Tag[T10],
                                  _11: Tag[T11],
                                  _12: Tag[T12],
                                  _13: Tag[T13],
                                  _14: Tag[T14],
                                  _15: Tag[T15],
                                  _16: Tag[T16],
                                  _17: Tag[T17],
                                  _18: Tag[T18],
                                  _19: Tag[T19],
                                  _20: Tag[T20],
                                  _21: Tag[T21],
                                  _22: Tag[T22])
      extends Tag[
        native.CStruct22[T1,
                         T2,
                         T3,
                         T4,
                         T5,
                         T6,
                         T7,
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
                         T22]]
      with CStruct {
    final def size: Int = {
      var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _18.alignment) + _18.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _19.alignment) + _19.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _20.alignment) + _20.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _21.alignment) + _21.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 104)
      res = align(res, _22.alignment) + _22.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 106)
      align(res, alignment)
    }
    final def alignment: Int = {
      var res = 1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _1.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _2.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _3.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _4.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _5.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _6.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _7.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _8.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _9.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _10.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _11.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _12.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _13.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _14.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _15.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _16.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _17.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _18.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _19.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _20.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _21.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 111)
      res = res max _22.alignment
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 113)
      res
    }
    override def offset(idx: Int): Int = idx match {
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 0 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _1.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 1 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _2.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 2 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _3.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 3 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _4.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 4 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _5.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 5 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _6.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 6 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _7.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 7 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _8.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 8 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _9.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 9 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _10.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 10 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _11.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 11 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _12.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 12 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _13.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 13 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _14.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 14 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _15.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 15 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _16.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 16 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _17.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 17 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _18.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 18 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _18.alignment) + _18.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _19.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 19 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _18.alignment) + _18.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _19.alignment) + _19.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _20.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 20 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _18.alignment) + _18.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _19.alignment) + _19.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _20.alignment) + _20.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _21.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 117)
      case 21 =>
        var res = 0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _1.alignment) + _1.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _2.alignment) + _2.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _3.alignment) + _3.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _4.alignment) + _4.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _5.alignment) + _5.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _6.alignment) + _6.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _7.alignment) + _7.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _8.alignment) + _8.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _9.alignment) + _9.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _10.alignment) + _10.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _11.alignment) + _11.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _12.alignment) + _12.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _13.alignment) + _13.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _14.alignment) + _14.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _15.alignment) + _15.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _16.alignment) + _16.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _17.alignment) + _17.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _18.alignment) + _18.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _19.alignment) + _19.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _20.alignment) + _20.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 120)
        res = align(res, _21.alignment) + _21.size
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 122)
        align(res, _22.alignment)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 124)
      case _ =>
        throwUndefined()
    }
  }

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 130)

  implicit def materializePtrTag[T](implicit tag: Tag[T]): Tag[native.Ptr[T]] =
    Tag.Ptr(tag)
  implicit def materializeClassTag[T <: AnyRef: ClassTag]: Tag[T] =
    Tag.Class(
      implicitly[ClassTag[T]].runtimeClass.asInstanceOf[java.lang.Class[T]])
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 136)
  implicit def materializeUnitTag: Tag[scala.Unit] =
    Unit
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 136)
  implicit def materializeBooleanTag: Tag[scala.Boolean] =
    Boolean
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 136)
  implicit def materializeCharTag: Tag[scala.Char] =
    Char
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 136)
  implicit def materializeByteTag: Tag[scala.Byte] =
    Byte
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 136)
  implicit def materializeUByteTag: Tag[native.UByte] =
    UByte
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 136)
  implicit def materializeShortTag: Tag[scala.Short] =
    Short
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 136)
  implicit def materializeUShortTag: Tag[native.UShort] =
    UShort
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 136)
  implicit def materializeIntTag: Tag[scala.Int] =
    Int
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 136)
  implicit def materializeUIntTag: Tag[native.UInt] =
    UInt
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 136)
  implicit def materializeLongTag: Tag[scala.Long] =
    Long
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 136)
  implicit def materializeULongTag: Tag[native.ULong] =
    ULong
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 136)
  implicit def materializeFloatTag: Tag[scala.Float] =
    Float
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 136)
  implicit def materializeDoubleTag: Tag[scala.Double] =
    Double
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 140)
  implicit def materializeNat0Tag: Tag[native.Nat._0] =
    Nat0
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 140)
  implicit def materializeNat1Tag: Tag[native.Nat._1] =
    Nat1
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 140)
  implicit def materializeNat2Tag: Tag[native.Nat._2] =
    Nat2
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 140)
  implicit def materializeNat3Tag: Tag[native.Nat._3] =
    Nat3
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 140)
  implicit def materializeNat4Tag: Tag[native.Nat._4] =
    Nat4
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 140)
  implicit def materializeNat5Tag: Tag[native.Nat._5] =
    Nat5
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 140)
  implicit def materializeNat6Tag: Tag[native.Nat._6] =
    Nat6
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 140)
  implicit def materializeNat7Tag: Tag[native.Nat._7] =
    Nat7
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 140)
  implicit def materializeNat8Tag: Tag[native.Nat._8] =
    Nat8
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 140)
  implicit def materializeNat9Tag: Tag[native.Nat._9] =
    Nat9
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 143)
  implicit def materializeNatDigitTag[N <: native.Nat.Base: Tag,
                                      M <: native.Nat: Tag]
    : Tag[native.Nat.Digit[N, M]] =
    Tag.Digit(implicitly[Tag[N]], implicitly[Tag[M]])
  implicit def materializeCArrayTag[T: Tag, N <: native.Nat: Tag]
    : Tag[native.CArray[T, N]] =
    Tag.CArray(implicitly[Tag[T]], implicitly[Tag[N]])
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct0Tag: Tag[native.CStruct0] =
    Tag.CStruct0()
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct1Tag[T1: Tag]: Tag[native.CStruct1[T1]] =
    Tag.CStruct1(implicitly[Tag[T1]])
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct2Tag[T1: Tag, T2: Tag]
    : Tag[native.CStruct2[T1, T2]] =
    Tag.CStruct2(implicitly[Tag[T1]], implicitly[Tag[T2]])
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct3Tag[T1: Tag, T2: Tag, T3: Tag]
    : Tag[native.CStruct3[T1, T2, T3]] =
    Tag.CStruct3(implicitly[Tag[T1]], implicitly[Tag[T2]], implicitly[Tag[T3]])
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct4Tag[T1: Tag, T2: Tag, T3: Tag, T4: Tag]
    : Tag[native.CStruct4[T1, T2, T3, T4]] =
    Tag.CStruct4(implicitly[Tag[T1]],
                 implicitly[Tag[T2]],
                 implicitly[Tag[T3]],
                 implicitly[Tag[T4]])
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct5Tag[T1: Tag,
                                      T2: Tag,
                                      T3: Tag,
                                      T4: Tag,
                                      T5: Tag]
    : Tag[native.CStruct5[T1, T2, T3, T4, T5]] =
    Tag.CStruct5(implicitly[Tag[T1]],
                 implicitly[Tag[T2]],
                 implicitly[Tag[T3]],
                 implicitly[Tag[T4]],
                 implicitly[Tag[T5]])
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct6Tag[T1: Tag,
                                      T2: Tag,
                                      T3: Tag,
                                      T4: Tag,
                                      T5: Tag,
                                      T6: Tag]
    : Tag[native.CStruct6[T1, T2, T3, T4, T5, T6]] =
    Tag.CStruct6(implicitly[Tag[T1]],
                 implicitly[Tag[T2]],
                 implicitly[Tag[T3]],
                 implicitly[Tag[T4]],
                 implicitly[Tag[T5]],
                 implicitly[Tag[T6]])
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct7Tag[T1: Tag,
                                      T2: Tag,
                                      T3: Tag,
                                      T4: Tag,
                                      T5: Tag,
                                      T6: Tag,
                                      T7: Tag]
    : Tag[native.CStruct7[T1, T2, T3, T4, T5, T6, T7]] =
    Tag.CStruct7(implicitly[Tag[T1]],
                 implicitly[Tag[T2]],
                 implicitly[Tag[T3]],
                 implicitly[Tag[T4]],
                 implicitly[Tag[T5]],
                 implicitly[Tag[T6]],
                 implicitly[Tag[T7]])
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct8Tag[T1: Tag,
                                      T2: Tag,
                                      T3: Tag,
                                      T4: Tag,
                                      T5: Tag,
                                      T6: Tag,
                                      T7: Tag,
                                      T8: Tag]
    : Tag[native.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]] =
    Tag.CStruct8(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]]
    )
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct9Tag[T1: Tag,
                                      T2: Tag,
                                      T3: Tag,
                                      T4: Tag,
                                      T5: Tag,
                                      T6: Tag,
                                      T7: Tag,
                                      T8: Tag,
                                      T9: Tag]
    : Tag[native.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]] =
    Tag.CStruct9(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]]
    )
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct10Tag[T1: Tag,
                                       T2: Tag,
                                       T3: Tag,
                                       T4: Tag,
                                       T5: Tag,
                                       T6: Tag,
                                       T7: Tag,
                                       T8: Tag,
                                       T9: Tag,
                                       T10: Tag]
    : Tag[native.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10]] =
    Tag.CStruct10(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]]
    )
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct11Tag[T1: Tag,
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
    : Tag[native.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11]] =
    Tag.CStruct11(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]]
    )
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct12Tag[T1: Tag,
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
    : Tag[native.CStruct12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12]] =
    Tag.CStruct12(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]]
    )
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct13Tag[T1: Tag,
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
                                       T13: Tag]: Tag[
    native.CStruct13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13]] =
    Tag.CStruct13(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]],
      implicitly[Tag[T13]]
    )
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct14Tag[T1: Tag,
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
    native.CStruct14[T1,
                     T2,
                     T3,
                     T4,
                     T5,
                     T6,
                     T7,
                     T8,
                     T9,
                     T10,
                     T11,
                     T12,
                     T13,
                     T14]] =
    Tag.CStruct14(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]],
      implicitly[Tag[T13]],
      implicitly[Tag[T14]]
    )
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct15Tag[T1: Tag,
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
    native.CStruct15[T1,
                     T2,
                     T3,
                     T4,
                     T5,
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
    Tag.CStruct15(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]],
      implicitly[Tag[T13]],
      implicitly[Tag[T14]],
      implicitly[Tag[T15]]
    )
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct16Tag[T1: Tag,
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
    native.CStruct16[T1,
                     T2,
                     T3,
                     T4,
                     T5,
                     T6,
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
    Tag.CStruct16(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]],
      implicitly[Tag[T13]],
      implicitly[Tag[T14]],
      implicitly[Tag[T15]],
      implicitly[Tag[T16]]
    )
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct17Tag[T1: Tag,
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
    native.CStruct17[T1,
                     T2,
                     T3,
                     T4,
                     T5,
                     T6,
                     T7,
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
    Tag.CStruct17(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]],
      implicitly[Tag[T13]],
      implicitly[Tag[T14]],
      implicitly[Tag[T15]],
      implicitly[Tag[T16]],
      implicitly[Tag[T17]]
    )
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct18Tag[T1: Tag,
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
    native.CStruct18[T1,
                     T2,
                     T3,
                     T4,
                     T5,
                     T6,
                     T7,
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
    Tag.CStruct18(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]],
      implicitly[Tag[T13]],
      implicitly[Tag[T14]],
      implicitly[Tag[T15]],
      implicitly[Tag[T16]],
      implicitly[Tag[T17]],
      implicitly[Tag[T18]]
    )
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct19Tag[T1: Tag,
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
    native.CStruct19[T1,
                     T2,
                     T3,
                     T4,
                     T5,
                     T6,
                     T7,
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
    Tag.CStruct19(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]],
      implicitly[Tag[T13]],
      implicitly[Tag[T14]],
      implicitly[Tag[T15]],
      implicitly[Tag[T16]],
      implicitly[Tag[T17]],
      implicitly[Tag[T18]],
      implicitly[Tag[T19]]
    )
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct20Tag[T1: Tag,
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
    native.CStruct20[T1,
                     T2,
                     T3,
                     T4,
                     T5,
                     T6,
                     T7,
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
    Tag.CStruct20(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]],
      implicitly[Tag[T13]],
      implicitly[Tag[T14]],
      implicitly[Tag[T15]],
      implicitly[Tag[T16]],
      implicitly[Tag[T17]],
      implicitly[Tag[T18]],
      implicitly[Tag[T19]],
      implicitly[Tag[T20]]
    )
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct21Tag[T1: Tag,
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
    native.CStruct21[T1,
                     T2,
                     T3,
                     T4,
                     T5,
                     T6,
                     T7,
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
    Tag.CStruct21(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]],
      implicitly[Tag[T13]],
      implicitly[Tag[T14]],
      implicitly[Tag[T15]],
      implicitly[Tag[T16]],
      implicitly[Tag[T17]],
      implicitly[Tag[T18]],
      implicitly[Tag[T19]],
      implicitly[Tag[T20]],
      implicitly[Tag[T21]]
    )
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 152)
  implicit def materializeCStruct22Tag[T1: Tag,
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
    native.CStruct22[T1,
                     T2,
                     T3,
                     T4,
                     T5,
                     T6,
                     T7,
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
    Tag.CStruct22(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]],
      implicitly[Tag[T13]],
      implicitly[Tag[T14]],
      implicitly[Tag[T15]],
      implicitly[Tag[T16]],
      implicitly[Tag[T17]],
      implicitly[Tag[T18]],
      implicitly[Tag[T19]],
      implicitly[Tag[T20]],
      implicitly[Tag[T21]],
      implicitly[Tag[T22]]
    )
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Tag.scala.gyb", line: 155)
}
