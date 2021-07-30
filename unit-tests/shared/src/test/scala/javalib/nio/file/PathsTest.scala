package javalib.nio.file

import java.nio.file._
import java.io.File
import java.net.URI

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows.assertThrows

class PathsTest {
  @Test def pathsGetRelativePathReturnsPathRelativeToCwd(): Unit = {
    val path = Paths.get("foo/bar")
    val file = new File("foo/bar")
    assertTrue(path.toString == "foo/bar")
    assertTrue(path.toAbsolutePath.toString != path.toString)
    assertTrue(path.toAbsolutePath.toString.endsWith(path.toString))

    assertTrue(file.getAbsolutePath != path.toString)
    assertTrue(file.getAbsolutePath == path.toAbsolutePath.toString)
  }

  @Test def pathsGetAbsolutePathReturnsAnAbsolutePath(): Unit = {
    val path = Paths.get("/foo/bar")
    val file = new File("/foo/bar")
    assertTrue(path.toString == "/foo/bar")
    assertTrue(path.toAbsolutePath.toString == path.toString)

    assertTrue(file.getAbsolutePath == path.toString)
    assertTrue(file.getAbsolutePath == path.toAbsolutePath.toString)
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
    val path =
      Paths.get(new URI("file", null, null, 0, "/foo/bar", null, null))
    assertTrue(path.toString == "/foo/bar")

    val path2 =
      Paths.get(new URI("fIlE", null, null, 0, "/hello/world", null, null))
    assertTrue(path2.toString == "/hello/world")
  }
}
