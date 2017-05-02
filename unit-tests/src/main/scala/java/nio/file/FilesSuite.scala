package java.nio.file

import java.nio.ByteBuffer
import java.nio.file._
import java.io.{
  BufferedWriter,
  ByteArrayInputStream,
  File,
  FileInputStream,
  FileOutputStream,
  IOException,
  OutputStreamWriter
}
import java.nio.file.attribute.{
  BasicFileAttributes,
  FileTime,
  PosixFileAttributes,
  PosixFilePermission,
  PosixFilePermissions
}

import java.util.function.BiPredicate

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

  test("Files.createTempDirectory works without parent directory") {
    val dir = Files.createTempDirectory("tmp")
    assert(Files.exists(dir))
    assert(Files.isDirectory(dir))

    val file = dir.resolve("file")
    assert(!Files.exists(file))
    Files.createFile(file)
    assert(Files.exists(file))
  }

  test("Files.createTempDirectory works with parent directory") {
    withTemporaryDirectory { parent =>
      val dir = Files.createTempDirectory(parent.toPath(), "tmp")
      assert(dir.getParent() == parent.toPath())
      assert(Files.exists(dir))
      assert(Files.isDirectory(dir))

      val file = dir.resolve("file")
      assert(!Files.exists(file))
      Files.createFile(file)
      assert(Files.exists(file))
    }
  }

  test("Files.isRegularFile reports files as such") {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath
      val file = dir.resolve("file")
      Files.createFile(file)
      assert(Files.exists(file))
      assert(Files.isRegularFile(file))
    }
  }

  test("Files.isRegularFile handles directories") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath
      assert(Files.exists(dir))
      assert(!Files.isRegularFile(dir))
    }
  }

  test("Files.isRegularFile handles symlinks") {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath
      val file = dir.resolve("file")
      val link = dir.resolve("link")
      Files.createFile(file)
      Files.createSymbolicLink(link, file)
      assert(Files.exists(file))
      assert(Files.isRegularFile(file))
      assert(Files.isSymbolicLink(link))
      assert(Files.isRegularFile(link))
      assert(!Files.isRegularFile(link, LinkOption.NOFOLLOW_LINKS))
    }
  }

  test("Files.createTempFile works without parent directory") {
    val tmp = Files.createTempFile("temp", ".tmp")
    assert(Files.exists(tmp))
    assert(Files.isRegularFile(tmp))
  }

  test("Files.createTempFile works with parent directory") {
    withTemporaryDirectory { parent =>
      val tmp = Files.createTempFile(parent.toPath(), "temp", ".tmp")
      assert(tmp.getParent() == parent.toPath())
      assert(Files.exists(tmp))
      assert(Files.isRegularFile(tmp))
    }
  }

  test("Files.delete can delete files") {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath()
      val file = dir.resolve("file")
      Files.createFile(file)

      assert(Files.exists(file))
      assert(Files.isRegularFile(file))
      Files.delete(file)
      assert(!Files.exists(file))
    }
  }

  test("Files.delete can delete empty directories") {
    withTemporaryDirectory { dirFile =>
      val dir    = dirFile.toPath()
      val subdir = dir.resolve("subdir")
      Files.createDirectory(subdir)
      assert(Files.exists(subdir))
      assert(Files.isDirectory(subdir))
      Files.delete(subdir)
      assert(!Files.exists(subdir))
    }
  }

  test("Files.delete throws when deleting a non-existing file") {
    withTemporaryDirectory { dirFile =>
      val dir         = dirFile.toPath()
      val nonexisting = dir.resolve("nonexisting")
      assert(!Files.exists(nonexisting))

      assertThrows[NoSuchFileException] {
        Files.delete(nonexisting)
      }
    }
  }

  test("Files.delete throws when deleting a non-empty directory") {
    withTemporaryDirectory { dirFile =>
      val dir    = dirFile.toPath()
      val subdir = dir.resolve("subdir")
      val file   = subdir.resolve("file")
      Files.createDirectory(subdir)
      Files.createFile(file)
      assert(Files.exists(subdir))
      assert(Files.isDirectory(subdir))
      assertThrows[IOException] {
        Files.delete(subdir)
      }
    }
  }

  test("Files.deleteIfExists works if the file exists") {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath()
      val file = dir.resolve("file")
      Files.createFile(file)
      assert(Files.exists(file))
      assert(Files.isRegularFile(file))
      assert(Files.deleteIfExists(file))
      assert(!Files.exists(file))
    }
  }

  test("Files.deleteIfExists works if the file doesn't exist") {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath()
      val file = dir.resolve("file")
      assert(!Files.exists(file))
      assert(!Files.deleteIfExists(file))
      assert(!Files.exists(file))
    }
  }

  test("Files.list lists files") {
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

      val it    = Files.list(dir).iterator()
      val files = scala.collection.mutable.Set.empty[Path]
      while (it.hasNext()) {
        files += it.next()
      }
      assert(files.size == 3)
      assert(files contains d0)
      assert(files contains f0)
      assert(files contains f1)
    }
  }

  test("Files.readSymbolicLink can read a valid symbolic link") {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath()
      val link = dir.resolve("link")
      val file = dir.resolve("file")
      Files.createFile(file)
      Files.createSymbolicLink(link, file)

      assert(Files.exists(file))
      assert(Files.exists(link))
      assert(Files.readSymbolicLink(link) == file)
    }
  }

  test("Files.readSymbolicLink can read a broken symbolic link") {
    withTemporaryDirectory { dirFile =>
      val dir        = dirFile.toPath()
      val brokenLink = dir.resolve("link")
      val file       = dir.resolve("file")
      Files.createSymbolicLink(brokenLink, file)

      assert(!Files.exists(file))
      assert(Files.exists(brokenLink, LinkOption.NOFOLLOW_LINKS))
      assert(Files.readSymbolicLink(brokenLink) == file)
    }
  }

  test("Files.walk walks files") {
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

      val it    = Files.walk(dir).iterator()
      val files = scala.collection.mutable.Set.empty[Path]
      while (it.hasNext()) {
        files += it.next()
      }
      assert(files.size == 5)
      assert(files contains dir)
      assert(files contains d0)
      assert(files contains f2)
      assert(files contains f0)
      assert(files contains f1)
    }
  }

  test("Files.walk follows symlinks") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()

      val d0   = dir.resolve("d0")
      val f0   = d0.resolve("f0")
      val f1   = d0.resolve("f1")
      val link = d0.resolve("link")

      val d1 = dir.resolve("d1")
      val f2 = d1.resolve("f2")

      Files.createDirectory(d0)
      Files.createFile(f0)
      Files.createFile(f1)
      Files.createSymbolicLink(link, d1)

      Files.createDirectory(d1)
      Files.createFile(f2)

      assert(Files.exists(d0) && Files.isDirectory(d0))
      assert(Files.exists(f0) && Files.isRegularFile(f0))
      assert(Files.exists(f1) && Files.isRegularFile(f1))
      assert(Files.exists(f2) && Files.isRegularFile(f2))

      val it    = Files.walk(d0, FileVisitOption.FOLLOW_LINKS).iterator()
      val files = scala.collection.mutable.Set.empty[Path]
      while (it.hasNext()) {
        files += it.next()
      }
      assert(files.size == 5)
      assert(files contains d0)
      assert(files contains f0)
      assert(files contains f1)
      assert(files contains link)
    }
  }

  test("Files.walk detects cycles") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()

      val d0   = dir.resolve("d0")
      val d1   = d0.resolve("d1")
      val link = d1.resolve("link")

      Files.createDirectory(d0)
      Files.createDirectory(d1)
      Files.createSymbolicLink(link, d0)

      val it       = Files.walk(d0, FileVisitOption.FOLLOW_LINKS).iterator()
      val expected = Set(d0, d1)
      assert(expected contains it.next())
      assert(expected contains it.next())
      assertThrows[FileSystemLoopException] { it.next() }
    }
  }

  test("Files.walkFileTree walks the tree") {
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

      val visitor = new QueueingVisitor
      Files.walkFileTree(dir, visitor)
      val expected = Map(dir -> 2, d0 -> 2, f2 -> 1, f0 -> 1, f1 -> 1)
      val result   = scala.collection.mutable.Map.empty[Path, Int]
      while (!visitor.isEmpty) {
        val f     = visitor.dequeue()
        val count = result.getOrElse(f, 0)
        result(f) = count + 1
      }
      assert(result == expected)
    }
  }

  test("Files.walkFileTree can be terminated") {
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

      val visitor = new QueueingVisitor()
      val terminatingVisitor = new QueueingVisitor {
        override def visitFile(
            file: Path,
            attributes: BasicFileAttributes): FileVisitResult =
          if (file == f2) FileVisitResult.TERMINATE
          else super.visitFile(file, attributes)
      }
      Files.walkFileTree(dir, visitor)
      Files.walkFileTree(dir, terminatingVisitor)

      val expected = scala.collection.mutable.Set.empty[Path]
      var continue = true
      while (continue) {
        val p = visitor.dequeue()
        if (p == f2) continue = false
        else expected += p
      }
      val found = scala.collection.mutable.Set.empty[Path]
      while (!terminatingVisitor.isEmpty) {
        found += terminatingVisitor.dequeue()
      }
      assert(found == expected)
    }
  }

  test("Files.walkFileTree can skip subtrees") {
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

      val visitor = new QueueingVisitor {
        override def preVisitDirectory(
            dir: Path,
            attributes: BasicFileAttributes): FileVisitResult =
          if (dir == d0) FileVisitResult.SKIP_SUBTREE
          else super.preVisitDirectory(dir, attributes)
      }
      Files.walkFileTree(dir, visitor)
      val expected = Map(dir -> 2, f0 -> 1, f1 -> 1)
      val result   = scala.collection.mutable.Map.empty[Path, Int]
      while (!visitor.isEmpty) {
        val f     = visitor.dequeue()
        val count = result.getOrElse(f, 0)
        result(f) = count + 1
      }
      assert(result == expected)
    }
  }

  test("Files.walkFileTree can skip siblings") {
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

      val visitor  = new QueueingVisitor()
      val expected = scala.collection.mutable.Set.empty[Path]
      var skip     = false
      val skippingVisitor = new QueueingVisitor {
        override def visitFile(
            file: Path,
            attributes: BasicFileAttributes): FileVisitResult =
          if (file == f0) FileVisitResult.SKIP_SIBLINGS
          else super.visitFile(file, attributes)
      }
      Files.walkFileTree(dir, visitor)
      Files.walkFileTree(dir, skippingVisitor)
      while (!visitor.isEmpty) {
        val p = visitor.dequeue()
        if (p == f0) skip = true
        if (skip && p.getParent == f0.getParent()) ()
        else expected += p
      }

      val result = scala.collection.mutable.Set.empty[Path]
      while (!skippingVisitor.isEmpty) {
        result += skippingVisitor.dequeue()
      }
      assert(result == expected)
    }
  }

  test("Files.find finds files") {
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

      val predicate = new BiPredicate[Path, BasicFileAttributes] {
        override def test(path: Path, attrs: BasicFileAttributes): Boolean =
          path.getFileName.toString.startsWith("f")
      }
      val it       = Files.find(dir, 10, predicate).iterator
      val expected = Set(f0, f1, f2)

      assert(expected contains it.next())
      assert(expected contains it.next())
      assert(expected contains it.next())
    }
  }

  test("Files.getLastModifiedTime works") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")

      Files.createFile(f0)
      assert(Files.exists(f0))

      val referenceMs = f0.toFile().lastModified()
      val filetimeMs  = Files.getLastModifiedTime(f0).toMillis()
      assert(referenceMs == filetimeMs)
    }
  }

  test("Files.getAttribute can fetch attributes from BasicFileAttributeView") {
    withTemporaryDirectory { dirFile =>
      val d0 = dirFile.toPath()
      val f0 = d0.resolve("f0")

      Files.createFile(f0)
      assert(Files.exists(f0))

      val d0mtime =
        Files.getAttribute(d0, "lastModifiedTime").asInstanceOf[FileTime]
      val d0atime =
        Files.getAttribute(d0, "lastAccessTime").asInstanceOf[FileTime]
      val d0ctime =
        Files.getAttribute(d0, "creationTime").asInstanceOf[FileTime]
      val d0size = Files.getAttribute(d0, "size").asInstanceOf[Long]
      val d0isReg =
        Files.getAttribute(d0, "isRegularFile").asInstanceOf[Boolean]
      val d0isDir = Files.getAttribute(d0, "isDirectory").asInstanceOf[Boolean]
      val d0isSym =
        Files.getAttribute(d0, "isSymbolicLink").asInstanceOf[Boolean]
      val d0isOth = Files.getAttribute(d0, "isOther").asInstanceOf[Boolean]
      val d0fkey  = Files.getAttribute(d0, "fileKey")

      assert(!d0isReg)
      assert(d0isDir)
      assert(!d0isSym)
      assert(!d0isOth)

      val f0mtime =
        Files.getAttribute(f0, "lastModifiedTime").asInstanceOf[FileTime]
      val f0atime =
        Files.getAttribute(f0, "lastAccessTime").asInstanceOf[FileTime]
      val f0ctime =
        Files.getAttribute(f0, "creationTime").asInstanceOf[FileTime]
      val f0size = Files.getAttribute(f0, "size").asInstanceOf[Long]
      val f0isReg =
        Files.getAttribute(f0, "isRegularFile").asInstanceOf[Boolean]
      val f0isDir = Files.getAttribute(f0, "isDirectory").asInstanceOf[Boolean]
      val f0isSym =
        Files.getAttribute(f0, "isSymbolicLink").asInstanceOf[Boolean]
      val f0isOth = Files.getAttribute(f0, "isOther").asInstanceOf[Boolean]
      val f0fkey  = Files.getAttribute(f0, "fileKey")

      assert(f0mtime.toMillis() == f0.toFile().lastModified())
      assert(f0size == f0.toFile().length())
      assert(f0isReg)
      assert(!f0isDir)
      assert(!f0isSym)
      assert(!f0isOth)
    }
  }

  test("Files.getAttribute obeys given LinkOption") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")
      val l0  = dir.resolve("l0")

      Files.createFile(f0)
      Files.createSymbolicLink(l0, f0)
      assert(Files.exists(f0))
      assert(Files.exists(l0))

      val normalL0IsReg =
        Files.getAttribute(l0, "isRegularFile").asInstanceOf[Boolean]
      val noFollowL0IsReg = Files
        .getAttribute(l0, "isRegularFile", LinkOption.NOFOLLOW_LINKS)
        .asInstanceOf[Boolean]
      val normalL0IsLink =
        Files.getAttribute(l0, "isSymbolicLink").asInstanceOf[Boolean]
      val noFollowL0IsLink = Files
        .getAttribute(l0, "isSymbolicLink", LinkOption.NOFOLLOW_LINKS)
        .asInstanceOf[Boolean]

      assert(normalL0IsReg)
      assert(!noFollowL0IsReg)
      assert(!normalL0IsLink)
      assert(noFollowL0IsLink)
    }
  }

  test("Files.getAttribute accepts a view name") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val isDir =
        Files.getAttribute(dir, "basic:isDirectory").asInstanceOf[Boolean]
      assert(isDir)
    }
  }

  test("Files.getOwner works") {
    withTemporaryDirectory { dirFile =>
      val dir   = dirFile.toPath()
      val owner = Files.getOwner(dir)
      assert(owner.getName().nonEmpty)
    }
  }

  test("Files.getPosixFilePermissions works") {
    withTemporaryDirectory { dirFile =>
      val dir    = dirFile.toPath()
      val f0     = dir.resolve("f0")
      val f0File = f0.toFile()

      Files.createFile(f0)
      assert(Files.exists(f0))

      f0File.setReadable(true)
      f0File.setWritable(false)
      f0File.setExecutable(true)

      val permissions = Files.getPosixFilePermissions(f0)

      import PosixFilePermission._
      assert(permissions.contains(OWNER_READ))
      assert(permissions.contains(GROUP_READ))
      assert(permissions.contains(OTHERS_READ))
      assert(!permissions.contains(OWNER_WRITE))
      assert(!permissions.contains(GROUP_WRITE))
      assert(!permissions.contains(OTHERS_WRITE))
      assert(permissions.contains(OWNER_EXECUTE))
      assert(permissions.contains(GROUP_EXECUTE))
      assert(permissions.contains(OTHERS_EXECUTE))
      assert(permissions.size() == 6)

    }
  }

  test("Files.lines returns the lines") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")

      val writer = new BufferedWriter(
        new OutputStreamWriter(new FileOutputStream(f0.toFile())))
      writer.write("first line")
      writer.newLine()
      writer.write("second line")
      writer.flush()
      writer.close()

      assert(Files.exists(f0))

      val it = Files.lines(f0).iterator()
      assert(it.next() == "first line")
      assert(it.next() == "second line")
      assert(!it.hasNext())
    }
  }

  test("Files.write can write to a file") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")

      val lines = new Iterable(Array("first line", "second line"))
      Files.write(f0, lines)

      val it = Files.lines(f0).iterator()
      assert(it.next() == "first line")
      assert(it.next() == "second line")
      assert(!it.hasNext())
    }
  }

  test("Files.move moves files") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")
      val f1  = dir.resolve("f1")

      val lines = new Iterable(Array("first line", "second line"))
      Files.write(f0, lines)
      assert(Files.exists(f0))
      assert(!Files.exists(f1))
      Files.move(f0, f1)
      assert(!Files.exists(f0))
      assert(Files.exists(f1))

      val it = Files.lines(f1).iterator
      assert(it.next() == "first line")
      assert(it.next() == "second line")
      assert(!it.hasNext())
    }
  }

  test("Files.setAttribute can set lastModifiedTime") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")

      Files.createFile(f0)
      assert(Files.exists(f0))

      val time0 = Files.getAttribute(f0, "lastModifiedTime")
      val time1 = FileTime.fromMillis(1000)
      Files.setAttribute(f0, "lastModifiedTime", FileTime.fromMillis(1000))
      val time2 = Files.getAttribute(f0, "lastModifiedTime")

      assert(time0 != time2)
      assert(time1 == time2)
    }
  }

  test("Files.setAttribute can set lastAccessTime") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")

      Files.createFile(f0)
      assert(Files.exists(f0))

      val time0 = Files.getAttribute(f0, "lastAccessTime")
      val time1 = FileTime.fromMillis(1000)
      Files.setAttribute(f0, "lastAccessTime", time1)
      val time2 = Files.getAttribute(f0, "lastAccessTime")

      assert(time0 != time2)
      assert(time1 == time2)
    }
  }

  test("Files.setAttribute can set permissions") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")

      Files.createFile(f0)
      assert(Files.exists(f0))

      val perm0 = Files.getAttribute(f0, "posix:permissions")
      val perm1 = PosixFilePermissions.fromString("rwxrwxrwx")
      Files.setAttribute(f0, "posix:permissions", perm1)
      val perm2 = Files.getAttribute(f0, "posix:permissions")

      assert(perm0 != perm2)
      assert(perm1 == perm2)
    }
  }

  test("Files.readAllLines returns all the lines") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")

      val lines = new Iterable(Array("first line", "second line"))
      Files.write(f0, lines)
      assert(Files.exists(f0))

      val list = Files.readAllLines(f0)
      assert(list.size() == 2)
      assert(list.get(0) == "first line")
      assert(list.get(1) == "second line")
    }
  }

  test("Files.readAllBytes reads all bytes") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")
      val in  = new ByteArrayInputStream(Array(1, 2, 3))
      Files.copy(in, f0)
      assert(Files.exists(f0))
      assert(Files.size(f0) == 3)

      val bytes = Files.readAllBytes(f0)
      assert(bytes(0) == 1)
      assert(bytes(1) == 2)
      assert(bytes(2) == 3)
    }
  }

  test("Files.readAttributes(Path, Class[_], Array[LinkOption]) works") {
    withTemporaryDirectory { dirFile =>
      val dir   = dirFile.toPath
      val attrs = Files.readAttributes(dir, classOf[PosixFileAttributes])
      assert(attrs.isDirectory())
      assert(!attrs.isOther())
      assert(!attrs.isRegularFile())
      assert(!attrs.isSymbolicLink())
    }
  }

  test("Files.readAttributes(Path, String, Array[LinkOption]) works") {
    withTemporaryDirectory { dirFile =>
      val dir   = dirFile.toPath
      val attrs = Files.readAttributes(dir, "posix:permissions,size")
      assert(attrs.size == 2)
      assert(attrs.containsKey("permissions"))
      assert(attrs.containsKey("size"))
    }
  }

  test("Files.readAttributes(Path, String, Array[LinkOption]) supports *") {
    withTemporaryDirectory { dirFile =>
      val dir   = dirFile.toPath
      val attrs = Files.readAttributes(dir, "posix:*")
      assert(!attrs.isEmpty())
    }
  }

  test("Files.newByteChannel returns a channel") {
    withTemporaryDirectory { dir =>
      val f = dir.toPath.resolve("f0")
      Files.write(f, Array[Byte](1, 2, 3))
      val channel = Files.newByteChannel(f)
      val buffer  = ByteBuffer.allocate(10)
      var read    = 0
      while (channel.read(buffer) != -1) {
        read += 1
      }
      assert(read == 3)
      assert(buffer.get(0) == 1)
      assert(buffer.get(1) == 2)
      assert(buffer.get(2) == 3)
    }
  }

  test("newInputStream returns an inputStream") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath
      val f0  = dir.resolve("f0")
      Files.write(f0, Array[Byte](1, 2, 3))

      val in = Files.newInputStream(f0)
      assert(in.read() == 1)
      assert(in.read() == 2)
      assert(in.read() == 3)
      assert(in.read() == -1)
    }
  }

  test("newOutputStream returns an OutputStream") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath
      val f0  = dir.resolve("f0")
      val out = Files.newOutputStream(f0)

      assert(Files.exists(f0))

      out.write(Array[Byte](1, 2, 3))

      val in = Files.newInputStream(f0)
      assert(in.read() == 1)
      assert(in.read() == 2)
      assert(in.read() == 3)
      assert(in.read() == -1)
    }
  }

  test("newOutputStream honors OpenOptions") {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath
      val f0  = dir.resolve("f0")

      val out0 = Files.newOutputStream(f0)
      out0.write(Array[Byte](1, 2, 3))
      out0.close()

      val out1 =
        Files.newOutputStream(f0, StandardOpenOption.TRUNCATE_EXISTING)
      out1.close()

      val in0 = Files.newInputStream(f0)
      assert(in0.read() == -1)

      val f1 = dir.resolve("f1")
      Files.createFile(f1)
      assertThrows[FileAlreadyExistsException] {
        Files.newOutputStream(f1, StandardOpenOption.CREATE_NEW)
      }

      val f2 = dir.resolve("f2")
      assertThrows[NoSuchFileException] {
        Files.newOutputStream(f2, StandardOpenOption.WRITE)
      }
    }
  }

  def withTemporaryDirectory(fn: File => Unit) {
    val file = File.createTempFile("test", ".tmp")
    assert(file.delete())
    assert(file.mkdir())
    fn(file)
  }

}

class Iterable[T](elems: Array[T]) extends java.lang.Iterable[T] {
  override val iterator = new java.util.Iterator[T] {
    private var i                   = 0
    override def hasNext(): Boolean = i < elems.length
    override def next(): T =
      if (hasNext) {
        val elem = elems(i)
        i += 1
        elem
      } else throw new NoSuchElementException()
    override def remove(): Unit =
      throw new UnsupportedOperationException()
  }
}

class QueueingVisitor extends SimpleFileVisitor[Path] {
  private val visited    = scala.collection.mutable.Queue.empty[Path]
  def isEmpty(): Boolean = visited.isEmpty
  def dequeue(): Path    = visited.dequeue()

  override def visitFileFailed(file: Path,
                               error: IOException): FileVisitResult =
    throw error

  override def preVisitDirectory(
      dir: Path,
      attributes: BasicFileAttributes): FileVisitResult = {
    visited.enqueue(dir)
    FileVisitResult.CONTINUE
  }

  override def postVisitDirectory(dir: Path,
                                  error: IOException): FileVisitResult = {
    visited.enqueue(dir)
    FileVisitResult.CONTINUE
  }

  override def visitFile(file: Path,
                         attributes: BasicFileAttributes): FileVisitResult = {
    visited.enqueue(file)
    FileVisitResult.CONTINUE
  }
}
