package java.util.concurrent.atomic

import java.util.concurrent.atomic.AtomicStampedReference.ReferenceIntegerPair

class AtomicStampedReference[V <: AnyRef](initialRef: V, initialStamp: Int) {

  import AtomicStampedReference._

  private final val atomicRef: AtomicReference[ReferenceIntegerPair[V]] =
    new AtomicReference[ReferenceIntegerPair[V]](
      new ReferenceIntegerPair[V](initialRef, initialStamp))

  def getReference: V = atomicRef.get().reference

  def getStamp: Int = atomicRef.get().integer

  def get(stampHolder: Array[Int]): V = {
    val p: ReferenceIntegerPair[V] = atomicRef.get()
    stampHolder(0) = p.integer
    p.reference
  }

  def weakCompareAndSet(expectedReference: V,
                        newReference: V,
                        expectedStamp: Int,
                        newStamp: Int): Boolean = {
    val current: ReferenceIntegerPair[V] = atomicRef.get()

    expectedReference == current.reference && expectedStamp == current.integer &&
    ((newReference == current.reference && newStamp == current.integer) ||
    atomicRef.weakCompareAndSet(
      current,
      new ReferenceIntegerPair[V](newReference, newStamp)))
  }

  def compareAndSet(expectedReference: V,
                    newReference: V,
                    expectedStamp: Int,
                    newStamp: Int): Boolean = {
    val current: ReferenceIntegerPair[V] = atomicRef.get()

    expectedReference == current.reference && expectedStamp == current.integer &&
    ((newReference == current.reference && newStamp == current.integer) ||
    atomicRef.compareAndSet(
      current,
      new ReferenceIntegerPair[V](newReference, newStamp)))
  }

  def set(newReference: V, newStamp: Int): Unit = {
    val current: ReferenceIntegerPair[V] = atomicRef.get()
    if (newReference != current.reference || newStamp != current.integer)
      atomicRef.set(new ReferenceIntegerPair[V](newReference, newStamp))
  }

  def attemptStamp(expectedReference: V, newStamp: Int): Boolean = {
    val current: ReferenceIntegerPair[V] = atomicRef.get()

    expectedReference == current.reference &&
    (newStamp == current.integer ||
    atomicRef.compareAndSet(
      current,
      new ReferenceIntegerPair[V](expectedReference, newStamp)))
  }
}

object AtomicStampedReference {

  private class ReferenceIntegerPair[T](
      private[AtomicStampedReference] final val reference: T,
      private[AtomicStampedReference] final val integer: Int) {}

}
