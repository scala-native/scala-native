package java.nio.file

import java.nio.file._
import java.io.{ByteArrayInputStream, File, FileInputStream, IOException}
import java.nio.file.attribute.BasicFileAttributes

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

      val it = Files.list(dir).iterator()
      assert(it.next() == d0)
      assert(it.next() == f0)
      assert(it.next() == f1)
      assert(it.hasNext() == false)
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

      val it = Files.walk(dir).iterator()
      assert(it.next() == dir)
      assert(it.next() == d0)
      assert(it.next() == f2)
      assert(it.next() == f0)
      assert(it.next() == f1)
      assert(it.hasNext() == false)
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

      val it = Files.walk(d0, FileVisitOption.FOLLOW_LINKS).iterator()
      assert(it.next() == d0)
      assert(it.next() == f0)
      assert(it.next() == f1)
      assert(it.next() == link)
      assert(it.next() == link.resolve("f2"))
      assert(it.hasNext() == false)
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

      val it = Files.walk(d0, FileVisitOption.FOLLOW_LINKS).iterator()
      assert(it.next() == d0)
      assert(it.next() == d1)
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

      assert(visitor.dequeue() == dir)
      assert(visitor.dequeue() == d0)
      assert(visitor.dequeue() == f2)
      assert(visitor.dequeue() == d0)
      assert(visitor.dequeue() == f0)
      assert(visitor.dequeue() == f1)
      assert(visitor.dequeue() == dir)
      assert(visitor.isEmpty)
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

      val visitor = new QueueingVisitor {
        override def visitFile(
            file: Path,
            attributes: BasicFileAttributes): FileVisitResult =
          if (file == f2) FileVisitResult.TERMINATE
          else super.visitFile(file, attributes)
      }
      Files.walkFileTree(dir, visitor)

      assert(visitor.dequeue() == dir)
      assert(visitor.dequeue() == d0)
      assert(visitor.isEmpty)
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

      assert(visitor.dequeue() == dir)
      assert(visitor.dequeue() == f0)
      assert(visitor.dequeue() == f1)
      assert(visitor.dequeue() == dir)
      assert(visitor.isEmpty)
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

      val visitor = new QueueingVisitor {
        override def visitFile(
            file: Path,
            attributes: BasicFileAttributes): FileVisitResult =
          if (file == f0) FileVisitResult.SKIP_SIBLINGS
          else super.visitFile(file, attributes)
      }
      Files.walkFileTree(dir, visitor)

      assert(visitor.dequeue() == dir)
      assert(visitor.dequeue() == d0)
      assert(visitor.dequeue() == f2)
      assert(visitor.dequeue() == d0)
      assert(visitor.dequeue() == dir)
      assert(visitor.isEmpty)
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
      val it = Files.find(dir, 10, predicate).iterator

      assert(it.next() == f2)
      assert(it.next() == f0)
      assert(it.next() == f1)
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

  def withTemporaryDirectory(fn: File => Unit) {
    val file = File.createTempFile("test", ".tmp")
    assert(file.delete())
    assert(file.mkdir())
    fn(file)
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
