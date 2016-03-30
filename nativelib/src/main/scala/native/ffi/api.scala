package native

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros

package ffi {
  final class Ptr[T](val addr: Addr) extends AnyVal {
    def apply(): T                      = builtin
    def apply(i: Size): T               = builtin
    def update(value: T): Unit          = builtin
    def update(i: Size, value: T): Unit = builtin
    def +(i: Int): Ptr[T]               = builtin
    def -(i: Int): Ptr[T]               = builtin
  }
  object Ptr {
    def Null[T]: Ptr[T]                 = new Ptr[T](0L)
  }

  final class extern extends StaticAnnotation
  // final class struct extends StaticAnnotation
  // final class union  extends StaticAnnotation

  // @builtin
  // final class Arr[T, N <: Size with Singleton] private {
  //   def apply(i: Size): T               = builtin
  //   def update(i: Size, value: T): Unit = builtin
  //   def size: N                         = builtin
  // }
  // object Arr {
  //   def apply[T, N <: Size with Singleton](elems: T*): Arr[T, N] = builtin
  // }
}

package object ffi {
  type Addr   = Int64
  type Size   = Int64
  type Int8   = scala.Byte
  type Int16  = scala.Short
  type Int32  = scala.Int
  type Int64  = scala.Long
  type Char8  = Byte
  // type UInt8  = scala.UByte
  // type UInt16 = scala.UShort
  // type UInt32 = scala.UInt
  // type UInt64 = scala.ULong
  // type Char16 = ???
  // type Char32 = ???

  def extern: Nothing    = builtin
  // def &[T](v: T): Ptr[T] = builtin
  // def sizeOf[T]: Size    = builtin
  // def alignOf[T]: Size   = builtin

  implicit class CQuote(ctx: StringContext) {
    def c(args: Any*): ffi.Ptr[ffi.Char8] = builtin
  }
}
