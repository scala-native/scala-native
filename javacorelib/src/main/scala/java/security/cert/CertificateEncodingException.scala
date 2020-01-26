package java.security.cert

// Ported from Harmony

import java.security.GeneralSecurityException

@SerialVersionUID(6219492851589449162L)
class CertificateEncodingException(private[this] val message: String,
                                   private[this] val cause: Throwable)
    extends CertificateException(message, cause) {
  def this(msg: String) = this(msg, null)

  def this(cause: Throwable) = this(null, cause)

  def this() = this(null, null)
}
