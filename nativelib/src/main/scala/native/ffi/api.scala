package native

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros

package ffi {
  @builtin
  final class Ptr[T] private {
    def apply(): T                      = builtin
    def apply(i: Size): T               = builtin
    def update(value: T): Unit          = builtin
    def update(i: Size, value: T): Unit = builtin
    def +(i: Int): Ptr[T]               = builtin
    def -(i: Int): Ptr[T]               = builtin
  }
  object Ptr {
    def Null[T]: Ptr[T]                 = builtin
  }

  @builtin
  final class Arr[T, N <: Size with Singleton] private {
    def apply(i: Size): T               = builtin
    def update(i: Size, value: T): Unit = builtin
    def size: N                         = builtin
  }
  object Arr {
    def apply[T, N <: Size with Singleton](elems: T*): Arr[T, N] = builtin
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

  def &[T](v: T): Ptr[T] = builtin
  def sizeOf[T]: Size    = builtin
  def alignOf[T]: Size   = builtin
  def extern: Nothing    = builtin

  implicit class CQuote(ctx: StringContext) {
    def c(args: Any*): ffi.Ptr[ffi.Char8] =
      macro native.internal.Macros.cquote
  }
}
