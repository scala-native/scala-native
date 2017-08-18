// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 1)
package scala.scalanative
package runtime

import scala.scalanative.native._
import scala.scalanative.runtime.Atomic._

abstract class CAtomic {

  import CAtomicsImplicits._

}

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 26)

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 28)

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 34)

class CAtomicByte(default: Byte = 0.asInstanceOf[Byte]) extends CAtomic {

  private[this] val atm = Atomic.alloc(sizeof[Byte])
  init_byte(atm, default)

  def load(): Byte = load_byte(atm)

  def store(value: Byte): Unit = store_byte(atm, value)

  def free(): Unit = Atomic.free(atm)

  def compareAndSwapStrong(expected: Byte, desired: Byte): (Boolean, Byte) = {
    val expectedPtr = stackalloc[Byte]
    !expectedPtr = expected

    if (compare_and_swap_strong_byte(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: Byte, desired: Byte): (Boolean, Byte) = {
    val expectedPtr = stackalloc[Byte]
    !expectedPtr = expected

    if (compare_and_swap_weak_byte(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: Byte): Byte = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: Byte): Byte = atomic_add_byte(atm, value)

  def subFetch(value: Byte): Byte = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: Byte): Byte = atomic_sub_byte(atm, value)

  def andFetch(value: Byte): Byte = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: Byte): Byte = atomic_and_byte(atm, value)

  def orFetch(value: Byte): Byte = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: Byte): Byte = atomic_or_byte(atm, value)

  def xorFetch(value: Byte): Byte = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: Byte): Byte = atomic_xor_byte(atm, value)

  override def toString: String = load().toString

  override def equals(that: Any): Boolean = that match {
    case o: CAtomicByte => o.load() == load()
    case o: Byte        => load() == o
    case _              => false
  }

}

object CAtomicByte extends CAtomic {

  def apply(initValue: Byte) = new CAtomicByte(initValue)

  def apply() = new CAtomicByte()

}

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 28)

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 34)

class CAtomicShort(default: CShort = 0.asInstanceOf[CShort]) extends CAtomic {

  private[this] val atm = Atomic.alloc(sizeof[CShort]).cast[Ptr[CShort]]
  init_short(atm, default)

  def load(): CShort = load_short(atm)

  def store(value: CShort): Unit = store_short(atm, value)

  def free(): Unit = Atomic.free(atm.cast[Ptr[Byte]])

  def compareAndSwapStrong(expected: CShort,
                           desired: CShort): (Boolean, CShort) = {
    val expectedPtr = stackalloc[CShort]
    !expectedPtr = expected

    if (compare_and_swap_strong_short(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: CShort,
                         desired: CShort): (Boolean, CShort) = {
    val expectedPtr = stackalloc[CShort]
    !expectedPtr = expected

    if (compare_and_swap_weak_short(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: CShort): CShort = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: CShort): CShort = atomic_add_short(atm, value)

  def subFetch(value: CShort): CShort = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: CShort): CShort = atomic_sub_short(atm, value)

  def andFetch(value: CShort): CShort = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: CShort): CShort = atomic_and_short(atm, value)

  def orFetch(value: CShort): CShort = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: CShort): CShort = atomic_or_short(atm, value)

  def xorFetch(value: CShort): CShort = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: CShort): CShort = atomic_xor_short(atm, value)

  override def toString: String = load().toString

  override def equals(that: Any): Boolean = that match {
    case o: CAtomicShort => o.load() == load()
    case o: CShort       => load() == o
    case _               => false
  }

}

object CAtomicShort extends CAtomic {

  def apply(initValue: CShort) = new CAtomicShort(initValue)

  def apply() = new CAtomicShort()

}

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 28)

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 34)

class CAtomicInt(default: CInt = 0) extends CAtomic {

  private[this] val atm = Atomic.alloc(sizeof[CInt]).cast[Ptr[CInt]]
  init_int(atm, default)

  def load(): CInt = load_int(atm)

  def store(value: CInt): Unit = store_int(atm, value)

  def free(): Unit = Atomic.free(atm.cast[Ptr[Byte]])

  def compareAndSwapStrong(expected: CInt, desired: CInt): (Boolean, CInt) = {
    val expectedPtr = stackalloc[CInt]
    !expectedPtr = expected

    if (compare_and_swap_strong_int(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: CInt, desired: CInt): (Boolean, CInt) = {
    val expectedPtr = stackalloc[CInt]
    !expectedPtr = expected

    if (compare_and_swap_weak_int(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: CInt): CInt = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: CInt): CInt = atomic_add_int(atm, value)

  def subFetch(value: CInt): CInt = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: CInt): CInt = atomic_sub_int(atm, value)

  def andFetch(value: CInt): CInt = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: CInt): CInt = atomic_and_int(atm, value)

  def orFetch(value: CInt): CInt = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: CInt): CInt = atomic_or_int(atm, value)

  def xorFetch(value: CInt): CInt = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: CInt): CInt = atomic_xor_int(atm, value)

  override def toString: String = load().toString

  override def equals(that: Any): Boolean = that match {
    case o: CAtomicInt => o.load() == load()
    case o: CInt       => load() == o
    case _             => false
  }

}

object CAtomicInt extends CAtomic {

  def apply(initValue: CInt) = new CAtomicInt(initValue)

  def apply() = new CAtomicInt()

}

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 28)

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 34)

class CAtomicLong(default: CLong = 0.asInstanceOf[CLong]) extends CAtomic {

  private[this] val atm = Atomic.alloc(sizeof[CLong]).cast[Ptr[CLong]]
  init_long(atm, default)

  def load(): CLong = load_long(atm)

  def store(value: CLong): Unit = store_long(atm, value)

  def free(): Unit = Atomic.free(atm.cast[Ptr[Byte]])

  def compareAndSwapStrong(expected: CLong,
                           desired: CLong): (Boolean, CLong) = {
    val expectedPtr = stackalloc[CLong]
    !expectedPtr = expected

    if (compare_and_swap_strong_long(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: CLong, desired: CLong): (Boolean, CLong) = {
    val expectedPtr = stackalloc[CLong]
    !expectedPtr = expected

    if (compare_and_swap_weak_long(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: CLong): CLong = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: CLong): CLong = atomic_add_long(atm, value)

  def subFetch(value: CLong): CLong = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: CLong): CLong = atomic_sub_long(atm, value)

  def andFetch(value: CLong): CLong = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: CLong): CLong = atomic_and_long(atm, value)

  def orFetch(value: CLong): CLong = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: CLong): CLong = atomic_or_long(atm, value)

  def xorFetch(value: CLong): CLong = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: CLong): CLong = atomic_xor_long(atm, value)

  override def toString: String = load().toString

  override def equals(that: Any): Boolean = that match {
    case o: CAtomicLong => o.load() == load()
    case o: CLong       => load() == o
    case _              => false
  }

}

object CAtomicLong extends CAtomic {

  def apply(initValue: CLong) = new CAtomicLong(initValue)

  def apply() = new CAtomicLong()

}

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 28)

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 34)

class CAtomicUnsignedByte(default: Byte = 0.asInstanceOf[Byte])
    extends CAtomic {

  private[this] val atm = Atomic.alloc(sizeof[Byte])
  init_ubyte(atm, default)

  def load(): Byte = load_ubyte(atm)

  def store(value: Byte): Unit = store_ubyte(atm, value)

  def free(): Unit = Atomic.free(atm)

  def compareAndSwapStrong(expected: Byte, desired: Byte): (Boolean, Byte) = {
    val expectedPtr = stackalloc[Byte]
    !expectedPtr = expected

    if (compare_and_swap_strong_ubyte(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: Byte, desired: Byte): (Boolean, Byte) = {
    val expectedPtr = stackalloc[Byte]
    !expectedPtr = expected

    if (compare_and_swap_weak_ubyte(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: Byte): Byte = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: Byte): Byte = atomic_add_ubyte(atm, value)

  def subFetch(value: Byte): Byte = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: Byte): Byte = atomic_sub_ubyte(atm, value)

  def andFetch(value: Byte): Byte = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: Byte): Byte = atomic_and_ubyte(atm, value)

  def orFetch(value: Byte): Byte = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: Byte): Byte = atomic_or_ubyte(atm, value)

  def xorFetch(value: Byte): Byte = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: Byte): Byte = atomic_xor_ubyte(atm, value)

  override def toString: String = load().toString

  override def equals(that: Any): Boolean = that match {
    case o: CAtomicUnsignedByte => o.load() == load()
    case o: Byte                => load() == o
    case _                      => false
  }

}

object CAtomicUnsignedByte extends CAtomic {

  def apply(initValue: Byte) = new CAtomicUnsignedByte(initValue)

  def apply() = new CAtomicUnsignedByte()

}

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 28)

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 34)

class CAtomicUnsignedShort(
    default: CUnsignedShort = 0.asInstanceOf[CUnsignedShort])
    extends CAtomic {

  private[this] val atm =
    Atomic.alloc(sizeof[CUnsignedShort]).cast[Ptr[CUnsignedShort]]
  init_ushort(atm, default)

  def load(): CUnsignedShort = load_ushort(atm)

  def store(value: CUnsignedShort): Unit = store_ushort(atm, value)

  def free(): Unit = Atomic.free(atm.cast[Ptr[Byte]])

  def compareAndSwapStrong(
      expected: CUnsignedShort,
      desired: CUnsignedShort): (Boolean, CUnsignedShort) = {
    val expectedPtr = stackalloc[CUnsignedShort]
    !expectedPtr = expected

    if (compare_and_swap_strong_ushort(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: CUnsignedShort,
                         desired: CUnsignedShort): (Boolean, CUnsignedShort) = {
    val expectedPtr = stackalloc[CUnsignedShort]
    !expectedPtr = expected

    if (compare_and_swap_weak_ushort(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: CUnsignedShort): CUnsignedShort = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: CUnsignedShort): CUnsignedShort =
    atomic_add_ushort(atm, value)

  def subFetch(value: CUnsignedShort): CUnsignedShort = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: CUnsignedShort): CUnsignedShort =
    atomic_sub_ushort(atm, value)

  def andFetch(value: CUnsignedShort): CUnsignedShort = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: CUnsignedShort): CUnsignedShort =
    atomic_and_ushort(atm, value)

  def orFetch(value: CUnsignedShort): CUnsignedShort = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: CUnsignedShort): CUnsignedShort =
    atomic_or_ushort(atm, value)

  def xorFetch(value: CUnsignedShort): CUnsignedShort = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: CUnsignedShort): CUnsignedShort =
    atomic_xor_ushort(atm, value)

  override def toString: String = load().toString

  override def equals(that: Any): Boolean = that match {
    case o: CAtomicUnsignedShort => o.load() == load()
    case o: CUnsignedShort       => load() == o
    case _                       => false
  }

}

object CAtomicUnsignedShort extends CAtomic {

  def apply(initValue: CUnsignedShort) = new CAtomicUnsignedShort(initValue)

  def apply() = new CAtomicUnsignedShort()

}

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 28)

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 34)

class CAtomicUnsignedInt(default: CUnsignedInt = 0.asInstanceOf[CUnsignedInt])
    extends CAtomic {

  private[this] val atm =
    Atomic.alloc(sizeof[CUnsignedInt]).cast[Ptr[CUnsignedInt]]
  init_uint(atm, default)

  def load(): CUnsignedInt = load_uint(atm)

  def store(value: CUnsignedInt): Unit = store_uint(atm, value)

  def free(): Unit = Atomic.free(atm.cast[Ptr[Byte]])

  def compareAndSwapStrong(expected: CUnsignedInt,
                           desired: CUnsignedInt): (Boolean, CUnsignedInt) = {
    val expectedPtr = stackalloc[CUnsignedInt]
    !expectedPtr = expected

    if (compare_and_swap_strong_uint(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: CUnsignedInt,
                         desired: CUnsignedInt): (Boolean, CUnsignedInt) = {
    val expectedPtr = stackalloc[CUnsignedInt]
    !expectedPtr = expected

    if (compare_and_swap_weak_uint(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: CUnsignedInt): CUnsignedInt = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: CUnsignedInt): CUnsignedInt = atomic_add_uint(atm, value)

  def subFetch(value: CUnsignedInt): CUnsignedInt = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: CUnsignedInt): CUnsignedInt = atomic_sub_uint(atm, value)

  def andFetch(value: CUnsignedInt): CUnsignedInt = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: CUnsignedInt): CUnsignedInt = atomic_and_uint(atm, value)

  def orFetch(value: CUnsignedInt): CUnsignedInt = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: CUnsignedInt): CUnsignedInt = atomic_or_uint(atm, value)

  def xorFetch(value: CUnsignedInt): CUnsignedInt = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: CUnsignedInt): CUnsignedInt = atomic_xor_uint(atm, value)

  override def toString: String = load().toString

  override def equals(that: Any): Boolean = that match {
    case o: CAtomicUnsignedInt => o.load() == load()
    case o: CUnsignedInt       => load() == o
    case _                     => false
  }

}

object CAtomicUnsignedInt extends CAtomic {

  def apply(initValue: CUnsignedInt) = new CAtomicUnsignedInt(initValue)

  def apply() = new CAtomicUnsignedInt()

}

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 28)

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 34)

class CAtomicUnsignedLong(
    default: CUnsignedLong = 0.asInstanceOf[CUnsignedLong])
    extends CAtomic {

  private[this] val atm =
    Atomic.alloc(sizeof[CUnsignedLong]).cast[Ptr[CUnsignedLong]]
  init_ulong(atm, default)

  def load(): CUnsignedLong = load_ulong(atm)

  def store(value: CUnsignedLong): Unit = store_ulong(atm, value)

  def free(): Unit = Atomic.free(atm.cast[Ptr[Byte]])

  def compareAndSwapStrong(expected: CUnsignedLong,
                           desired: CUnsignedLong): (Boolean, CUnsignedLong) = {
    val expectedPtr = stackalloc[CUnsignedLong]
    !expectedPtr = expected

    if (compare_and_swap_strong_ulong(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: CUnsignedLong,
                         desired: CUnsignedLong): (Boolean, CUnsignedLong) = {
    val expectedPtr = stackalloc[CUnsignedLong]
    !expectedPtr = expected

    if (compare_and_swap_weak_ulong(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: CUnsignedLong): CUnsignedLong = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: CUnsignedLong): CUnsignedLong =
    atomic_add_ulong(atm, value)

  def subFetch(value: CUnsignedLong): CUnsignedLong = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: CUnsignedLong): CUnsignedLong =
    atomic_sub_ulong(atm, value)

  def andFetch(value: CUnsignedLong): CUnsignedLong = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: CUnsignedLong): CUnsignedLong =
    atomic_and_ulong(atm, value)

  def orFetch(value: CUnsignedLong): CUnsignedLong = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: CUnsignedLong): CUnsignedLong =
    atomic_or_ulong(atm, value)

  def xorFetch(value: CUnsignedLong): CUnsignedLong = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: CUnsignedLong): CUnsignedLong =
    atomic_xor_ulong(atm, value)

  override def toString: String = load().toString

  override def equals(that: Any): Boolean = that match {
    case o: CAtomicUnsignedLong => o.load() == load()
    case o: CUnsignedLong       => load() == o
    case _                      => false
  }

}

object CAtomicUnsignedLong extends CAtomic {

  def apply(initValue: CUnsignedLong) = new CAtomicUnsignedLong(initValue)

  def apply() = new CAtomicUnsignedLong()

}

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 28)

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 34)

class CAtomicChar(default: CChar = 'a'.asInstanceOf[CChar]) extends CAtomic {

  private[this] val atm = Atomic.alloc(sizeof[CChar]).cast[Ptr[CChar]]
  init_char(atm, default)

  def load(): CChar = load_char(atm)

  def store(value: CChar): Unit = store_char(atm, value)

  def free(): Unit = Atomic.free(atm.cast[Ptr[Byte]])

  def compareAndSwapStrong(expected: CChar,
                           desired: CChar): (Boolean, CChar) = {
    val expectedPtr = stackalloc[CChar]
    !expectedPtr = expected

    if (compare_and_swap_strong_char(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: CChar, desired: CChar): (Boolean, CChar) = {
    val expectedPtr = stackalloc[CChar]
    !expectedPtr = expected

    if (compare_and_swap_weak_char(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: CChar): CChar = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: CChar): CChar = atomic_add_char(atm, value)

  def subFetch(value: CChar): CChar = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: CChar): CChar = atomic_sub_char(atm, value)

  def andFetch(value: CChar): CChar = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: CChar): CChar = atomic_and_char(atm, value)

  def orFetch(value: CChar): CChar = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: CChar): CChar = atomic_or_char(atm, value)

  def xorFetch(value: CChar): CChar = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: CChar): CChar = atomic_xor_char(atm, value)

  override def toString: String = load().toString

  override def equals(that: Any): Boolean = that match {
    case o: CAtomicChar => o.load() == load()
    case o: CChar       => load() == o
    case _              => false
  }

}

object CAtomicChar extends CAtomic {

  def apply(initValue: CChar) = new CAtomicChar(initValue)

  def apply() = new CAtomicChar()

}

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 28)

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 34)

class CAtomicUnsignedChar(
    default: CUnsignedChar = 'a'.asInstanceOf[CUnsignedChar])
    extends CAtomic {

  private[this] val atm =
    Atomic.alloc(sizeof[CUnsignedChar]).cast[Ptr[CUnsignedChar]]
  init_uchar(atm, default)

  def load(): CUnsignedChar = load_uchar(atm)

  def store(value: CUnsignedChar): Unit = store_uchar(atm, value)

  def free(): Unit = Atomic.free(atm.cast[Ptr[Byte]])

  def compareAndSwapStrong(expected: CUnsignedChar,
                           desired: CUnsignedChar): (Boolean, CUnsignedChar) = {
    val expectedPtr = stackalloc[CUnsignedChar]
    !expectedPtr = expected

    if (compare_and_swap_strong_uchar(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: CUnsignedChar,
                         desired: CUnsignedChar): (Boolean, CUnsignedChar) = {
    val expectedPtr = stackalloc[CUnsignedChar]
    !expectedPtr = expected

    if (compare_and_swap_weak_uchar(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: CUnsignedChar): CUnsignedChar = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: CUnsignedChar): CUnsignedChar =
    atomic_add_uchar(atm, value)

  def subFetch(value: CUnsignedChar): CUnsignedChar = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: CUnsignedChar): CUnsignedChar =
    atomic_sub_uchar(atm, value)

  def andFetch(value: CUnsignedChar): CUnsignedChar = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: CUnsignedChar): CUnsignedChar =
    atomic_and_uchar(atm, value)

  def orFetch(value: CUnsignedChar): CUnsignedChar = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: CUnsignedChar): CUnsignedChar =
    atomic_or_uchar(atm, value)

  def xorFetch(value: CUnsignedChar): CUnsignedChar = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: CUnsignedChar): CUnsignedChar =
    atomic_xor_uchar(atm, value)

  override def toString: String = load().toString

  override def equals(that: Any): Boolean = that match {
    case o: CAtomicUnsignedChar => o.load() == load()
    case o: CUnsignedChar       => load() == o
    case _                      => false
  }

}

object CAtomicUnsignedChar extends CAtomic {

  def apply(initValue: CUnsignedChar) = new CAtomicUnsignedChar(initValue)

  def apply() = new CAtomicUnsignedChar()

}

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 28)

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 34)

class CAtomicCSize(default: CSize = 0.asInstanceOf[CSize]) extends CAtomic {

  private[this] val atm = Atomic.alloc(sizeof[CSize]).cast[Ptr[CSize]]
  init_csize(atm, default)

  def load(): CSize = load_csize(atm)

  def store(value: CSize): Unit = store_csize(atm, value)

  def free(): Unit = Atomic.free(atm.cast[Ptr[Byte]])

  def compareAndSwapStrong(expected: CSize,
                           desired: CSize): (Boolean, CSize) = {
    val expectedPtr = stackalloc[CSize]
    !expectedPtr = expected

    if (compare_and_swap_strong_csize(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: CSize, desired: CSize): (Boolean, CSize) = {
    val expectedPtr = stackalloc[CSize]
    !expectedPtr = expected

    if (compare_and_swap_weak_csize(atm, expectedPtr, desired)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: CSize): CSize = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: CSize): CSize = atomic_add_csize(atm, value)

  def subFetch(value: CSize): CSize = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: CSize): CSize = atomic_sub_csize(atm, value)

  def andFetch(value: CSize): CSize = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: CSize): CSize = atomic_and_csize(atm, value)

  def orFetch(value: CSize): CSize = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: CSize): CSize = atomic_or_csize(atm, value)

  def xorFetch(value: CSize): CSize = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: CSize): CSize = atomic_xor_csize(atm, value)

  override def toString: String = load().toString

  override def equals(that: Any): Boolean = that match {
    case o: CAtomicCSize => o.load() == load()
    case o: CSize        => load() == o
    case _               => false
  }

}

object CAtomicCSize extends CAtomic {

  def apply(initValue: CSize) = new CAtomicCSize(initValue)

  def apply() = new CAtomicCSize()

}

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 122)

class CAtomicRef[T <: AnyRef](default: T = 0L.asInstanceOf[T])
    extends CAtomicLong(default.asInstanceOf[Long]) {}

object CAtomicRef extends CAtomic {

  def apply[T <: AnyRef](initValue: T) = new CAtomicRef[T](initValue)

  def apply[T <: AnyRef]() = new CAtomicRef[T]()

}

// Helper object, can be imported for ease of use
object CAtomicsImplicits {

  implicit def toLong[T <: AnyRef](r: T): CLong = r.asInstanceOf[CLong]
  implicit def toRef[T <: AnyRef](l: CLong): T  = l.asInstanceOf[T]
  implicit def underlying[T <: AnyRef](a: CAtomicRef[T]): T =
    a.load().asInstanceOf[T]
  implicit def cas[T](v: (Boolean, T)): Boolean = v._1
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 141)
  implicit def underlying(a: CAtomicByte): Byte = a.load()
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 141)
  implicit def underlying(a: CAtomicShort): CShort = a.load()
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 141)
  implicit def underlying(a: CAtomicInt): CInt = a.load()
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 141)
  implicit def underlying(a: CAtomicLong): CLong = a.load()
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 141)
  implicit def underlying(a: CAtomicUnsignedByte): Byte = a.load()
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 141)
  implicit def underlying(a: CAtomicUnsignedShort): CUnsignedShort = a.load()
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 141)
  implicit def underlying(a: CAtomicUnsignedInt): CUnsignedInt = a.load()
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 141)
  implicit def underlying(a: CAtomicUnsignedLong): CUnsignedLong = a.load()
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 141)
  implicit def underlying(a: CAtomicChar): CChar = a.load()
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 141)
  implicit def underlying(a: CAtomicUnsignedChar): CUnsignedChar = a.load()
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 141)
  implicit def underlying(a: CAtomicCSize): CSize = a.load()
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/CAtomics.scala.gyb", line: 143)

}
