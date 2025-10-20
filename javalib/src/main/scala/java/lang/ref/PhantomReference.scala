package java.lang.ref

class PhantomReference[T >: Null <: AnyRef](
    referent: T,
    queue: ReferenceQueue[? >: T]
) extends Reference[T](null) {

  override def get(): T = null
}
