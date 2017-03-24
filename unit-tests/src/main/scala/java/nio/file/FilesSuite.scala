package java.nio.file

import java.io.{ByteArrayInputStream, File, FileInputStream}

object FilesSuite extends tests.Suite {

  test("Files.copy can copy to a non-existing file") {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assert(!targetFile.exists || targetFile.delete())

    val in = new ByteArrayInputStream(Array(1, 2, 3))
    assert(Files.copy(in, target) == 3)
    assert(targetFile.exists())
    assert(in.read == -1)

    val fromFile = new FileInputStream(targetFile)
    assert(fromFile.read() == 1)
    assert(fromFile.read() == 2)
    assert(fromFile.read() == 3)
    assert(fromFile.read() == -1)
  }

  test("Files.copy throws if the target exists and is a file") {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assert(targetFile.exists() && targetFile.isFile())

    val in = new ByteArrayInputStream(Array(1, 2, 3))
    assertThrows[FileAlreadyExistsException] {
      Files.copy(in, target)
    }
  }

  test("Files.copy throws if the target exists and is an empty directory") {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assert(targetFile.delete())
    assert(targetFile.mkdir())
    assert(
      targetFile.exists() && targetFile
        .isDirectory() && targetFile.list().isEmpty)

    val in = new ByteArrayInputStream(Array(1, 2, 3))
    assertThrows[FileAlreadyExistsException] {
      Files.copy(in, target)
    }
  }

  test(
    "Files.copy works if the target exists and is an empty directory and REPLACE_EXISTING is set") {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assert(targetFile.delete())
    assert(targetFile.mkdir())
    assert(
      targetFile.exists() && targetFile
        .isDirectory() && targetFile.list().isEmpty)

    val in = new ByteArrayInputStream(Array(1, 2, 3))
    assert(Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING) == 3)
    assert(targetFile.exists() && targetFile.isFile())
    assert(in.read() == -1)

    val fromFile = new FileInputStream(targetFile)
    assert(fromFile.read() == 1)
    assert(fromFile.read() == 2)
    assert(fromFile.read() == 3)
    assert(fromFile.read() == -1)
  }

  test("Files.copy throws if the target exists and is a non-empty directory") {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assert(targetFile.delete())
    assert(targetFile.mkdir())
    assert(targetFile.exists() && targetFile.isDirectory())
    File.createTempFile("test", ".tmp", targetFile)
    assert(targetFile.list().nonEmpty)

    val in = new ByteArrayInputStream(Array(1, 2, 3))

    assertThrows[FileAlreadyExistsException] {
      Files.copy(in, target)
    }
  }

  test(
    "Files.copy throws if the target exists and is a non-empty directory and REPLACE_EXISTING is set") {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assert(targetFile.delete())
    assert(targetFile.mkdir())
    assert(targetFile.exists() && targetFile.isDirectory())
    File.createTempFile("test", ".tmp", targetFile)
    assert(targetFile.list().nonEmpty)

    val in = new ByteArrayInputStream(Array(1, 2, 3))

    assertThrows[FileAlreadyExistsException] {
      Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING)
    }
  }

  test(
    "Files.copy replaces the target if its an existing file and REPLACE_EXISTING is set") {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assert(targetFile.exists() && targetFile.isFile())

    val in = new ByteArrayInputStream(Array(1, 2, 3))
    assert(Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING) == 3)
    assert(in.read() == -1)

    val fromFile = new FileInputStream(targetFile)
    assert(fromFile.read() == 1)
    assert(fromFile.read() == 2)
    assert(fromFile.read() == 3)
    assert(fromFile.read() == -1)
  }

  test("Files.createSymbolicLink can create symbolic links") {
    withTemporaryDirectory { dirFile =>
      val dir    = dirFile.toPath()
      val link   = dir.resolve("link")
      val target = dir.resolve("target")
      Files.createSymbolicLink(link, target)
      assert(Files.isSymbolicLink(link))
    }
  }

  test("Files.createSymbolicLink throws if the link already exists") {
    withTemporaryDirectory { dirFile =>
      val dir    = dirFile.toPath
      val link   = dir.resolve("link")
      val target = dir.resolve("target")
      link.toFile().createNewFile()
      assert(link.toFile().exists())

      assertThrows[FileAlreadyExistsException] {
        Files.createSymbolicLink(link, target)
      }
    }
  }

  test("Files.exists reports existing files as existing") {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assert(targetFile.exists() && targetFile.isFile())

    assert(Files.exists(target))
    assert(Files.exists(target, LinkOption.NOFOLLOW_LINKS))
  }

  test("Files.exists reports existing directories as existing") {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assert(targetFile.delete())
    assert(targetFile.mkdir())
    assert(targetFile.exists() && targetFile.isDirectory())

    assert(Files.exists(target))
    assert(Files.exists(target, LinkOption.NOFOLLOW_LINKS))
  }

  test("Files.exists reports non-existing files as such") {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assert(targetFile.delete())
    assert(!targetFile.exists())
    assert(!Files.exists(target))
    assert(!Files.exists(target, LinkOption.NOFOLLOW_LINKS))
  }

  test("Files.exists handles symlinks") {
    withTemporaryDirectory { dirFile =>
      val dir         = dirFile.toPath()
      val existing    = new File(dirFile, "existing")
      val nonexisting = dir.resolve("nonexisting")
      val brokenLink  = dir.resolve("brokenlink")
      val correctLink = dir.resolve("correctlink")

      existing.createNewFile()
      assert(existing.exists())

      Files.createSymbolicLink(brokenLink, nonexisting)
      Files.createSymbolicLink(correctLink, existing.toPath)
      assert(!Files.exists(brokenLink))
      assert(Files.exists(brokenLink, LinkOption.NOFOLLOW_LINKS))
      assert(Files.exists(correctLink))
      assert(Files.exists(correctLink, LinkOption.NOFOLLOW_LINKS))
    }
  }

  test("Files.createDirectory can create a directory") {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assert(targetFile.delete())

    Files.createDirectory(target)
    assert(targetFile.exists())
    assert(targetFile.isDirectory())
  }

  test("Files.createDirectory throws if the file already exists") {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()

    assertThrows[FileAlreadyExistsException] {
      Files.createDirectory(target)
    }
  }

  test(
    "Files.createDirectories can create directories if none of the hierarchy exists") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val p1  = dir.resolve("p1")
      val p2  = p1.resolve("p2")
      val p3  = p2.resolve("p3")

      assert(!Files.exists(p1))
      assert(!Files.exists(p2))
      assert(!Files.exists(p3))

      Files.createDirectories(p3)
      assert(Files.exists(p3))
    }
  }

  test(
    "Files.createDirectories can create directories if some of the hierarchy exists") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val p1  = dir.resolve("p1")
      val p2  = p1.resolve("p2")
      val p3  = p2.resolve("p3")
      val p4  = p3.resolve("p4")

      assert(!Files.exists(p1))
      assert(!Files.exists(p2))
      assert(!Files.exists(p3))
      assert(!Files.exists(p4))

      Files.createDirectories(p2)

      assert(Files.exists(p1))
      assert(Files.exists(p2))
      assert(!Files.exists(p3))
      assert(!Files.exists(p4))

      Files.createDirectories(p4)
      assert(Files.exists(p1))
      assert(Files.exists(p2))
      assert(Files.exists(p3))
      assert(Files.exists(p4))
    }
  }

  test("Files.createFile can create a new file") {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath()
      val file = dir.resolve("file")

      assert(!Files.exists(file))
      Files.createFile(file)
      assert(Files.exists(file))
    }
  }

  test("Files.createFile throws if the file already exists") {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath()
      val file = dir.resolve("file")

      assert(!Files.exists(file))
      Files.createFile(file)
      assert(Files.exists(file))

      assertThrows[FileAlreadyExistsException] {
        Files.createFile(file)
      }
    }
  }

  test("Files.createLink can create links") {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath()
      val file = dir.resolve("file")
      val link = dir.resolve("link")

      Files.createFile(file)
      assert(Files.exists(file))

      Files.createLink(link, file)
      assert(Files.exists(link))
    }
  }

  test("Files.createLink throws if the file already exists") {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath()
      val file = dir.resolve("file")
      val link = dir.resolve("link")

      Files.createFile(file)
      Files.createFile(link)
      assert(Files.exists(file))
      assert(Files.exists(link))

      assertThrows[FileAlreadyExistsException] {
        Files.createLink(link, file)
      }
    }
  }

  private def withTemporaryDirectory(fn: File => Unit) {
    val file = File.createTempFile("test", ".tmp")
    assert(file.delete())
    assert(file.mkdir())
    fn(file)
  }

}
