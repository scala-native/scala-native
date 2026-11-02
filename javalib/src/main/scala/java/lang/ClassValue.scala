package java.lang

import scala.annotation.tailrec

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic
import scala.scalanative.libc.stdatomic.AtomicRef
import scala.scalanative.libc.stdatomic.memory_order._
import scala.scalanative.runtime.{ObjectArray, fromRawPtr}

/* Unlike on the JVM the bindings are owned by the ClassValue, not by the Class.
 * Classes are static and never unloaded, so the lifetime of a value depends
 * only on its ClassValue. Keeping them here requires no weak references, no
 * mutable state in Class and no scanning of class metadata by the GC.
 *
 * Readers are lock-free. Slots are only ever replaced, never cleared, so probe
 * sequences stay valid. A binding is published by a release store to its slot,
 * a resized table by the volatile field. Writers are serialized by the lock,
 * which is never held in computeValue.
 */
abstract class ClassValue[T <: AnyRef] protected () {
  import ClassValue._

  private val lock = new Object
  @volatile private var table = new scala.Array[Binding](InitialCapacity)
  // Number of occupied slots, guarded by the lock
  private var occupied: Int = 0

  // Like final fields on the JVM these must be visible to threads receiving
  // this instance through a data race. The constructor can be inlined, which
  // would lose the fence emitted for @safePublish.
  stdatomic.atomic_thread_fence(memory_order_release)

  protected def computeValue(`type`: Class[_]): T

  def get(`type`: Class[_]): T = {
    if (`type` == null) throw new NullPointerException()
    val binding = find(table, `type`)
    if (binding != null && binding.isValue) binding.value.asInstanceOf[T]
    else computeAndAssociate(`type`, binding)
  }

  def remove(`type`: Class[_]): Unit = {
    if (`type` == null) throw new NullPointerException()
    // Always leave a token, it invalidates ongoing computations
    val token = new Binding(`type`, null, Thread.currentThread().threadId())
    lock.synchronized(publish(token))
  }

  @alwaysinline private def find(
      bindings: scala.Array[Binding],
      cls: Class[_]
  ): Binding = {
    val mask = bindings.length - 1
    var index = hash(cls) & mask
    var binding = slot(bindings, index).load(memory_order_acquire)
    while ((binding ne null) && (binding.cls ne cls)) {
      index = (index + 1) & mask
      binding = slot(bindings, index).load(memory_order_acquire)
    }
    binding
  }

  /** @param start
   *    the token, or null, observed before computing the value
   */
  @noinline @tailrec private def computeAndAssociate(
      cls: Class[_],
      start: Binding
  ): T = {
    val value =
      try computeValue(cls)
      catch {
        case ex: Throwable =>
          val current = find(table, cls)
          if (current != null && current.isValue)
            return current.value.asInstanceOf[T]
          ex match {
            case _: RuntimeException | _: Error => throw ex
            case _                              => throw new Error(ex)
          }
      }
    val associated = associate(cls, start, value)
    if (associated.isValue) associated.value.asInstanceOf[T]
    else computeAndAssociate(cls, associated)
  }

  /** @return
   *    the associated value, or the token invalidating the computed one
   */
  private def associate(
      cls: Class[_],
      start: Binding,
      value: AnyRef
  ): Binding = lock.synchronized {
    val current = find(table, cls)
    if (current != null && current.isValue) current
    else if ((current eq start) ||
        current.removerId == Thread.currentThread().threadId()) {
      val binding = new Binding(cls, value, NoRemover)
      publish(binding)
      binding
    } else current
  }

  // Requires holding the lock
  private def publish(binding: Binding): Unit = {
    val current = table
    val isNewClass = find(current, binding.cls) == null
    if (isNewClass) occupied += 1
    if (occupied * 2 <= current.length) insert(current, binding)
    else {
      val updated = new scala.Array[Binding](current.length * 2)
      var index = 0
      while (index < current.length) {
        val existing = current(index)
        if (existing != null) insert(updated, existing)
        index += 1
      }
      insert(updated, binding)
      table = updated
    }
  }

  private def insert(bindings: scala.Array[Binding], binding: Binding): Unit = {
    val mask = bindings.length - 1
    var index = hash(binding.cls) & mask
    while ((bindings(index) ne null) && (bindings(index).cls ne binding.cls))
      index = (index + 1) & mask
    slot(bindings, index).store(binding, memory_order_release)
  }

  // The index is always masked by the length of the table
  @alwaysinline private def slot(
      bindings: scala.Array[Binding],
      index: Int
  ): AtomicRef[Binding] = new AtomicRef[Binding](
    fromRawPtr[Binding](bindings.asInstanceOf[ObjectArray].atRawUnsafe(index))
  )

  // Class hash codes are aligned addresses, their low bits carry no information
  @alwaysinline private def hash(cls: Class[_]): Int = {
    val mixed = cls.hashCode() * 0x9e3779b9
    mixed ^ (mixed >>> 16)
  }
}

object ClassValue {
  private final val InitialCapacity = 8
  private final val NoRemover = -1L

  /** Either a value, possibly null, associated to a class, or a removal token
   *  created by the thread identified by removerId
   */
  private final class Binding(
      val cls: Class[_],
      val value: AnyRef,
      val removerId: scala.Long
  ) {
    @alwaysinline def isValue: scala.Boolean = removerId == NoRemover
  }

  /* Not used. The class file of java.lang.Class on the JDK refers to it as an
   * inner class of ClassValue, the Scala compilers crash if they can't find it.
   */
  private[lang] class ClassValueMap
}
