package org.scalanative.testsuite.javalib.nio.file

import java.nio.file._

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

class FilesCreateTempTest {

  private def createTemp(isDir: Boolean, dir: Path, prefix: String): Path =
    if (isDir) Files.createTempDirectory(dir, prefix)
    else Files.createTempFile(dir, prefix, ".tmp")

  private def assertNamesChild(e: Exception, parent: Path): Unit = {
    val msg = e.getMessage
    assertNotNull(s"${e.getClass.getName} has no message", msg)
    assertTrue(
      s"message '$msg' does not name a path in '$parent'",
      msg.startsWith(parent.toString + parent.getFileSystem.getSeparator)
    )
  }

  private def missingParent(isDir: Boolean): Unit =
    withTempDirectory("create-temp-missing") { root =>
      val missing = root.resolve("missing")
      val e = assertThrows(
        classOf[NoSuchFileException],
        createTemp(isDir, missing, "p")
      )
      assertNamesChild(e, missing)
      assertEquals(missing.toString, Paths.get(e.getFile).getParent.toString)
    }

  @Test def createTempDirectoryInMissingDirectory(): Unit =
    missingParent(isDir = true)

  @Test def createTempFileInMissingDirectory(): Unit =
    missingParent(isDir = false)

  private def parentIsFile(isDir: Boolean): Unit =
    withTempDirectory("create-temp-parent-file") { root =>
      val file = Files.createFile(root.resolve("plain"))
      val e = assertThrows(
        classOf[FileSystemException],
        createTemp(isDir, file, "p")
      )
      assertNamesChild(e, file)
    }

  @Test def createTempDirectoryInRegularFile(): Unit =
    parentIsFile(isDir = true)

  @Test def createTempFileInRegularFile(): Unit =
    parentIsFile(isDir = false)

  private def readOnlyParent(isDir: Boolean): Unit = {
    assumeFalse("POSIX permissions", Platform.isWindows)
    withTempDirectory("create-temp-read-only") { root =>
      val ro = Files.createDirectory(root.resolve("ro"))
      assumeTrue(ro.toFile.setWritable(false))
      try {
        assumeFalse("running as root", Files.isWritable(ro))
        val e = assertThrows(
          classOf[AccessDeniedException],
          createTemp(isDir, ro, "p")
        )
        assertNamesChild(e, ro)
      } finally ro.toFile.setWritable(true)
    }
  }

  @Test def createTempDirectoryInReadOnlyDirectory(): Unit =
    readOnlyParent(isDir = true)

  @Test def createTempFileInReadOnlyDirectory(): Unit =
    readOnlyParent(isDir = false)

  @Test def createTempRejectsSeparatorInPrefixOrSuffix(): Unit =
    withTempDirectory("create-temp-invalid") { root =>
      def check(op: => Path): Unit = {
        val e = assertThrows(classOf[IllegalArgumentException], op)
        assertEquals("Invalid prefix or suffix", e.getMessage)
      }
      check(Files.createTempDirectory(root, "a/b"))
      check(Files.createTempDirectory("a/b"))
      check(Files.createTempFile(root, "a/b", ".tmp"))
      check(Files.createTempFile("a/b", ".tmp"))
      check(Files.createTempFile(root, "p", "a/b"))
      if (Platform.isWindows) {
        check(Files.createTempDirectory(root, "a\\b"))
        check(Files.createTempFile(root, "p", "a\\b"))
      }
      assertEquals(0, root.toFile.list().length)
    }

  private def withTempDirectory(prefix: String)(use: Path => Unit): Unit = {
    val root = Files.createTempDirectory(prefix)
    try use(root)
    finally remove(root)
  }

  private def remove(path: Path): Unit =
    if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
      if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
        path.toFile.setWritable(true)
        val children = Files.list(path)
        try {
          val it = children.iterator()
          while (it.hasNext()) remove(it.next())
        } finally children.close()
      }
      Files.delete(path)
    }
}
