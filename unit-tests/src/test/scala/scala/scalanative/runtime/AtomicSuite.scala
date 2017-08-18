// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 1)
package scala.scalanative
package runtime

import runtime._
import native._
import stdlib._

object AtomicSuite extends tests.Suite {

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 22)

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 29)

  test("compare and swap Strong byte") {
    val a = CAtomicByte()

    val b = 3.asInstanceOf[Byte]

    assertNot(
      a.compareAndSwapStrong(1.asInstanceOf[Byte], 3.asInstanceOf[Byte])._1)

    assert(
      a.compareAndSwapStrong(0.asInstanceOf[Byte], 3.asInstanceOf[Byte])
        ._2 == 3.asInstanceOf[Byte])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 29)

  test("compare and swap Weak byte") {
    val a = CAtomicByte()

    val b = 3.asInstanceOf[Byte]

    assertNot(
      a.compareAndSwapWeak(1.asInstanceOf[Byte], 3.asInstanceOf[Byte])._1)

    assert(
      a.compareAndSwapWeak(0.asInstanceOf[Byte], 3.asInstanceOf[Byte])._2 == 3
        .asInstanceOf[Byte])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 42)

  test("load and store byte") {
    val a = CAtomicByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.load() == 0.asInstanceOf[Byte])

    a.store(b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_add byte") {
    val a = CAtomicByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.fetchAdd(b) == 0.asInstanceOf[Byte])

    assert(a.load() == b)

    a.free()
  }

  test("add_fetch byte") {
    val a = CAtomicByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.addFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_sub byte") {
    val a = CAtomicByte(1.asInstanceOf[Byte])

    val b = 1.asInstanceOf[Byte]

    assert(a.fetchSub(b) == b)

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }

  test("sub_fetch byte") {
    val a = CAtomicByte(1.asInstanceOf[Byte])

    val b = 1.asInstanceOf[Byte]

    assert(a.subFetch(b) == 0.asInstanceOf[Byte])

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }

  test("fetch_and byte") {
    val a = CAtomicByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.fetchAnd(b) == 0.asInstanceOf[Byte])

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }

  test("and_fetch byte") {
    val a = CAtomicByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.andFetch(b) == 0.asInstanceOf[Byte])

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }

  test("fetch_or byte") {
    val a = CAtomicByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.fetchOr(b) == 0.asInstanceOf[Byte])

    assert(a.load() == b)

    a.free()
  }

  test("or_fetch byte") {
    val a = CAtomicByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.orFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_xor byte") {
    val a = CAtomicByte(1.asInstanceOf[Byte])

    val b = 1.asInstanceOf[Byte]

    assert(a.fetchXor(b) == 1.asInstanceOf[Byte])

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }

  test("xor_fetch byte") {
    val a = CAtomicByte(1.asInstanceOf[Byte])

    val b = 1.asInstanceOf[Byte]

    assert(a.xorFetch(b) == 0.asInstanceOf[Byte])

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 29)

  test("compare and swap Strong short") {
    val a = CAtomicShort()

    val b = 3.asInstanceOf[CShort]

    assertNot(
      a.compareAndSwapStrong(1.asInstanceOf[CShort], 3.asInstanceOf[CShort])
        ._1)

    assert(
      a.compareAndSwapStrong(0.asInstanceOf[CShort], 3.asInstanceOf[CShort])
        ._2 == 3.asInstanceOf[CShort])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 29)

  test("compare and swap Weak short") {
    val a = CAtomicShort()

    val b = 3.asInstanceOf[CShort]

    assertNot(
      a.compareAndSwapWeak(1.asInstanceOf[CShort], 3.asInstanceOf[CShort])._1)

    assert(
      a.compareAndSwapWeak(0.asInstanceOf[CShort], 3.asInstanceOf[CShort])
        ._2 == 3.asInstanceOf[CShort])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 42)

  test("load and store short") {
    val a = CAtomicShort()

    val b = 1.asInstanceOf[CShort]

    assert(a.load() == 0.asInstanceOf[CShort])

    a.store(b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_add short") {
    val a = CAtomicShort()

    val b = 1.asInstanceOf[CShort]

    assert(a.fetchAdd(b) == 0.asInstanceOf[CShort])

    assert(a.load() == b)

    a.free()
  }

  test("add_fetch short") {
    val a = CAtomicShort()

    val b = 1.asInstanceOf[CShort]

    assert(a.addFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_sub short") {
    val a = CAtomicShort(1.asInstanceOf[CShort])

    val b = 1.asInstanceOf[CShort]

    assert(a.fetchSub(b) == b)

    assert(a.load() == 0.asInstanceOf[CShort])

    a.free()
  }

  test("sub_fetch short") {
    val a = CAtomicShort(1.asInstanceOf[CShort])

    val b = 1.asInstanceOf[CShort]

    assert(a.subFetch(b) == 0.asInstanceOf[CShort])

    assert(a.load() == 0.asInstanceOf[CShort])

    a.free()
  }

  test("fetch_and short") {
    val a = CAtomicShort()

    val b = 1.asInstanceOf[CShort]

    assert(a.fetchAnd(b) == 0.asInstanceOf[CShort])

    assert(a.load() == 0.asInstanceOf[CShort])

    a.free()
  }

  test("and_fetch short") {
    val a = CAtomicShort()

    val b = 1.asInstanceOf[CShort]

    assert(a.andFetch(b) == 0.asInstanceOf[CShort])

    assert(a.load() == 0.asInstanceOf[CShort])

    a.free()
  }

  test("fetch_or short") {
    val a = CAtomicShort()

    val b = 1.asInstanceOf[CShort]

    assert(a.fetchOr(b) == 0.asInstanceOf[CShort])

    assert(a.load() == b)

    a.free()
  }

  test("or_fetch short") {
    val a = CAtomicShort()

    val b = 1.asInstanceOf[CShort]

    assert(a.orFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_xor short") {
    val a = CAtomicShort(1.asInstanceOf[CShort])

    val b = 1.asInstanceOf[CShort]

    assert(a.fetchXor(b) == 1.asInstanceOf[CShort])

    assert(a.load() == 0.asInstanceOf[CShort])

    a.free()
  }

  test("xor_fetch short") {
    val a = CAtomicShort(1.asInstanceOf[CShort])

    val b = 1.asInstanceOf[CShort]

    assert(a.xorFetch(b) == 0.asInstanceOf[CShort])

    assert(a.load() == 0.asInstanceOf[CShort])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 29)

  test("compare and swap Strong int") {
    val a = CAtomicInt()

    val b = 3

    assertNot(a.compareAndSwapStrong(1, 3)._1)

    assert(a.compareAndSwapStrong(0, 3)._2 == 3)

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 29)

  test("compare and swap Weak int") {
    val a = CAtomicInt()

    val b = 3

    assertNot(a.compareAndSwapWeak(1, 3)._1)

    assert(a.compareAndSwapWeak(0, 3)._2 == 3)

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 42)

  test("load and store int") {
    val a = CAtomicInt()

    val b = 1

    assert(a.load() == 0)

    a.store(b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_add int") {
    val a = CAtomicInt()

    val b = 1

    assert(a.fetchAdd(b) == 0)

    assert(a.load() == b)

    a.free()
  }

  test("add_fetch int") {
    val a = CAtomicInt()

    val b = 1

    assert(a.addFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_sub int") {
    val a = CAtomicInt(1)

    val b = 1

    assert(a.fetchSub(b) == b)

    assert(a.load() == 0)

    a.free()
  }

  test("sub_fetch int") {
    val a = CAtomicInt(1)

    val b = 1

    assert(a.subFetch(b) == 0)

    assert(a.load() == 0)

    a.free()
  }

  test("fetch_and int") {
    val a = CAtomicInt()

    val b = 1

    assert(a.fetchAnd(b) == 0)

    assert(a.load() == 0)

    a.free()
  }

  test("and_fetch int") {
    val a = CAtomicInt()

    val b = 1

    assert(a.andFetch(b) == 0)

    assert(a.load() == 0)

    a.free()
  }

  test("fetch_or int") {
    val a = CAtomicInt()

    val b = 1

    assert(a.fetchOr(b) == 0)

    assert(a.load() == b)

    a.free()
  }

  test("or_fetch int") {
    val a = CAtomicInt()

    val b = 1

    assert(a.orFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_xor int") {
    val a = CAtomicInt(1)

    val b = 1

    assert(a.fetchXor(b) == 1)

    assert(a.load() == 0)

    a.free()
  }

  test("xor_fetch int") {
    val a = CAtomicInt(1)

    val b = 1

    assert(a.xorFetch(b) == 0)

    assert(a.load() == 0)

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 29)

  test("compare and swap Strong long") {
    val a = CAtomicLong()

    val b = 3.asInstanceOf[CLong]

    assertNot(
      a.compareAndSwapStrong(1.asInstanceOf[CLong], 3.asInstanceOf[CLong])._1)

    assert(
      a.compareAndSwapStrong(0.asInstanceOf[CLong], 3.asInstanceOf[CLong])
        ._2 == 3.asInstanceOf[CLong])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 29)

  test("compare and swap Weak long") {
    val a = CAtomicLong()

    val b = 3.asInstanceOf[CLong]

    assertNot(
      a.compareAndSwapWeak(1.asInstanceOf[CLong], 3.asInstanceOf[CLong])._1)

    assert(
      a.compareAndSwapWeak(0.asInstanceOf[CLong], 3.asInstanceOf[CLong])
        ._2 == 3.asInstanceOf[CLong])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 42)

  test("load and store long") {
    val a = CAtomicLong()

    val b = 1.asInstanceOf[CLong]

    assert(a.load() == 0.asInstanceOf[CLong])

    a.store(b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_add long") {
    val a = CAtomicLong()

    val b = 1.asInstanceOf[CLong]

    assert(a.fetchAdd(b) == 0.asInstanceOf[CLong])

    assert(a.load() == b)

    a.free()
  }

  test("add_fetch long") {
    val a = CAtomicLong()

    val b = 1.asInstanceOf[CLong]

    assert(a.addFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_sub long") {
    val a = CAtomicLong(1.asInstanceOf[CLong])

    val b = 1.asInstanceOf[CLong]

    assert(a.fetchSub(b) == b)

    assert(a.load() == 0.asInstanceOf[CLong])

    a.free()
  }

  test("sub_fetch long") {
    val a = CAtomicLong(1.asInstanceOf[CLong])

    val b = 1.asInstanceOf[CLong]

    assert(a.subFetch(b) == 0.asInstanceOf[CLong])

    assert(a.load() == 0.asInstanceOf[CLong])

    a.free()
  }

  test("fetch_and long") {
    val a = CAtomicLong()

    val b = 1.asInstanceOf[CLong]

    assert(a.fetchAnd(b) == 0.asInstanceOf[CLong])

    assert(a.load() == 0.asInstanceOf[CLong])

    a.free()
  }

  test("and_fetch long") {
    val a = CAtomicLong()

    val b = 1.asInstanceOf[CLong]

    assert(a.andFetch(b) == 0.asInstanceOf[CLong])

    assert(a.load() == 0.asInstanceOf[CLong])

    a.free()
  }

  test("fetch_or long") {
    val a = CAtomicLong()

    val b = 1.asInstanceOf[CLong]

    assert(a.fetchOr(b) == 0.asInstanceOf[CLong])

    assert(a.load() == b)

    a.free()
  }

  test("or_fetch long") {
    val a = CAtomicLong()

    val b = 1.asInstanceOf[CLong]

    assert(a.orFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_xor long") {
    val a = CAtomicLong(1.asInstanceOf[CLong])

    val b = 1.asInstanceOf[CLong]

    assert(a.fetchXor(b) == 1.asInstanceOf[CLong])

    assert(a.load() == 0.asInstanceOf[CLong])

    a.free()
  }

  test("xor_fetch long") {
    val a = CAtomicLong(1.asInstanceOf[CLong])

    val b = 1.asInstanceOf[CLong]

    assert(a.xorFetch(b) == 0.asInstanceOf[CLong])

    assert(a.load() == 0.asInstanceOf[CLong])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 29)

  test("compare and swap Strong ubyte") {
    val a = CAtomicUnsignedByte()

    val b = 3.asInstanceOf[Byte]

    assertNot(
      a.compareAndSwapStrong(1.asInstanceOf[Byte], 3.asInstanceOf[Byte])._1)

    assert(
      a.compareAndSwapStrong(0.asInstanceOf[Byte], 3.asInstanceOf[Byte])
        ._2 == 3.asInstanceOf[Byte])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 29)

  test("compare and swap Weak ubyte") {
    val a = CAtomicUnsignedByte()

    val b = 3.asInstanceOf[Byte]

    assertNot(
      a.compareAndSwapWeak(1.asInstanceOf[Byte], 3.asInstanceOf[Byte])._1)

    assert(
      a.compareAndSwapWeak(0.asInstanceOf[Byte], 3.asInstanceOf[Byte])._2 == 3
        .asInstanceOf[Byte])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 42)

  test("load and store ubyte") {
    val a = CAtomicUnsignedByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.load() == 0.asInstanceOf[Byte])

    a.store(b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_add ubyte") {
    val a = CAtomicUnsignedByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.fetchAdd(b) == 0.asInstanceOf[Byte])

    assert(a.load() == b)

    a.free()
  }

  test("add_fetch ubyte") {
    val a = CAtomicUnsignedByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.addFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_sub ubyte") {
    val a = CAtomicUnsignedByte(1.asInstanceOf[Byte])

    val b = 1.asInstanceOf[Byte]

    assert(a.fetchSub(b) == b)

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }

  test("sub_fetch ubyte") {
    val a = CAtomicUnsignedByte(1.asInstanceOf[Byte])

    val b = 1.asInstanceOf[Byte]

    assert(a.subFetch(b) == 0.asInstanceOf[Byte])

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }

  test("fetch_and ubyte") {
    val a = CAtomicUnsignedByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.fetchAnd(b) == 0.asInstanceOf[Byte])

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }

  test("and_fetch ubyte") {
    val a = CAtomicUnsignedByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.andFetch(b) == 0.asInstanceOf[Byte])

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }

  test("fetch_or ubyte") {
    val a = CAtomicUnsignedByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.fetchOr(b) == 0.asInstanceOf[Byte])

    assert(a.load() == b)

    a.free()
  }

  test("or_fetch ubyte") {
    val a = CAtomicUnsignedByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.orFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_xor ubyte") {
    val a = CAtomicUnsignedByte(1.asInstanceOf[Byte])

    val b = 1.asInstanceOf[Byte]

    assert(a.fetchXor(b) == 1.asInstanceOf[Byte])

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }

  test("xor_fetch ubyte") {
    val a = CAtomicUnsignedByte(1.asInstanceOf[Byte])

    val b = 1.asInstanceOf[Byte]

    assert(a.xorFetch(b) == 0.asInstanceOf[Byte])

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 29)

  test("compare and swap Strong ushort") {
    val a = CAtomicUnsignedShort()

    val b = 3.asInstanceOf[CUnsignedShort]

    assertNot(
      a.compareAndSwapStrong(1.asInstanceOf[CUnsignedShort],
                              3.asInstanceOf[CUnsignedShort])
        ._1)

    assert(
      a.compareAndSwapStrong(0.asInstanceOf[CUnsignedShort],
                              3.asInstanceOf[CUnsignedShort])
        ._2 == 3.asInstanceOf[CUnsignedShort])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 29)

  test("compare and swap Weak ushort") {
    val a = CAtomicUnsignedShort()

    val b = 3.asInstanceOf[CUnsignedShort]

    assertNot(
      a.compareAndSwapWeak(1.asInstanceOf[CUnsignedShort],
                            3.asInstanceOf[CUnsignedShort])
        ._1)

    assert(
      a.compareAndSwapWeak(0.asInstanceOf[CUnsignedShort],
                            3.asInstanceOf[CUnsignedShort])
        ._2 == 3.asInstanceOf[CUnsignedShort])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 42)

  test("load and store ushort") {
    val a = CAtomicUnsignedShort()

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.load() == 0.asInstanceOf[CUnsignedShort])

    a.store(b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_add ushort") {
    val a = CAtomicUnsignedShort()

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.fetchAdd(b) == 0.asInstanceOf[CUnsignedShort])

    assert(a.load() == b)

    a.free()
  }

  test("add_fetch ushort") {
    val a = CAtomicUnsignedShort()

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.addFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_sub ushort") {
    val a = CAtomicUnsignedShort(1.asInstanceOf[CUnsignedShort])

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.fetchSub(b) == b)

    assert(a.load() == 0.asInstanceOf[CUnsignedShort])

    a.free()
  }

  test("sub_fetch ushort") {
    val a = CAtomicUnsignedShort(1.asInstanceOf[CUnsignedShort])

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.subFetch(b) == 0.asInstanceOf[CUnsignedShort])

    assert(a.load() == 0.asInstanceOf[CUnsignedShort])

    a.free()
  }

  test("fetch_and ushort") {
    val a = CAtomicUnsignedShort()

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.fetchAnd(b) == 0.asInstanceOf[CUnsignedShort])

    assert(a.load() == 0.asInstanceOf[CUnsignedShort])

    a.free()
  }

  test("and_fetch ushort") {
    val a = CAtomicUnsignedShort()

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.andFetch(b) == 0.asInstanceOf[CUnsignedShort])

    assert(a.load() == 0.asInstanceOf[CUnsignedShort])

    a.free()
  }

  test("fetch_or ushort") {
    val a = CAtomicUnsignedShort()

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.fetchOr(b) == 0.asInstanceOf[CUnsignedShort])

    assert(a.load() == b)

    a.free()
  }

  test("or_fetch ushort") {
    val a = CAtomicUnsignedShort()

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.orFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_xor ushort") {
    val a = CAtomicUnsignedShort(1.asInstanceOf[CUnsignedShort])

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.fetchXor(b) == 1.asInstanceOf[CUnsignedShort])

    assert(a.load() == 0.asInstanceOf[CUnsignedShort])

    a.free()
  }

  test("xor_fetch ushort") {
    val a = CAtomicUnsignedShort(1.asInstanceOf[CUnsignedShort])

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.xorFetch(b) == 0.asInstanceOf[CUnsignedShort])

    assert(a.load() == 0.asInstanceOf[CUnsignedShort])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 29)

  test("compare and swap Strong uint") {
    val a = CAtomicUnsignedInt()

    val b = 3.asInstanceOf[CUnsignedInt]

    assertNot(
      a.compareAndSwapStrong(1.asInstanceOf[CUnsignedInt],
                              3.asInstanceOf[CUnsignedInt])
        ._1)

    assert(
      a.compareAndSwapStrong(0.asInstanceOf[CUnsignedInt],
                              3.asInstanceOf[CUnsignedInt])
        ._2 == 3.asInstanceOf[CUnsignedInt])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 29)

  test("compare and swap Weak uint") {
    val a = CAtomicUnsignedInt()

    val b = 3.asInstanceOf[CUnsignedInt]

    assertNot(
      a.compareAndSwapWeak(1.asInstanceOf[CUnsignedInt],
                            3.asInstanceOf[CUnsignedInt])
        ._1)

    assert(
      a.compareAndSwapWeak(0.asInstanceOf[CUnsignedInt],
                            3.asInstanceOf[CUnsignedInt])
        ._2 == 3.asInstanceOf[CUnsignedInt])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 42)

  test("load and store uint") {
    val a = CAtomicUnsignedInt()

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.load() == 0.asInstanceOf[CUnsignedInt])

    a.store(b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_add uint") {
    val a = CAtomicUnsignedInt()

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.fetchAdd(b) == 0.asInstanceOf[CUnsignedInt])

    assert(a.load() == b)

    a.free()
  }

  test("add_fetch uint") {
    val a = CAtomicUnsignedInt()

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.addFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_sub uint") {
    val a = CAtomicUnsignedInt(1.asInstanceOf[CUnsignedInt])

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.fetchSub(b) == b)

    assert(a.load() == 0.asInstanceOf[CUnsignedInt])

    a.free()
  }

  test("sub_fetch uint") {
    val a = CAtomicUnsignedInt(1.asInstanceOf[CUnsignedInt])

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.subFetch(b) == 0.asInstanceOf[CUnsignedInt])

    assert(a.load() == 0.asInstanceOf[CUnsignedInt])

    a.free()
  }

  test("fetch_and uint") {
    val a = CAtomicUnsignedInt()

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.fetchAnd(b) == 0.asInstanceOf[CUnsignedInt])

    assert(a.load() == 0.asInstanceOf[CUnsignedInt])

    a.free()
  }

  test("and_fetch uint") {
    val a = CAtomicUnsignedInt()

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.andFetch(b) == 0.asInstanceOf[CUnsignedInt])

    assert(a.load() == 0.asInstanceOf[CUnsignedInt])

    a.free()
  }

  test("fetch_or uint") {
    val a = CAtomicUnsignedInt()

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.fetchOr(b) == 0.asInstanceOf[CUnsignedInt])

    assert(a.load() == b)

    a.free()
  }

  test("or_fetch uint") {
    val a = CAtomicUnsignedInt()

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.orFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_xor uint") {
    val a = CAtomicUnsignedInt(1.asInstanceOf[CUnsignedInt])

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.fetchXor(b) == 1.asInstanceOf[CUnsignedInt])

    assert(a.load() == 0.asInstanceOf[CUnsignedInt])

    a.free()
  }

  test("xor_fetch uint") {
    val a = CAtomicUnsignedInt(1.asInstanceOf[CUnsignedInt])

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.xorFetch(b) == 0.asInstanceOf[CUnsignedInt])

    assert(a.load() == 0.asInstanceOf[CUnsignedInt])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 29)

  test("compare and swap Strong ulong") {
    val a = CAtomicUnsignedLong()

    val b = 3.asInstanceOf[CUnsignedLong]

    assertNot(
      a.compareAndSwapStrong(1.asInstanceOf[CUnsignedLong],
                              3.asInstanceOf[CUnsignedLong])
        ._1)

    assert(
      a.compareAndSwapStrong(0.asInstanceOf[CUnsignedLong],
                              3.asInstanceOf[CUnsignedLong])
        ._2 == 3.asInstanceOf[CUnsignedLong])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 29)

  test("compare and swap Weak ulong") {
    val a = CAtomicUnsignedLong()

    val b = 3.asInstanceOf[CUnsignedLong]

    assertNot(
      a.compareAndSwapWeak(1.asInstanceOf[CUnsignedLong],
                            3.asInstanceOf[CUnsignedLong])
        ._1)

    assert(
      a.compareAndSwapWeak(0.asInstanceOf[CUnsignedLong],
                            3.asInstanceOf[CUnsignedLong])
        ._2 == 3.asInstanceOf[CUnsignedLong])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 42)

  test("load and store ulong") {
    val a = CAtomicUnsignedLong()

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.load() == 0.asInstanceOf[CUnsignedLong])

    a.store(b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_add ulong") {
    val a = CAtomicUnsignedLong()

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.fetchAdd(b) == 0.asInstanceOf[CUnsignedLong])

    assert(a.load() == b)

    a.free()
  }

  test("add_fetch ulong") {
    val a = CAtomicUnsignedLong()

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.addFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_sub ulong") {
    val a = CAtomicUnsignedLong(1.asInstanceOf[CUnsignedLong])

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.fetchSub(b) == b)

    assert(a.load() == 0.asInstanceOf[CUnsignedLong])

    a.free()
  }

  test("sub_fetch ulong") {
    val a = CAtomicUnsignedLong(1.asInstanceOf[CUnsignedLong])

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.subFetch(b) == 0.asInstanceOf[CUnsignedLong])

    assert(a.load() == 0.asInstanceOf[CUnsignedLong])

    a.free()
  }

  test("fetch_and ulong") {
    val a = CAtomicUnsignedLong()

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.fetchAnd(b) == 0.asInstanceOf[CUnsignedLong])

    assert(a.load() == 0.asInstanceOf[CUnsignedLong])

    a.free()
  }

  test("and_fetch ulong") {
    val a = CAtomicUnsignedLong()

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.andFetch(b) == 0.asInstanceOf[CUnsignedLong])

    assert(a.load() == 0.asInstanceOf[CUnsignedLong])

    a.free()
  }

  test("fetch_or ulong") {
    val a = CAtomicUnsignedLong()

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.fetchOr(b) == 0.asInstanceOf[CUnsignedLong])

    assert(a.load() == b)

    a.free()
  }

  test("or_fetch ulong") {
    val a = CAtomicUnsignedLong()

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.orFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_xor ulong") {
    val a = CAtomicUnsignedLong(1.asInstanceOf[CUnsignedLong])

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.fetchXor(b) == 1.asInstanceOf[CUnsignedLong])

    assert(a.load() == 0.asInstanceOf[CUnsignedLong])

    a.free()
  }

  test("xor_fetch ulong") {
    val a = CAtomicUnsignedLong(1.asInstanceOf[CUnsignedLong])

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.xorFetch(b) == 0.asInstanceOf[CUnsignedLong])

    assert(a.load() == 0.asInstanceOf[CUnsignedLong])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 29)

  test("compare and swap Strong csize") {
    val a = CAtomicCSize()

    val b = 3.asInstanceOf[CSize]

    assertNot(
      a.compareAndSwapStrong(1.asInstanceOf[CSize], 3.asInstanceOf[CSize])._1)

    assert(
      a.compareAndSwapStrong(0.asInstanceOf[CSize], 3.asInstanceOf[CSize])
        ._2 == 3.asInstanceOf[CSize])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 29)

  test("compare and swap Weak csize") {
    val a = CAtomicCSize()

    val b = 3.asInstanceOf[CSize]

    assertNot(
      a.compareAndSwapWeak(1.asInstanceOf[CSize], 3.asInstanceOf[CSize])._1)

    assert(
      a.compareAndSwapWeak(0.asInstanceOf[CSize], 3.asInstanceOf[CSize])
        ._2 == 3.asInstanceOf[CSize])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 42)

  test("load and store csize") {
    val a = CAtomicCSize()

    val b = 1.asInstanceOf[CSize]

    assert(a.load() == 0.asInstanceOf[CSize])

    a.store(b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_add csize") {
    val a = CAtomicCSize()

    val b = 1.asInstanceOf[CSize]

    assert(a.fetchAdd(b) == 0.asInstanceOf[CSize])

    assert(a.load() == b)

    a.free()
  }

  test("add_fetch csize") {
    val a = CAtomicCSize()

    val b = 1.asInstanceOf[CSize]

    assert(a.addFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_sub csize") {
    val a = CAtomicCSize(1.asInstanceOf[CSize])

    val b = 1.asInstanceOf[CSize]

    assert(a.fetchSub(b) == b)

    assert(a.load() == 0.asInstanceOf[CSize])

    a.free()
  }

  test("sub_fetch csize") {
    val a = CAtomicCSize(1.asInstanceOf[CSize])

    val b = 1.asInstanceOf[CSize]

    assert(a.subFetch(b) == 0.asInstanceOf[CSize])

    assert(a.load() == 0.asInstanceOf[CSize])

    a.free()
  }

  test("fetch_and csize") {
    val a = CAtomicCSize()

    val b = 1.asInstanceOf[CSize]

    assert(a.fetchAnd(b) == 0.asInstanceOf[CSize])

    assert(a.load() == 0.asInstanceOf[CSize])

    a.free()
  }

  test("and_fetch csize") {
    val a = CAtomicCSize()

    val b = 1.asInstanceOf[CSize]

    assert(a.andFetch(b) == 0.asInstanceOf[CSize])

    assert(a.load() == 0.asInstanceOf[CSize])

    a.free()
  }

  test("fetch_or csize") {
    val a = CAtomicCSize()

    val b = 1.asInstanceOf[CSize]

    assert(a.fetchOr(b) == 0.asInstanceOf[CSize])

    assert(a.load() == b)

    a.free()
  }

  test("or_fetch csize") {
    val a = CAtomicCSize()

    val b = 1.asInstanceOf[CSize]

    assert(a.orFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  test("fetch_xor csize") {
    val a = CAtomicCSize(1.asInstanceOf[CSize])

    val b = 1.asInstanceOf[CSize]

    assert(a.fetchXor(b) == 1.asInstanceOf[CSize])

    assert(a.load() == 0.asInstanceOf[CSize])

    a.free()
  }

  test("xor_fetch csize") {
    val a = CAtomicCSize(1.asInstanceOf[CSize])

    val b = 1.asInstanceOf[CSize]

    assert(a.xorFetch(b) == 0.asInstanceOf[CSize])

    assert(a.load() == 0.asInstanceOf[CSize])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 177)

// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 181)

  test("compare and swap Strong Char") {
    val a = CAtomicChar()

    val b = 'b'.asInstanceOf[CChar]

    assertNot(
      a.compareAndSwapStrong('b'.asInstanceOf[CChar], 'b'.asInstanceOf[CChar])
        ._1)

    assert(
      a.compareAndSwapStrong('a'.asInstanceOf[CChar], 'b'.asInstanceOf[CChar])
        ._2 == 'b'.asInstanceOf[CChar])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 181)

  test("compare and swap Weak Char") {
    val a = CAtomicChar()

    val b = 'b'.asInstanceOf[CChar]

    assertNot(
      a.compareAndSwapWeak('b'.asInstanceOf[CChar], 'b'.asInstanceOf[CChar])
        ._1)

    assert(
      a.compareAndSwapWeak('a'.asInstanceOf[CChar], 'b'.asInstanceOf[CChar])
        ._2 == 'b'.asInstanceOf[CChar])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 181)

  test("compare and swap Strong UnsignedChar") {
    val a = CAtomicUnsignedChar()

    val b = 'b'.asInstanceOf[CUnsignedChar]

    assertNot(
      a.compareAndSwapStrong('b'.asInstanceOf[CUnsignedChar],
                              'b'.asInstanceOf[CUnsignedChar])
        ._1)

    assert(
      a.compareAndSwapStrong('a'.asInstanceOf[CUnsignedChar],
                              'b'.asInstanceOf[CUnsignedChar])
        ._2 == 'b'.asInstanceOf[CUnsignedChar])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 181)

  test("compare and swap Weak UnsignedChar") {
    val a = CAtomicUnsignedChar()

    val b = 'b'.asInstanceOf[CUnsignedChar]

    assertNot(
      a.compareAndSwapWeak('b'.asInstanceOf[CUnsignedChar],
                            'b'.asInstanceOf[CUnsignedChar])
        ._1)

    assert(
      a.compareAndSwapWeak('a'.asInstanceOf[CUnsignedChar],
                            'b'.asInstanceOf[CUnsignedChar])
        ._2 == 'b'.asInstanceOf[CUnsignedChar])

    a.free()
  }
// ###sourceLocation(file: "/home/remi/perso/Projects/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 195)

  test("multiple compare and swap should yield correct results") {
    val a = CAtomicInt()

    var i = 0

    while (i < 10) {
      assert(a.compareAndSwapStrong(i, i + 1)._2 == i + 1)
      i += 1
    }

    assert(a.load() == 10)

    a.free()
  }

  test("store behaves as expected") {
    val a = CAtomicInt()

    a.store(1)

    assert(a.load() == 1)

    a.free()
  }

  test("constructor with initial value") {
    val a = CAtomicLong(2.toLong)

    assert(a.load() == 2.toLong)

    a.free()
  }

}
