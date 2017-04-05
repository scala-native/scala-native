package java.util.concurrent

class RejectedExecutionException(s: String, e: Throwable) extends RuntimeException(s, e) {
  def this() = this(null, null)
  def this(s: String) = this(s, null)
  def this(e: Throwable) = this(if (e == null) null else e.toString, e)
}
