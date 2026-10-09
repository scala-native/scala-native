package java.lang
package reflect

class UndeclaredThrowableException(undeclaredThrowable: Throwable, s: String)
    extends RuntimeException(s, undeclaredThrowable) {
  def this(undeclaredThrowable: Throwable) = this(undeclaredThrowable, null)

  def getUndeclaredThrowable(): Throwable = super.getCause()

  override def initCause(cause: Throwable): Throwable =
    throw new IllegalStateException("Can't overwrite cause")
}
