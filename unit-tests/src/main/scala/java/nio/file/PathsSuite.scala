package java.nio.file

import java.io.File

object PathsSuite extends tests.Suite {
  test("Paths.get(relative path) returns a path relative to cwd") {
    val path = Paths.get("foo/bar")
    val file = new File("foo/bar")
    assert(path.toString == "foo/bar")
    assert(path.toAbsolutePath.toString != path.toString)
    assert(path.toAbsolutePath.toString.endsWith(path.toString))

    assert(file.getAbsolutePath != path.toString)
    assert(file.getAbsolutePath == path.toAbsolutePath.toString)
  }

  test("Paths.get(absolute path) returns an absolute path") {
    val path = Paths.get("/foo/bar")
    val file = new File("/foo/bar")
    assert(path.toString == "/foo/bar")
    assert(path.toAbsolutePath.toString == path.toString)

    assert(file.getAbsolutePath == path.toString)
    assert(file.getAbsolutePath == path.toAbsolutePath.toString)
  }
}
