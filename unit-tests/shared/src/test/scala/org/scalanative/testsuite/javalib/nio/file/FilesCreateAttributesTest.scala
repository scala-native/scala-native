package org.scalanative.testsuite.javalib.nio.file

import java.nio.file._
import java.nio.file.attribute._
import java.{util => ju}

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform
import org.scalanative.testsuite.utils.Platform.isWindows

/* Initial attributes given to Files.create* are applied when the file is
 * created (Issue #3158): the permissions are passed to the system call, so
 * the process umask applies to them, as it does on the JVM.
 */
class FilesCreateAttributesTest {

  private def perms(s: String): FileAttribute[ju.Set[PosixFilePermission]] =
    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(s))

  private def attr(n: String, v: AnyRef): FileAttribute[AnyRef] =
    new FileAttribute[AnyRef] {
      def name(): String = n
      def value(): AnyRef = v
    }

  private val lastModified =
    attr("basic:lastModifiedTime", FileTime.fromMillis(0))

  private def permsOf(p: Path): String =
    PosixFilePermissions.toString(
      Files.getPosixFilePermissions(p, LinkOption.NOFOLLOW_LINKS)
    )

  private def withDir(fn: Path => Unit): Unit = {
    val dir = Files.createTempDirectory("FilesCreateAttributesTest")
    try fn(dir)
    finally {
      val it = Files.list(dir).iterator()
      while (it.hasNext()) Files.delete(it.next())
      Files.delete(dir)
    }
  }

  private def isEmptyDir(dir: Path): Boolean = {
    val s = Files.list(dir)
    try s.count() == 0L
    finally s.close()
  }

  /* Expected permissions after the umask: the bits of `requested` that a
   * directory created with the default mode 0777 also has.
   */
  private def masked(requested: String, allowed: String): String =
    requested
      .zip(allowed)
      .map { case (r, a) => if (a == '-') '-' else r }
      .mkString

  private def umaskAllowed(dir: Path): String =
    permsOf(Files.createDirectory(dir.resolve("umask-probe")))

  @Test def createFileUsesPermissionsMaskedByUmask(): Unit = {
    assumeFalse("POSIX permissions", isWindows)
    withDir { dir =>
      val allowed = umaskAllowed(dir)
      for ((p, i) <- Seq("rwxrwxrwx", "rw-r-----", "r--------").zipWithIndex) {
        val f = Files.createFile(dir.resolve(s"f$i"), perms(p))
        assertEquals(s"file $p", masked(p, allowed), permsOf(f))
        val d = Files.createDirectory(dir.resolve(s"d$i"), perms(p))
        assertEquals(s"dir $p", masked(p, allowed), permsOf(d))
      }
      val u = Files.createFile(
        dir.resolve("unix"),
        attr("unix:permissions", PosixFilePermissions.fromString("rwx------"))
      )
      assertEquals("unix:permissions", masked("rwx------", allowed), permsOf(u))
    }
  }

  @Test def createFileWithoutAttributesUsesDefaultMode(): Unit = {
    assumeFalse("POSIX permissions", isWindows)
    withDir { dir =>
      val allowed = umaskAllowed(dir)
      val f = Files.createFile(dir.resolve("f"))
      assertEquals(masked("rw-rw-rw-", allowed), permsOf(f))
    }
  }

  @Test def lastPermissionsAttributeWins(): Unit = {
    assumeFalse("POSIX permissions", isWindows)
    withDir { dir =>
      val allowed = umaskAllowed(dir)
      val f1 = Files.createFile(
        dir.resolve("f1"),
        perms("rwx------"),
        perms("r--------")
      )
      assertEquals(masked("r--------", allowed), permsOf(f1))
      val f2 = Files.createFile(
        dir.resolve("f2"),
        perms("r--------"),
        perms("rwx------")
      )
      assertEquals(masked("rwx------", allowed), permsOf(f2))
    }
  }

  @Test def createTempUsesOwnerOnlyDefaults(): Unit = {
    assumeFalse("POSIX permissions", isWindows)
    withDir { dir =>
      val allowed = umaskAllowed(dir)
      val f = Files.createTempFile(dir, "p", ".s")
      assertEquals("file", masked("rw-------", allowed), permsOf(f))
      val d = Files.createTempDirectory(dir, "p")
      assertEquals("dir", masked("rwx------", allowed), permsOf(d))
    }
  }

  @Test def createTempUsesPermissionsMaskedByUmask(): Unit = {
    assumeFalse("POSIX permissions", isWindows)
    withDir { dir =>
      val allowed = umaskAllowed(dir)
      val f = Files.createTempFile(dir, "p", ".s", perms("rwxrwxrwx"))
      assertTrue(Files.isRegularFile(f))
      assertEquals("file", masked("rwxrwxrwx", allowed), permsOf(f))
      val d = Files.createTempDirectory(dir, "p", perms("rwxrwxrwx"))
      assertTrue(Files.isDirectory(d))
      assertEquals("dir", masked("rwxrwxrwx", allowed), permsOf(d))
    }
  }

  @Test def unsupportedAttributeCreatesNothing(): Unit = {
    val unsupported =
      if (isWindows)
        Seq(
          lastModified,
          attr("dos:hidden", java.lang.Boolean.TRUE),
          perms("rw-------")
        )
      else
        Seq(
          lastModified,
          attr("dos:hidden", java.lang.Boolean.TRUE),
          attr("posix:owner", null),
          attr("permissions", ju.Collections.emptySet[PosixFilePermission]())
        )
    withDir { dir =>
      for (a <- unsupported) {
        val name = a.name()
        assertThrows(
          name,
          classOf[UnsupportedOperationException],
          Files.createFile(dir.resolve("f"), a)
        )
        assertThrows(
          name,
          classOf[UnsupportedOperationException],
          Files.createDirectory(dir.resolve("d"), a)
        )
        assertThrows(
          name,
          classOf[UnsupportedOperationException],
          Files.createTempFile(dir, "p", ".s", a)
        )
        assertThrows(
          name,
          classOf[UnsupportedOperationException],
          Files.createTempDirectory(dir, "p", a)
        )
        assertTrue(s"nothing created for $name", isEmptyDir(dir))
      }
    }
  }

  @Test def attributesAreCheckedBeforeExistence(): Unit = {
    withDir { dir =>
      val f = Files.createFile(dir.resolve("f"))
      assertThrows(
        classOf[UnsupportedOperationException],
        Files.createFile(f, lastModified)
      )
      assertThrows(
        classOf[FileAlreadyExistsException],
        Files.createFile(f)
      )
    }
  }

  @Test def invalidPermissionsValueCreatesNothing(): Unit = {
    assumeFalse("POSIX permissions", isWindows)
    withDir { dir =>
      assertThrows(
        classOf[ClassCastException],
        Files.createFile(dir.resolve("f"), attr("posix:permissions", "rwx"))
      )
      assertThrows(
        classOf[NullPointerException],
        Files.createFile(dir.resolve("f"), attr("posix:permissions", null))
      )
      assertTrue(isEmptyDir(dir))
    }
  }

  @Test def symbolicLinkRejectsInitialAttributes(): Unit = {
    assumeTrue(
      "Symbolic link creation requires admin privileges or Developer Mode on Windows",
      Platform.canCreateSymbolicLinks
    )
    withDir { dir =>
      val target = Files.createFile(dir.resolve("target"))
      val link = dir.resolve("link")
      assertThrows(
        classOf[UnsupportedOperationException],
        Files.createSymbolicLink(link, target, perms("rwx------"))
      )
      assertFalse(Files.exists(link, LinkOption.NOFOLLOW_LINKS))
    }
  }
}
