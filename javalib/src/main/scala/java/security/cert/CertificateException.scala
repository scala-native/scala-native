package java.security.cert

import java.security.GeneralSecurityException

class CertificateException(message: String, cause: Throwable)
    extends GeneralSecurityException(message, cause) {
  def this(msg: String) = this(msg, null)
  def this(cause: Throwable) = this(null, cause)
  def this() = this(null, null)
}
