package java.util.concurrent

@SerialVersionUID(1L)
class CompletionException(message: String, cause: Throwable)
    extends RuntimeException(message, cause) {
  protected def this() = this(null, null)
  protected def this(message: String) = this(message, null)
  def this(cause: Throwable) = this(null, cause)
}
