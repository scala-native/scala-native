package org.scalanative.testsuite.javalib.nio.file

import java.io.File
import java.net.URI
import java.nio.file._

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform.isWindows

class PathsTest {
  @Test def pathsGetRelativePathReturnsPathRelativeToCwd(): Unit = {
    val pathString = if (isWindows) raw"foo\bar" else "foo/bar"
    val path = Paths.get(pathString)
    val file = new File(pathString)
    assertEquals(pathString, path.toString)
    assertTrue(path.toAbsolutePath.toString != path.toString)
    assertTrue(path.toAbsolutePath.toString.endsWith(path.toString))

    assertTrue(file.getAbsolutePath != path.toString)
    assertEquals(path.toAbsolutePath.toString, file.getAbsolutePath)
  }

  @Test def pathsGetAbsolutePathReturnsAnAbsolutePath(): Unit = {
    val pathString = if (isWindows) raw"C:\foo\bar" else "/foo/bar"

    val path = Paths.get(pathString)
    val file = new File(pathString)
    assertEquals(pathString, path.toString)
    assertEquals(path.toString, path.toAbsolutePath.toString)

    assertEquals(path.toString, file.getAbsolutePath)
    assertEquals(path.toAbsolutePath.toString, file.getAbsolutePath)
  }

  @Test def pathsGetUriThrowsExceptionWhenSchemeIsMissing(): Unit = {
    assertThrows(
      classOf[IllegalArgumentException],
      Paths.get(new URI(null, null, null, 0, "foo", null, null))
    )
  }

  @Test def pathsGetUriThrowsExceptionWhenSchemeIsNotFile(): Unit = {
    assertThrows(
      classOf[FileSystemNotFoundException],
      Paths.get(new URI("http", null, "google.com", 0, "/", null, null))
    )
  }

  @Test def pathsGetUriReturnsPathIfSchemeIsFile(): Unit = {
    val pathString1 = if (isWindows) "/C:/foo/bar" else "/foo/bar"
    val expected1 = if (isWindows) raw"C:\foo\bar" else pathString1
    val pathString2 = if (isWindows) "/C:/hello/world" else "/hello/world"
    val expected2 = if (isWindows) raw"C:\hello\world" else pathString2

    val path =
      Paths.get(new URI("file", null, null, 0, pathString1, null, null))
    assertEquals(expected1, path.toString)

    val path2 =
      Paths.get(new URI("fIlE", null, null, 0, pathString2, null, null))
    assertEquals(expected2, path2.toString)
  }

  @Test def driveRelativePathToStringShownAsAbsolute() = {
    val absolutePath = "/absolute/file"
    val expected = if (isWindows) "\\absolute\\file" else "/absolute/file"

    val path = Paths.get(absolutePath)

    assertEquals(expected, path.toString)
  }

  // issue #2433
  @Test def spaceAllowedInPath() = {
    val withSpaces = "space dir/space file"
    val expected = if (isWindows) raw"space dir\space file" else withSpaces

    val path = Paths.get("space dir/space file")
    assertEquals(expected, path.toString)
  }

  @Test def joiningEmptyIsEmpty() = {
    assertEquals(Paths.get(""), Paths.get("", ""))
  }

  // issue #3212
  @Test def emptyPathHasSingleEmptyName(): Unit = {
    val empty = Paths.get("")
    assertEquals("", empty.toString)
    assertEquals(1, empty.getNameCount)
    assertEquals(empty, empty.getName(0))
    assertEquals(empty, empty.getFileName)
    assertEquals(empty, empty.subpath(0, 1))
    assertNull(empty.getParent)
    assertNull(empty.getRoot)
    assertFalse(empty.isAbsolute)
    assertTrue(empty.iterator().hasNext)
    assertEquals(empty, empty.iterator().next())
    assertEquals(empty, empty.normalize)
    assertEquals(empty, Paths.get("foo").relativize(Paths.get("foo")))
    assertEquals(1, Paths.get("foo").relativize(Paths.get("foo")).getNameCount)
    assertEquals(empty, Paths.get("foo/..").normalize)
    assertEquals(1, Paths.get("foo/..").normalize.getNameCount)
    assertTrue(empty.startsWith(empty))
    assertTrue(empty.endsWith(empty))
    assertEquals(Paths.get("foo"), empty.resolve("foo"))
    assertEquals(Paths.get("foo"), Paths.get("foo").resolve(empty))
    assertThrows(classOf[IllegalArgumentException], empty.subpath(0, 2))
    assertThrows(classOf[IllegalArgumentException], empty.getName(1))
  }

  @Test def emptyPathToAbsolutePathIsCurrentDirectory(): Unit = {
    val cwd = Paths.get(System.getProperty("user.dir"))
    assertEquals(cwd, Paths.get("").toAbsolutePath)
    assertEquals(cwd, Paths.get(".").toAbsolutePath.normalize)
  }
}
