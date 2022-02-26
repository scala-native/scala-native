package java.rmi

class RemoteException(s: String, exception: Throwable)
    extends java.io.IOException(s) {
  initCause(null) // Disallow subsequent initCause

  def this(s: String) = this(s, null)
  def this() = this(null, null)

  override def getMessage(): String = {
    if (exception == null) super.getMessage()
    else s"${super.getMessage()}; nested exception is: \n\t ${exception}"
  }

  override def getCause(): Throwable = exception
}
