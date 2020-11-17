package java.security

// Ported from Apache Harmony

class NoSuchAlgorithmException(message: String, cause: Throwable)
    extends GeneralSecurityException(message, cause) {
  def this(message: String) = this(message, null)
  def this(cause: Throwable) = this(null, cause)
  def this() = this(null, null)
}
