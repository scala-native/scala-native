package java.security

// Ported from Harmony

import java.util.Date

/**
 * Tests for [[Timestamp]] class fields and methods
 */
object TimestampSuite extends tests.Suite {

  private[this] val now: Date = new Date()

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

  test("constructor") {
    // Check that nulls are not accepted.
    assertThrows[NullPointerException](new Timestamp(null, MockCertPath))
    assertThrows[NullPointerException](new Timestamp(now, null))
  }

  test("equals") {
    val one = new Timestamp(now, MockCertPath)
    val two = new Timestamp(now, MockCertPath)

    assert(one.equals(one))
    assert(one.equals(two))
    assert(two.equals(one))
    assert(one != null)
    assertNot(one.equals(new Object()))

    val two1 = new Timestamp(new Date(9999), MockCertPath)
    assertNot(one.equals(two1))
    assert(two1.equals(two1))
  }

  test("getSignerCertPath") {
    val t = new Timestamp(now, MockCertPath)
    assertEquals(t.getSignerCertPath, MockCertPath)
  }

  test("getTimestamp") {
    assertEquals(now, new Timestamp(now, MockCertPath).getTimestamp)
  }

  test("toString") {
    new Timestamp(now, MockCertPath).toString
  }
}
