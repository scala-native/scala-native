package java.security

// Ported from Harmony

import java.security.cert.CertPath
import java.util.Date

final class Timestamp private (private val signerCertPath: CertPath,
                               private val timestamp: Date)
    extends Serializable {

  if (signerCertPath eq null) {
    throw new NullPointerException("signerCertPath cannot be null")
  }

  /** Constructor overload with null checking and timestamp cloning **/
  @throws[NullPointerException]
  def this(timestamp: Date, signerCertPath: CertPath) =
    this(
      signerCertPath,
      TimestampConstructorHelper.validateAndCloneInputTimestamp(timestamp)
    )

  @inline def getSignerCertPath: CertPath = signerCertPath

  @inline def getTimestamp: Date = new Date(timestamp.getTime)

  override def equals(obj: Any): Boolean =
    obj match {
      case objRef: AnyRef if this eq objRef =>
        true
      case that: Timestamp =>
        timestamp.equals(that.getTimestamp) &&
          signerCertPath.equals(that.getSignerCertPath)
      case _ =>
        false
    }

  @transient
  override lazy val hashCode: Int = timestamp.hashCode ^ signerCertPath.hashCode

  override def toString: String = {
    val buf = new java.lang.StringBuilder()
    // Dump only the first certificate
    buf.append("Timestamp [")
    buf.append(timestamp.toString)
    buf.append(" certPath=")
    val certificates = signerCertPath.getCertificates
    if (certificates.isEmpty) {
      buf.append(certificates.get(0).toString)
    } else {
      buf.append("<empty>")
    }
    buf.append("]")
    buf.toString
  }
}

private object TimestampConstructorHelper {

  @throws[NullPointerException]
  def validateAndCloneInputTimestamp(timestamp: Date): Date =
    if (timestamp eq null) {
      throw new NullPointerException("Timestamp cannot be null")
    } else {
      new Date(timestamp.getTime)
    }
}
