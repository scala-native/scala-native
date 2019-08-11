package java.lang

// Ported from Harmony

class ThreadLocal[T] {

  import java.lang.ThreadLocal._

  protected def initialValue(): T = null.asInstanceOf[T]

  def get(): T = {
    val currentThread  = Thread.currentThread()
    val values: Values = currentThread.localValues
    if (values != null) {
      values.map
        .getOrElseUpdate(this, initialValue().asInstanceOf[Object])
        .asInstanceOf[T]
    } else {
      currentThread.localValues = new ThreadLocal.Values()
      initialValue()
    }
  }

  def set(value: T): Unit = {
    val currentThread         = Thread.currentThread()
    val currentValues: Values = currentThread.localValues
    val values = if (currentValues == null) {
      val newValues = new Values()
      currentThread.localValues = newValues
      newValues
    } else {
      currentValues
    }

    values.map.put(this, value.asInstanceOf[Object])
  }

  def remove(): Unit = {
    val currentThread = Thread.currentThread()
    val vals: Values  = currentThread.localValues
    if (vals != null) {
      vals.map.remove(this)
    }
  }
}

object ThreadLocal {
  class Values() {
    private[ThreadLocal] val map =
      scala.collection.mutable.WeakHashMap.empty[AnyRef, Object]
    def this(values: Values) = {
      this()
      map ++= values.map
    }
  }
}
