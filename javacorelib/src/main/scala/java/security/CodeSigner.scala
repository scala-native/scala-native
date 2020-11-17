package java.security

import java.security.cert.CertPath

final class CodeSigner(signerCertPath: CertPath, timestamp: Timestamp)
    extends Serializable {
  def getSignerCertPath(): CertPath = signerCertPath
}
