package java.nio.file

import java.io.File
import java.net.URI
import scala.scalanative.runtime.Platform

object PathsSuite extends tests.Suite {

  test("Paths.isAbsolute") {
    assert(Paths.get("/foo/bar").isAbsolute)
    assertNot(Paths.get(".").isAbsolute)
    assertNot(Paths.get("..").isAbsolute)
  }

  test("Paths.get(relative path) returns a path relative to cwd") {
    val path = Paths.get("foo/bar")
    val file = new File("foo/bar")

    if (Platform.isWindows)
      assert(path.toString == "foo\\bar")
    else
      assert(path.toString == "foo/bar")
    assert(path.toAbsolutePath.toString != path.toString)
    assert(path.toAbsolutePath.toString.endsWith(path.toString))

    assert(file.getAbsolutePath != path.toString)
    assert(file.getAbsolutePath == path.toAbsolutePath.toString)
  }

  test("Paths.get(absolute path) returns an absolute path") {
    val path = Paths.get("/foo/bar")
    val file = new File("/foo/bar")

    if (Platform.isWindows)
      assert(path.toString == "\\foo\\bar")
    else
      assert(path.toString == "/foo/bar")
    assert(path.toAbsolutePath.toString == path.toString)

    assert(file.getAbsolutePath == path.toString)
    assert(file.getAbsolutePath == path.toAbsolutePath.toString)
  }

  test("Paths.get(URI) throws an exception when the scheme is missing") {
    assertThrows[IllegalArgumentException] {
      Paths.get(new URI(null, null, null, 0, "foo", null, null))
    }
  }

  test(
    "Paths.get(URI) throws an exception when the scheme is different from `file`") {
    assertThrows[FileSystemNotFoundException] {
      Paths.get(new URI("http", null, "google.com", 0, "/", null, null))
    }
  }

  test("Paths.get(URI) returns a path if the scheme is `file`") {
    val path =
      Paths.get(new URI("file", null, null, 0, "/foo/bar", null, null))
    assert(path.toString == Paths.get("/foo/bar").toString)

    val path2 =
      Paths.get(new URI("fIlE", null, null, 0, "/hello/world", null, null))
    assert(path2.toString == Paths.get("/hello/world").toString)
  }
}
