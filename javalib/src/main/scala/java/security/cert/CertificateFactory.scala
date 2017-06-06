package java.security.cert

// TODO: Port me from Apache Harmony

import java.util.List

class CertificateFactory {
  def generateCertPath(certificates: List[_ <: Certificate]): CertPath =
    throw new Exception
}
object CertificateFactory {
  def getInstance(x: String): CertificateFactory = new CertificateFactory
}
