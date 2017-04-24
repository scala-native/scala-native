package java.io

object FileOutputStreamSuite extends tests.Suite {
  test("write null") {
    val file = new File(".")
    val fos  = new FileOutputStream(file)
    assertThrows[NullPointerException] {
      fos.write(null)
    }
    assertThrows[NullPointerException] {
      fos.write(null, 0, 0)
    }
  }

  test("write out of bounds negative count") {
    val file = new File(".")
    val fos  = new FileOutputStream(file)
    val arr  = new Array[Byte](8)
    assertThrows[IndexOutOfBoundsException] {
      fos.write(arr, 0, -1)
    }
  }

  test("write out of bounds negative offset") {
    val file = new File(".")
    val fos  = new FileOutputStream(file)
    val arr  = new Array[Byte](8)
    assertThrows[IndexOutOfBoundsException] {
      fos.write(arr, -1, 0)
    }
  }

  test("write out of bounds array too small") {
    val file = new File(".")
    val fos  = new FileOutputStream(file)
    val arr  = new Array[Byte](8)
    assertThrows[IndexOutOfBoundsException] {
      fos.write(arr, 0, 16)
    }
    assertThrows[IndexOutOfBoundsException] {
      fos.write(arr, 4, 8)
    }
  }
}
