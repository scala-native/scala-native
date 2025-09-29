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
}
