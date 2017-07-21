package java.io

import scala.util.Try

object FileInputStreamSuite extends tests.Suite {
  test("read null") {
    val file = new File(".")
    val fis  = new FileInputStream(file)
    assertThrows[NullPointerException] {
      fis.read(null)
    }
    assertThrows[NullPointerException] {
      fis.read(null, 0, 0)
    }
  }

  test("read out of bounds negative count") {
    val file = new File(".")
    val fis  = new FileInputStream(file)
    val arr  = new Array[Byte](8)
    assertThrows[IndexOutOfBoundsException] {
      fis.read(arr, 0, -1)
    }
  }

  test("read out of bounds negative offset") {
    val file = new File(".")
    val fis  = new FileInputStream(file)
    val arr  = new Array[Byte](8)
    assertThrows[IndexOutOfBoundsException] {
      fis.read(arr, -1, 0)
    }
  }

  test("read out of bounds array too small") {
    val file = new File(".")
    val fis  = new FileInputStream(file)
    val arr  = new Array[Byte](8)
    assertThrows[IndexOutOfBoundsException] {
      fis.read(arr, 0, 16)
    }
    assertThrows[IndexOutOfBoundsException] {
      fis.read(arr, 4, 8)
    }
  }

  test("valid file descriptor and sync success") {
    val file = File.createTempFile("fisfdtest", "")
    val fis  = new FileInputStream(file)
    val fd   = fis.getFD
    assert(fd.valid())
    assert(Try(fd.sync()).isSuccess)
    fis.close()
  }

  test("can read 0xFF correctly") {
    val file = File.createTempFile("file", ".tmp")
    val fos  = new FileOutputStream(file)
    fos.write(0xFF)
    fos.close()

    val fis = new FileInputStream(file)
    assert(fis.read() == 0xFF)
    assert(fis.read() == -1)
    fis.close()
  }

  test(
    "throws FileNotFoundException when creating new FileInputStream with non-existing file path") {
    assertThrows[FileNotFoundException] {
      new FileInputStream("/the/path/does/not/exist/for/sure")
    }
  }
}
