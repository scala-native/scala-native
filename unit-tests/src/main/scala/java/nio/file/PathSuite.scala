package java.nio.file

object PathSuite extends tests.Suite {

  test("Path.getNameCount()") {
    assert(Paths.get("/").getNameCount == 0)
    assert(Paths.get("///").getNameCount == 0)
    assert(Paths.get("").getNameCount == 1)
    assert(Paths.get("foo").getNameCount == 1)
    assert(Paths.get("foo//bar").getNameCount == 2)
    assert(Paths.get("foo/bar/baz").getNameCount == 3)
    assert(Paths.get("/foo/bar/baz").getNameCount == 3)
  }

  test("Path.getName") {
    assert(Paths.get("").getName(0).toString == "")
    assert(Paths.get("foo").getName(0).toString == "foo")
    assert(Paths.get("foo//bar").getName(0).toString == "foo")
    assert(Paths.get("foo//bar").getName(1).toString == "bar")

    assert(Paths.get("foo/bar/baz").getName(0).toString == "foo")
    assert(Paths.get("foo/bar/baz").getName(1).toString == "bar")
    assert(Paths.get("foo/bar/baz").getName(2).toString == "baz")

    assert(Paths.get("/foo/bar/baz").getName(0).toString == "foo")
    assert(Paths.get("/foo/bar/baz").getName(1).toString == "bar")
    assert(Paths.get("/foo/bar/baz").getName(2).toString == "baz")

  }

  test("Path.endsWith with absolute path") {
    assert(Paths.get("/foo/bar/baz").endsWith(Paths.get("baz")))
    assert(!Paths.get("/foo/bar/baz").endsWith(Paths.get("/baz")))
    assert(Paths.get("/foo/bar/baz").endsWith(Paths.get("bar/baz")))
    assert(!Paths.get("/foo/bar/baz").endsWith(Paths.get("/bar/baz")))
    assert(Paths.get("/foo/bar/baz").endsWith(Paths.get("foo/bar/baz")))
    assert(Paths.get("/foo/bar/baz").endsWith(Paths.get("/foo/bar/baz")))
  }

  test("Path.endsWith with relative path") {
    assert(Paths.get("foo/bar/baz").endsWith(Paths.get("baz")))
    assert(!Paths.get("foo/bar/baz").endsWith(Paths.get("/baz")))
    assert(Paths.get("foo/bar/baz").endsWith(Paths.get("bar/baz")))
    assert(!Paths.get("foo/bar/baz").endsWith(Paths.get("/bar/baz")))
    assert(Paths.get("foo/bar/baz").endsWith(Paths.get("foo/bar/baz")))
    assert(!Paths.get("foo/bar/baz").endsWith(Paths.get("/foo/bar/baz")))
  }

  test("Path.getFileName") {
    assert(Paths.get("").getFileName.toString == "")
    assert(Paths.get("foo").getFileName.toString == "foo")
    assert(Paths.get("/foo").getFileName.toString == "foo")
    assert(Paths.get("foo/bar").getFileName.toString == "bar")
    assert(Paths.get("/foo/bar").getFileName.toString == "bar")
  }

  test("Path.subpath") {
    assert(Paths.get("").subpath(0, 1).toString == "")
    assertThrows[IllegalArgumentException] {
      Paths.get("").subpath(0, 2)
    }

    assert(Paths.get("foo/bar/baz").subpath(0, 1).toString == "foo")
    assert(Paths.get("foo/bar/baz").subpath(0, 2).toString == "foo/bar")
    assert(Paths.get("foo/bar/baz").subpath(0, 3).toString == "foo/bar/baz")
    assert(Paths.get("foo/bar/baz").subpath(1, 3).toString == "bar/baz")
    assert(Paths.get("foo/bar/baz").subpath(2, 3).toString == "baz")

    assert(Paths.get("/foo/bar/baz").subpath(0, 1).toString == "foo")
    assert(Paths.get("/foo/bar/baz").subpath(0, 2).toString == "foo/bar")
    assert(Paths.get("/foo/bar/baz").subpath(0, 3).toString == "foo/bar/baz")
    assert(Paths.get("/foo/bar/baz").subpath(1, 3).toString == "bar/baz")
    assert(Paths.get("/foo/bar/baz").subpath(2, 3).toString == "baz")
  }

}
