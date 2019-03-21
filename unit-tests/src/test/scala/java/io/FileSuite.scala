package java.io

import java.net.URI

object FileSuite extends tests.Suite {

  test("Try to create a `File` with a bad `URI`") {
    val badURIs = Array(
      new URI("http://foo.com/"),
      new URI("relpath/foo.txt"),
      new URI("file://path?foo=bar"),
      new URI("file://path#frag"),
      new URI("file://user@host/path"),
      new URI("file://host:8080/path")
    )

    for (uri <- badURIs) {
      assertThrows[IllegalArgumentException] { new File(uri) }
    }
  }

  test("Get a `URI` from a `File`") {
    val u1 = new File("path").toURI
    assertNotNull(u1)
    assert(u1.getScheme == "file")
    assert(u1.getPath.endsWith("path"))

    val u2 = new File("/path/to/file.txt").toURI
    assertNotNull(u2)
    assert(u2.getScheme == "file")
    assert(u2.getPath.endsWith("file.txt"))
    assert(u2.toString == "file:/path/to/file.txt")
  }
}
