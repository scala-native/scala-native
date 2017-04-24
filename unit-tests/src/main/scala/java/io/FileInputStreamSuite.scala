package java.io

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
}
