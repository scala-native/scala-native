package java.security.cert

abstract class CertPath protected (`type`: String) {

  def getCertificates(): java.util.List[_ <: Certificate]

  def getType(): String = `type`

  override def equals(other: Any): Boolean =
    other match {
      case otherRef: AnyRef if this eq otherRef => true
      case otherCp: CertPath if otherCp.getType.equals(`type`) =>
        getCertificates().equals(otherCp.getCertificates)
      case _ => false
    }
}
