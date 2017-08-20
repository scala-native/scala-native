package java.util.zip

// Ported from Apache Harmony

object CRC32Suite extends tests.Suite {

  test("Constructor()") {
    val crc = new CRC32()
    assert(crc.getValue() == 0)
  }

  test("getValue()") {
    val crc = new CRC32()
    assert(crc.getValue() == 0)

    crc.reset()
    crc.update(Int.MaxValue)
    assert(crc.getValue() == 4278190080L)

    crc.reset()
    val byteEmpty = new Array[Byte](10000)
    crc.update(byteEmpty)
    assert(crc.getValue() == 1295764014L)

    crc.reset()
    crc.update(1)
    assert(crc.getValue() == 2768625435L)

    crc.reset()
    assert(crc.getValue() == 0)
  }

  test("update(Array[Byte])") {
    val byteArray = Array[Byte](1, 2)
    val crc       = new CRC32()
    crc.update(byteArray)
    assert(crc.getValue() == 3066839698L)

    crc.reset()
    val empty = new Array[Byte](10000)
    crc.update(empty)
    assert(crc.getValue() == 1295764014L)
  }

  test("update(Array[Byte], Int, Int)") {
    val byteArray = Array[Byte](1, 2, 3)
    val crc       = new CRC32()
    val off       = 2
    val len       = 1
    val lenError  = 3
    val offError  = 4
    crc.update(byteArray, off, len)
    assert(crc.getValue() == 1259060791L)

    assertThrows[ArrayIndexOutOfBoundsException] {
      crc.update(byteArray, off, lenError)
    }

    assertThrows[ArrayIndexOutOfBoundsException] {
      crc.update(byteArray, offError, len)
    }
  }

}
