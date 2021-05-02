package java.security

class DigestException(message: String, cause: Throwable)
    extends GeneralSecurityException(message, cause) {
  def this(message: String) = this(message, null)
  def this(cause: Throwable) = this(null, cause)
  def this() = this(null, null)
}
