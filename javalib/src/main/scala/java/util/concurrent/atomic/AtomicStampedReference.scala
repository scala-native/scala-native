/*
 * Based on JSR-166 originally written by Doug Lea with assistance
 * from members of JCP JSR-166 Expert Group and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.atomic.CAtomicRef
import scala.scalanative.libc.atomic.memory_order._
import scala.scalanative.runtime.{fromRawPtr, Intrinsics}

object AtomicStampedReference {
  private[concurrent] case class StampedReference[V <: AnyRef](
      ref: V,
      stamp: Int
  )
}

import AtomicStampedReference._

class AtomicStampedReference[V <: AnyRef] private (
    private[this] var value: StampedReference[V]
) {

  def this(initialRef: V, initialStamp: Int) = {
    this(StampedReference(initialRef, initialStamp))
  }

  // Pointer to field containing underlying StampedReference.
  @alwaysinline
  private[concurrent] def valueRef: CAtomicRef[StampedReference[V]] =
    new CAtomicRef(
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "value"))
    )

  /** Returns the current value of the reference.
   *
   *  @return
   *    the current value of the reference
   */
  def getReference(): V = valueRef.load().ref

  /** Returns the current value of the stamp.
   *
   *  @return
   *    the current value of the stamp
   */
  def getStamp(): Int = valueRef.load().stamp

  /** Returns the current values of both the reference and the stamp. Typical
   *  usage is {@code int[1] holder; ref = v.get(holder); }.
   *
   *  @param stampHolder
   *    an array of size of at least one. On return, {@code stampHolder[0]} will
   *    hold the value of the stamp.
   *  @return
   *    the current value of the reference
   */
  def get(stampHolder: Array[Int]): V = {
    val current = valueRef.load()
    stampHolder(0) = current.stamp
    current.ref
  }

  /** Atomically sets the value of both the reference and stamp to the given
   *  update values if the current reference is {@code ==} to the expected
   *  reference and the current stamp is equal to the expected stamp. This
   *  operation may fail spuriously and does not provide ordering guarantees, so
   *  is only rarely an appropriate alternative to {@code compareAndSet}.
   *
   *  @param expectedReference
   *    the expected value of the reference
   *  @param newReference
   *    the new value for the reference
   *  @param expectedStamp
   *    the expected value of the stamp
   *  @param newStamp
   *    the new value for the stamp
   *  @return
   *    {@code true} if successful
   */
  def weakCompareAndSet(
      expectedReference: V,
      newReference: V,
      expectedStamp: Int,
      newStamp: Int
  ): Boolean =
    compareAndSet(expectedReference, newReference, expectedStamp, newStamp)

  /** Atomically sets the value of both the reference and stamp to the given
   *  update values if the current reference is {@code ==} to the expected
   *  reference and the current stamp is equal to the expected stamp.
   *
   *  @param expectedReference
   *    the expected value of the reference
   *  @param newReference
   *    the new value for the reference
   *  @param expectedStamp
   *    the expected value of the stamp
   *  @param newStamp
   *    the new value for the stamp
   *  @return
   *    {@code true} if successful
   */
  def compareAndSet(
      expectedReference: V,
      newReference: V,
      expectedStamp: Int,
      newStamp: Int
  ): Boolean = {
    val current = valueRef.load()

    def matchesExpected: Boolean =
      (expectedReference eq current.ref) &&
        (expectedStamp == current.stamp)

    def matchesNew: Boolean =
      (newReference eq current.ref) && newStamp == current.stamp

    def compareAndSetNew(): Boolean =
      valueRef
        .compareExchangeStrong(
          current,
          StampedReference(newReference, newStamp)
        )

    matchesExpected && (matchesNew || compareAndSetNew())
  }

  /** Unconditionally sets the value of both the reference and stamp.
   *
   *  @param newReference
   *    the new value for the reference
   *  @param newStamp
   *    the new value for the stamp
   */
  def set(newReference: V, newStamp: Int): Unit = {
    val current = valueRef.load()
    if ((newReference ne current.ref) || newStamp != current.stamp) {
      valueRef.store(StampedReference(newReference, newStamp))
    }
  }

  /** Atomically sets the value of the stamp to the given update value if the
   *  current reference is {@code ==} to the expected reference. Any given
   *  invocation of this operation may fail (return {@code false}) spuriously,
   *  but repeated invocation when the current value holds the expected value
   *  and no other thread is also attempting to set the value will eventually
   *  succeed.
   *
   *  @param expectedReference
   *    the expected value of the reference
   *  @param newStamp
   *    the new value for the stamp
   *  @return
   *    {@code true} if successful
   */
  def attemptStamp(expectedReference: V, newStamp: Int): Boolean = {
    val current = valueRef.load()

    (expectedReference eq current.ref) && {
      newStamp == current.stamp ||
      valueRef
        .compareExchangeStrong(
          current,
          StampedReference(expectedReference, newStamp)
        )
    }
  }
}
