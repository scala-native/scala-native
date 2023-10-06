package scala.scalanative.libc

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

@extern
trait stdatomicExt { self: stdatomic.type =>
  type _Atomic[T] = atomic[T]

  // C++ like std::atomic<T>
  type atomic[T] = T match {
    case Boolean           => AtomicBool
    case Byte              => AtomicByte
    case UByte             => AtomicUnsignedByte
    case CShort            => AtomicShort
    case CUnsignedShort    => AtomicUnsignedShort
    case CInt              => AtomicInt
    case CUnsignedInt      => AtomicUnsignedInt
    case CLong             => AtomicLong
    case CUnsignedLong     => AtomicUnsignedLong
    case CLongLong         => AtomicLongLong
    case CUnsignedLongLong => AtomicUnsignedLongLong
    // Non standard
    case Ptr[t] => AtomicPtr[t]
    case _      => AtomicRef[T]
  }
}
