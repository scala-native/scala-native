package java.lang

class InheritableThreadLocal[T <: AnyRef] extends ThreadLocal[T] {

  /** Computes the initial value of this thread-local variable for the child
   *  thread given the parent thread's value. Called from the parent thread when
   *  creating a child thread. The default implementation returns the parent
   *  thread's value.
   *
   *  @param parentValue
   *    the value of the variable in the parent thread.
   *  @return
   *    the initial value of the variable for the child thread.
   */
  protected def childValue(parentValue: T): T = parentValue

  // Proxy to childValue to mitigate access restrictions
  private[lang] final def getChildValue(parentValue: T): T =
    childValue(parentValue)

  override protected[lang] def values(current: Thread): ThreadLocal.Values =
    current.inheritableThreadLocals

  override protected[lang] def initializeValues(
      current: Thread
  ): ThreadLocal.Values = {
    val instance = new ThreadLocal.Values()
    current.inheritableThreadLocals = instance
    instance
  }
}
