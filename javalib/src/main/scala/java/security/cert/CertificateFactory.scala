package java.security.cert

import java.util.List

class CertificateFactory {
  def generateCertPath(certificates: List[_ <: Certificate]): CertPath =
    throw new Exception
}
object CertificateFactory {
  def getInstance(x: String): CertificateFactory = new CertificateFactory
}
