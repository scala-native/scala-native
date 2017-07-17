package java.lang.ref

class ReferenceQueue[T >: Null <: AnyRef] extends Object {

  private var firstReference: Reference[_ <: T] =
    null.asInstanceOf[Reference[_ <: T]]

  @SuppressWarnings(Array("unchecked"))
  //synchronized
  def poll: Reference[_ <: T] = {
    if (firstReference == null) return null
    val ref: Reference[_ <: T] = firstReference
    firstReference =
      if (firstReference.next == firstReference) null
      else firstReference.next.asInstanceOf[Reference[_ <: T]]
    ref.next = null
    ref
  }

  @SuppressWarnings(Array("unchecked"))
  //synchronized
  def remove(timeout: scala.Long): Reference[_ <: T] = {
    if (firstReference == null) wait(timeout)
    if (firstReference == null) return null

    val ref: Reference[_ <: T] = firstReference
    firstReference =
      if (firstReference.next == firstReference) null
      else firstReference.next.asInstanceOf[Reference[_ <: T]]
    ref.next = null
    ref
  }

  def remove: Reference[_ <: T] = remove(0L)

  //synchronized
  def enqueue(ref: Reference[_ <: T]): scala.Boolean = {
    ref.next = if (firstReference == null) ref else firstReference
    firstReference = ref
    notify()
    true
  }

}