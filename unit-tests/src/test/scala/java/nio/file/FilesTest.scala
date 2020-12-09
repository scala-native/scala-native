package java.nio.file

import java.nio.ByteBuffer
import java.io._
import java.nio.file.attribute._

import java.util.function.BiPredicate
import PosixFilePermission._
import StandardCopyOption._

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows._
import scala.scalanative.junit.utils.CollectionConverters._

class FilesTest {
  import FilesTest._

  @Test def filesCopyCanCopyToNonExistingFile(): Unit = {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assertTrue(!targetFile.exists || targetFile.delete())

    val in = new ByteArrayInputStream(Array(1, 2, 3))
    assertTrue(Files.copy(in, target) == 3)
    assertTrue(targetFile.exists())
    assertTrue(in.read == -1)

    val fromFile = new FileInputStream(targetFile)
    assertTrue(fromFile.read() == 1)
    assertTrue(fromFile.read() == 2)
    assertTrue(fromFile.read() == 3)
    assertTrue(fromFile.read() == -1)
  }

  @Test def filesCopyThrowsIfTargetExistsAndIsFile(): Unit = {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assertTrue(targetFile.exists() && targetFile.isFile())

    val in = new ByteArrayInputStream(Array(1, 2, 3))
    assertThrows(classOf[FileAlreadyExistsException], Files.copy(in, target))
  }

  @Test def filesCopyThrowsIfTargetExistsAndIsEmptyDir(): Unit = {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assertTrue(targetFile.delete())
    assertTrue(targetFile.mkdir())
    assertTrue(
      targetFile.exists() && targetFile
        .isDirectory() && targetFile.list().isEmpty)

    val in = new ByteArrayInputStream(Array(1, 2, 3))
    assertThrows(classOf[FileAlreadyExistsException], Files.copy(in, target))
  }

  @Test def filesCopyWorksIfTargetExistsAndIsAnEmptyDirAndReplaceExistingIsSet()
      : Unit = {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assertTrue(targetFile.delete())
    assertTrue(targetFile.mkdir())
    assertTrue(
      targetFile.exists() && targetFile
        .isDirectory() && targetFile.list().isEmpty)

    val in = new ByteArrayInputStream(Array(1, 2, 3))
    assertTrue(Files.copy(in, target, REPLACE_EXISTING) == 3)
    assertTrue(targetFile.exists() && targetFile.isFile())
    assertTrue(in.read() == -1)

    val fromFile = new FileInputStream(targetFile)
    assertTrue(fromFile.read() == 1)
    assertTrue(fromFile.read() == 2)
    assertTrue(fromFile.read() == 3)
    assertTrue(fromFile.read() == -1)
  }

  @Test def filesCopyThrowsIfTheTargetExistsAndIsNonEmptyDir(): Unit = {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assertTrue(targetFile.delete())
    assertTrue(targetFile.mkdir())
    assertTrue(targetFile.exists() && targetFile.isDirectory())
    File.createTempFile("test", ".tmp", targetFile)
    assertTrue(targetFile.list().nonEmpty)

    val in = new ByteArrayInputStream(Array(1, 2, 3))

    assertThrows(classOf[FileAlreadyExistsException], Files.copy(in, target))
  }

  @Test def filesCopyThrowsIfTargetExistsAndIsNonEmptyDirAndReplaceExistingIsSet()
      : Unit = {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assertTrue(targetFile.delete())
    assertTrue(targetFile.mkdir())
    assertTrue(targetFile.exists() && targetFile.isDirectory())
    File.createTempFile("test", ".tmp", targetFile)
    assertTrue(targetFile.list().nonEmpty)

    val in = new ByteArrayInputStream(Array(1, 2, 3))

    assertThrows(classOf[FileAlreadyExistsException],
                 Files.copy(in, target, REPLACE_EXISTING))
  }

  @Test def filesCopyReplacesTheTargetIfExistingFileAndReplaceExistingIsSet()
      : Unit = {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assertTrue(targetFile.exists() && targetFile.isFile())

    val in = new ByteArrayInputStream(Array(1, 2, 3))
    assertTrue(Files.copy(in, target, REPLACE_EXISTING) == 3)
    assertTrue(in.read() == -1)

    val fromFile = new FileInputStream(targetFile)
    assertTrue(fromFile.read() == 1)
    assertTrue(fromFile.read() == 2)
    assertTrue(fromFile.read() == 3)
    assertTrue(fromFile.read() == -1)
  }

  @Test def filesCopyCreatesNewDirectory(): Unit = {
    withTemporaryDirectory { dirFile =>
      val targetDir = File.createTempFile("test", "").toPath()
      Files.delete(targetDir)
      Files.copy(dirFile.toPath, targetDir)
      assertTrue(Files.exists(targetDir))
      assertTrue(Files.isDirectory(targetDir))
      assertTrue(Files.deleteIfExists(targetDir))
    }
  }

  @Test def filesCopyDoesNotReplaceExistingDirectory(): Unit = {
    withTemporaryDirectory { dirFile =>
      val targetDir = File.createTempFile("test", "").toPath()
      assertThrows(classOf[FileAlreadyExistsException],
                   Files.copy(dirFile.toPath, targetDir))
    }
  }

  @Test def filesCopyReplacesTheTargetDirectory(): Unit = {
    withTemporaryDirectory { dirFile =>
      val targetDir = File.createTempFile("test", "").toPath()
      Files.delete(targetDir)
      Files.copy(dirFile.toPath, targetDir, REPLACE_EXISTING)
      assertTrue(Files.exists(targetDir))
      assertTrue(Files.isDirectory(targetDir))
      assertTrue(Files.deleteIfExists(targetDir))
    }
  }

  @Test def filesCopyDoesNotReplaceNonEmptyTargetDirectory(): Unit = {
    withTemporaryDirectory { dirFile =>
      val targetDir = File.createTempFile("test", "").toPath()
      Files.delete(targetDir)
      Files.copy(dirFile.toPath, targetDir, REPLACE_EXISTING)
      val f = Files.createTempFile(targetDir, "", "")
      assertThrows(classOf[DirectoryNotEmptyException],
                   Files.copy(dirFile.toPath, targetDir, REPLACE_EXISTING))
    }
  }

  @Test def filesCopyDoesNotCopySymlinks(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath
      val link = dir.resolve("link")
      val file = dir.resolve("target")
      Files.createSymbolicLink(link, dir.resolve("foo"))
      assertThrows(classOf[IOException], Files.copy(link, file))
    }
  }

  @Test def filesCopyshouldCopyAttributes(): Unit = {
    withTemporaryDirectory { dirFile =>
      val foo = dirFile.toPath.resolve("foo")
      Files.createFile(foo)
      Files.write(foo, "foo".getBytes)
      val permissions = Set(OWNER_EXECUTE, OWNER_READ, OWNER_WRITE).toJavaSet
      Files.setPosixFilePermissions(foo, permissions)
      val fooCopy = dirFile.toPath.resolve("foocopy")
      Files.copy(foo, fooCopy, COPY_ATTRIBUTES)
      val attrs = Files.readAttributes(foo, classOf[PosixFileAttributes])
      val copyAttrs =
        Files.readAttributes(fooCopy, classOf[PosixFileAttributes])
      assertTrue(attrs.lastModifiedTime == copyAttrs.lastModifiedTime)
      assertTrue(attrs.lastAccessTime == copyAttrs.lastAccessTime)
      assertTrue(attrs.creationTime == copyAttrs.creationTime)
      assertTrue(
        attrs.permissions.toScalaSet == copyAttrs.permissions.toScalaSet)
    }
  }

  @Test def filesCreateSymbolicLinkCanCreateSymbolicLinks(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir    = dirFile.toPath()
      val link   = dir.resolve("link")
      val target = dir.resolve("target")
      Files.createSymbolicLink(link, target)
      assertTrue(Files.isSymbolicLink(link))
    }
  }

  @Test def filesCreateSymbolicLinkThrowsIfTheLinkAlreadyExists(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir    = dirFile.toPath
      val link   = dir.resolve("link")
      val target = dir.resolve("target")
      link.toFile().createNewFile()
      assertTrue(link.toFile().exists())

      assertThrows(classOf[FileAlreadyExistsException],
                   Files.createSymbolicLink(link, target))
    }
  }

  @Test def filesExistsReportsExistingFilesAsExisting(): Unit = {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assertTrue(targetFile.exists() && targetFile.isFile())

    assertTrue(Files.exists(target))
    assertTrue(Files.exists(target, LinkOption.NOFOLLOW_LINKS))
  }

  @Test def filesExistsReportsExistingDirectoriesAsExisting(): Unit = {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assertTrue(targetFile.delete())
    assertTrue(targetFile.mkdir())
    assertTrue(targetFile.exists() && targetFile.isDirectory())

    assertTrue(Files.exists(target))
    assertTrue(Files.exists(target, LinkOption.NOFOLLOW_LINKS))
  }

  @Test def filesExistsReportsNonExistingFilesAsSuch(): Unit = {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assertTrue(targetFile.delete())
    assertFalse(targetFile.exists())
    assertFalse(Files.exists(target))
    assertFalse(Files.exists(target, LinkOption.NOFOLLOW_LINKS))
  }

  @Test def filesExistsHandlesSymlinks(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir         = dirFile.toPath()
      val existing    = new File(dirFile, "existing")
      val nonexisting = dir.resolve("nonexisting")
      val brokenLink  = dir.resolve("brokenlink")
      val correctLink = dir.resolve("correctlink")

      existing.createNewFile()
      assertTrue(existing.exists())

      Files.createSymbolicLink(brokenLink, nonexisting)
      Files.createSymbolicLink(correctLink, existing.toPath)
      assertFalse(Files.exists(brokenLink))
      assertTrue(Files.exists(brokenLink, LinkOption.NOFOLLOW_LINKS))
      assertTrue(Files.exists(correctLink))
      assertTrue(Files.exists(correctLink, LinkOption.NOFOLLOW_LINKS))
    }
  }

  @Test def filesCreateDirectoryCanCreateDirectory(): Unit = {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()
    assertTrue(targetFile.delete())

    Files.createDirectory(target)
    assertTrue(targetFile.exists())
    assertTrue(targetFile.isDirectory())
  }

  @Test def filesCreateDirectoryThrowsIfTheFileAlreadyExists(): Unit = {
    val targetFile = File.createTempFile("test", ".tmp")
    val target     = targetFile.toPath()

    assertThrows(classOf[FileAlreadyExistsException],
                 Files.createDirectory(target))
  }

  @Test def filesCreateDirectoriesCanCreateDirsIfNoneOfTheHierarchyExists()
      : Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val p1  = dir.resolve("p1")
      val p2  = p1.resolve("p2")
      val p3  = p2.resolve("p3")

      assertFalse(Files.exists(p1))
      assertFalse(Files.exists(p2))
      assertFalse(Files.exists(p3))

      Files.createDirectories(p3)
      assertTrue(Files.exists(p3))
    }
  }

  @Test def FilesCreateDirectoriesCanCreateDirsIfSomeOfTheHierarchyExists()
      : Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val p1  = dir.resolve("p1")
      val p2  = p1.resolve("p2")
      val p3  = p2.resolve("p3")
      val p4  = p3.resolve("p4")

      assertFalse(Files.exists(p1))
      assertFalse(Files.exists(p2))
      assertFalse(Files.exists(p3))
      assertFalse(Files.exists(p4))

      Files.createDirectories(p2)

      assertTrue(Files.exists(p1))
      assertTrue(Files.exists(p2))
      assertFalse(Files.exists(p3))
      assertFalse(Files.exists(p4))

      Files.createDirectories(p4)
      assertTrue(Files.exists(p1))
      assertTrue(Files.exists(p2))
      assertTrue(Files.exists(p3))
      assertTrue(Files.exists(p4))
    }
  }

  @Test def filesCreateFileCanCreateNewFile(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath()
      val file = dir.resolve("file")

      assertFalse(Files.exists(file))
      Files.createFile(file)
      assertTrue(Files.exists(file))
    }
  }

  @Test def filesCreateFileThrowsIfTheFileAlreadyExists(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath()
      val file = dir.resolve("file")

      assertFalse(Files.exists(file))
      Files.createFile(file)
      assertTrue(Files.exists(file))

      assertThrows(classOf[FileAlreadyExistsException], Files.createFile(file))
    }
  }

  @Test def filesCreateLinkCanCreateLinks(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath()
      val file = dir.resolve("file")
      val link = dir.resolve("link")

      Files.createFile(file)
      assertTrue(Files.exists(file))

      Files.createLink(link, file)
      assertTrue(Files.exists(link))
    }
  }

  @Test def filesCreateLinkThrowsIfTheFileAlreadyExists(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath()
      val file = dir.resolve("file")
      val link = dir.resolve("link")

      Files.createFile(file)
      Files.createFile(link)
      assertTrue(Files.exists(file))
      assertTrue(Files.exists(link))

      assertThrows(classOf[FileAlreadyExistsException],
                   Files.createLink(link, file))
    }
  }

  @Test def filesCreateTempDirectoryWorksWithoutParentDirectory(): Unit = {
    val dir = Files.createTempDirectory("tmp")
    assertTrue(Files.exists(dir))
    assertTrue(Files.isDirectory(dir))

    val file = dir.resolve("file")
    assertFalse(Files.exists(file))
    Files.createFile(file)
    assertTrue(Files.exists(file))
  }

  @Test def filesCreateTempDirectoryWorksWithParentDirectory(): Unit = {
    withTemporaryDirectory { parent =>
      val dir = Files.createTempDirectory(parent.toPath(), "tmp")
      assertTrue(dir.getParent() == parent.toPath())
      assertTrue(Files.exists(dir))
      assertTrue(Files.isDirectory(dir))

      val file = dir.resolve("file")
      assertFalse(Files.exists(file))
      Files.createFile(file)
      assertTrue(Files.exists(file))
    }
  }

  private val tempFile = "^a?\\d+\\.?(?:[a-z]*)$".r

  @Test def filesCreateTempDirectoryWorksWithNullPrefix(): Unit = {
    val dir = Files.createTempDirectory(null)
    try {
      assertTrue(tempFile.findFirstIn(dir.getFileName.toString).isDefined)
      assertTrue(Files.exists(dir))
      assertTrue(Files.isDirectory(dir))
    } finally Files.delete(dir)
  }

  @Test def filesCreateTempDirectoryWorksWithShortPrefix(): Unit = {
    val dir = Files.createTempDirectory("a")
    try {
      assertTrue(tempFile.findFirstIn(dir.getFileName.toString).isDefined)
      assertTrue(Files.exists(dir))
      assertTrue(Files.isDirectory(dir))
    } finally Files.delete(dir)
  }
  @Test def filesCreateTempFileWorksWithNullPrefix(): Unit = {
    val file = Files.createTempFile(null, "txt")
    try {
      assertTrue(tempFile.findFirstIn(file.getFileName.toString).isDefined)
      assertTrue(Files.exists(file))
      assertTrue(Files.isRegularFile(file))
    } finally Files.delete(file)
  }

  @Test def filesCreateTempFileWorksWithShortPrefix(): Unit = {
    val file = Files.createTempFile("a", null)
    try {
      assertTrue(tempFile.findFirstIn(file.getFileName.toString).isDefined)
      assertTrue(Files.exists(file))
      assertTrue(Files.isRegularFile(file))
    } finally Files.delete(file)
  }

  @Test def filesIsRegularFileReportsFilesAsSuch(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath
      val file = dir.resolve("file")
      Files.createFile(file)
      assertTrue(Files.exists(file))
      assertTrue(Files.isRegularFile(file))
    }
  }

  @Test def filesIsRegularFileHandlesDirectories(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath
      assertTrue(Files.exists(dir))
      assertFalse(Files.isRegularFile(dir))
    }
  }

  @Test def filesIsRegularFileHandlesSymlinks(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath
      val file = dir.resolve("file")
      val link = dir.resolve("link")
      Files.createFile(file)
      Files.createSymbolicLink(link, file)
      assertTrue(Files.exists(file))
      assertTrue(Files.isRegularFile(file))
      assertTrue(Files.isSymbolicLink(link))
      assertTrue(Files.isRegularFile(link))
      assertFalse(Files.isRegularFile(link, LinkOption.NOFOLLOW_LINKS))
    }
  }

  @Test def filesCreateTempFileWorksWithoutParentDirectory(): Unit = {
    val tmp = Files.createTempFile("temp", ".tmp")
    assertTrue(Files.exists(tmp))
    assertTrue(Files.isRegularFile(tmp))
  }

  @Test def filesCreateTempFileWorksWithParentDirectory(): Unit = {
    withTemporaryDirectory { parent =>
      val tmp = Files.createTempFile(parent.toPath(), "temp", ".tmp")
      assertTrue(tmp.getParent() == parent.toPath())
      assertTrue(Files.exists(tmp))
      assertTrue(Files.isRegularFile(tmp))
    }
  }

  @Test def filesDeleteCanDeleteFiles(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath()
      val file = dir.resolve("file")
      Files.createFile(file)

      assertTrue(Files.exists(file))
      assertTrue(Files.isRegularFile(file))
      Files.delete(file)
      assertFalse(Files.exists(file))
    }
  }

  @Test def filesDeleteCanDeleteEmptyDirectories(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir    = dirFile.toPath()
      val subdir = dir.resolve("subdir")
      Files.createDirectory(subdir)
      assertTrue(Files.exists(subdir))
      assertTrue(Files.isDirectory(subdir))
      Files.delete(subdir)
      assertFalse(Files.exists(subdir))
    }
  }

  @Test def filesDeleteThrowsWhenDeletingNonExistingFile(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir         = dirFile.toPath()
      val nonexisting = dir.resolve("nonexisting")
      assertFalse(Files.exists(nonexisting))

      assertThrows(classOf[NoSuchFileException], Files.delete(nonexisting))
    }
  }

  @Test def filesDeleteThrowsWhenDeletingNonEmptyDirectory(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir    = dirFile.toPath()
      val subdir = dir.resolve("subdir")
      val file   = subdir.resolve("file")
      Files.createDirectory(subdir)
      Files.createFile(file)
      assertTrue(Files.exists(subdir))
      assertTrue(Files.isDirectory(subdir))
      assertThrows(classOf[IOException], Files.delete(subdir))
    }
  }

  @Test def filesDeleteIfExistsWorksIfTheFileExists(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath()
      val file = dir.resolve("file")
      Files.createFile(file)
      assertTrue(Files.exists(file))
      assertTrue(Files.isRegularFile(file))
      assertTrue(Files.deleteIfExists(file))
      assertFalse(Files.exists(file))
    }
  }

  @Test def filesDeleteIfExistsWorksIfTheFileDoesNotExist(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath()
      val file = dir.resolve("file")
      assertFalse(Files.exists(file))
      assertFalse(Files.deleteIfExists(file))
      assertFalse(Files.exists(file))
    }
  }

  @Test def filesListListsFiles(): Unit = {
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
      assertTrue(Files.exists(d0) && Files.isDirectory(d0))
      assertTrue(Files.exists(f0) && Files.isRegularFile(f0))
      assertTrue(Files.exists(f1) && Files.isRegularFile(f1))
      assertTrue(Files.exists(f2) && Files.isRegularFile(f2))

      val it    = Files.list(dir).iterator()
      val files = scala.collection.mutable.Set.empty[Path]
      while (it.hasNext()) {
        files += it.next()
      }
      assertTrue(files.size == 3)
      assertTrue(files contains d0)
      assertTrue(files contains f0)
      assertTrue(files contains f1)
    }
  }

  @Test def filesReadSymbolicLinkCanReadValidSymbolicLink(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir  = dirFile.toPath()
      val link = dir.resolve("link")
      val file = dir.resolve("file")
      Files.createFile(file)
      Files.createSymbolicLink(link, file)

      assertTrue(Files.exists(file))
      assertTrue(Files.exists(link))
      assertTrue(Files.readSymbolicLink(link) == file)
    }
  }

  @Test def filesReadSymbolicLinkCanReadBrokenSymbolicLink(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir        = dirFile.toPath()
      val brokenLink = dir.resolve("link")
      val file       = dir.resolve("file")
      Files.createSymbolicLink(brokenLink, file)

      assertFalse(Files.exists(file))
      assertTrue(Files.exists(brokenLink, LinkOption.NOFOLLOW_LINKS))
      assertTrue(Files.readSymbolicLink(brokenLink) == file)
    }
  }

  @Test def filesWalkWalksDirectory(): Unit = {
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
      assertTrue(Files.exists(d0) && Files.isDirectory(d0))
      assertTrue(Files.exists(f0) && Files.isRegularFile(f0))
      assertTrue(Files.exists(f1) && Files.isRegularFile(f1))
      assertTrue(Files.exists(f2) && Files.isRegularFile(f2))

      val it    = Files.walk(dir).iterator()
      val files = scala.collection.mutable.Set.empty[Path]
      while (it.hasNext()) {
        files += it.next()
      }
      assertTrue(files.size == 5)
      assertTrue(files contains dir)
      assertTrue(files contains d0)
      assertTrue(files contains f2)
      assertTrue(files contains f0)
      assertTrue(files contains f1)
    }
  }

  @Test def filesWalkWalksSingleFile(): Unit = {
    withTemporaryDirectory { dirFile =>
      val f0 = dirFile.toPath.resolve("f0")

      Files.createFile(f0)
      assertTrue(Files.exists(f0) && Files.isRegularFile(f0))

      val it    = Files.walk(f0).iterator()
      val files = scala.collection.mutable.Set.empty[Path]
      while (it.hasNext) {
        files += it.next()
      }
      assertTrue(files.size == 1)
      assertTrue(files contains f0)
    }
  }

  @Test def filesWalkFollowsSymlinks(): Unit = {
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

      assertTrue(Files.exists(d0) && Files.isDirectory(d0))
      assertTrue(Files.exists(f0) && Files.isRegularFile(f0))
      assertTrue(Files.exists(f1) && Files.isRegularFile(f1))
      assertTrue(Files.exists(f2) && Files.isRegularFile(f2))

      val it    = Files.walk(d0, FileVisitOption.FOLLOW_LINKS).iterator()
      val files = scala.collection.mutable.Set.empty[Path]
      while (it.hasNext()) {
        files += it.next()
      }
      assertTrue(files.size == 5)
      assertTrue(files contains d0)
      assertTrue(files contains f0)
      assertTrue(files contains f1)
      assertTrue(files contains link)
    }
  }

  @Test def filesWalkDetectsCycles(): Unit = {
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
      assertTrue(expected contains it.next())
      assertTrue(expected contains it.next())
      assertThrows(classOf[FileSystemLoopException], it.next())
    }
  }

  @Test def filesWalkFileTreeWalksTheTree(): Unit = {
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
      assertTrue(Files.exists(d0) && Files.isDirectory(d0))
      assertTrue(Files.exists(f0) && Files.isRegularFile(f0))
      assertTrue(Files.exists(f1) && Files.isRegularFile(f1))
      assertTrue(Files.exists(f2) && Files.isRegularFile(f2))

      val visitor = new QueueingVisitor
      Files.walkFileTree(dir, visitor)
      val expected = Map(dir -> 2, d0 -> 2, f2 -> 1, f0 -> 1, f1 -> 1)
      val result   = scala.collection.mutable.Map.empty[Path, Int]
      while (!visitor.isEmpty) {
        val f     = visitor.dequeue()
        val count = result.getOrElse(f, 0)
        result(f) = count + 1
      }
      assertTrue(result == expected)
    }
  }

  @Test def filesWalkFileTreeCanBeTerminated(): Unit = {
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
      assertTrue(Files.exists(d0) && Files.isDirectory(d0))
      assertTrue(Files.exists(f0) && Files.isRegularFile(f0))
      assertTrue(Files.exists(f1) && Files.isRegularFile(f1))
      assertTrue(Files.exists(f2) && Files.isRegularFile(f2))

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
      assertTrue(found == expected)
    }
  }

  @Test def filesWalkFileTreeCanSkipSubtrees(): Unit = {
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
      assertTrue(Files.exists(d0) && Files.isDirectory(d0))
      assertTrue(Files.exists(f0) && Files.isRegularFile(f0))
      assertTrue(Files.exists(f1) && Files.isRegularFile(f1))
      assertTrue(Files.exists(f2) && Files.isRegularFile(f2))

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
      assertTrue(result == expected)
    }
  }

  @Test def filesWalkFileTreeCanSkipSiblings(): Unit = {
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
      assertTrue(Files.exists(d0) && Files.isDirectory(d0))
      assertTrue(Files.exists(f0) && Files.isRegularFile(f0))
      assertTrue(Files.exists(f1) && Files.isRegularFile(f1))
      assertTrue(Files.exists(f2) && Files.isRegularFile(f2))

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
      assertTrue(result == expected)
    }
  }

  // The next two tests were inspired by Issue #1354
  // "NIO File Walker fails on broken links."

  @Test def filesWalkFileTreeRespectsFileVisitOptionFollowLinksNo(): Unit = {
    withTemporaryDirectoryPath { dirPath =>
      val context = new FollowLinksTestsContext(dirPath)

      val visitor = new QueueingVisitor()

      // Will not follow links, so broken link does not throw exception.
      Files.walkFileTree(dirPath, visitor)

      val resultSet    = visitorToFileNamesSet(visitor)
      val resultLength = resultSet.size

      val expectedFileNamesSet = context.expectedFollowFilesSet()
      val expectedLength       = expectedFileNamesSet.size

      assertTrue(s"result length: $resultLength != expected: $expectedLength",
                 resultLength == expectedLength)

      assertTrue(s"result: ${resultSet} != expected: ${expectedFileNamesSet}",
                 resultSet == expectedFileNamesSet)
    }
  }

  @Test def filesWalkFileTreeRespectsFileVisitOptionFollowLinksYes(): Unit = {
    withTemporaryDirectoryPath { dirPath =>
      val context = new FollowLinksTestsContext(dirPath)

      val visitor = new QueueingVisitor()

      // Follow the broken link; expect a NoSuchFileException to be thrown.

      assertThrows(classOf[NoSuchFileException], {
        val fvoSet = Set(FileVisitOption.FOLLOW_LINKS).toJavaSet
        Files.walkFileTree(dirPath, fvoSet, Int.MaxValue, visitor)
      })
    }
  }

  @Test def filesFindFindsFiles(): Unit = {
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
      assertTrue(Files.exists(d0) && Files.isDirectory(d0))
      assertTrue(Files.exists(f0) && Files.isRegularFile(f0))
      assertTrue(Files.exists(f1) && Files.isRegularFile(f1))
      assertTrue(Files.exists(f2) && Files.isRegularFile(f2))

      val predicate = new BiPredicate[Path, BasicFileAttributes] {
        override def test(path: Path, attrs: BasicFileAttributes): Boolean =
          path.getFileName.toString.startsWith("f")
      }
      val it       = Files.find(dir, 10, predicate).iterator
      val expected = Set(f0, f1, f2)

      assertTrue(expected contains it.next())
      assertTrue(expected contains it.next())
      assertTrue(expected contains it.next())
    }
  }

  private def setupFindSymlinkContext(
      top: File,
      soughtName: String): Tuple4[Path, Path, Path, Path] = {

    // Create environment used to test both valid and invalid symlinks.

    val dir = top.toPath()
    val d1  = dir.resolve("d1")
    Files.createDirectory(d1)
    assertTrue(Files.exists(d1) && Files.isDirectory(d1))

    // f0 & f1 are just to give find() a bit more complicated case.
    val f0 = d1.resolve("f0")
    val f1 = d1.resolve("f1")
    Files.createFile(f0)
    Files.createFile(f1)

    val d2 = dir.resolve("d2")
    Files.createDirectory(d2)
    assertTrue(Files.exists(d2) && Files.isDirectory(d2))

    val symlinkTarget = d2

    val sought = d2.resolve(soughtName)
    Files.createFile(sought)
    assertTrue(Files.exists(sought) && Files.isRegularFile(sought))

    // Tricky bit here, symlink target is a directory, not a file.
    val symlink = d1.resolve("dirSymlink")
    Files.createSymbolicLink(symlink, symlinkTarget)

    assertTrue(Files.exists(symlink) && Files.isSymbolicLink(symlink))

    (dir, d1, d2, symlink)
  }

  @Test def filesFindRespectsFileVisitOptionFollowLinksValidSymlinks(): Unit = {
    withTemporaryDirectory { dirFile =>
      val soughtName             = "quaesitum" // That which is sought.
      val (dir, d1, d2, symlink) = setupFindSymlinkContext(dirFile, soughtName)

      val predicate = new BiPredicate[Path, BasicFileAttributes] {
        override def test(path: Path, attrs: BasicFileAttributes): Boolean =
          path.getFileName.toString == soughtName
      }

      // Test good symlink when following links.

      val itFollowGood =
        Files.find(d1, 10, predicate, FileVisitOption.FOLLOW_LINKS).iterator

      assertTrue(s"Should have found a Path when following symlinks",
                 itFollowGood.hasNext)

      val foundPath = itFollowGood.next
      val foundName = foundPath.getFileName.toString

      assertTrue(s"found: |$foundName| != expected: |$soughtName|",
                 foundName == soughtName)

      // Test good symlink when not following links.

      val itNoFollowGood = Files.find(d1, 10, predicate).iterator

      assertTrue(s"Should not have found anything when not following symlinks",
                 itNoFollowGood.hasNext == false)
    }
  }

  // Issue #1530
  @Test def filesFindRespectsFileVisitOptionFollowLinksBrokenSymlinks()
      : Unit = {
    withTemporaryDirectory { dirFile =>
      val soughtName             = "quaesitum" // That which is sought.
      val (dir, d1, d2, symlink) = setupFindSymlinkContext(dirFile, soughtName)

      val predicate = new BiPredicate[Path, BasicFileAttributes] {
        override def test(path: Path, attrs: BasicFileAttributes): Boolean =
          path.getFileName.toString == soughtName
      }

      // Break symlink.

      Files.move(d2, dir.resolve("notD2"))
      assertFalse("A1", Files.exists(d2)) // symlink target is gone
      // Now broken symlink itself should continue to exist
      assertTrue("A2.1", Files.exists(symlink, LinkOption.NOFOLLOW_LINKS))
      assertTrue("A3", Files.isSymbolicLink(symlink))

      // Test broken symlink when following links.

      assertThrows(
        classOf[NoSuchFileException],
        Files
          .find(d1, 10, predicate, FileVisitOption.FOLLOW_LINKS)
          .iterator
          .hasNext //used to materialize underlying LazyList (since 2.13)
      )

      // Test broken symlink when not following links.

      val itNotFollowBad = Files.find(d1, 10, predicate).iterator

      assertFalse(s"Should not have found a Path when following broken symlink",
                  itNotFollowBad.hasNext)
    }
  }

  @Test def filesGetLastModifiedTimeWorks(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")

      Files.createFile(f0)
      assertTrue(Files.exists(f0))

      val referenceMs = f0.toFile().lastModified()
      val filetimeMs  = Files.getLastModifiedTime(f0).toMillis()
      assertTrue(referenceMs == filetimeMs)
    }
  }

  @Test def filesGetAttributeCanFetchAttributesFromBasicFileAttributeView()
      : Unit = {
    withTemporaryDirectory { dirFile =>
      val d0 = dirFile.toPath()
      val f0 = d0.resolve("f0")

      Files.createFile(f0)
      assertTrue(Files.exists(f0))

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

      assertFalse(d0isReg)
      assertTrue(d0isDir)
      assertFalse(d0isSym)
      assertFalse(d0isOth)

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

      assertTrue(f0mtime.toMillis() == f0.toFile().lastModified())
      assertTrue(f0size == f0.toFile().length())
      assertTrue(f0isReg)
      assertFalse(f0isDir)
      assertFalse(f0isSym)
      assertFalse(f0isOth)
    }
  }

  @Test def filesGetAttributeObeysGivenLinkOption(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")
      val l0  = dir.resolve("l0")

      Files.createFile(f0)
      Files.createSymbolicLink(l0, f0)
      assertTrue(Files.exists(f0))
      assertTrue(Files.exists(l0))

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

      assertTrue(normalL0IsReg)
      assertFalse(noFollowL0IsReg)
      assertFalse(normalL0IsLink)
      assertTrue(noFollowL0IsLink)
    }
  }

  @Test def filesGetAttributeAcceptsViewName(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val isDir =
        Files.getAttribute(dir, "basic:isDirectory").asInstanceOf[Boolean]
      assertTrue(isDir)
    }
  }

  @Test def filesGetOwnerWorks(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir   = dirFile.toPath()
      val owner = Files.getOwner(dir)
      assertTrue(owner.getName().nonEmpty)
    }
  }

  @Test def filesGetPosixFilePermissionsWorks(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir    = dirFile.toPath()
      val f0     = dir.resolve("f0")
      val f0File = f0.toFile()

      Files.createFile(f0)
      assertTrue(Files.exists(f0))

      f0File.setReadable(true)
      f0File.setWritable(false)
      f0File.setExecutable(true)

      val permissions = Files.getPosixFilePermissions(f0)

      import PosixFilePermission._
      assertTrue(permissions.contains(OWNER_READ))
      assertFalse(permissions.contains(OWNER_WRITE))
      assertTrue(permissions.contains(OWNER_EXECUTE))
    }
  }

  @Test def filesLinesReturnsTheLines(): Unit = {
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

      assertTrue(Files.exists(f0))

      val it = Files.lines(f0).iterator()
      assertTrue(it.next() == "first line")
      assertTrue(it.next() == "second line")
      assertFalse(it.hasNext())
    }
  }

  @Test def filesWriteCanWriteToFile(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")

      val lines = new Iterable(Array("first line", "second line"))
      Files.write(f0, lines)

      val it = Files.lines(f0).iterator()
      assertTrue(it.next() == "first line")
      assertTrue(it.next() == "second line")
      assertFalse(it.hasNext())
    }
  }

  @Test def filesMoveMovesFiles(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")
      val f1  = dir.resolve("f1")

      val lines = new Iterable(Array("first line", "second line"))
      Files.write(f0, lines)
      assertTrue(Files.exists(f0))
      assertFalse(Files.exists(f1))
      Files.move(f0, f1)
      assertFalse(Files.exists(f0))
      assertTrue(Files.exists(f1))

      val it = Files.lines(f1).iterator
      assertTrue(it.next() == "first line")
      assertTrue(it.next() == "second line")
      assertFalse(it.hasNext())
    }
  }

  def moveDirectoryTest(delete: Boolean, options: CopyOption*) {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")
      Files.write(f0, "foo\n".getBytes)
      val target = Files.createTempDirectory(null)
      if (delete) assertTrue(Files.deleteIfExists(target))
      Files.move(dir, target, options: _*)
      assertFalse(Files.exists(dir))
      assertFalse(Files.exists(f0))

      val newF0 = target.resolve("f0")
      assertTrue(Files.exists(newF0))
      assertTrue(Files.lines(newF0).iterator().toScalaSeq.mkString == "foo")
    }
  }
  @Test def filesMoveDirectory(): Unit = {
    moveDirectoryTest(delete = true)
  }

  @Test def filesMoveReplaceDirectory(): Unit = {
    moveDirectoryTest(delete = false, REPLACE_EXISTING)
  }

  @Test def filesMoveDoesNotReplaceDirectory(): Unit = {
    assertThrows(classOf[FileAlreadyExistsException],
                 moveDirectoryTest(delete = false))
  }

  @Test def filesSetAttributeCanSetLastModifiedTime(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")

      Files.createFile(f0)
      assertTrue(Files.exists(f0))

      val time0 = Files.getAttribute(f0, "lastModifiedTime")
      val time1 = FileTime.fromMillis(1000)
      Files.setAttribute(f0, "lastModifiedTime", FileTime.fromMillis(1000))
      val time2 = Files.getAttribute(f0, "lastModifiedTime")

      assertTrue(time0 != time2)
      assertTrue(time1 == time2)
    }
  }

  @Test def filesSetAttributeCanSetLastAccessTime(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")

      Files.createFile(f0)
      assertTrue(Files.exists(f0))

      val time0 = Files.getAttribute(f0, "lastAccessTime")
      val time1 = FileTime.fromMillis(1000)
      Files.setAttribute(f0, "lastAccessTime", time1)
      val time2 = Files.getAttribute(f0, "lastAccessTime")

      assertTrue(time0 != time2)
      assertTrue(time1 == time2)
    }
  }

  @Test def filesSetAttributeCanSetPermissions(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")

      Files.createFile(f0)
      assertTrue(Files.exists(f0))

      val perm0 = Files.getAttribute(f0, "posix:permissions")
      val perm1 = PosixFilePermissions.fromString("rwxrwxrwx")
      Files.setAttribute(f0, "posix:permissions", perm1)
      val perm2 = Files.getAttribute(f0, "posix:permissions")

      assertTrue(perm0 != perm2)
      assertTrue(perm1 == perm2)
    }
  }

  @Test def filesReadAllLinesReturnsAllTheLines(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")

      val lines = new Iterable(Array("first line", "second line"))
      Files.write(f0, lines)
      assertTrue(Files.exists(f0))

      val list = Files.readAllLines(f0)
      assertTrue(list.size() == 2)
      assertTrue(list.get(0) == "first line")
      assertTrue(list.get(1) == "second line")
    }
  }

  @Test def filesReadAllBytesReadsAllBytes(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0  = dir.resolve("f0")
      val in  = new ByteArrayInputStream(Array(1, 2, 3))
      Files.copy(in, f0)
      assertTrue(Files.exists(f0))
      assertTrue(Files.size(f0) == 3)

      val bytes = Files.readAllBytes(f0)
      assertTrue(bytes(0) == 1)
      assertTrue(bytes(1) == 2)
      assertTrue(bytes(2) == 3)
    }
  }

  @Test def filesReadAttributesPathClassArrayLinkOptionWorks(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir   = dirFile.toPath
      val attrs = Files.readAttributes(dir, classOf[PosixFileAttributes])
      assertTrue(attrs.isDirectory())
      assertFalse(attrs.isOther())
      assertFalse(attrs.isRegularFile())
      assertFalse(attrs.isSymbolicLink())
    }
  }

  @Test def filesReadAttributesPathStringArrayLinkOptionWorks(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir   = dirFile.toPath
      val attrs = Files.readAttributes(dir, "posix:permissions,size")
      assertTrue(attrs.size == 2)
      assertTrue(attrs.containsKey("permissions"))
      assertTrue(attrs.containsKey("size"))
    }
  }

  @Test def filesReadAttributesPathStringArrayLinkOptionSupportsAsterisk()
      : Unit = {
    withTemporaryDirectory { dirFile =>
      val dir   = dirFile.toPath
      val attrs = Files.readAttributes(dir, "posix:*")
      assertFalse(attrs.isEmpty())
    }
  }

  @Test def filesNewByteChannelReturnsChannel(): Unit = {
    withTemporaryDirectory { dir =>
      val f = dir.toPath.resolve("f0")
      Files.write(f, Array[Byte](1, 2, 3))
      val channel = Files.newByteChannel(f)
      val buffer  = ByteBuffer.allocate(10)

      val read = channel.read(buffer)
      buffer.flip()

      assertTrue(buffer.limit() == 3)
      assertTrue(read == 3)
      assertTrue(buffer.get(0) == 1)
      assertTrue(buffer.get(1) == 2)
      assertTrue(buffer.get(2) == 3)
    }
  }

  @Test def newInputStreamReturnsAnInputStream(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath
      val f0  = dir.resolve("f0")
      Files.write(f0, Array[Byte](1, 2, 3))

      val in = Files.newInputStream(f0)
      assertTrue(in.read() == 1)
      assertTrue(in.read() == 2)
      assertTrue(in.read() == 3)
      assertTrue(in.read() == -1)
    }
  }

  @Test def newOutputStreamReturnsAnOutputStream(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath
      val f0  = dir.resolve("f0")
      val out = Files.newOutputStream(f0)

      assertTrue(Files.exists(f0))

      out.write(Array[Byte](1, 2, 3))

      val in = Files.newInputStream(f0)
      assertTrue(in.read() == 1)
      assertTrue(in.read() == 2)
      assertTrue(in.read() == 3)
      assertTrue(in.read() == -1)
    }
  }

  @Test def newOutputStreamHonorsOpenOptions(): Unit = {
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
      assertTrue(in0.read() == -1)

      val f1 = dir.resolve("f1")
      Files.createFile(f1)
      assertThrows(classOf[FileAlreadyExistsException],
                   Files.newOutputStream(f1, StandardOpenOption.CREATE_NEW))

      val f2 = dir.resolve("f2")
      assertThrows(classOf[NoSuchFileException],
                   Files.newOutputStream(f2, StandardOpenOption.WRITE))
    }
  }

  private class FollowLinksTestsContext(dirPath: Path) {

    final val fNames =
      Array(
        "missingtarget",
        "A0", // sort before "b" in "brokenlink"
        "z99" // sort after "m" in "missingtarget"
      )

    def expectedFollowFilesSet(): Set[String] = fNames.drop(1).toSet

    for (i <- 0 until fNames.length) {
      val f = dirPath.resolve(fNames(i))
      Files.createFile(f)
      assertTrue(Files.exists(f) && Files.isRegularFile(f))
    }

    val brokenLink    = dirPath.resolve("brokenlink")
    val missingTarget = dirPath.resolve(fNames(0))

    // Create valid symbolic link from brokenLink to missingTarget,
    // then remove missingTarget to break link.
    // This could probably be done in one step, but use two to avoid
    // filesystem optimizations and to more closely emulate what happens
    // in the real world.

    Files.createSymbolicLink(brokenLink, missingTarget)

    assertTrue(
      s"File '${brokenLink}' does not exist or is not a symbolic link.",
      Files.exists(brokenLink) && Files.isSymbolicLink(brokenLink))

    Files.delete(missingTarget)

    assertFalse(s"File '${missingTarget}' should not exist.",
                Files.exists(missingTarget))
  }

  private def visitorToFileNamesSet(v: QueueingVisitor): Set[String] = {
    v.dequeue() // skip temp directory pre-visit.

    // -1 to skip temp directory post-visit
    val nStrings = v.length - 1
    val strings  = new Array[String](nStrings)

    for (i <- 0 until nStrings) {
      strings(i) = v.dequeue().getFileName.toString
    }

    strings.toSet
  }
}

object FilesTest {
  def makeTemporaryDir(): File = {
    val file = File.createTempFile("test", ".tmp")
    assertTrue(file.delete())
    assertTrue(file.mkdir())
    file
  }

  def withTemporaryDirectory(fn: File => Unit) {
    fn(makeTemporaryDir())
  }

  def withTemporaryDirectoryPath(fn: Path => Unit) {
    fn(makeTemporaryDir().toPath)
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
  def length()           = visited.length

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
