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

object AtomicMarkableReference {
  private[concurrent] case class MarkableReference[T <: AnyRef](
      reference: T,
      mark: Boolean
  )
}

import AtomicMarkableReference._
class AtomicMarkableReference[V <: AnyRef](
    private[this] var value: MarkableReference[V]
) {

  def this(initialRef: V, initialMark: Boolean) = {
    this(MarkableReference(initialRef, initialMark))
  }

  // Pointer to field containing underlying MarkableReference.
  @alwaysinline
  private[concurrent] def valueRef: CAtomicRef[MarkableReference[V]] = {
    new CAtomicRef(
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "value"))
    )
  }

  /** Returns the current value of the reference.
   *
   *  @return
   *    the current value of the reference
   */
  def getReference(): V = valueRef.load().reference

  /** Returns the current value of the mark.
   *
   *  @return
   *    the current value of the mark
   */
  def isMarked(): Boolean = valueRef.load().mark

  /** Returns the current values of both the reference and the mark. Typical
   *  usage is {@code boolean[1] holder; ref = v.get(holder); }.
   *
   *  @param markHolder
   *    an array of size of at least one. On return, {@code markHolder[0]} will
   *    hold the value of the mark.
   *  @return
   *    the current value of the reference
   */
  def get(markHolder: Array[Boolean]): V = {
    val current = valueRef.load()
    markHolder(0) = current.mark
    current.reference
  }

  /** Atomically sets the value of both the reference and mark to the given
   *  update values if the current reference is {@code ==} to the expected
   *  reference and the current mark is equal to the expected mark. This
   *  operation may fail spuriously and does not provide ordering guarantees, so
   *  is only rarely an appropriate alternative to {@code compareAndSet}.
   *
   *  @param expectedReference
   *    the expected value of the reference
   *  @param newReference
   *    the new value for the reference
   *  @param expectedMark
   *    the expected value of the mark
   *  @param newMark
   *    the new value for the mark
   *  @return
   *    {@code true} if successful
   */
  def weakCompareAndSet(
      expectedReference: V,
      newReference: V,
      expectedMark: Boolean,
      newMark: Boolean
  ): Boolean =
    compareAndSet(expectedReference, newReference, expectedMark, newMark)

  /** Atomically sets the value of both the reference and mark to the given
   *  update values if the current reference is {@code ==} to the expected
   *  reference and the current mark is equal to the expected mark.
   *
   *  @param expectedReference
   *    the expected value of the reference
   *  @param newReference
   *    the new value for the reference
   *  @param expectedMark
   *    the expected value of the mark
   *  @param newMark
   *    the new value for the mark
   *  @return
   *    {@code true} if successful
   */
  def compareAndSet(
      expectedReference: V,
      newReference: V,
      expectedMark: Boolean,
      newMark: Boolean
  ): Boolean = {
    val current = valueRef.load()

    (expectedReference eq current.reference) &&
      expectedMark == current.mark && {
        ((newReference eq current.reference) && newMark == current.mark) ||
        valueRef
          .compareExchangeStrong(
            current,
            MarkableReference(newReference, newMark)
          )
      }
  }

  /** Unconditionally sets the value of both the reference and mark.
   *
   *  @param newReference
   *    the new value for the reference
   *  @param newMark
   *    the new value for the mark
   */
  def set(newReference: V, newMark: Boolean): Unit = {
    val current = valueRef.load()
    if ((newReference ne current.reference) || newMark != current.mark) {
      valueRef.store(MarkableReference(newReference, newMark))
    }
  }

  /** Atomically sets the value of the mark to the given update value if the
   *  current reference is {@code ==} to the expected reference. Any given
   *  invocation of this operation may fail (return {@code false}) spuriously,
   *  but repeated invocation when the current value holds the expected value
   *  and no other thread is also attempting to set the value will eventually
   *  succeed.
   *
   *  @param expectedReference
   *    the expected value of the reference
   *  @param newMark
   *    the new value for the mark
   *  @return
   *    {@code true} if successful
   */
  def attemptMark(expectedReference: V, newMark: Boolean): Boolean = {
    val current = valueRef.load()
    (expectedReference eq current.reference) && {
      newMark == current.mark ||
      valueRef
        .compareExchangeStrong(
          current,
          MarkableReference(expectedReference, newMark)
        )
    }
  }

}
