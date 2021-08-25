package javalib.io

import java.io._

import java.net.URI

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows.assertThrows

class FileTest {

  @Test def tryToCreateFileWithBadUri(): Unit = {
    val badURIs = Array(
      new URI("http://foo.com/"),
      new URI("relpath/foo.txt"),
      new URI("file://path?foo=bar"),
      new URI("file://path#frag"),
      new URI("file://user@host/path"),
      new URI("file://host:8080/path")
    )

    for (uri <- badURIs) {
      assertThrows(classOf[IllegalArgumentException], new File(uri))
    }
  }

  @Test def getUriFromFile(): Unit = {
    val u1 = new File("path").toURI
    assertNotNull(u1)
    assertTrue(u1.getScheme == "file")
    assertTrue(u1.getPath.endsWith("path"))

    val u2 = new File("/path/to/file.txt").toURI
    assertNotNull(u2)
    assertTrue(u2.getScheme == "file")
    assertTrue(u2.getPath.endsWith("file.txt"))
    assertTrue(u2.toString == "file:/path/to/file.txt")
  }
}
