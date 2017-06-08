package java.util.concurrent

class ExecutionException(message: String, cause: Throwable) extends RuntimeException(message, cause) {
  def this() = this(null, null)
  def this(message: String) = this(message, null)
  def this(cause: Throwable) = this(if (cause == null) null else cause.toString, cause)
}