package java.security.cert

import java.util.List

abstract class CertPath protected (`type`: String) {
  def getCertificates(): List[_ <: Certificate]
}
