package java.nio

class BufferOverflowException private[java] (msg: String)
    extends RuntimeException(msg) {
  def this() = this(null)
}
