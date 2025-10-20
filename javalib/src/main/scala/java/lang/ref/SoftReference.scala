package java.lang.ref

class SoftReference[T >: Null <: AnyRef](
    referent: T,
    queue: ReferenceQueue[? >: T]
) extends Reference[T](referent) {

  def this(referent: T) = this(referent, null)

  override def get(): T = super.get()
}
