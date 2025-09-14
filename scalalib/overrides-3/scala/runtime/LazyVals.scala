package scala.runtime

import language.experimental.captureChecking
import java.util.concurrent.CountDownLatch

import scala.annotation.*

import scala.scalanative.runtime.*
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

/**
 * Helper methods used in thread-safe lazy vals.
 */
object LazyVals {
  private final val LAZY_VAL_MASK = 3L

  /* ------------- Start of public API ------------- */

  // This trait extends Serializable to fix #16806 that caused a race condition
  sealed trait LazyValControlState extends Serializable

  /**
   * Used to indicate the state of a lazy val that is being
   * evaluated and of which other threads await the result.
   */
  final class Waiting extends CountDownLatch(1) with LazyValControlState {
    /* #20856 If not fully evaluated yet, serialize as if not-evaluat*ing* yet.
     * This strategy ensures the "serializability" condition of parallel
     * programs--not to be confused with the data being `java.io.Serializable`.
     * Indeed, if thread A is evaluating the lazy val while thread B attempts
     * to serialize its owner object, there is also an alternative schedule
     * where thread B serializes the owner object *before* A starts evaluating
     * the lazy val. Therefore, forcing B to see the non-evaluating state is
     * correct.
     */
    private def writeReplace(): Any = null
    override def countDown(): Unit = if(isMultithreadingEnabled) super.countDown()
    override def await(): Unit =  if(isMultithreadingEnabled) super.await()
  }

  /**
   * Used to indicate the state of a lazy val that is currently being
   * evaluated with no other thread awaiting its result.
   */
  object Evaluating extends LazyValControlState {
    /* #20856 If not fully evaluated yet, serialize as if not-evaluat*ing* yet.
     * See longer comment in `Waiting.writeReplace()`.
     */
    private def writeReplace(): Any = null
  }

  /**
   * Used to indicate the state of a lazy val that has been evaluated to
   * `null`.
   */
  object NullValue extends LazyValControlState

  final val BITS_PER_LAZY_VAL = 2L

  def STATE(cur: Long, ord: Int): Long = {
    val r = (cur >> (ord * BITS_PER_LAZY_VAL)) & LAZY_VAL_MASK
    r
  }

  def CAS(t: Object, offset: Long, e: Long, v: Int, ord: Int): Boolean = {
    unexpectedUsage()
  }

  def objCAS(t: Object, offset: Long, exp: Object, n: Object): Boolean = {
    unexpectedUsage()
  }

  def setFlag(t: Object, offset: Long, v: Int, ord: Int): Unit = {
    unexpectedUsage()
  }

  def wait4Notification(t: Object, offset: Long, cur: Long, ord: Int): Unit = {
    unexpectedUsage()
  }

  def get(t: Object, off: Long): Long = {
    unexpectedUsage()
  }

  // kept for backward compatibility
  def getOffset(clz: Class[?], name: String): Long = {
    unexpectedUsage()
  }

  def getStaticFieldOffset(field: java.lang.reflect.Field): Long = {
    unexpectedUsage()
  }

  def getOffsetStatic(field: java.lang.reflect.Field) =
    unexpectedUsage()

  private def unexpectedUsage() = {
    throw new IllegalStateException(
      "Unexpected usage of scala.runtime.LazyVals method, " +
      "in Scala Native lazy vals use overriden version of this class"
    )
  }

  object Names {
    final val state = "STATE"
    final val cas = "CAS"
    final val setFlag = "setFlag"
    final val wait4Notification = "wait4Notification"
    final val get = "get"
    final val getOffset = "getOffset"
  }
}
