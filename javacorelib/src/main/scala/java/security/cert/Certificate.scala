package java.security.cert

// Note: Partially implemented

// Ported from Harmony

abstract class Certificate(private val `type`: String) extends Serializable {

  override def equals(other: Any): Boolean = {
    other match {
      case objRef: AnyRef if this eq objRef =>
        true
      case otherCertificate: Certificate =>
        try {
          java.util.Arrays
            .equals(this.getEncoded(), otherCertificate.getEncoded)
        } catch {
          case e: CertificateEncodingException =>
            throw new RuntimeException(e)
        }
      case _ => false
    }
  }

  override def toString: String

  def getType(): String = `type`

  @throws[CertificateEncodingException]
  def getEncoded(): Array[Byte]

}
