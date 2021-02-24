package java.security

import java.util.Date

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows._

/**
 * Tests for [[MessageDigest]] class fields and methods
 */
class MessageDigestTest {

  @Test def basicMd5Digest(): Unit = {
    val instance = java.security.MessageDigest.getInstance("MD5")
    val result   = instance.digest("Hello world!".getBytes("UTF-8"))
    val expected = Array[Byte](-122, -5, 38, -99, 25, 13, 44, -123, -10, -32,
      70, -116, -20, -92, 42, 32)

    assertTrue(result.sameElements((expected)))
  }

  @Test def emptyMd5Digest(): Unit = {
    val instance = java.security.MessageDigest.getInstance("MD5")
    val result   = instance.digest(new Array[Byte](0))
    val expected = Array[Byte](-44, 29, -116, -39, -113, 0, -78, 4, -23, -128,
      9, -104, -20, -8, 66, 126)

    assertTrue(result.sameElements((expected)))
  }

}
