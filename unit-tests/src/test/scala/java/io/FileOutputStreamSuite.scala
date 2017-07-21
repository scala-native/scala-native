package java.io

object FileOutputStreamSuite extends tests.Suite {
  def withTempFile(f: File => Unit): Unit = {
    val tmpfile = File.createTempFile("scala-native-test", null)
    try {
      f(tmpfile)
    } finally {
      tmpfile.delete()
    }
  }

  def withTempDirectory(f: File => Unit): Unit = {
    import java.nio.file._
    import attribute._
    val tmpdir = Files.createTempDirectory("scala-native-test")
    try {
      f(tmpdir.toFile())
    } finally {
      Files.walkFileTree(
        tmpdir,
        new SimpleFileVisitor[Path]() {
          override def visitFile(
              file: Path,
              attrs: BasicFileAttributes): FileVisitResult = {
            Files.delete(file)
            FileVisitResult.CONTINUE
          }
          override def postVisitDirectory(
              dir: Path,
              exc: IOException): FileVisitResult = {
            Files.delete(dir)
            FileVisitResult.CONTINUE
          }
        }
      )
    }
  }

  test("write null") {
    withTempFile { file =>
      val fos = new FileOutputStream(file)
      assertThrows[NullPointerException] {
        fos.write(null)
      }
      assertThrows[NullPointerException] {
        fos.write(null, 0, 0)
      }
      fos.close()
    }
  }

  test("write out of bounds negative count") {
    withTempFile { file =>
      val fos = new FileOutputStream(file)
      val arr = new Array[Byte](8)
      assertThrows[IndexOutOfBoundsException] {
        fos.write(arr, 0, -1)
      }
      fos.close()
    }
  }

  test("write out of bounds negative offset") {
    withTempFile { file =>
      val fos = new FileOutputStream(file)
      val arr = new Array[Byte](8)
      assertThrows[IndexOutOfBoundsException] {
        fos.write(arr, -1, 0)
      }
      fos.close()
    }
  }

  test("write out of bounds array too small") {
    withTempFile { file =>
      val fos = new FileOutputStream(file)
      val arr = new Array[Byte](8)
      assertThrows[IndexOutOfBoundsException] {
        fos.write(arr, 0, 16)
      }
      assertThrows[IndexOutOfBoundsException] {
        fos.write(arr, 4, 8)
      }
      fos.close()
    }
  }

  test("attempt to open a readonly regular file") {
    withTempFile { ro =>
      ro.setReadOnly()
      assertThrows[FileNotFoundException](new FileOutputStream(ro))
    }
  }

  test("attempt to open a directory") {
    withTempDirectory { dir =>
      assertThrows[FileNotFoundException](new FileOutputStream(dir))
    }
  }

  test("attempt to create a file in a readonly directory") {
    withTempDirectory { ro =>
      ro.setReadOnly()
      assertThrows[FileNotFoundException](
        new FileOutputStream(new File(ro, "child")))
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
