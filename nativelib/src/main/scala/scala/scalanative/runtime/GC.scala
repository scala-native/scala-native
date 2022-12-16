package scala.scalanative
package runtime

import scalanative.unsafe._

/** The Boehm GC conservative garbage collector
 *
 *  @see
 *    [[http://hboehm.info/gc/gcinterface.html C Interface]]
 */
@extern
object GC {
  @deprecated("Marked for removal, use alloc(Class[_], CSize) instead", "0.4.1")
  @name("scalanative_alloc")
  def alloc(rawty: RawPtr, size: CSize): RawPtr = extern

  @deprecated(
    "Marked for removal, use alloc_atomic(Class[_], CSize) instead",
    "0.4.1"
  )
  @name("scalanative_alloc_atomic")
  def alloc_atomic(rawty: RawPtr, size: CSize): RawPtr = extern

  @name("scalanative_alloc")
  def alloc(cls: Class[_], size: CSize): RawPtr = extern
  @name("scalanative_alloc_atomic")
  def alloc_atomic(cls: Class[_], size: CSize): RawPtr = extern
  @name("scalanative_alloc_small")
  def alloc_small(cls: Class[_], size: CSize): RawPtr = extern
  @name("scalanative_alloc_large")
  def alloc_large(cls: Class[_], size: CSize): RawPtr = extern
  @name("scalanative_collect")
  def collect(): Unit = extern
  @name("scalanative_init")
  def init(): Unit = extern
  @name("scalanative_register_weak_reference_handler")
  def registerWeakReferenceHandler(handler: Ptr[Byte]): Unit = extern

  /** Notify the Garbage Collector about the range of memory which should be
   *  scanned when marking the objects. The range should contain only memory NOT
   *  allocated using the GC, eg. using malloc. Otherwise it might lead to the
   *  undefined behaviour at runtime.
   *
   *  @param addressLow
   *    Start of the range including the first address that should be scanned
   *    when marking
   *  @param addressHigh
   *    End of the range including the last address that should be scanned when
   *    marking
   */
  @name("scalanative_add_roots")
  def addRoots(addressLow: Ptr[_], addressHigh: Ptr[_]): Unit = extern

  /** Notify the Garbage Collector about the range of memory which should no
   *  longer should be scanned when marking the objects. Every previously
   *  registered range of addressed using [[addRoots]] which is fully contained
   *  withen the range of addressLow and addressHigh would be
   *  exluded from the subsequent scanning during the GC. It is safe to pass a
   *  range of addressed which doen't match any of the previously registered
   *  memory regions.
   *
   *  @param addressLow
   *    Start of the range including the first address that should be scanned
   *    when marking
   *  @param addressHigh
   *    End of the range including the last address that should be scanned when
   *    marking
   */
  @name("scalanative_remove_roots")
  def removeRoots(addressLow: Ptr[_], addressHigh: Ptr[_]): Unit = extern
}
