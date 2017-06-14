package java.nio.file

import FilesSuite.withTemporaryDirectory

object DirectoryStreamSuite extends tests.Suite {

  test("Files.newDirectoryStream(Path)") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")
      val f1  = dir.resolve("f1")
      val d0  = dir.resolve("d0")
      val f2  = d0.resolve("f2")

      Files.createDirectory(d0)
      Files.createFile(f0)
      Files.createFile(f1)
      Files.createFile(f2)
      assert(Files.exists(d0) && Files.isDirectory(d0))
      assert(Files.exists(f0) && Files.isRegularFile(f0))
      assert(Files.exists(f1) && Files.isRegularFile(f1))
      assert(Files.exists(f2) && Files.isRegularFile(f2))

      val stream   = Files.newDirectoryStream(dir)
      val expected = Set(f0, f1, d0)
      val result   = scala.collection.mutable.Set.empty[Path]

      val it = stream.iterator()
      while (it.hasNext()) {
        result += it.next()
      }
      assert(result == expected)
    }
  }

  test("Files.newDirectoryStream(Path, DirectoryStream.Filter[Path])") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")
      val f1  = dir.resolve("f1")
      val d0  = dir.resolve("d0")
      val f2  = d0.resolve("f2")

      Files.createDirectory(d0)
      Files.createFile(f0)
      Files.createFile(f1)
      Files.createFile(f2)
      assert(Files.exists(d0) && Files.isDirectory(d0))
      assert(Files.exists(f0) && Files.isRegularFile(f0))
      assert(Files.exists(f1) && Files.isRegularFile(f1))
      assert(Files.exists(f2) && Files.isRegularFile(f2))

      val filter = new DirectoryStream.Filter[Path] {
        override def accept(p: Path): Boolean = !p.toString.endsWith("f1")
      }
      val stream   = Files.newDirectoryStream(dir, filter)
      val expected = Set(f0, d0)
      val result   = scala.collection.mutable.Set.empty[Path]

      val it = stream.iterator()
      while (it.hasNext()) {
        result += it.next()
      }
      assert(result == expected)
    }
  }

  test("Cannot get iterator more than once") {
    val stream = Files.newDirectoryStream(Paths.get("."))
    stream.iterator()
    assertThrows[IllegalStateException] {
      stream.iterator()
    }
  }

  test("Cannot get an iterator after close()") {
    val stream = Files.newDirectoryStream(Paths.get("."))
    stream.close()
    assertThrows[IllegalStateException] {
      stream.iterator()
    }
  }

  test("hasNext returns false after stream is closed") {
    val stream = Files.newDirectoryStream(Paths.get("."))
    val it     = stream.iterator()
    stream.close()
    assert(!it.hasNext())
    assertThrows[NoSuchElementException] {
      it.next()
    }
  }

}
