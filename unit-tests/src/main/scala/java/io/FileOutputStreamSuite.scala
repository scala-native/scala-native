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

  test("truncate a file on initialization if append=false") {
    val nonEmpty = File.createTempFile("scala-native-unit-test", null)
    try {
      // prepares a non-empty file
      locally {
        val fos = new FileOutputStream(nonEmpty)
        try { fos.write(0x20) } finally { fos.close() }
      }
      // re-opens the file with append=false so that it is truncated
      locally {
        val fos = new FileOutputStream(nonEmpty)
        fos.close()
      }
      // checks the content
      locally {
        val fin = new FileInputStream(nonEmpty)
        try {
          assertEquals(-1, fin.read())
        } finally {
          fin.close()
        }
      }
    } finally {
      nonEmpty.delete()
    }
  }

  test("do not truncate a file on initialization if append=true") {
    val nonEmpty = File.createTempFile("scala-native-unit-test", null)
    try {
      val written = 0x20
      // prepares a non-empty file
      locally {
        val fos = new FileOutputStream(nonEmpty)
        try { fos.write(written) } finally { fos.close() }
      }
      // re-opens the file with append=true
      locally {
        val fos = new FileOutputStream(nonEmpty, true)
        fos.close()
      }
      // checks the content
      locally {
        val fin = new FileInputStream(nonEmpty)
        try {
          assertEquals(written, fin.read())
          assertEquals(-1, fin.read())
        } finally {
          fin.close()
        }
      }
    } finally {
      nonEmpty.delete()
    }
  }
}
