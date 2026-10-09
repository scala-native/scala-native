package java.lang
package reflect

class InvocationTargetException(private val target: Throwable, s: String)
    extends ReflectiveOperationException(s, target) {
  def this(target: Throwable) = this(target, null)
  protected def this() = this(null, null)

  def getTargetException(): Throwable = target
  override def getCause(): Throwable = target

  override def initCause(cause: Throwable): Throwable =
    throw new IllegalStateException("Can't overwrite cause")
}
