package org.scalanative.testsuite.javalib.security

import java.security._

// Ported from Harmony

import java.util.Date

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/** Tests for [[Timestamp]] class fields and methods
 */
class TimestampTest {

  private val now: Date = new Date()

  case object MockCertificate extends java.security.cert.Certificate("") {
    override def getEncoded: Array[Byte] = Array.empty[Byte]

    override def getPublicKey: PublicKey = null

    override def verify(key: PublicKey): Unit = ()

    override def verify(key: PublicKey, sigProvider: String): Unit = ()

    override def toString: String = "MockCertificate"

    override def equals(other: scala.Any): Boolean = true
  }

  case object MockCertPath extends java.security.cert.CertPath("") {

    override def getEncoded: Array[Byte] = Array.empty[Byte]

    override def getEncoded(encoding: String): Array[Byte] = Array.empty[Byte]

    override def getCertificates: java.util.List[cert.Certificate] = {
      val certificates = new java.util.ArrayList[cert.Certificate]()
      certificates.add(MockCertificate)
      certificates
    }

    override def getEncodings: java.util.Iterator[String] = null
  }

  @Test def constructor(): Unit = {
    // Check that nulls are not accepted.
    assertThrows(
      classOf[NullPointerException],
      new Timestamp(null, MockCertPath)
    )
    assertThrows(classOf[NullPointerException], new Timestamp(now, null))
  }

  @Test def testEquals(): Unit = {
    val one = new Timestamp(now, MockCertPath)
    val two = new Timestamp(now, MockCertPath)

    assertTrue(one.equals(one))
    assertTrue(one.equals(two))
    assertTrue(two.equals(one))
    assertTrue(one != null)
    assertFalse(one.equals(new Object()))

    val two1 = new Timestamp(new Date(9999), MockCertPath)
    assertFalse(one.equals(two1))
    assertTrue(two1.equals(two1))
  }

  @Test def getSignerCertPath(): Unit = {
    val t = new Timestamp(now, MockCertPath)
    assertEquals(t.getSignerCertPath, MockCertPath)
  }

  @Test def getTimestamp(): Unit = {
    assertEquals(now, new Timestamp(now, MockCertPath).getTimestamp)
  }

  @Test def testToString(): Unit = {
    new Timestamp(now, MockCertPath).toString
  }
}
