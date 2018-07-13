package java.security

import java.security.cert.{CertificateEncodingException, CertificateException}

// Note: Partially implemented

// Ported from Harmony

object CertificateSuite extends tests.Suite {

  /**
   * Meaningless cert encoding just for testing purposes
   */
  private val testEncoding =
    Array[Byte](1.toByte, 2.toByte, 3.toByte, 4.toByte, 5.toByte)

  test("getType") {
    val c1 = new MyCertificate("TEST_TYPE", testEncoding)
    assertEquals("TEST_TYPE", c1.getType)
  }

  test("equals") {

    val c1 = new MyCertificate("TEST_TYPE", testEncoding)
    val c2 = new MyCertificate("TEST_TYPE", testEncoding)

    assertTrue(c1 == c1)
    assertTrue(c1 == c2 && c2 == c1)
    assertFalse(c1.equals(null))

    //noinspection ComparingUnrelatedTypes
    assertFalse(c1.equals("TEST_TYPE"))
  }

  /**
   * Helper Stub class ported from Harmony.
   */
  class MyCertificate(val `type`: String, val encoding: Array[Byte])
      extends java.security.cert.Certificate(`type`) {

    @throws[CertificateEncodingException]
    def getEncoded: Array[Byte] = // do copy to force NPE in test
      encoding.clone

    @throws[CertificateException]
    @throws[NoSuchAlgorithmException]
    @throws[InvalidKeyException]
    @throws[NoSuchProviderException]
    @throws[SignatureException]
    def verify(key: PublicKey): Unit = {}

    @throws[CertificateException]
    @throws[NoSuchAlgorithmException]
    @throws[InvalidKeyException]
    @throws[NoSuchProviderException]
    @throws[SignatureException]
    def verify(key: PublicKey, sigProvider: String): Unit = {}

    override def toString: String = s"[My test Certificate, type: $getType]"

    def getPublicKey: PublicKey = new PublicKey() {
      override def getAlgorithm: String = "TEST"

      override def getEncoded: Array[Byte] =
        Array[Byte](1.toByte, 2.toByte, 3.toByte)

      override def getFormat: String = "TEST_FORMAT"
    }
  }

}
