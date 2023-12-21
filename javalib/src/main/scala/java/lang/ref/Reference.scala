package java.lang.ref

abstract class Reference[T](private var referent: T) {
  def get(): T = referent
  def clear(): Unit = referent = null.asInstanceOf[T]
  def isEnqueued(): Boolean = false
  def enqueue(): Boolean = false
  private[ref] def dequeue(): Reference[T] = this
}
