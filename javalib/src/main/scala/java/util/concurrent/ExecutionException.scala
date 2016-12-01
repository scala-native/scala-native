package java.util.concurrent

class ExecutionException(message: String, cause: Throwable)
    extends Exception(message, cause)
    with Serializable {
  def this(message: String) = this(message, null)
  def this(cause: Throwable) = this(null, cause)
  def this() = this(null, null)
}
