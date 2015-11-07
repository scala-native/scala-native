package native

import scala.annotation.StaticAnnotation

package ffi {
  @intrinsic
  final class Ptr[T] private {
    def apply(): T                      = intrinsic.impl
    def apply(i: Size): T               = intrinsic.impl
    def update(value: T): Unit          = intrinsic.impl
    def update(i: Size, value: T): Unit = intrinsic.impl
    def +(i: Int): Ptr[T]               = intrinsic.impl
    def -(i: Int): Ptr[T]               = intrinsic.impl
    def -(other: Ptr[T]): PtrDiff       = intrinsic.impl
  }

  @intrinsic
  final class Arr[T, N <: Size with Singleton] private {
    def apply(i: Size): T               = intrinsic.impl
    def update(i: Size, value: T): Unit = intrinsic.impl
    def size: N                         = intrinsic.impl
  }

  final class struct extends StaticAnnotation
  final class union  extends StaticAnnotation
  final class extern extends StaticAnnotation
}

package object ffi {
  type Addr    = Nothing
  type Size    = Nothing
  type PtrDiff = Nothing
  type Int8    = Nothing
  type Int16   = Nothing
  type Int32   = Nothing
  type Int64   = Nothing
  type UInt8   = Nothing
  type UInt16  = Nothing
  type UInt32  = Nothing
  type UInt64  = Nothing

  def &[T](v: T): Ptr[T] = intrinsic.impl
  def sizeOf[T]: Size    = intrinsic.impl
  def alignOf[T]: Size   = intrinsic.impl
}
