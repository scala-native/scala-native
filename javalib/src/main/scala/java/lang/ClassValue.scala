package java.lang

import scala.annotation.tailrec

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic
import scala.scalanative.libc.stdatomic.AtomicRef
import scala.scalanative.libc.stdatomic.memory_order._
import scala.scalanative.runtime.{ObjectArray, fromRawPtr}

/* Values are owned by the ClassValue: classes are never unloaded, so no weak
 * references or state in Class are needed. Reads are lock-free, writes hold the
 * lock, but never while computing a value.
 */
abstract class ClassValue[T <: AnyRef] protected () {
  import ClassValue._

  private val lock = new Object
  @volatile private var table = new scala.Array[Binding](InitialCapacity)
  // Number of occupied slots, guarded by the lock
  private var occupied: Int = 0

  // Safely publish the fields, @safePublish is lost in inlined constructors
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

  // start is the token, or null, observed before computing the value
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

  // Returns the associated value, or the token invalidating the computed one
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

  // A value, possibly null, or a removal token made by the removerId thread
  private final class Binding(
      val cls: Class[_],
      val value: AnyRef,
      val removerId: scala.Long
  ) {
    @alwaysinline def isValue: scala.Boolean = removerId == NoRemover
  }

  // Unused, scalac crashes without it as the JDK's Class.class refers to it
  private[lang] class ClassValueMap
}
