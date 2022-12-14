package scala.scalanative.runtime

import scala.scalanative.annotation._
import scala.runtime.LazyVals.{BITS_PER_LAZY_VAL, STATE}

/** Helper methods used in thread-safe lazy vals adapted for Scala Native usage
 *  Make sure to sync them with the logic defined in Scala 3
 *  scala.runtime.LazyVals
 */
private object LazyVals {

  private final val LAZY_VAL_MASK = 3L

  /* ------------- Start of public API ------------- */

  @`inline`
  def CAS(bitmap: RawPtr, e: Long, v: Int, ord: Int): Boolean = {
    val mask = ~(LAZY_VAL_MASK << ord * BITS_PER_LAZY_VAL)
    val n = (e & mask) | (v.toLong << (ord * BITS_PER_LAZY_VAL))
    // Todo: with multithreading use atomic cas
    if (get(bitmap) != e) false
    else {
      Intrinsics.storeLong(bitmap, n)
      true
    }
  }

  @`inline`
  def objCAS(objPtr: RawPtr, exp: Object, n: Object): Boolean = {
    // Todo: with multithreading use atomic cas
    if (Intrinsics.loadObject(objPtr) ne exp) false
    else {
      Intrinsics.storeObject(objPtr, n)
      true
    }
  }

  @`inline`
  def setFlag(bitmap: RawPtr, v: Int, ord: Int): Unit = {
    val cur = get(bitmap)
    // TODO: with multithreading add waiting for notifications
    CAS(bitmap, cur, v, ord)
  }

  def wait4Notification(bitmap: RawPtr, cur: Long, ord: Int): Unit = {
    throw new IllegalStateException(
      "wait4Notification not supported in single-threaded Scala Native runtime"
    )
  }

  @alwaysinline
  def get(bitmap: RawPtr): Long = {
    // Todo: make it volatile read with multithreading
    Intrinsics.loadLong(bitmap)
  }

}
