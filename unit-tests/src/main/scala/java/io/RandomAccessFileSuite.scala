package java.io

import scala.util.Try

object RandomAccessFileSuite extends tests.Suite {
  test(
    "Creating a `RandomAccessFile` with an invalid mode throws an exception") {
    assertThrows[IllegalArgumentException] {
      new RandomAccessFile("file", "foo")
    }
  }

  test(
    "Creating a `RandomAccessFile` with mode = `r` and a non-existing file should throw an exception") {
    val file = new File("i-dont-exist")
    assert(!file.exists)
    assertThrows[FileNotFoundException] {
      new RandomAccessFile(file, "r")
    }
  }

  test("valid file descriptor and sync success") {
    val file = File.createTempFile("raffdtest", "")
    val raf  = new RandomAccessFile(file, "r")
    val fd   = raf.getFD
    assert(fd.valid())
    assert(Try(fd.sync()).isSuccess)
  }

  testWithRAF("Can write and read a boolean") { raf =>
    raf.writeBoolean(true)
    raf.writeBoolean(false)
    raf.seek(0)
    assert(raf.readBoolean() == true)
    assert(raf.readBoolean() == false)
  }

  testWithRAF("Can write and read a Byte") { raf =>
    val value: Byte = 42
    raf.writeByte(value)
    raf.seek(0)
    assert(raf.readByte() == value)
  }

  testWithRAF("Can write and read an unsigned byte") { raf =>
    val value: Int = 0xFE
    raf.writeByte(value.toByte)
    raf.seek(0)
    assert(raf.readUnsignedByte() == value)
  }

  testWithRAF("Can write and read a Char") { raf =>
    val value = 'c'
    raf.writeChar(value)
    raf.seek(0)
    assert(raf.readChar() == value)
  }

  testWithRAF("Can write and read a Double") { raf =>
    val value: Double = 42.42
    raf.writeDouble(value)
    raf.seek(0)
    assert(raf.readDouble() == value)
  }

  testWithRAF("Can write and read a Float") { raf =>
    val value: Float = 42.42f
    raf.writeFloat(value)
    raf.seek(0)
    assert(raf.readFloat() == value)
  }

  testWithRAF("Can write and read an Int") { raf =>
    val value: Int = 42
    raf.writeInt(value)
    raf.seek(0)
    assert(raf.readInt() == value)
  }

  testWithRAF("Can write and read a whole line with terminator = '\\n'") {
    raf =>
      val line = "Hello, world!"
      raf.writeChars(line + '\n')
      raf.seek(0)
      assert(raf.readLine() == line)
  }

  testWithRAF("Can write and read a whole line with terminator = '\\r'") {
    raf =>
      val line = "Hello, world!"
      raf.writeChars(line + '\r')
      raf.seek(0)
      assert(raf.readLine() == line)
  }

  testWithRAF("Can write and read a whole line with terminator = '\\r\\n'") {
    raf =>
      val line = "Hello, world!"
      raf.writeChars(line + "\r\n")
      raf.seek(0)
      assert(raf.readLine() == line)
  }

  testWithRAF("Can write and read a Long") { raf =>
    val value: Long = 4631166901565532406L
    raf.writeLong(value)
    raf.seek(0)
    assert(raf.readLong() == value)
  }

  testWithRAF("Can write and read a modified UTF string") { raf =>
    val value = "Hello, world!" + '\u0102' + '\u0802'
    raf.writeUTF(value)
    raf.seek(0)
    assert(raf.readUTF() == value)
  }

  private def testWithRAF(name: String)(tst: RandomAccessFile => Unit): Unit =
    test(name) {
      val file = File.createTempFile("tmp", "")
      val raf  = new RandomAccessFile(file, "rw")
      tst(raf)
    }
}
