package javalib.io

import java.io._

import java.net.URI

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform.isWindows

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
    assertEquals("file", u1.getScheme)
    assertTrue(u1.getPath.endsWith("path"))

    val absPathString =
      if (isWindows) raw"C:\path\to\file.txt"
      else "/path/to/file.txt"
    val expectedPath =
      if (isWindows) "/C:/path/to/file.txt"
      else absPathString
    val u2 = new File(absPathString).toURI
    assertNotNull(u2)
    assertEquals("file", u2.getScheme)
    assertEquals(expectedPath, u2.getPath())
    assertEquals(s"file:$expectedPath", u2.toString)
  }
}
