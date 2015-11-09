package native

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros

package ffi {
  @intrinsic
  final class Ptr[T] private {
    def apply(): T                      = intrinsic.impl
    def apply(i: Size): T               = intrinsic.impl
    def update(value: T): Unit          = intrinsic.impl
    def update(i: Size, value: T): Unit = intrinsic.impl
    def +(i: Int): Ptr[T]               = intrinsic.impl
    def -(i: Int): Ptr[T]               = intrinsic.impl
  }
  object Ptr {
    def Null[T]: Ptr[T] = intrinsic.impl
  }

  @intrinsic
  final class Arr[T, N <: Size with Singleton] private {
    def apply(i: Size): T               = intrinsic.impl
    def update(i: Size, value: T): Unit = intrinsic.impl
    def size: N                         = intrinsic.impl
  }
  object Arr {
    def apply[T, N <: Size with Singleton](elems: T*): Arr[T, N] = intrinsic.impl
  }

  final class struct extends StaticAnnotation
  final class union  extends StaticAnnotation
  final class extern extends StaticAnnotation
}

package object ffi {
  type Addr    = Nothing
  type Size    = Nothing
  type Int8    = Byte
  type Int16   = Short
  type Int32   = Int
  type Int64   = Long
  type UInt8   = Nothing
  type UInt16  = Nothing
  type UInt32  = Nothing
  type UInt64  = Nothing
  type Char8   = Byte
  type Char16  = Nothing
  type Char32  = Nothing

  def &[T](v: T): Ptr[T] = intrinsic.impl
  def sizeOf[T]: Size    = intrinsic.impl
  def alignOf[T]: Size   = intrinsic.impl
  def extern: Nothing    = intrinsic.impl

  implicit class CQuote(ctx: StringContext) {
    def c(args: Any*): ffi.Ptr[ffi.Char8] =
      macro native.internal.Macros.cquote
  }
}
