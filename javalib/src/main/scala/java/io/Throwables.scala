package java.io

class IOException(s: String, e: Throwable) extends Exception(s, e) {
  def this(e: Throwable) = this(null, e)
  def this(s: String) = this(s, null)
  def this() = this(null, null)
}

class UncheckedIOException(message: String, cause: IOException)
    extends RuntimeException(message, cause) {
  def this(cause: IOException) = this(null, cause)
  override def getCause(): IOException = cause
}

class FileNotFoundException(s: String) extends IOException(s) {
  def this() = this(null)
}

class EOFException(s: String) extends IOException(s) {
  def this() = this(null)
}

class UTFDataFormatException(s: String) extends IOException(s) {
  def this() = this(null)
}

class UnsupportedEncodingException(s: String) extends IOException(s) {
  def this() = this(null)
}

abstract class ObjectStreamException protected (s: String)
    extends IOException(s) {
  protected def this() = this(null)
}

class NotSerializableException(s: String) extends ObjectStreamException(s) {
  def this() = this(null)
}

class SyncFailedException(s: String) extends IOException(s)

class InterruptedIOException(s: String) extends IOException(s) {
  var bytesTransferred: Int = 0

  def this() = this(null)
}
