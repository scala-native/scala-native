package java.nio

class BufferUnderflowException private[java] (message: String)
    extends RuntimeException(message) {
  def this() = this(null)
}
