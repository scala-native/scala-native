package java.util.concurrent

class TimeoutException(message: String) extends Throwable(message, null) {
  def this() = this(null)
}